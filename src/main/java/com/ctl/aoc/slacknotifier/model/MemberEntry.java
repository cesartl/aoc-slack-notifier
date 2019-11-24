package com.ctl.aoc.slacknotifier.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@DynamoDBDocument
public class MemberEntry {
    private String id;

    @JsonProperty("local_score")
    private int localScore;

    @JsonProperty("global_score")
    private int globalScore;

    private String name;

    private int stars;

    /**
     * The last timestamp this user won a star, we use this field to know if there has been new star for this user
     */
    @JsonProperty("last_star_ts")
    private long lastStarTimestamp;

    @JsonProperty("completion_day_level")
    private Map<String, DayCompletion> completionByDay;
}
