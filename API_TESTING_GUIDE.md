# Minibank API Testing Guide

This guide will help you test all Minibank microservices using the provided Postman collection.

## Prerequisites

1. **Postman**: Download and install [Postman](https://www.postman.com/downloads/)
2. **Docker & Docker Compose**: For running databases and Kafka
3. **Java 21**: Required to run the microservices
4. **Maven**: For building the services

## Setup Instructions

### 1. Start Infrastructure Services

Start PostgreSQL databases, Kafka, and Zookeeper:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL databases for all services (ports 5433-5438)
- Zookeeper (port 2181)
- Kafka (port 9092)

### 2. Build All Services

```bash
mvn clean install
```

### 3. Start Microservices

Start each service in a separate terminal:

```bash
# Terminal 1 - Auth Service (port 8081)
cd authService
mvn spring-boot:run

# Terminal 2 - Customer Service (port 8082)
cd CustomerService
mvn spring-boot:run

# Terminal 3 - Account Service (port 8083)
cd AccountService
mvn spring-boot:run

# Terminal 4 - KYC Service (port 8084)
cd KycService
mvn spring-boot:run

# Terminal 5 - Card Service (port 8085)
cd CardService
mvn spring-boot:run

# Terminal 6 - Transaction Service (port 8086)
cd TransactionService
mvn spring-boot:run
```

### 4. Set Environment Variable (JWT Secret)

Set the JWT_SECRET environment variable before starting services:

```bash
export JWT_SECRET="your-fallback-secret-key-min-32-chars-long-1234567890"
```

Or configure it in each service's application.properties file.

## Importing the Postman Collection

1. Open Postman
2. Click **Import** button (top left)
3. Select **Upload Files**
4. Choose `Minibank_Postman_Collection.json` from the project root
5. Click **Import**

## Using the Postman Collection

### Collection Variables

The collection uses variables to store common values. These are automatically set by test scripts:

- `base_url`: http://localhost
- `auth_port`: 8081
- `customer_port`: 8082
- `account_port`: 8083
- `kyc_port`: 8084
- `card_port`: 8085
- `transaction_port`: 8086
- `jwt_token`: Auto-populated after login
- `refresh_token`: Auto-populated after login
- `customer_id`: Auto-populated after customer creation
- `account_id`: Auto-populated after account creation
- `card_id`: Auto-populated after card creation

### Testing Flow

Follow this sequence for a complete test:

#### 1. Authentication Flow

1. **Register User**
   - Creates a new user with CUSTOMER role
   - Request body includes username, email, and password

2. **Login**
   - Authenticates user and receives JWT tokens
   - Access token is automatically saved to `{{jwt_token}}`
   - Refresh token is saved to `{{refresh_token}}`
   - **All subsequent requests will use this token automatically**

3. **Refresh Token** (Optional)
   - Use when access token expires
   - Gets a new access token using refresh token

4. **Change Password** (Optional)
   - Change user password
   - Requires current password and new password

5. **Logout** (Optional)
   - Revokes refresh token

#### 2. Customer Management

1. **Create Customer**
   - Creates a customer profile
   - Customer ID is automatically saved to `{{customer_id}}`
   - Required fields: firstName, lastName, email, phone, dateOfBirth, address

2. **Get All Customers** (Admin Only)
   - Lists all customers in the system

3. **Get Customer By ID**
   - Retrieves specific customer details

4. **Get Customer By Email**
   - Find customer by email address

5. **Update Customer**
   - Update customer information

6. **Delete Customer** (Admin Only)
   - Remove customer from system

#### 3. KYC Verification

1. **Submit KYC**
   - Upload KYC documents (ID proof, address proof)
   - Uses multipart/form-data

2. **Get KYC Status**
   - Check KYC verification status

3. **Verify KYC** (Admin Only)
   - Approve KYC documents

4. **Check If KYC Verified**
   - Boolean check if KYC is verified
   - **Account creation requires verified KYC**

#### 4. Account Management

1. **Create Account**
   - Creates a bank account for customer
   - Account ID is automatically saved to `{{account_id}}`
   - **Requires verified KYC**
   - Account types: SAVINGS, CHECKING, CURRENT

2. **Get All Accounts** (Admin Only)
   - Lists all accounts

3. **Get Account By ID**
   - Retrieve specific account details

4. **Get Account By Customer ID**
   - Get customer's account

5. **Deposit**
   - Deposit money into account
   - Triggers email notification via Kafka

6. **Withdraw**
   - Withdraw money from account
   - Triggers email notification via Kafka

7. **Block Account**
   - Block an account temporarily

8. **Activate Account**
   - Activate a blocked account

9. **Delete Account** (Admin Only)
   - Delete account (must have zero balance)

#### 5. Card Management

1. **Create Card**
   - Issues a new debit/credit card
   - Card ID is automatically saved to `{{card_id}}`
   - Card types: DEBIT, CREDIT
   - Triggers email notification via Kafka

2. **Get All Cards** (Admin Only)
   - Lists all cards

3. **Get Card By ID**
   - Retrieve card details

4. **Get Cards By Customer**
   - Get all cards for a customer

5. **Get Active Cards**
   - List only active cards

6. **Block Card**
   - Block a card (lost/stolen)

7. **Update Card PIN**
   - Change card PIN

8. **Update Card Status**
   - Change card status (ACTIVE, BLOCKED, EXPIRED)

9. **Delete Card** (Admin Only)
   - Remove card from system

#### 6. Transaction Management

1. **Create Transaction**
   - Create a new transaction
   - Transaction types: DEPOSIT, WITHDRAWAL, TRANSFER
   - Triggers email notification via Kafka

2. **Get All Transactions** (Admin Only)
   - Lists all transactions

3. **Get Transaction By ID**
   - Retrieve specific transaction

4. **Get Transactions By Account ID**
   - Get all transactions for an account

5. **Get Transactions By Customer ID**
   - Get all transactions for a customer

6. **Get Transactions By Status**
   - Filter by status: SUCCESS, FAILED, PENDING

## Role-Based Access Control

The system implements role-based JWT authentication:

### Roles

- **CUSTOMER**: Default role for registered users
- **ADMIN**: Administrative privileges

### Endpoint Access

| Endpoint | CUSTOMER | ADMIN |
|----------|----------|-------|
| Register/Login | ✅ | ✅ |
| Create Customer | ✅ | ✅ |
| Create Account | ✅ | ✅ |
| Create Card | ✅ | ✅ |
| Create Transaction | ✅ | ✅ |
| Get All Customers | ❌ | ✅ |
| Get All Accounts | ❌ | ✅ |
| Get All Cards | ❌ | ✅ |
| Get All Transactions | ❌ | ✅ |
| Delete Resources | ❌ | ✅ |
| Verify KYC | ❌ | ✅ |

## Kafka Event-Driven Architecture

The system uses Kafka for asynchronous communication:

### Topics

- `account-created`: Account creation events
- `account-updated`: Account modification events
- `account-deleted`: Account deletion events
- `transaction-created`: New transaction events
- `transaction-completed`: Successful transaction events
- `transaction-failed`: Failed transaction events
- `email-notifications`: Email notification events (consumed by Auth Service)

### Email Notifications

Email notifications are sent for:
- ✅ Customer registration
- ✅ Account creation
- ✅ Card creation
- ✅ Money deposits
- ✅ Money withdrawals
- ✅ Transaction completion
- ✅ Transaction failures

Configure email settings in `authService/application.properties`:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

## Troubleshooting

### JWT Token Issues

- Ensure you've logged in first - the token is auto-set
- Check token hasn't expired (1 hour validity)
- Use refresh token endpoint to get new access token

### Database Connection Issues

- Verify Docker containers are running: `docker ps`
- Check database ports are not in use
- Restart containers: `docker-compose restart`

### Kafka Issues

- Verify Kafka is running: `docker ps | grep kafka`
- Check Kafka logs: `docker logs kafka`
- Verify topics are created automatically on first message

### Service Not Starting

- Check port is not already in use
- Verify Java 21 is installed: `java -version`
- Check application logs for errors

### KYC Verification Required Error

- Ensure you've submitted KYC documents
- Verify KYC status is VERIFIED before creating account
- Use admin role to verify KYC

## Additional Notes

### Testing Admin Endpoints

To test admin-only endpoints:
1. Manually assign ADMIN role to a user in the database
2. Login with that user
3. Use the JWT token for admin operations

### Security Best Practices

- Change default JWT secret in production
- Use strong passwords
- Enable HTTPS in production
- Configure proper CORS settings
- Set up proper email credentials

### Performance Testing

For load testing:
- Use Postman's Collection Runner
- Set appropriate delays between requests
- Monitor service logs and resource usage

## Support

For issues or questions:
1. Check service logs for error messages
2. Verify all prerequisites are met
3. Ensure services started in correct order
4. Check database connectivity

## API Ports Quick Reference

| Service | Port |
|---------|------|
| Auth Service | 8081 |
| Customer Service | 8082 |
| Account Service | 8083 |
| KYC Service | 8084 |
| Card Service | 8085 |
| Transaction Service | 8086 |
| Kafka | 9092 |
| Zookeeper | 2181 |

---

**Happy Testing! 🚀**
