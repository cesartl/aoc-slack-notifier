package com.ctl.aoc.slacknotifier.functions;

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.ctl.aoc.slacknotifier.ConfigVariables;
import com.ctl.aoc.slacknotifier.client.AocClient;
import com.ctl.aoc.slacknotifier.dao.PollingEventDao;
import com.ctl.aoc.slacknotifier.model.PollingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class PollingFunction implements Function<ScheduledEvent, PollingEvent> {

    private final PollingEventDao pollingEventDao;
    private final AocClient aocClient;

    @Autowired
    public PollingFunction(PollingEventDao pollingEventDao, AocClient aocClient) {
        this.pollingEventDao = pollingEventDao;
        this.aocClient = aocClient;
    }

    @Override
    public PollingEvent apply(ScheduledEvent scheduledEvent) {
        final String leaderboardId = System.getenv(ConfigVariables.AOC_LEADERBOARD_ID);
        final String yearEvent = System.getenv(ConfigVariables.AOC_YEAR_EVENT);
        final String sessionId = System.getenv(ConfigVariables.AOC_SESSION_ID);
        final PollingEvent pollingEvent = aocClient.pollLeaderboard(yearEvent, leaderboardId, sessionId);
        return pollingEventDao.save(pollingEvent);
    }
}
