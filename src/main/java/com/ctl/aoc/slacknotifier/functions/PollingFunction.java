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
import java.util.Optional;
import java.util.function.Function;

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

//    @Override
//    public void accept(Flux<ScheduledEvent> scheduledEventFlux) {
//        scheduledEventFlux.map(scheduledEvent -> {
//            System.out.println("Received scheduled event " + scheduledEvent.getId());
//            final String leaderboardId = System.getenv(ConfigVariables.AOC_LEADERBOARD_ID);
//            final String yearEvent = System.getenv(ConfigVariables.AOC_YEAR_EVENT);
//            final String sessionId = System.getenv(ConfigVariables.AOC_SESSION_ID);
//            final PollingEvent pollingEvent = aocClient.pollLeaderboard(yearEvent, leaderboardId, sessionId);
//            return pollingEventDao.save(pollingEvent);
//        }).blockFirst();
//    }


    @Override
    public Flux<PollingEvent> apply(Flux<Map> flux) {
        logger.info("Calling function");
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
            pollingEventDao.findLatest(leaderboardId, yearEvent, Instant.now())
                    .map(from -> AocCompareEvent.builder().from(from).to(pollingEvent).build())
                    .ifPresent(eventPublisher::publish);
            ;

            return pollingEventDao.save(pollingEvent);
        });
    }
}
