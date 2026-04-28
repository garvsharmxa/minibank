import json

def create_request(name, method, url_path, port, body=None, params=None):
    req = {
        "name": name,
        "request": {
            "method": method,
            "header": [
                {
                    "key": "Content-Type",
                    "value": "application/json",
                    "type": "text"
                }
            ],
            "url": {
                "raw": f"http://localhost:{port}{url_path}",
                "protocol": "http",
                "host": ["localhost"],
                "port": str(port),
                "path": [p for p in url_path.split("/") if p]
            }
        },
        "response": []
    }
    if params:
        req["request"]["url"]["query"] = [{"key": k, "value": v} for k, v in params.items()]
        # update raw url
        query_string = "&".join([f"{k}={v}" for k, v in params.items()])
        req["request"]["url"]["raw"] += f"?{query_string}"
    
    if body:
        req["request"]["body"] = {
            "mode": "raw",
            "raw": json.dumps(body, indent=4),
            "options": {
                "raw": {
                    "language": "json"
                }
            }
        }
    return req

def main():
    collection = {
        "info": {
            "name": "MiniBank APIs",
            "description": "Complete A-Z Postman collection for the MiniBank Microservices Project",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "item": []
    }

    # ==========================
    # 1. Auth Service (Port 8081)
    # ==========================
    auth_items = []
    auth_port = 8081
    
    auth_items.append(create_request("Register User", "POST", "/auth/register", auth_port, body={
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "password": "Password123!"
    }))
    auth_items.append(create_request("Login User", "POST", "/auth/login", auth_port, body={
        "email": "john.doe@example.com",
        "password": "Password123!"
    }))
    auth_items.append(create_request("Refresh Token", "POST", "/auth/refresh", auth_port, body={
        "refreshToken": "your_refresh_token_here"
    }))
    auth_items.append(create_request("Logout", "POST", "/auth/logout", auth_port, body={
         "refreshToken": "your_refresh_token_here"
    }))
    auth_items.append(create_request("Change Password", "POST", "/auth/change-password", auth_port, body={
        "currentPassword": "Password123!",
        "newPassword": "NewPassword123!"
    }))
    
    auth_items.append(create_request("Get All Users", "GET", "/api/users", auth_port))
    auth_items.append(create_request("Get User by ID", "GET", "/api/users/{{userId}}", auth_port))
    auth_items.append(create_request("Update User", "PUT", "/api/users/{{userId}}", auth_port, body={
        "firstName": "John",
        "lastName": "Smith"
    }))
    auth_items.append(create_request("Delete User", "DELETE", "/api/users/{{userId}}", auth_port))
    auth_items.append(create_request("Assign Role", "POST", "/api/users/{{userId}}/roles", auth_port, body={
        "roleName": "ADMIN"
    }))
    auth_items.append(create_request("Remove Role", "DELETE", "/api/users/{{userId}}/roles/ADMIN", auth_port))
    
    collection["item"].append({"name": "Auth Service", "item": auth_items})

    # ==========================
    # 2. Customer Service (Port 8082)
    # ==========================
    customer_items = []
    customer_port = 8082
    
    customer_items.append(create_request("Create Customer", "POST", "/customers", customer_port, body={
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "phoneNumber": "+1234567890",
        "address": "123 Main St"
    }))
    customer_items.append(create_request("Get All Customers", "GET", "/customers", customer_port))
    customer_items.append(create_request("Get Customer by ID", "GET", "/customers/{{customerId}}", customer_port))
    customer_items.append(create_request("Check if Customer Exists", "GET", "/customers/{{customerId}}/exists", customer_port))
    customer_items.append(create_request("Get Customer Details", "GET", "/customers/{{customerId}}/details", customer_port))
    customer_items.append(create_request("Update Customer", "PUT", "/customers/{{customerId}}", customer_port, body={
        "firstName": "John Updated",
        "lastName": "Doe Updated"
    }))
    customer_items.append(create_request("Patch Customer", "PATCH", "/customers/{{customerId}}", customer_port, body={
        "address": "456 New St"
    }))
    customer_items.append(create_request("Delete Customer", "DELETE", "/customers/{{customerId}}", customer_port))
    customer_items.append(create_request("Get Customer by Email", "GET", "/customers/email/john.doe@example.com", customer_port))
    customer_items.append(create_request("Get Customer by Phone", "GET", "/customers/phone/+1234567890", customer_port))
    customer_items.append(create_request("Update Customer KYC ID", "PUT", "/customers/{{customerId}}/kyc/{{kycId}}", customer_port))
    
    collection["item"].append({"name": "Customer Service", "item": customer_items})

    # ==========================
    # 3. Account Service (Port 8083)
    # ==========================
    account_items = []
    account_port = 8083
    
    account_items.append(create_request("Create Account", "POST", "/accounts", account_port, body={
        "customerId": "{{customerId}}",
        "accountType": "SAVINGS",
        "balance": 1000.0,
        "currency": "USD"
    }))
    account_items.append(create_request("Get Account by ID", "GET", "/accounts/{{accountId}}", account_port))
    account_items.append(create_request("Get Account by Customer ID", "GET", "/accounts/customer/{{customerId}}", account_port))
    account_items.append(create_request("Get All Accounts", "GET", "/accounts", account_port))
    account_items.append(create_request("Update Account", "PUT", "/accounts/{{accountId}}", account_port, body={
        "accountType": "CURRENT"
    }))
    account_items.append(create_request("Deposit", "POST", "/accounts/{{accountId}}/deposit", account_port, params={"amount": "500.0"}))
    account_items.append(create_request("Withdraw", "POST", "/accounts/{{accountId}}/withdraw", account_port, params={"amount": "200.0"}))
    account_items.append(create_request("Block Account", "PUT", "/accounts/{{accountId}}/block", account_port))
    account_items.append(create_request("Activate Account", "PUT", "/accounts/{{accountId}}/activate", account_port))
    account_items.append(create_request("Check if Account Exists", "GET", "/accounts/{{accountId}}/exists", account_port))
    account_items.append(create_request("Delete Account", "DELETE", "/accounts/{{accountId}}", account_port))

    collection["item"].append({"name": "Account Service", "item": account_items})

    # ==========================
    # 4. KYC Service (Port 8084)
    # ==========================
    kyc_items = []
    kyc_port = 8084
    
    kyc_items.append(create_request("Create KYC", "POST", "/kyc/create", kyc_port, params={
        "customerId": "{{customerId}}",
        "panNumber": "ABCDE1234F",
        "aadharNumber": "123456789012"
    }))
    kyc_items.append(create_request("Update KYC", "PUT", "/kyc/{{kycId}}", kyc_port, params={
        "customerId": "{{customerId}}",
        "panNumber": "ABCDE1234X",
        "verified": "true"
    }))
    kyc_items.append(create_request("Get All KYC", "GET", "/kyc", kyc_port))
    kyc_items.append(create_request("Get KYC by ID", "GET", "/kyc/{{kycId}}", kyc_port))
    kyc_items.append(create_request("Get KYC by Customer ID", "GET", "/kyc/customer/{{customerId}}", kyc_port))
    kyc_items.append(create_request("Check if KYC Verified", "GET", "/kyc/customer/{{customerId}}/verified", kyc_port))
    kyc_items.append(create_request("Get KYC by PAN", "GET", "/kyc/pan/ABCDE1234F", kyc_port))
    kyc_items.append(create_request("Get KYC by Aadhaar", "GET", "/kyc/aadhaar/123456789012", kyc_port))
    kyc_items.append(create_request("Delete KYC", "DELETE", "/kyc/{{kycId}}", kyc_port))
    
    collection["item"].append({"name": "KYC Service", "item": kyc_items})

    # ==========================
    # 5. Card Service (Port 8085)
    # ==========================
    card_items = []
    card_port = 8085
    
    card_items.append(create_request("Create Card", "POST", "/cards/create", card_port, body={
        "accountId": "{{accountId}}",
        "customerId": "{{customerId}}",
        "cardType": "DEBIT",
        "pin": "1234"
    }))
    card_items.append(create_request("Get Card by ID", "GET", "/cards/{{cardId}}", card_port))
    card_items.append(create_request("Get All Cards", "GET", "/cards/all", card_port))
    card_items.append(create_request("Block Card", "PATCH", "/cards/{{cardId}}/block", card_port))
    card_items.append(create_request("Update Card PIN", "PATCH", "/cards/{{cardId}}/update-pin", card_port, params={
        "newPin": "4321"
    }))
    card_items.append(create_request("Update Card Status", "PATCH", "/cards/{{cardId}}/status", card_port, params={
        "status": "ACTIVE"
    }))
    card_items.append(create_request("Get Cards by Customer ID", "GET", "/cards/customer/{{customerId}}", card_port))
    card_items.append(create_request("Get Active Cards", "GET", "/cards/active", card_port))
    card_items.append(create_request("Delete Card", "DELETE", "/cards/{{cardId}}", card_port))

    collection["item"].append({"name": "Card Service", "item": card_items})

    # ==========================
    # 6. Transaction Service (Port 8086)
    # ==========================
    tx_items = []
    tx_port = 8086
    
    tx_items.append(create_request("Create Transaction", "POST", "/transactions/create/{{accountId}}", tx_port, body={
        "amount": 100.0,
        "transactionType": "DEBIT",
        "description": "Payment for services",
        "targetAccountId": "{{targetAccountId}}"
    }))
    tx_items.append(create_request("Get Transaction by ID", "GET", "/transactions/{{transactionId}}", tx_port))
    tx_items.append(create_request("Get All Transactions", "GET", "/transactions/all", tx_port))
    tx_items.append(create_request("Get Transactions by Customer ID", "GET", "/transactions/customer/{{customerId}}", tx_port))
    tx_items.append(create_request("Get Transactions by Account ID", "GET", "/transactions/account/{{accountId}}", tx_port))
    tx_items.append(create_request("Get Transactions by Status", "GET", "/transactions/status/SUCCESS", tx_port))

    collection["item"].append({"name": "Transaction Service", "item": tx_items})

    # ==========================
    # Write to File
    # ==========================
    with open("MiniBank_Postman_Collection.json", "w") as f:
        json.dump(collection, f, indent=4)
    print("Successfully generated MiniBank_Postman_Collection.json")

if __name__ == "__main__":
    main()
