package com.ctl.aoc.slacknotifier.functions;

import com.ctl.aoc.slacknotifier.ConfigVariables;
import com.ctl.aoc.slacknotifier.client.AocClient;
import com.ctl.aoc.slacknotifier.client.EventPublisher;
import com.ctl.aoc.slacknotifier.dao.PollingEventDao;
import com.ctl.aoc.slacknotifier.model.AocCompareEvent;
import com.ctl.aoc.slacknotifier.model.AocLeaderboardResponse;
import com.ctl.aoc.slacknotifier.model.PollingConfiguration;
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
public class PollingFunction implements Function<Flux<Map>, Void> {

    private static final Logger log = LogManager.getLogger(PollingFunction.class);

    private final PollingEventDao pollingEventDao;
    private final AocClient aocClient;
    private final EventPublisher eventPublisher;
    private final PollingConfiguration pollingConfiguration;

    @Autowired
    public PollingFunction(PollingEventDao pollingEventDao, AocClient aocClient, EventPublisher eventPublisher, PollingConfiguration pollingConfiguration) {
        this.pollingEventDao = pollingEventDao;
        this.aocClient = aocClient;
        this.eventPublisher = eventPublisher;
        this.pollingConfiguration = pollingConfiguration;
    }

    @Override
    public Void apply(Flux<Map> flux) {
        final Map map = flux.blockFirst();
        log.info("Received scheduled event");
        final String sessionId = System.getenv(ConfigVariables.AOC_SESSION_ID);

        // we do poll in series (as opposed to in parallel not to overload the AOC api.
        pollingConfiguration.getLeaderboards().forEach(pollingConfiguration -> {
            final String leaderboardId = pollingConfiguration.getLeaderboardId();
            final String yearEvent = pollingConfiguration.getYear();

            log.info(String.format("Polling %s for year %s", leaderboardId, yearEvent));

            final AocLeaderboardResponse aocResponse = aocClient.pollLeaderboard(yearEvent, leaderboardId, sessionId);
            final PollingEvent pollingEvent = PollingEvent.builder()
                    .leaderBoardId(leaderboardId)
                    .yearEvent(yearEvent)
                    .timestamp(Instant.now().toEpochMilli())
                    .data(aocResponse)
                    .build();

            //if we find a previous event we send it for comparison
            pollingEventDao.findLatest(leaderboardId, yearEvent, Instant.now())
                    .map(from -> AocCompareEvent.builder()
                            .from(from)
                            .to(pollingEvent)
                            .slackToken(pollingConfiguration.getSlackToken())
                            .build())
                    .ifPresent(eventPublisher::publish);

            pollingEventDao.save(pollingEvent);
        });
        return null;
    }
}
