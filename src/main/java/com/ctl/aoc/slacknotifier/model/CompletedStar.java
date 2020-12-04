package com.ctl.aoc.slacknotifier.model;

import lombok.Data;

import java.util.Optional;

@Data
public class CompletedStar {
    private final int day;
    private final long timestamp;
    private final int part;

    public static Optional<CompletedStar> from(String day, int part, CompletionInfo info){
        return Optional.ofNullable(info)
                .map(CompletionInfo::getTimestamp)
                .map(timestamp -> new CompletedStar(Integer.parseInt(day), timestamp, part));
    }
}
