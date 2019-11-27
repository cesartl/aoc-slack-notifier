package com.ctl.aoc.slacknotifier.client;

import com.ctl.aoc.slacknotifier.model.LeaderboardChangeEvent;
import com.ctl.aoc.slacknotifier.model.LeaderboardMemberChange;
import com.github.seratch.jslack.Slack;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

class SlackLeaderboardNotifierTest {

    /**
     * Use this test if you want to test the Slack rendering. Just make sure you set the SLACK_TOKEN env variable
     * in your run configuration to be a token for Slack channel you own.
     * @throws Exception
     */
    @Test
    @Ignore
    void testNotifyLeaderboardChange() throws Exception {
        Slack slack = Slack.getInstance();

        LeaderboardNotifier leaderboardNotifier = new SlackLeaderboardNotifier(slack);

        final LeaderboardChangeEvent event = LeaderboardChangeEvent.builder()
                .leaderboardId("123")
                .yearEvent("2018")
                .memberEvent(LeaderboardMemberChange.builder()
                        .memberName("Alice")
                        .oldStars(10)
                        .oldRank(4)
                        .newStars(12)
                        .newRank(2)
                        .build())
                .memberEvent(LeaderboardMemberChange.builder()
                        .memberName("Bob")
                        .oldStars(10)
                        .oldRank(2)
                        .newStars(12)
                        .newRank(3)
                        .build())
                .build();

        leaderboardNotifier.notifyLeaderboardChange(event);
    }
}