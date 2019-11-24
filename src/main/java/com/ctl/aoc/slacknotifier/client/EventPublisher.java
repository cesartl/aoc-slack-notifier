package com.ctl.aoc.slacknotifier.client;

import com.ctl.aoc.slacknotifier.model.AocCompareEvent;

public interface EventPublisher {
    void publish(AocCompareEvent event);
}
