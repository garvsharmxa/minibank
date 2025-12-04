# MiniBank Authentication & Authorization System

## Overview

This document describes the comprehensive authentication and authorization system implemented for the MiniBank microservices application.

## Features

### 1. Authentication
- User registration with email validation
- Login with JWT token generation
- Access tokens (30 minutes expiry)
- Refresh tokens (30 days expiry) with database persistence
- Secure password storage using BCrypt
- Token-based stateless authentication
- IP address tracking for security

### 2. Role-Based Access Control (RBAC)
Four predefined roles with different permission levels:
- **ADMIN**: Full system access, user management, transaction oversight
- **CUSTOMER**: Account access, transactions, profile management (default role)
- **MANAGER**: Customer support, limited admin functions
- **AUDITOR**: Read-only access to transactions and reports

### 3. Security Features
- JWT tokens with role claims
- Method-level security using `@PreAuthorize`
- CORS configuration for cross-origin requests
- Password change with automatic token revocation
- Refresh token revocation on logout
- Protection against unauthorized access

### 4. Event-Driven Notifications
Automated notifications for:
- **User Events**: Registration, password changes, role modifications
- **Authentication Events**: Successful/failed login attempts with IP tracking
- **In-app Notifications**: Stored in database for user retrieval
- **Email Notifications**: Configurable email alerts (optional)

### 5. User Management (Admin)
- List all users
- View user details
- Update user information
- Delete users (with admin protection)
- Assign/remove roles
- Enable/disable user accounts

### 6. Audit Logging
- Track all authentication events
- Log user actions with timestamps
- IP address recording
- Action details and entity tracking

## API Endpoints

### Authentication Endpoints

#### Register User
```http
POST /auth/register
Content-Type: application/json

{
    "username": "johndoe",
    "email": "john.doe@example.com",
    "password": "SecurePass123!"
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
    "username": "johndoe",
    "password": "SecurePass123!"
}
```

Response:
```json
{
    "status": "success",
    "statusCode": 200,
    "message": "Login successful",
    "data": {
        "accessToken": "eyJhbGc...",
        "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
        "username": "johndoe",
        "tokenType": "Bearer",
        "expiresIn": 1800000
    }
}
```

#### Refresh Token
```http
POST /auth/refresh
Content-Type: application/json

{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### Change Password
```http
POST /auth/change-password
Authorization: Bearer <access_token>
Content-Type: application/json

{
    "currentPassword": "SecurePass123!",
    "newPassword": "NewSecurePass456!"
}
```

#### Logout
```http
POST /auth/logout
Authorization: Bearer <access_token>
Content-Type: application/json

{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### User Management Endpoints (Admin/Manager)

#### List All Users
```http
GET /api/users
Authorization: Bearer <access_token>
```
**Required Role**: ADMIN or MANAGER

#### Get User by ID
```http
GET /api/users/{id}
Authorization: Bearer <access_token>
```
**Required Role**: ADMIN or MANAGER

#### Update User
```http
PUT /api/users/{id}
Authorization: Bearer <access_token>
Content-Type: application/json

{
    "username": "johndoe_updated",
    "email": "john.updated@example.com",
    "enabled": true
}
```
**Required Role**: ADMIN

#### Delete User
```http
DELETE /api/users/{id}
Authorization: Bearer <access_token>
```
**Required Role**: ADMIN

#### Assign Role
```http
POST /api/users/{id}/roles
Authorization: Bearer <access_token>
Content-Type: application/json

{
    "roleName": "ADMIN"
}
```
**Required Role**: ADMIN

#### Remove Role
```http
DELETE /api/users/{id}/roles/{roleName}
Authorization: Bearer <access_token>
```
**Required Role**: ADMIN

### Notification Endpoints

#### Get Notifications (Paginated)
```http
GET /api/notifications?page=0&size=20
Authorization: Bearer <access_token>
```

#### Get Unread Notifications
```http
GET /api/notifications/unread
Authorization: Bearer <access_token>
```

#### Get Unread Count
```http
GET /api/notifications/unread/count
Authorization: Bearer <access_token>
```

#### Mark Notification as Read
```http
PUT /api/notifications/{id}/read
Authorization: Bearer <access_token>
```

#### Mark All as Read
```http
PUT /api/notifications/read-all
Authorization: Bearer <access_token>
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Roles Table
```sql
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);
```

### User Roles (Many-to-Many)
```sql
CREATE TABLE user_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### Refresh Tokens
```sql
CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(500) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN DEFAULT false,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Notifications
```sql
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Audit Log
```sql
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    details TEXT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## Configuration

### Application Properties
Key configuration properties in `application.properties`:

```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:your-fallback-secret-key-min-32-chars-long-1234567890}
jwt.access-token-expiration=1800000  # 30 minutes
jwt.refresh-token-expiration=2592000000  # 30 days

# Email Configuration (optional)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME:}
spring.mail.password=${EMAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Notification Settings
notification.email.enabled=${NOTIFICATION_EMAIL_ENABLED:false}
notification.sms.enabled=false
notification.in-app.enabled=true
```

### Environment Variables
- `JWT_SECRET`: Secret key for JWT token signing (minimum 32 characters)
- `EMAIL_USERNAME`: SMTP username for email notifications
- `EMAIL_PASSWORD`: SMTP password for email notifications
- `NOTIFICATION_EMAIL_ENABLED`: Enable/disable email notifications (default: false)

## Setup Instructions

### 1. Database Setup
Start PostgreSQL using Docker Compose:
```bash
docker-compose up -d auth-postgres
```

### 2. Configure Environment Variables
Set the required environment variables:
```bash
export JWT_SECRET="your-super-secret-jwt-key-at-least-32-characters-long"
export EMAIL_USERNAME="your-email@gmail.com"
export EMAIL_PASSWORD="your-app-password"
export NOTIFICATION_EMAIL_ENABLED="true"
```

### 3. Build and Run
```bash
cd authService
mvn clean package -DskipTests
java -jar target/authService-0.0.1-SNAPSHOT.jar
```

The service will start on port 8081.

### 4. Initial Setup
On first startup, the application automatically creates the four default roles:
- ADMIN
- CUSTOMER
- MANAGER
- AUDITOR

## Testing with Postman

### Import Collections
1. Import `MiniBank_API_Collection.postman_collection.json`
2. Import `MiniBank_Environment.postman_environment.json`

### Test Flow
1. **Register** a new user (gets CUSTOMER role by default)
2. **Login** to get access and refresh tokens (automatically saved to environment)
3. Create an **Admin user** manually in the database or through existing admin
4. **Assign roles** using admin endpoints
5. Test **role-based access** by trying different endpoints
6. Check **notifications** after various actions
7. Test **token refresh** mechanism
8. Test **change password** and verify token revocation

## Security Best Practices

### Implemented
✅ Passwords hashed with BCrypt (strength 12)
✅ JWT tokens with expiration
✅ Refresh token rotation
✅ Role-based authorization
✅ IP address tracking
✅ Audit logging
✅ CORS configuration
✅ Input validation
✅ Stateless authentication

### Recommendations for Production
- Use strong JWT secrets (at least 256 bits)
- Enable HTTPS/TLS
- Implement rate limiting
- Add account lockout after failed attempts
- Use secure session management
- Regular security audits
- Monitor audit logs
- Implement token blacklisting for immediate revocation
- Use environment-specific secrets
- Regular dependency updates

## Event System

### Event Types
- `UserCreatedEvent`: Triggered on user registration
- `LoginEvent`: Triggered on login attempts (success/failure)
- `PasswordChangedEvent`: Triggered on password change
- `RoleChangedEvent`: Triggered when roles are assigned/removed

### Event Listeners
- `UserEventListener`: Handles user-related events
- `AuthEventListener`: Handles authentication events

All event processing is asynchronous using `@Async` annotation.

## Troubleshooting

### Common Issues

**Issue**: Tests failing with connection refused
**Solution**: Start PostgreSQL database or skip tests with `-DskipTests`

**Issue**: Email notifications not working
**Solution**: Verify SMTP credentials and set `NOTIFICATION_EMAIL_ENABLED=true`

**Issue**: JWT token validation fails
**Solution**: Ensure JWT_SECRET is consistent and at least 32 characters

**Issue**: Role-based access not working
**Solution**: Verify user has correct roles assigned and tokens include role claims

## Architecture

### Package Structure
```
com.minibank.authservice/
├── Entity/          # JPA entities
├── Repository/      # Data access layer
├── Services/        # Business logic
├── Controller/      # REST endpoints
├── dto/            # Data transfer objects
├── event/          # Event classes
├── listener/       # Event listeners
├── exception/      # Custom exceptions
├── Conig/          # Security configuration
└── Utlity/         # JWT utilities
```

## Contributing

When contributing to the auth service:
1. Follow existing code patterns
2. Add validation to all DTOs
3. Use `@PreAuthorize` for role-based access
4. Publish events for significant actions
5. Add audit logging for security events
6. Update Postman collection with new endpoints
7. Document API changes

## License

This project is part of the MiniBank application.
