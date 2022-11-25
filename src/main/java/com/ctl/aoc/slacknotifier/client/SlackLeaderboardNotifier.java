package com.ctl.aoc.slacknotifier.client;

import com.ctl.aoc.slacknotifier.model.LeaderboardChangeEvent;
import com.ctl.aoc.slacknotifier.model.LeaderboardMemberChange;
import com.ctl.aoc.slacknotifier.util.Aoc;
import com.ctl.aoc.slacknotifier.util.Ordinal;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.github.seratch.jslack.api.webhook.Payload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SlackLeaderboardNotifier implements LeaderboardNotifier {

    private static final Logger log = LogManager.getLogger(SlackLeaderboardNotifier.class);

    private final Slack slack;

    @Autowired
    public SlackLeaderboardNotifier(Slack slack) {
        this.slack = slack;
    }

    @Override
    public void notifyLeaderboardChange(LeaderboardChangeEvent leaderboardChangeEvent, String slackToken) {
        try {
            final Payload payload = buildSlackPayload(leaderboardChangeEvent);
            final String url = buildSlackWebHook(slackToken);
            slack.methods(slackToken)
                    .chatPostMessage((ChatPostMessageRequest.ChatPostMessageRequestBuilder r) ->
                            buildSlackPayload(leaderboardChangeEvent, r.channel("adventofcode")));
        } catch (IOException | SlackApiException e) {
            log.error("Could not send message to slack", e);
            throw new IllegalArgumentException("Could not send message to slack", e);
        }
    }

    /**
     * Modify this method to change how the message is rendered in Slack.
     *
     * @param leaderboardChangeEvent
     * @return
     */
    private Payload buildSlackPayload(LeaderboardChangeEvent leaderboardChangeEvent) {
        final Payload.PayloadBuilder payloadBuilder = Payload.builder();

        final List<Field> fields = leaderboardChangeEvent.getMemberEvents()
                .stream()
                .filter(member -> member.getNewStars() > member.getOldStars()) //not showing member without update
                .map(this::buildAttachmentField)
                .collect(Collectors.toList());

        final Attachment attachment = Attachment.builder()
                .fields(fields)
                .fallback(fields.get(0).getValue())
                .color("#ff0012") //TODO
                .authorName(leaderboardChangeEvent.getYearEvent() + " Chief Elf Officer") //TODO
//                .authorIcon("https://static-s.aa-cdn.net/img/ios/1179905963/748be2960336a22c900af8903c355c6b?v=1") //TODO
                .footer(buildFooter(leaderboardChangeEvent))
                .mrkdwnIn(List.of("fields"))
                .build();

        payloadBuilder.attachments(List.of(attachment));
        payloadBuilder.iconEmoji("xmas-elf");
        return payloadBuilder.build();
    }


    private ChatPostMessageRequest.ChatPostMessageRequestBuilder buildSlackPayload(LeaderboardChangeEvent leaderboardChangeEvent, ChatPostMessageRequest.ChatPostMessageRequestBuilder request) {
        final List<Field> fields = leaderboardChangeEvent.getMemberEvents()
                .stream()
                .filter(member -> member.getNewStars() > member.getOldStars()) //not showing member without update
                .map(this::buildAttachmentField)
                .collect(Collectors.toList());

        final Attachment attachment = Attachment.builder()
                .fields(fields)
                .fallback(fields.get(0).getValue())
                .color("#ff0012") //TODO
                .authorName(leaderboardChangeEvent.getYearEvent() + " Chief Elf Officer") //TODO
//                .authorIcon("https://static-s.aa-cdn.net/img/ios/1179905963/748be2960336a22c900af8903c355c6b?v=1") //TODO
                .footer(buildFooter(leaderboardChangeEvent))
                .mrkdwnIn(List.of("fields"))
                .build();

        request.attachments(List.of(attachment));
        request.iconEmoji("xmas-elf");
        return request;
    }


    private String buildFooter(LeaderboardChangeEvent event) {
        var leaderboard = Aoc.buildLeaderboardUrl(event.getYearEvent(), event.getLeaderboardId());
        var stats = Aoc.buildStatsUrl(event.getYearEvent());
        return leaderboard + "\n" + stats;
    }

    private Field buildAttachmentField(LeaderboardMemberChange memberEvent) {
        final int rankDiff = memberEvent.getOldRank() - memberEvent.getNewRank();
        final int earnedStars = memberEvent.getNewStars() - memberEvent.getOldStars();
        final String newPlace = Ordinal.ordinalSuffix(memberEvent.getNewRank());
        String rankChange;
        if (rankDiff > 0) {
            rankChange = String.format("↑ %s \uD83C\uDFC6", newPlace); //↑ ${newPlace} 🏆
        } else if (rankDiff < 0) {
            rankChange = String.format("↓ %s \uD83D\uDC10", newPlace); //↓ ${newPlace} 🐐
        } else {
            rankChange = String.format("= %s \uD83D\uDC51", newPlace); //= ${newPlace} 👑
        }
        var day = LocalDate.now().getDayOfMonth();
        final List<String> times = memberEvent.getNewCompletedStars().stream()
                .map(star -> {
                    var dayInfo = "";
                    if (day != star.getDay()) {
                        dayInfo = String.format(" (day %s)", star.getDay());
                    }
                    final String icon;
                    if (star.getPart() == 1) {
                        icon = ":star:";
                    } else {
                        icon = ":star::star:";
                    }
                    var dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(star.getTimestamp()), ZoneOffset.UTC);
                    final String timestamp = DateTimeFormatter.ISO_LOCAL_TIME.format(dateTime);
                    return icon + timestamp + dayInfo;
                })
                .collect(Collectors.toList());
        final String timeInfo;
        if (times.isEmpty()) {
            timeInfo = "";
        } else {
            timeInfo = ":stopwatch:" + times.stream().collect(Collectors.joining(" ")) + ":stopwatch:";
        }
        return Field.builder()
                .value(String.format("*%s*+=%d\uD83C\uDF1F. %s \t%s",
                        getDisplayName(memberEvent),
                        earnedStars,
                        rankChange, timeInfo))
                .valueShortEnough(false) //TODO
                .build();
    }

    private static String getDisplayName(LeaderboardMemberChange memberEvent) {
        if (memberEvent.getMemberName() == null || memberEvent.getMemberName().isBlank()) {
            return String.format("\uD83D\uDC7B#%s\uD83D\uDC7B", memberEvent.getMemberId()); //👻#${member}👻
        }
        return memberEvent.getMemberName();
    }

    private static String buildSlackWebHook(String token) {
        return "https://hooks.slack.com/services/" + token;
    }
}
