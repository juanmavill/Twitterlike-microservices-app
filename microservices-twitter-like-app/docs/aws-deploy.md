# AWS Deployment Guide

## Prerequisites

Install and configure:

1. AWS CLI (`aws configure` with your credentials).
2. AWS SAM CLI.
3. Java 21 and Maven.
4. Node.js 20+ and npm (required for frontend build).

If you are working inside AWS Academy and do not have access keys or CLI deploy permissions, use the manual console guide:

- `docs/aws-academy-manual-deploy.md`

## A) Deploy microservices with SAM

From `infra/sam`:

```powershell
sam build
sam deploy --guided
```

When prompted:

- Stack Name: `twitterlike-microservices`
- Region: your AWS region (example `us-east-1`)
- Parameter `Auth0Issuer`: `https://YOUR-DOMAIN.auth0.com/`
- Parameter `Auth0Audience`: `https://twitterlike-api`
- Parameter `CorsAllowedOrigin`: your frontend URL

Copy output `ApiBaseUrl` and set it in frontend `VITE_API_BASE_URL`.

## B) Deploy frontend to S3 static hosting

From `frontend`:

```powershell
npm install
npm run build
```

Create and configure S3 bucket:

```powershell
aws s3 mb s3://YOUR_UNIQUE_BUCKET_NAME
aws s3 website s3://YOUR_UNIQUE_BUCKET_NAME --index-document index.html --error-document index.html
aws s3 sync dist s3://YOUR_UNIQUE_BUCKET_NAME --delete
```

Set public read policy (or use CloudFront + OAC for production hardening).

### Example policy file `bucket-policy.json`

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::YOUR_UNIQUE_BUCKET_NAME/*"
    }
  ]
}
```

Apply policy:

```powershell
aws s3api put-bucket-policy --bucket YOUR_UNIQUE_BUCKET_NAME --policy file://bucket-policy.json
```

S3 Website URL format:

`http://YOUR_UNIQUE_BUCKET_NAME.s3-website-REGION.amazonaws.com`

## C) Final Auth0 update

Add S3 website URL (or CloudFront domain) to Auth0 allowed URLs:

- Callback URLs
- Logout URLs
- Web Origins
- CORS origins
