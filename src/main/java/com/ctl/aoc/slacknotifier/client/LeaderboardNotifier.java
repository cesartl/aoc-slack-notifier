package com.ctl.aoc.slacknotifier.client;

import com.ctl.aoc.slacknotifier.model.LeaderboardChangeEvent;

public interface LeaderboardNotifier {
    void notifyLeaderboardChange(LeaderboardChangeEvent leaderboardChangeEvent, String slackToken);
}
