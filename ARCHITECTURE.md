# MiniBank Authentication System - Architecture

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT APPLICATIONS                          │
│  (Web App, Mobile App, Postman, Other Microservices)               │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             │ HTTP/REST
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      AUTH SERVICE (Port 8081)                        │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │                    CONTROLLERS                               │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐ │  │
│  │  │    Auth      │  │ User Mgmt    │  │  Notification   │  │  │
│  │  │ Controller   │  │  Controller  │  │   Controller    │  │  │
│  │  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘ │  │
│  └─────────┼──────────────────┼──────────────────┼───────────┘  │
│            │                  │                  │                 │
│  ┌─────────▼──────────────────▼──────────────────▼───────────┐  │
│  │                       SERVICES                             │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │  │
│  │  │   User   │  │   User   │  │  Notif.  │  │   Role   │ │  │
│  │  │ Service  │  │   Mgmt   │  │  Service │  │  Service │ │  │
│  │  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘ │  │
│  │       │             │              │             │        │  │
│  │  ┌────┴─────┐  ┌───┴───────┐  ┌──┴──────┐  ┌───┴──────┐ │  │
│  │  │ Refresh  │  │   Audit   │  │   JWT   │  │  Event   │ │  │
│  │  │  Token   │  │  Service  │  │   Util  │  │Publisher │ │  │
│  │  └──────────┘  └───────────┘  └─────────┘  └──────────┘ │  │
│  └────────────────────────────────────────────────────────────┘  │
│            │                                      │                │
│  ┌─────────▼──────────────────────────────────────▼───────────┐  │
│  │                  EVENT LISTENERS (Async)                    │  │
│  │  ┌──────────────────────┐  ┌──────────────────────┐       │  │
│  │  │  UserEventListener   │  │  AuthEventListener   │       │  │
│  │  │  - User Created      │  │  - Login Success     │       │  │
│  │  │  - Password Changed  │  │  - Login Failed      │       │  │
│  │  │  - Role Changed      │  │                       │       │  │
│  │  └──────────────────────┘  └──────────────────────┘       │  │
│  └────────────────────────────────────────────────────────────┘  │
│            │                                                       │
│  ┌─────────▼─────────────────────────────────────────────────┐  │
│  │                    REPOSITORIES                            │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │  │
│  │  │   User   │  │   Role   │  │  Notif.  │  │  Refresh │ │  │
│  │  │   Repo   │  │   Repo   │  │   Repo   │  │  Token   │ │  │
│  │  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘ │  │
│  │       │             │              │             │        │  │
│  │  ┌────┴──────────────┴──────────────┴─────────────┴─────┐ │  │
│  │  │              JPA / Hibernate                          │ │  │
│  │  └────────────────────────────────────────────────────────┘ │  │
│  └────────────────────────────┬──────────────────────────────┘  │
│                                │                                  │
│  ┌─────────────────────────────▼────────────────────────────┐  │
│  │              SECURITY CONFIGURATION                       │  │
│  │  ┌──────────────────┐  ┌──────────────────┐             │  │
│  │  │  SecurityConfig  │  │    JwtFilter     │             │  │
│  │  │  - CORS          │  │  - Token Valid.  │             │  │
│  │  │  - Auth Provider │  │  - Auth Context  │             │  │
│  │  │  - Role Rules    │  │                   │             │  │
│  │  └──────────────────┘  └──────────────────┘             │  │
│  └────────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬────────────────────────────────────┘
                                │
                                │ JDBC
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    PostgreSQL Database (Port 5433)                   │
│                                                                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐       │
│  │  users   │  │  roles   │  │ user_    │  │ refresh_     │       │
│  │          │  │          │  │ roles    │  │ tokens       │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────┘       │
│                                                                       │
│  ┌──────────────┐  ┌──────────────┐                                │
│  │ notifications│  │  audit_log   │                                │
│  │              │  │              │                                │
│  └──────────────┘  └──────────────┘                                │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                         EXTERNAL SERVICES                            │
│                                                                       │
│  ┌──────────────────┐                                               │
│  │  Email Service   │  (JavaMailSender via SMTP)                   │
│  │  - Gmail SMTP    │  [Optional - Configurable]                   │
│  └──────────────────┘                                               │
└─────────────────────────────────────────────────────────────────────┘
```

## Request Flow Diagrams

### 1. User Registration Flow

```
┌─────────┐      ┌──────────────┐      ┌────────────┐      ┌──────────┐
│ Client  │      │ AuthController│     │UserService │      │ Database │
└────┬────┘      └──────┬───────┘      └─────┬──────┘      └────┬─────┘
     │                  │                     │                   │
     │ POST /register   │                     │                   │
     ├─────────────────>│                     │                   │
     │                  │                     │                   │
     │                  │ register(request)   │                   │
     │                  ├────────────────────>│                   │
     │                  │                     │                   │
     │                  │                     │ Check existing    │
     │                  │                     ├──────────────────>│
     │                  │                     │<──────────────────┤
     │                  │                     │                   │
     │                  │                     │ Hash password     │
     │                  │                     │ (BCrypt)          │
     │                  │                     │                   │
     │                  │                     │ Get CUSTOMER role │
     │                  │                     ├──────────────────>│
     │                  │                     │<──────────────────┤
     │                  │                     │                   │
     │                  │                     │ Save user         │
     │                  │                     ├──────────────────>│
     │                  │                     │<──────────────────┤
     │                  │                     │                   │
     │                  │                     │ Publish Event     │
     │                  │                     │ (UserCreated)     │
     │                  │                     │                   │
     │                  │<────────────────────┤                   │
     │                  │                     │                   │
     │<─────────────────┤                     │                   │
     │ 201 Created      │                     │                   │
     │ {id, username}   │                     │                   │
     │                  │                     │                   │
     │                  │    ┌──────────────────────┐             │
     │                  │    │ UserEventListener    │             │
     │                  │    │ (Async)              │             │
     │                  │    │ Create notification  │             │
     │                  │    │ Send welcome email   │             │
     │                  │    └──────────────────────┘             │
```

### 2. Login Flow

```
┌─────────┐      ┌──────────────┐      ┌────────────┐      ┌──────────┐
│ Client  │      │ AuthController│     │UserService │      │ Database │
└────┬────┘      └──────┬───────┘      └─────┬──────┘      └────┬─────┘
     │                  │                     │                   │
     │ POST /login      │                     │                   │
     ├─────────────────>│                     │                   │
     │                  │                     │                   │
     │                  │ login(credentials)  │                   │
     │                  ├────────────────────>│                   │
     │                  │                     │                   │
     │                  │                     │ Authenticate      │
     │                  │                     │ (Spring Security) │
     │                  │                     │                   │
     │                  │                     │ Load user         │
     │                  │                     ├──────────────────>│
     │                  │                     │<──────────────────┤
     │                  │                     │ (with roles)      │
     │                  │                     │                   │
     │                  │                     │ Verify password   │
     │                  │                     │ (BCrypt)          │
     │                  │                     │                   │
     │                  │                     │ Generate tokens   │
     │                  │                     │ - Access (JWT)    │
     │                  │                     │ - Refresh (UUID)  │
     │                  │                     │                   │
     │                  │                     │ Save refresh token│
     │                  │                     ├──────────────────>│
     │                  │                     │<──────────────────┤
     │                  │                     │                   │
     │                  │                     │ Publish Event     │
     │                  │                     │ (LoginSuccess)    │
     │                  │                     │                   │
     │                  │<────────────────────┤                   │
     │                  │                     │                   │
     │<─────────────────┤                     │                   │
     │ 200 OK           │                     │                   │
     │ {accessToken,    │                     │                   │
     │  refreshToken,   │                     │                   │
     │  username}       │                     │                   │
     │                  │                     │                   │
     │                  │    ┌──────────────────────┐             │
     │                  │    │ AuthEventListener    │             │
     │                  │    │ (Async)              │             │
     │                  │    │ Log audit entry      │             │
     │                  │    │ Create notification  │             │
     │                  │    └──────────────────────┘             │
```

### 3. Authenticated Request Flow

```
┌─────────┐      ┌──────────┐      ┌────────────┐      ┌──────────┐
│ Client  │      │JwtFilter │      │ Controller │      │ Service  │
└────┬────┘      └─────┬────┘      └─────┬──────┘      └────┬─────┘
     │                 │                  │                   │
     │ GET /api/users  │                  │                   │
     │ Authorization:  │                  │                   │
     │ Bearer <token>  │                  │                   │
     ├────────────────>│                  │                   │
     │                 │                  │                   │
     │                 │ Extract token    │                   │
     │                 │                  │                   │
     │                 │ Validate token   │                   │
     │                 │ (JwtUtil)        │                   │
     │                 │                  │                   │
     │                 │ Extract roles    │                   │
     │                 │                  │                   │
     │                 │ Set Security     │                   │
     │                 │ Context          │                   │
     │                 │                  │                   │
     │                 ├─────────────────>│                   │
     │                 │                  │                   │
     │                 │                  │ Check @PreAuthorize│
     │                 │                  │ (ADMIN role?)     │
     │                 │                  │                   │
     │                 │                  │ getAllUsers()     │
     │                 │                  ├──────────────────>│
     │                 │                  │                   │
     │                 │                  │<──────────────────┤
     │                 │                  │                   │
     │                 │<─────────────────┤                   │
     │<────────────────┤                  │                   │
     │ 200 OK          │                  │                   │
     │ [user list]     │                  │                   │
```

## Component Interactions

### Security Layer

```
┌───────────────────────────────────────────────────────────────┐
│                      SECURITY LAYER                            │
│                                                                 │
│  ┌────────────────┐         ┌──────────────────┐             │
│  │  JwtFilter     │────────>│ UserDetailsService│             │
│  │  - Extract JWT │         │ - Load user       │             │
│  │  - Validate    │         │ - Get roles       │             │
│  └────────┬───────┘         └──────────────────┘             │
│           │                                                     │
│           ▼                                                     │
│  ┌────────────────┐         ┌──────────────────┐             │
│  │ SecurityContext│────────>│ Authentication   │             │
│  │  - Store auth  │         │ - Principal      │             │
│  └────────────────┘         │ - Roles          │             │
│                              └──────────────────┘             │
└───────────────────────────────────────────────────────────────┘
```

### Event Processing

```
┌───────────────────────────────────────────────────────────────┐
│                     EVENT PROCESSING                           │
│                                                                 │
│  ┌────────────┐      ┌─────────────────┐      ┌────────────┐│
│  │  Service   │─────>│ Event Publisher │─────>│  Listener  ││
│  │  Actions   │      │ (Spring Events) │      │  (Async)   ││
│  └────────────┘      └─────────────────┘      └─────┬──────┘│
│       │                                              │        │
│       │ Publish:                                     │ Handle:│
│       │ - UserCreated                                │ - Create│
│       │ - LoginEvent                                 │   notif││
│       │ - PasswordChanged                            │ - Send  │
│       │ - RoleChanged                                │   email││
│       │                                              │ - Log   │
│       └──────────────────────────────────────────────┘ audit  │
│                                                                 │
└───────────────────────────────────────────────────────────────┘
```

## Data Flow

### Token Management

```
┌─────────────────────────────────────────────────────────────┐
│                     TOKEN LIFECYCLE                          │
│                                                               │
│  Login                                                        │
│    │                                                          │
│    ▼                                                          │
│  ┌────────────────┐                                          │
│  │ Generate Tokens│                                          │
│  │ - Access (JWT) │────> Sent to client                     │
│  │ - Refresh (DB) │────> Saved in database                  │
│  └────────────────┘                                          │
│                                                               │
│  Access Token Expires                                         │
│    │                                                          │
│    ▼                                                          │
│  ┌────────────────┐                                          │
│  │ Refresh Token  │                                          │
│  │ - Validate     │<──── Sent by client                     │
│  │ - Check DB     │<──── Verify in database                 │
│  │ - Generate new │────> New access token                   │
│  └────────────────┘                                          │
│                                                               │
│  Logout / Password Change                                     │
│    │                                                          │
│    ▼                                                          │
│  ┌────────────────┐                                          │
│  │ Revoke Token   │                                          │
│  │ - Mark revoked │────> Update database                    │
│  │ - Clear context│────> Remove from security context       │
│  └────────────────┘                                          │
└─────────────────────────────────────────────────────────────┘
```

## Deployment Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    DEPLOYMENT ENVIRONMENT                      │
│                                                                │
│  ┌────────────────────────────────────────────────────────┐  │
│  │              Docker Compose Network                     │  │
│  │                                                          │  │
│  │  ┌─────────────────┐         ┌─────────────────┐      │  │
│  │  │  Auth Service   │         │   PostgreSQL    │      │  │
│  │  │  (Spring Boot)  │────────>│   Container     │      │  │
│  │  │  Port: 8081     │         │   Port: 5433    │      │  │
│  │  └─────────────────┘         └─────────────────┘      │  │
│  │         │                              │               │  │
│  │         │                              │               │  │
│  │         ▼                              ▼               │  │
│  │  ┌─────────────┐              ┌──────────────┐       │  │
│  │  │   Volume    │              │   Volume     │       │  │
│  │  │  (Logs)     │              │  (auth_data) │       │  │
│  │  └─────────────┘              └──────────────┘       │  │
│  │                                                        │  │
│  └────────────────────────────────────────────────────────┘  │
│                              │                                │
│                              │ Network: minibank-network      │
│                              ▼                                │
│  ┌────────────────────────────────────────────────────────┐  │
│  │         Other MiniBank Services (Future)                │  │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐      │  │
│  │  │  Account   │  │  Customer  │  │Transaction │      │  │
│  │  │  Service   │  │  Service   │  │  Service   │      │  │
│  │  └────────────┘  └────────────┘  └────────────┘      │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

## Technology Stack Details

```
┌─────────────────────────────────────────────────────────────┐
│                    TECHNOLOGY LAYERS                         │
│                                                               │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Framework: Spring Boot 3.5.7                         │   │
│  │ Language: Java 17                                    │   │
│  └─────────────────────────────────────────────────────┘   │
│                          │                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Security: Spring Security + JWT (jjwt 0.13.0)       │   │
│  │ Validation: Jakarta Validation                       │   │
│  │ Events: Spring Events + @Async                       │   │
│  └─────────────────────────────────────────────────────┘   │
│                          │                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Persistence: JPA/Hibernate                           │   │
│  │ Database: PostgreSQL 15                              │   │
│  │ Connection Pool: HikariCP                            │   │
│  └─────────────────────────────────────────────────────┘   │
│                          │                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Email: JavaMailSender (Spring Mail)                  │   │
│  │ Build: Maven 3.9+                                    │   │
│  │ Containerization: Docker                             │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Security Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    SECURITY ARCHITECTURE                      │
│                                                                │
│  ┌────────────────────────────────────────────────────────┐  │
│  │                  REQUEST LAYER                          │  │
│  │  - CORS Filter                                          │  │
│  │  - JWT Filter (Extract & Validate)                     │  │
│  │  - Security Context Setup                              │  │
│  └────────────┬───────────────────────────────────────────┘  │
│               │                                               │
│  ┌────────────▼───────────────────────────────────────────┐  │
│  │              AUTHORIZATION LAYER                        │  │
│  │  - Role-based Access Control                           │  │
│  │  - Method-level Security (@PreAuthorize)               │  │
│  │  - Endpoint Protection                                  │  │
│  └────────────┬───────────────────────────────────────────┘  │
│               │                                               │
│  ┌────────────▼───────────────────────────────────────────┐  │
│  │               AUTHENTICATION LAYER                      │  │
│  │  - Username/Password Authentication                    │  │
│  │  - BCrypt Password Encoder                             │  │
│  │  - UserDetailsService                                   │  │
│  └────────────┬───────────────────────────────────────────┘  │
│               │                                               │
│  ┌────────────▼───────────────────────────────────────────┐  │
│  │                  TOKEN LAYER                            │  │
│  │  - JWT Generation & Validation                         │  │
│  │  - Refresh Token Management                            │  │
│  │  - Token Revocation                                     │  │
│  └────────────┬───────────────────────────────────────────┘  │
│               │                                               │
│  ┌────────────▼───────────────────────────────────────────┐  │
│  │                   AUDIT LAYER                           │  │
│  │  - Event Logging                                        │  │
│  │  - IP Address Tracking                                  │  │
│  │  - Action Auditing                                      │  │
│  └─────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

## Scalability Considerations

### Horizontal Scaling
```
                    ┌──────────────┐
                    │ Load Balancer│
                    └───────┬──────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│Auth Service 1 │   │Auth Service 2 │   │Auth Service N │
└───────┬───────┘   └───────┬───────┘   └───────┬───────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            │
                    ┌───────▼──────┐
                    │  PostgreSQL  │
                    │   (Master)   │
                    └──────────────┘
```

### Stateless Design
- No session state stored in application
- All state in JWT tokens or database
- Easy to add/remove instances
- Load balancing friendly

---

## Key Design Patterns

1. **Repository Pattern**: Data access abstraction
2. **Service Layer Pattern**: Business logic separation
3. **DTO Pattern**: Data transfer objects
4. **Event-Driven Pattern**: Async notifications
5. **Filter Pattern**: JWT validation
6. **Factory Pattern**: Token generation
7. **Strategy Pattern**: Authentication providers

---

## Integration Points

### Future Microservices
- Account Service (via REST/Events)
- Customer Service (via REST/Events)
- Transaction Service (via REST/Events)
- API Gateway (centralized auth)

### External Systems
- Email Service (SMTP)
- SMS Gateway (ready for integration)
- Webhook endpoints (ready for integration)
- Monitoring systems (logs, metrics)

---

**For detailed implementation, see AUTH_README.md**
