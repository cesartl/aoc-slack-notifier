package com.ctl.aoc.slacknotifier.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.ctl.aoc.slacknotifier.model.PollingEvent;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Component
public class PollingEventDaoImpl implements PollingEventDao {
    private final AmazonDynamoDB dynamoDbClient;
    private final DynamoDBMapper mapper;
    private final Clock clock;

    public PollingEventDaoImpl(AmazonDynamoDB dynamoDbClient, Clock clock) {
        this.dynamoDbClient = dynamoDbClient;
        mapper = new DynamoDBMapper(dynamoDbClient);
        this.clock = clock;
    }

    @Override
    public PollingEvent save(PollingEvent pollingEvent) {
        mapper.save(pollingEvent);
        return pollingEvent;
    }

    @Override
    public Optional<PollingEvent> findLatest(String leaderBoardId, String yearEvent) {
        final String identifier = PollingEvent.identifier(leaderBoardId, yearEvent);

        final Instant now = Instant.now(clock);

        final Condition rangeKeyCondition = new Condition();
        rangeKeyCondition
                .withComparisonOperator(ComparisonOperator.LE)
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(now.toEpochMilli())));

        final DynamoDBQueryExpression<PollingEvent> expression = new DynamoDBQueryExpression<>();
        expression
                .withHashKeyValues(PollingEvent.forHashValue(leaderBoardId, yearEvent))
                .withRangeKeyCondition("timestamp", rangeKeyCondition)
                .withLimit(1);

        return mapper.query(PollingEvent.class, expression).stream().findFirst();
    }
}
