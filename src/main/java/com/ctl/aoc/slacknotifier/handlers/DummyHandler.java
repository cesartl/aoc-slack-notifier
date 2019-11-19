package com.ctl.aoc.slacknotifier.handlers;

import com.ctl.aoc.slacknotifier.model.Dummy;
import org.springframework.cloud.function.adapter.aws.SpringBootRequestHandler;

public class DummyHandler extends SpringBootRequestHandler<Dummy, Dummy> {
}
