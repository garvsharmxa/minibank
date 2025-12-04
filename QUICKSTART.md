# MiniBank Authentication System - Quick Start Guide

## 🚀 Get Started in 5 Minutes

### Step 1: Start the Database
```bash
# From the project root
docker-compose up -d auth-postgres
```

### Step 2: Set Environment Variables
```bash
# Required - JWT Secret (minimum 32 characters)
export JWT_SECRET="your-super-secret-jwt-key-at-least-32-characters-long-change-in-production"

# Optional - Email notifications (if you want to enable them)
export EMAIL_USERNAME="your-email@gmail.com"
export EMAIL_PASSWORD="your-app-password"
export NOTIFICATION_EMAIL_ENABLED="false"  # Set to "true" to enable
```

### Step 3: Build and Run the Auth Service
```bash
cd authService
mvn clean package -DskipTests
java -jar target/authService-0.0.1-SNAPSHOT.jar
```

The service will start on **http://localhost:8081**

### Step 4: Test with Postman

#### Import the Collection
1. Open Postman
2. Import `MiniBank_API_Collection.postman_collection.json`
3. Import `MiniBank_Environment.postman_environment.json`
4. Select "MiniBank Local Environment" as active environment

#### Test the API
1. **Register a User**
   - Endpoint: `POST /auth/register`
   - The user will automatically get the CUSTOMER role
   - A welcome notification will be created

2. **Login**
   - Endpoint: `POST /auth/login`
   - Access token and refresh token are automatically saved to environment
   - A login success notification will be created

3. **Get Notifications**
   - Endpoint: `GET /api/notifications/unread`
   - You'll see your welcome and login notifications

4. **Create Admin User** (Manual step required)
   ```sql
   -- Connect to auth_db database
   -- Find your user ID from users table
   -- Find ADMIN role ID from roles table
   -- Insert into user_roles
   INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
   ```

5. **Test Admin Endpoints**
   - Login again to get new token with ADMIN role
   - Try `GET /api/users` to see all users
   - Try `POST /api/users/{id}/roles` to assign roles

## 🎯 Key Endpoints to Try

### Authentication
- `POST /auth/register` - Create new account
- `POST /auth/login` - Get access token
- `POST /auth/refresh` - Refresh access token
- `POST /auth/change-password` - Change password
- `POST /auth/logout` - Logout and revoke token

### User Management (Admin)
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user details
- `PUT /api/users/{id}` - Update user
- `POST /api/users/{id}/roles` - Assign role
- `DELETE /api/users/{id}/roles/{roleName}` - Remove role

### Notifications
- `GET /api/notifications` - Get all notifications (paginated)
- `GET /api/notifications/unread` - Get unread notifications
- `GET /api/notifications/unread/count` - Get unread count
- `PUT /api/notifications/{id}/read` - Mark as read

## 📝 Example API Calls

### Register
```bash
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john.doe@example.com",
    "password": "SecurePass123!"
  }'
```

### Login
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123!"
  }'
```

### Get Notifications (with token)
```bash
curl -X GET http://localhost:8081/api/notifications/unread \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 🔐 Default Roles

The system automatically creates 4 roles on startup:
- **ADMIN** - Full system access
- **CUSTOMER** - Default role for new users
- **MANAGER** - Customer support functions
- **AUDITOR** - Read-only access

## 🎪 Event-Driven Features

Notifications are automatically created for:
- ✅ User registration (Welcome message)
- ✅ Successful login (Login confirmation)
- ✅ Failed login attempts (Security alert)
- ✅ Password changes (Security notification)
- ✅ Role changes (Role update notification)

## 🔧 Troubleshooting

### Database Connection Issues
```bash
# Check if PostgreSQL is running
docker ps | grep auth-postgres

# Restart if needed
docker-compose restart auth-postgres
```

### Port Already in Use
```bash
# Change port in application.properties
server.port=8082
```

### Email Notifications Not Working
- Make sure you're using an app-specific password (not your regular password)
- For Gmail, enable 2FA and generate an app password
- Set `NOTIFICATION_EMAIL_ENABLED=true`

## 📚 Full Documentation

For complete documentation, see:
- **AUTH_README.md** - Comprehensive guide with all details
- **Postman Collection** - All endpoints with examples
- **Postman Environment** - Pre-configured variables

## 🎉 What's Next?

1. ✅ Test the authentication flow
2. ✅ Create an admin user
3. ✅ Test role-based access control
4. ✅ Explore notification system
5. ✅ Integrate with other microservices

## 💡 Tips

- Access tokens expire in 30 minutes
- Refresh tokens expire in 30 days
- All passwords are hashed with BCrypt
- IP addresses are logged for security
- Email notifications are optional
- In-app notifications are always enabled

## 🆘 Need Help?

Check the full documentation in **AUTH_README.md** for:
- Complete API reference
- Database schema details
- Security best practices
- Configuration options
- Architecture overview

---

**Happy Building! 🚀**
