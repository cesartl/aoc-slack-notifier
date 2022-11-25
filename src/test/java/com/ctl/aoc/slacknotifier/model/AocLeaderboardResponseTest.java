package com.ctl.aoc.slacknotifier.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.webhook.Payload;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class AocLeaderboardResponseTest {
    @Test
    void jsonParseTest() throws IOException {
        final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("leaderBoardResponse.json");

        final ObjectMapper mapper = new ObjectMapper();


        final AocLeaderboardResponse aocLeaderboardResponse = mapper.readValue(inputStream, AocLeaderboardResponse.class);
        System.out.println("");
        assertThat(aocLeaderboardResponse.getMembers())
                .hasEntrySatisfying("257500", memberEntry -> {
                    assertThat(memberEntry).hasFieldOrPropertyWithValue("name", "Cesar Tron-Lozai");
                    assertThat(memberEntry).hasFieldOrPropertyWithValue("id", "257500");
                    assertThat(memberEntry.getCompletionByDay()).hasEntrySatisfying("1", completion -> {
                        assertThat(completion.getPart1().getTimestamp()).isEqualTo(1543651716);
                        assertThat(completion.getPart2().getTimestamp()).isEqualTo(1543652670);
                    });
                });
    }

    @Test
    void slack() throws IOException, SlackApiException {
        final Payload.PayloadBuilder payloadBuilder = Payload.builder();
        payloadBuilder.text("hello");

        final Slack slack = Slack.getInstance();
        var token = "XXXXX";
        slack.methods(token)
                .chatPostMessage(r -> r.channel("adventofcode").text("hello"));
    }
}