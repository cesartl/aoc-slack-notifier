package com.ctl.aoc.slacknotifier.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AocCompareEvent {
    private PollingEvent from;
    private PollingEvent to;
    private String slackToken;
}
