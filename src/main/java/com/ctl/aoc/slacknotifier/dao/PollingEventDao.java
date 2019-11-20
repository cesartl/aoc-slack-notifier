package com.ctl.aoc.slacknotifier.dao;

import com.ctl.aoc.slacknotifier.model.PollingEvent;

import java.time.Instant;
import java.util.Optional;

public interface PollingEventDao {

    PollingEvent save(PollingEvent pollingEvent);

    Optional<PollingEvent> findLatest(String leaderBoardId, String yearEvent, Instant now);
}
