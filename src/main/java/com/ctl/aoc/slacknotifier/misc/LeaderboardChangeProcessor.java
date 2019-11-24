package com.ctl.aoc.slacknotifier.misc;

import com.ctl.aoc.slacknotifier.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardChangeProcessor {

    private static final Logger logger = LogManager.getLogger(LeaderboardChangeProcessor.class);

    public static LeaderboardChangeEvent computeChanges(AocCompareEvent aocCompareEvent) {
        final LeaderboardChangeEvent.LeaderboardChangeEventBuilder builder = LeaderboardChangeEvent.builder();

        builder.yearEvent(aocCompareEvent.getTo().getYearEvent());
        builder.leaderboardId(aocCompareEvent.getTo().getLeaderBoardId());

        final AocLeaderboardResponse oldResponse = aocCompareEvent.getFrom().getData();
        final AocLeaderboardResponse newResponse = aocCompareEvent.getTo().getData();

        final Map<String, Integer> oldRank = rankMembers(oldResponse.getMembers().values(), Comparator.comparing(MemberEntry::getLocalScore));
        final Map<String, Integer> newRank = rankMembers(newResponse.getMembers().values(), Comparator.comparing(MemberEntry::getLocalScore));

        newResponse.getMembers().values().forEach(newMember -> {
            final String memberId = newMember.getId();
            // optional in case the member didn't exist previously
            final Optional<MemberEntry> oldMember = Optional.ofNullable(oldResponse.getMembers().get(memberId));

            final Long oldLastStartTimestamp = oldMember
                    .map(MemberEntry::getLastStarTimestamp)
                    .orElse(0L);

            // the user has won at least one start if the timestamp have changed
            if (oldLastStartTimestamp != newMember.getLastStarTimestamp()) {
                final LeaderboardMemberChange memberChange = LeaderboardMemberChange.builder()
                        .memberId(memberId)
                        .memberName(newMember.getName())
                        .newRank(newRank.get(memberId))
                        .newStars(newMember.getStars())
                        .oldRank(Optional.ofNullable(oldRank.get(memberId)).orElse(999))
                        .oldStars(oldMember.map(MemberEntry::getStars).orElse(0))
                        .build();
                builder.memberEvent(memberChange);
            }
        });

        return builder.build();
    }

    /**
     * Returns a map from [Member ID] to [Rank] (1 being the highest entry)
     *
     * @param members
     * @param comparator
     * @return
     */
    private static Map<String, Integer> rankMembers(Collection<MemberEntry> members, Comparator<MemberEntry> comparator) {
        final List<MemberEntry> sorted = members.stream()
                .sorted(comparator.reversed())
                .collect(Collectors.toList());
        final Map<String, Integer> sortedMap = new HashMap<>();
        for (int i = 0; i < sorted.size(); i++) {
            sortedMap.put(sorted.get(i).getId(), i + 1);
        }
        return sortedMap;
    }
}
