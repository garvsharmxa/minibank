# MiniBank

MiniBank is a simple, microservices-based banking system implemented in Java.  
It provides core banking functionality such as user account management, transactions, and service-oriented architecture — designed to demonstrate a modular and extensible banking backend.

## 🏗️ Project Structure

The repository contains multiple services, each responsible for a specific business domain:

| Service | Responsibility |
|---------|----------------|
| `AccountService`     | Manage user accounts, balances, account creation / retrieval |
| `CustomerService`    | Manage customer data and profiles |
| `TransactionService` | Handle money transfers, deposits, withdrawals, transaction history |
| `CardService`        | (If applicable) Manage debit/credit cards |
| `KycService`         | (If applicable) Handle Know-Your-Customer / verification logic |
| `authService`        | Authentication & authorization (login, tokens, etc.) |
| `service-registry`  | Service discovery / registry (for microservices orchestration) |
| `ApiGateway`         | Entry point / gateway that routes external requests to appropriate services |
| `docker-compose.yml` | Docker Compose setup to bring up all services together |

## ✅ Features

- Create and manage user accounts  
- Perform transactions: deposit, withdrawal, transfer  
- Maintain customer profiles and account data  
- Modular microservices architecture for scalability and separation of concerns  
- Easily extendable — e.g. add card services, KYC, more services  

## 🛠️ Getting Started (Development Setup)

> Prerequisites: Java (JDK), Maven, Docker & Docker Compose

```bash
# 1. Clone this repository
git clone https://github.com/garvsharmxa/minibank.git
cd minibank

# 2. Build each service
mvn clean install

# 3. Bring up services via Docker Compose (if configured)
docker-compose up --build
