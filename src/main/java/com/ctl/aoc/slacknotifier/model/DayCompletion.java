package com.ctl.aoc.slacknotifier.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DynamoDBDocument
public class DayCompletion {
    @JsonProperty("1")
    private CompletionInfo part1;

    @JsonProperty("2")
    private CompletionInfo part2;

    public int numberOfStars() {
        int stars = 0;
        if (part1 != null) {
            stars++;
            if (part2 != null) {
                stars++;
            }
        }
        return stars;
    }
}
