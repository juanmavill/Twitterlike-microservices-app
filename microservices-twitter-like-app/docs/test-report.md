# Test Report

## Automated tests executed

### Monolith

Command: `mvn test` in monolith folder.

Result:
- Tests run: 6
- Failures: 0
- Errors: 0
- Skipped: 0
- Status: PASS

Coverage by test class:
- Public stream endpoint is accessible without authentication.
- Protected create-post endpoint rejects unauthenticated requests.
- Protected create-post endpoint enforces write:posts scope.
- Protected /api/me endpoint enforces read:profile scope.

### Microservices

Commands executed:
- `mvn test` in posts-service
- `mvn test` in stream-service
- `mvn test` in user-service

Result:
- Maven test lifecycle status: PASS for all three services.
- Automated handler-level test results:
  - `posts-service`: 3 tests passed
  - `stream-service`: 2 tests passed
  - `user-service`: 2 tests passed
- Covered behavior includes:
  - public stream retrieval
  - protected post creation behavior with and without JWT claims
  - profile retrieval from JWT claims
  - DynamoDB interaction behavior through mocked clients

## Manual E2E tests to execute before final delivery

1. Login/Logout in SPA using Auth0 redirect flow.
2. Create valid post (<=140 chars) and verify stream update.
3. Attempt to create post >140 chars and verify rejection.
4. Access GET /api/stream without token and verify public access.
5. Access POST /api/posts without token and verify 401/403.
6. Access GET /api/me with token missing read:profile scope and verify denial.
7. Access GET /api/me with valid read:profile scope and verify profile response.
8. Validate same flow against Lambda endpoints behind API Gateway.

## Current practical deployment status

- Local monolith: validated manually
- Local frontend against Auth0 + local monolith: validated manually
- Local frontend against API Gateway + Lambda backend: validated manually
- Remaining final-delivery validation still required:
  - public S3-hosted frontend
  - complete E2E verification from the S3 URL
