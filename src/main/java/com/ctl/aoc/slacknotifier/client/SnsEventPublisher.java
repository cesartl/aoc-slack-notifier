package com.ctl.aoc.slacknotifier.client;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.ctl.aoc.slacknotifier.ConfigVariables;
import com.ctl.aoc.slacknotifier.model.AocCompareEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SnsEventPublisher implements EventPublisher {

    private static final Logger logger = LogManager.getLogger(SnsEventPublisher.class);

    private final AmazonSNS snsClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private String snsTopicArn;

    @Autowired
    public SnsEventPublisher(AmazonSNS snsClient) {
        this.snsClient = snsClient;
    }

    private synchronized void createSnsTopicArn() {
        if (this.snsTopicArn == null) {
            final String topicName = System.getenv(ConfigVariables.SNS_TOPIC);
            logger.info(String.format("Finding ARN for SNS topic '%s'", topicName));
            final CreateTopicResult createTopicResult = snsClient.createTopic(topicName);
            this.snsTopicArn = createTopicResult.getTopicArn();
        }
    }

    @Override
    public void publish(AocCompareEvent event) {
        try {
            createSnsTopicArn();
            final String jsonString = mapper.writeValueAsString(event);
            logger.info("Publishing to topic: " + snsTopicArn);
            final PublishRequest publishRequest = new PublishRequest();
            snsClient.publish(snsTopicArn, jsonString);
        } catch (JsonProcessingException e) {
            logger.error("Could not write event to JSON", e);
            throw new IllegalArgumentException("Could not write event to JSOn", e);
        }
    }
}
