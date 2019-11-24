package com.ctl.aoc.slacknotifier.functions;

import com.ctl.aoc.slacknotifier.ConfigVariables;
import com.ctl.aoc.slacknotifier.client.AocClient;
import com.ctl.aoc.slacknotifier.client.EventPublisher;
import com.ctl.aoc.slacknotifier.dao.PollingEventDao;
import com.ctl.aoc.slacknotifier.model.AocCompareEvent;
import com.ctl.aoc.slacknotifier.model.AocLeaderboardResponse;
import com.ctl.aoc.slacknotifier.model.PollingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

/**
 * This is the function which is responsible for
 * <p>
 * - Calling the AOC API to get the score information modelled as a {@link PollingEvent}
 * <p>
 * - Store the historical {@link PollingEvent} in Dynamo DB
 * <p>
 * - Send a {@link AocCompareEvent} to SQS so that it can be processed by {@link CompareFunction}
 */
@Component
public class PollingFunction implements Function<Flux<Map>, Flux<PollingEvent>> {

    private static final Logger logger = LogManager.getLogger(PollingFunction.class);

    private final PollingEventDao pollingEventDao;
    private final AocClient aocClient;
    private final EventPublisher eventPublisher;

    @Autowired
    public PollingFunction(PollingEventDao pollingEventDao, AocClient aocClient, EventPublisher eventPublisher) {
        this.pollingEventDao = pollingEventDao;
        this.aocClient = aocClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Flux<PollingEvent> apply(Flux<Map> flux) {
        return flux.map(map -> {
            logger.info("Received scheduled event " + map.keySet());
            final String leaderboardId = System.getenv(ConfigVariables.AOC_LEADERBOARD_ID);
            final String yearEvent = System.getenv(ConfigVariables.AOC_YEAR_EVENT);
            final String sessionId = System.getenv(ConfigVariables.AOC_SESSION_ID);
            final AocLeaderboardResponse aocResponse = aocClient.pollLeaderboard(yearEvent, leaderboardId, sessionId);
            final PollingEvent pollingEvent = PollingEvent.builder()
                    .leaderBoardId(leaderboardId)
                    .yearEvent(yearEvent)
                    .timestamp(Instant.now().toEpochMilli())
                    .data(aocResponse)
                    .build();

            //if we find a previous event we send it for comparison
            logger.info("Searching latest event in dynamoDB");
            pollingEventDao.findLatest(leaderboardId, yearEvent, Instant.now())
                    .map(from -> AocCompareEvent.builder().from(from).to(pollingEvent).build())
                    .ifPresent(event -> {
                        logger.info("Found previous event in dynamoDB");
                        eventPublisher.publish(event);
                    });
            ;

            return pollingEventDao.save(pollingEvent);
        });
    }
}
