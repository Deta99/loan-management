package com.loan.kafka;

import com.loan.dto.event.LoanApprovedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoanApprovedConsumer {

    @KafkaListener(topics = "loan-approved", groupId = "loan-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(LoanApprovedEvent event) {
        log.info("Loan approved event received:");
        log.info("Loan ID: {}", event.getLoanId());
        log.info("Customer: {} ({})", event.getCustomerName(), event.getCustomerEmail());
        log.info("Amount: ${} for {} months", event.getAmount(), event.getTermMonths());
        log.info("--- Sending notification to customer ---");
    }
}