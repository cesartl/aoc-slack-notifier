package com.ctl.aoc.slacknotifier.handlers;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.springframework.cloud.function.adapter.aws.SpringBootRequestHandler;
import org.springframework.cloud.function.adapter.aws.SpringBootStreamHandler;

public class CompareHandler extends SpringBootRequestHandler<SQSEvent, Void> {
}
