# Architecture Overview

## Monolith phase

```mermaid
flowchart LR
    A[React SPA] -->|JWT Access Token| B[Spring Boot Monolith]
    B --> C[(H2 DB)]
    D[Auth0] -->|OIDC Login + JWT| A
    B -->|JWT validation issuer + audience| D
```

## Microservices phase

```mermaid
flowchart LR
    A[React SPA on S3] -->|GET /api/stream| G[API Gateway HTTP API]
    A -->|POST /api/posts + JWT| G
    A -->|GET /api/me + JWT| G

    G -->|public route| S[Stream Lambda]
    G -->|JWT + scope write:posts| P[Posts Lambda]
    G -->|JWT + scope read:profile| U[User Lambda]

    P --> D[(DynamoDB PostsTable)]
    S --> D

    H[Auth0] -->|OIDC Login + Access Token| A
    G -->|JWT Authorizer issuer + audience| H
```

## Security flow

1. User signs in through Auth0 from the SPA.
2. SPA obtains an access token with audience equal to the backend API identifier.
3. SPA calls protected endpoints with Authorization Bearer token.
4. Monolith validates issuer and audience; route method security enforces scopes.
5. In microservices mode, API Gateway JWT authorizer validates token and route scopes.
