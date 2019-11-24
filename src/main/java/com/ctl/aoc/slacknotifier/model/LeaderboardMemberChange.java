package com.ctl.aoc.slacknotifier.model;

import lombok.Builder;
import lombok.Data;

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
