package com.ctl.aoc.slacknotifier.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@DynamoDBDocument
public class CompletionInfo {
    @JsonProperty("get_star_ts")
    private long timestamp;
}
