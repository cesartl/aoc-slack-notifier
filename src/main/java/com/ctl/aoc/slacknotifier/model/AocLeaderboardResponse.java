package com.ctl.aoc.slacknotifier.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@DynamoDBDocument
public class AocLeaderboardResponse {

    @JsonProperty("owner_id")
    private String ownerId;

    private String event;

    private Map<String, MemberEntry> members;
}
