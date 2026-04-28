import os
services = ['AccountService', 'TransactionService', 'CardService', 'CustomerService', 'KycService']
block = """
        <!-- Security & JWT -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.13.0</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.13.0</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.13.0</version>
            <scope>runtime</scope>
        </dependency>
"""
for s in services:
    with open(f'{s}/pom.xml', 'r') as f:
        content = f.read()
    if 'spring-boot-starter-security' not in content:
        content = content.replace('</dependencies>', block + '    </dependencies>')
        with open(f'{s}/pom.xml', 'w') as f:
            f.write(content)

