package com.ctl.aoc.slacknotifier.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class LeaderboardChangeEvent {

    private String yearEvent;

    private String leaderboardId;

    @Singular
    private List<LeaderboardMemberChange> memberEvents;
}
