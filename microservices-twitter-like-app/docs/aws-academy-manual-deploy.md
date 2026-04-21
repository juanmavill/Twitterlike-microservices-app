# AWS Academy Manual Deployment Guide

This guide is for environments where AWS Academy does not allow `aws configure`, access keys, or SAM/CLI-driven deploys. It covers the remaining manual steps through the AWS Console.

## 1. Preconditions

Before starting, make sure you already have:

- Auth0 API configured with audience `https://twitterlike-api`
- Auth0 SPA configured for local testing
- The three Lambda JARs packaged locally:
  - `microservices/posts-service/target/posts-service.jar`
  - `microservices/stream-service/target/stream-service.jar`
  - `microservices/user-service/target/user-service.jar`

## 2. DynamoDB

Create a table in DynamoDB:

- Table name: `PostsTable`
- Partition key: `id` (String)
- Capacity mode: On-demand

## 3. Lambda functions

Create three Lambda functions with runtime `Java 21` and execution role `LabRole`:

- `twitter-posts-service`
- `twitter-stream-service`
- `twitter-user-service`

Upload the packaged JAR for each function from the Lambda console.

### Handlers

- Posts: `com.example.posts.PostsHandler::handleRequest`
- Stream: `com.example.stream.StreamHandler::handleRequest`
- User: `com.example.user.UserHandler::handleRequest`

### Environment variables

For `twitter-posts-service`:

- `TABLE_NAME` = `PostsTable`
- `CORS_ALLOWED_ORIGIN` = `http://localhost:5173`

For `twitter-stream-service`:

- `TABLE_NAME` = `PostsTable`
- `CORS_ALLOWED_ORIGIN` = `http://localhost:5173`

For `twitter-user-service`:

- `CORS_ALLOWED_ORIGIN` = `http://localhost:5173`

When the frontend is later deployed to S3, replace `http://localhost:5173` with the S3 website URL in all three Lambdas.

## 4. HTTP API Gateway

Create an HTTP API and then configure:

### Integrations

- `GET /api/posts` -> `twitter-posts-service`
- `POST /api/posts` -> `twitter-posts-service`
- `GET /api/stream` -> `twitter-stream-service`
- `GET /api/me` -> `twitter-user-service`

### JWT Authorizer

Create a JWT authorizer:

- Name: `Auth0Authorizer`
- Identity source: `$request.header.Authorization`
- Issuer URL: `https://YOUR-DOMAIN.auth0.com/`
- Audience: `https://twitterlike-api`

### Route protection

Public routes:

- `GET /api/posts`
- `GET /api/stream`

Protected routes:

- `POST /api/posts` with scope `write:posts`
- `GET /api/me` with scope `read:profile`

### CORS

Configure API Gateway CORS with exact origins, no trailing slash:

- Allowed origin: `http://localhost:5173`
- Allowed headers: `Authorization`, `Content-Type`
- Allowed methods: `GET`, `POST`, `OPTIONS`

Important:

- Use `http://localhost:5173`
- Do not use `http://localhost:5173/`

## 5. Verify the AWS backend

Use the API Gateway invoke URL and verify:

- `GET /api/stream` returns JSON
- Authenticated `GET /api/me` returns the current user profile
- Authenticated `POST /api/posts` creates a new item
- DynamoDB `PostsTable` receives the post item

Suggested evidence:

- API Gateway URL screenshot
- Lambda monitor / CloudWatch screenshot after invocation
- DynamoDB item screenshot

## 6. Frontend local against AWS

Set the frontend API base URL to the API Gateway invoke URL in `frontend/.env`:

```env
VITE_API_BASE_URL=https://YOUR_HTTP_API.execute-api.YOUR_REGION.amazonaws.com
```

Then run:

```powershell
cd frontend
npm.cmd run dev
```

Verify the full flow from `http://localhost:5173`.

## 7. Frontend deployment to S3

Build the frontend locally:

```powershell
cd frontend
npm.cmd run build
```

In the S3 console:

1. Create a bucket with a globally unique name.
2. Disable "Block all public access" if your class allows public static website hosting.
3. Enable static website hosting:
   - Index document: `index.html`
   - Error document: `index.html`
4. Upload all files from `frontend/dist`.
5. Add a bucket policy for public read if required by your lab setup.

S3 website URL format:

`http://YOUR_BUCKET_NAME.s3-website-REGION.amazonaws.com`

## 8. Final Auth0 update

When the frontend is on S3, update Auth0 SPA settings with the S3 website URL:

- Allowed Callback URLs
- Allowed Logout URLs
- Allowed Web Origins
- Allowed Origins (CORS)

Also update:

- API Gateway CORS allowed origin
- Lambda `CORS_ALLOWED_ORIGIN` environment variable

to match the S3 URL exactly.

## 9. Final delivery checklist

Before submission, confirm:

- Monolith works locally with Swagger and Auth0
- AWS microservices work through API Gateway
- Frontend works locally against AWS
- Frontend works from the public S3 URL
- README contains:
  - Live S3 frontend URL
  - Swagger URL or screenshot
  - Architecture summary
  - Test report
  - Video demo link
