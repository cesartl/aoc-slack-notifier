package com.ctl.aoc.slacknotifier.util;

public final class Aoc {

    private Aoc() {
    }

    public static String buildLeaderboardUrl(String year, String leaderboardId) {
        return String.format("https://adventofcode.com/%s/leaderboard/private/view/%s", year, leaderboardId);
    }
}
