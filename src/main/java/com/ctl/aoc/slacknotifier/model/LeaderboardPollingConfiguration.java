package com.ctl.aoc.slacknotifier.model;

import lombok.Data;

@Data
public class LeaderboardPollingConfiguration {
    private String leaderboardId;
    private String year;
    private String slackToken;
}
