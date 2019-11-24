package com.ctl.aoc.slacknotifier.client;

import com.ctl.aoc.slacknotifier.model.LeaderboardChangeEvent;
import com.ctl.aoc.slacknotifier.model.LeaderboardMemberChange;
import com.ctl.aoc.slacknotifier.util.Aoc;
import com.ctl.aoc.slacknotifier.util.Ordinal;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.github.seratch.jslack.api.webhook.Payload;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SlackLeaderboardNotifier implements LeaderboardNotifier {

    private final Slack slack = Slack.getInstance();

    @Override
    public void notify(LeaderboardChangeEvent leaderboardChangeEvent) {
        try {
            final Payload payload = buildSlackPayload(leaderboardChangeEvent);
            slack.send("", payload);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not send message to slack", e);
        }
    }

    private Payload buildSlackPayload(LeaderboardChangeEvent leaderboardChangeEvent) {
        final Payload.PayloadBuilder payloadBuilder = Payload.builder();

        final List<Field> fields = leaderboardChangeEvent.getMemberEvents()
                .stream()
                .map(this::buildAttachmentField).collect(Collectors.toList());

        final Attachment attachment = Attachment.builder()
                .fields(fields)
                .fallback(fields.get(0).getValue())
                .color("#ff0012") //TODO
                .authorName("Chief Elf Officer")
                .authorIcon("https://static-s.aa-cdn.net/img/ios/1179905963/748be2960336a22c900af8903c355c6b?v=1")
                .footer(Aoc.buildLeaderboardUrl(leaderboardChangeEvent.getYearEvent(), leaderboardChangeEvent.getLeaderboardId()))
                .mrkdwnIn(List.of("fields"))
                .build();

        payloadBuilder.attachments(List.of(attachment));
        return payloadBuilder.build();
    }

    private Field buildAttachmentField(LeaderboardMemberChange memberEvent) {
        final Field.FieldBuilder fieldBuilder = Field.builder();
        final int rankDiff = memberEvent.getOldRank() - memberEvent.getNewRank();
        final int earnedStars = memberEvent.getNewStars() - memberEvent.getOldStars();
        final String newPlace = Ordinal.ordinalSuffix(memberEvent.getNewRank() + 1);
        String rankChange;
        if (rankDiff > 0) {
            rankChange = String.format("↑ %s \uD83C\uDFC6", newPlace); //↑ ${newPlace} 🏆
        } else if (rankDiff < 0) {
            rankChange = String.format("↓ %s \uD83D\uDC10", newPlace); //↓ ${newPlace} 🐐
        } else {
            rankChange = String.format("= %s \uD83D\uDC51", newPlace); //= ${newPlace} 👑
        }
        return Field.builder()
                .value(String.format("*%s*+=%d\uD83C\uDF1F. %s",
                        getDisplayName(memberEvent),
                        earnedStars,
                        rankChange))
                .valueShortEnough(false) //TODO
                .build();
    }

    private static String getDisplayName(LeaderboardMemberChange memberEvent) {
        if (memberEvent.getMemberName() == null || memberEvent.getMemberName().isBlank()) {
            return String.format("\uD83D\uDC7B#%s\uD83D\uDC7B", memberEvent.getMemberId()); //👻#${member}👻
        }
        return memberEvent.getMemberName();
    }
}
