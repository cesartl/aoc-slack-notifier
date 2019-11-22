package com.ctl.aoc.slacknotifier.client;

import com.ctl.aoc.slacknotifier.model.AocLeaderboardResponse;

public interface AocClient {
    AocLeaderboardResponse pollLeaderboard(String year, String leaderboardId, String sessionId);
}
