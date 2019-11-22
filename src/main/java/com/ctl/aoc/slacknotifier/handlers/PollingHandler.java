package com.ctl.aoc.slacknotifier.handlers;

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.ctl.aoc.slacknotifier.model.PollingEvent;
import org.springframework.cloud.function.adapter.aws.SpringBootRequestHandler;
import org.springframework.cloud.function.adapter.aws.SpringBootStreamHandler;

public class PollingHandler extends SpringBootStreamHandler {

}
