package com.minibank.authservice.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.minibank.authservice.Services.NotificationService;
import com.minibank.authservice.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationConsumer {

    private final NotificationService notificationService;
    
    private static final String DEFAULT_FALLBACK_EMAIL = "noreply@minibank.com";

    @KafkaListener(topics = "email-notifications", groupId = "auth-service-email-group")
    public void consumeEmailNotification(JsonNode event) {
        try {
            log.info("Received email notification event: {}", event);

            String eventType = event.has("eventType") ? event.get("eventType").asText() : "";
            String email = (event.has("email") && event.get("email") != null && !event.get("email").isNull()) 
                    ? event.get("email").asText() 
                    : DEFAULT_FALLBACK_EMAIL;

            // Build notification DTO
            NotificationDto notification = new NotificationDto();
            notification.setChannel("EMAIL");

            // Determine notification content based on event type
            switch (eventType) {
                case "CREATED":
                    if (event.has("accountId")) {
                        notification.setSubject("Account Created Successfully");
                        notification.setMessage("Your account has been created successfully. Account Number: " +
                                event.get("accountNumber").asText());
                        notification.setRecipient(email);
                    } else if (event.has("customerId")) {
                        notification.setSubject("Customer Registration Successful");
                        notification.setMessage("Welcome! Your customer profile has been created successfully.");
                        notification.setRecipient(email);
                    } else if (event.has("cardId")) {
                        notification.setSubject("Card Created Successfully");
                        notification.setMessage("Your new card has been created. Card Number: " +
                                event.get("cardNumber").asText());
                        notification.setRecipient(email);
                    }
                    break;

                case "DEPOSIT":
                    notification.setSubject("Deposit Transaction Successful");
                    notification.setMessage(String.format("Amount of $%.2f has been deposited to your account.",
                            event.get("amount").asDouble()));
                    notification.setRecipient(email);
                    break;

                case "WITHDRAW":
                    notification.setSubject("Withdrawal Transaction Successful");
                    notification.setMessage(String.format("Amount of $%.2f has been withdrawn from your account.",
                            event.get("amount").asDouble()));
                    notification.setRecipient(email);
                    break;

                case "COMPLETED":
                    notification.setSubject("Transaction Completed");
                    notification.setMessage(String.format("Your transaction of $%.2f has been completed successfully.",
                            event.get("amount").asDouble()));
                    notification.setRecipient(email);
                    break;

                case "FAILED":
                    notification.setSubject("Transaction Failed");
                    notification.setMessage("Your recent transaction has failed. Please contact support if you need assistance.");
                    notification.setRecipient(email);
                    break;

                default:
                    log.warn("Unknown event type: {}", eventType);
                    return;
            }

            // Send notification
            notificationService.sendNotification(notification);
            log.info("Email notification sent successfully for event type: {}", eventType);

        } catch (Exception e) {
            log.error("Error processing email notification event: {}", e.getMessage(), e);
        }
    }
}
