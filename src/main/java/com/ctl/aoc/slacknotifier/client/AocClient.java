package com.ctl.aoc.slacknotifier.client;

import com.ctl.aoc.slacknotifier.model.PollingEvent;

public interface AocClient {
    PollingEvent pollLeaderboard(String year, String leaderboardId, String sessionId);
}
