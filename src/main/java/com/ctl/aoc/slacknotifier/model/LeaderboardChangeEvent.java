package com.ctl.aoc.slacknotifier.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 * An event build by {@link com.ctl.aoc.slacknotifier.misc.LeaderboardChangeProcessor},
 * which describes the change in ranks or stars earned between two polling event
 */
@Data
@Builder
public class LeaderboardChangeEvent {

    private String yearEvent;

    private String leaderboardId;

    @Singular
    private List<LeaderboardMemberChange> memberEvents;
}
