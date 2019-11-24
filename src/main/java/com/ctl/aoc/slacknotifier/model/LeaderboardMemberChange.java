package com.ctl.aoc.slacknotifier.model;

import lombok.Builder;
import lombok.Data;

/**
 * Represents the change for one member of a leaderboard between two polling events
 */
@Data
@Builder
public class LeaderboardMemberChange {
    private String memberId;
    private String memberName;
    private int oldStars;
    private int newStars;
    private int oldRank;
    private int newRank;
}
