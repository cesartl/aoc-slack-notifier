package com.ctl.aoc.slacknotifier.util;

public class Aoc {

    public static String buildLeaderboardUrl(String year, String leaderboardId) {
        return String.format("https://adventofcode.com/%s/leaderboard/private/view/%s", year, leaderboardId);
    }
}
