package com.ctl.aoc.slacknotifier.client;

import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.ctl.aoc.slacknotifier.ConfigVariables;
import com.ctl.aoc.slacknotifier.model.AocCompareEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SqsEventPublisher implements EventPublisher {

    private static final Logger logger = LogManager.getLogger(SqsEventPublisher.class);

    private final AmazonSQS amazonSQS;
    private final ObjectMapper mapper = new ObjectMapper();

    private String queueUrl;

    @Autowired
    public SqsEventPublisher(AmazonSQS amazonSQS) {
        this.amazonSQS = amazonSQS;
    }

    private synchronized void createQueueIfNeeded() {
        if (this.queueUrl == null) {
            final String queueName = System.getenv(ConfigVariables.SQS_QUEUE_NAME);
            logger.info(String.format("Finding ARN for SQS queue '%s'", queueName));
            final CreateQueueResult queue = amazonSQS.createQueue(queueName);
            this.queueUrl = queue.getQueueUrl();
        }
    }

    @Override
    public void publish(AocCompareEvent event) {
        try {
            createQueueIfNeeded();
            final String jsonString = mapper.writeValueAsString(event);
            logger.info("Publishing to queue: " + queueUrl);
            logger.info("Message body: " + jsonString);
            amazonSQS.sendMessage(queueUrl, jsonString);
        } catch (JsonProcessingException e) {
            logger.error("Could not write event to JSON", e);
            throw new IllegalArgumentException("Could not write event to JSOn", e);
        }
    }
}
