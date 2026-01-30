package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.config.RabbitMQConfig;
import com.finance.tracker.transactions.domain.SmsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageConsumer {

    private final TransactionService transactionService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void receiveMessage(SmsRequest message) {
        try {
            System.out.println("Received message: " + message);
            transactionService.createTransactionFromQueueMsg(message);
        } catch (Exception e) {
            // Throw to trigger DLX routing
            throw new AmqpRejectAndDontRequeueException("Processing failed, sending to DLQ", e);
        }
    }
}
