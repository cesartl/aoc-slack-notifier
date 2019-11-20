package com.ctl.aoc.slacknotifier.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.*;

@DynamoDBTable(tableName = "AOC_Polling_Event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollingEvent {
    private String leaderBoardId;

    private String yearEvent;

    @DynamoDBRangeKey()
    private long timestamp;

    private AocLeaderboardResponse data;

    /**
     * We need to create a partition id for Dynamo DB, because the composite key is (leaderBoardId,yearEvent,timestamp)
     * and we want to use timestamp as the range key, we need to concatenate leaderBoardId and yearEvent into a single field
     *
     * @return
     */
    @DynamoDBHashKey
    public String getIdentifier() {
        return identifier(leaderBoardId, yearEvent);
    }

    //dummy method to avoid reflexion problems
    public void setIdentifier(String identifier) {

    }

    public static String identifier(String leaderBoardId, String yearEvent) {
        return leaderBoardId + "-" + yearEvent;
    }

    /**
     * Returns a {@link PollingEvent} with the given primary key elements for searching
     *
     * @param leaderBoardId
     * @param yearEvent
     * @return
     */
    public static PollingEvent forHashValue(String leaderBoardId, String yearEvent) {
        return PollingEvent.builder().leaderBoardId(leaderBoardId).yearEvent(yearEvent).build();
    }
}
