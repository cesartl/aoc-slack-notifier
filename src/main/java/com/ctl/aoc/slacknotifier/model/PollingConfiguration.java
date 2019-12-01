package com.ctl.aoc.slacknotifier.model;

import lombok.Data;

import java.util.List;

@Data
public class PollingConfiguration {
    private List<LeaderboardPollingConfiguration> leaderboards;
}
