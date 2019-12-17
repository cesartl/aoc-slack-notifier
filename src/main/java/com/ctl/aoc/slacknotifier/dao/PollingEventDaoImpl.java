package com.ctl.aoc.slacknotifier.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.ctl.aoc.slacknotifier.model.PollingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class PollingEventDaoImpl implements PollingEventDao {

    private static final Logger logger = LogManager.getLogger(PollingEventDaoImpl.class);

    private final AmazonDynamoDB dynamoDbClient;
    private final DynamoDBMapper mapper;

    public PollingEventDaoImpl(AmazonDynamoDB dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
        mapper = new DynamoDBMapper(dynamoDbClient);
    }

    @Override
    public PollingEvent save(PollingEvent pollingEvent) {
        mapper.save(pollingEvent);
        return pollingEvent;
    }

    @Override
    public Optional<PollingEvent> findLatest(String leaderBoardId, String yearEvent, Instant now) {

        final Condition rangeKeyCondition = new Condition();
        rangeKeyCondition
                .withComparisonOperator(ComparisonOperator.LE)
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(now.toEpochMilli())));

        final DynamoDBQueryExpression<PollingEvent> expression = new DynamoDBQueryExpression<>();
        expression
                .withHashKeyValues(PollingEvent.forHashValue(leaderBoardId, yearEvent))
                .withRangeKeyCondition("timestamp", rangeKeyCondition)
                .withScanIndexForward(false)
                .withLimit(1); //for some reason it goes into infinite loop if this is set to one

        logger.info("Running DynamoDB query ");
        final QueryResultPage<PollingEvent> result = mapper.queryPage(PollingEvent.class, expression);
        return result.getResults().stream().findFirst();
    }
}
