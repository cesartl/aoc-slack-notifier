package com.ctl.aoc.slacknotifier.functions;

import com.ctl.aoc.slacknotifier.model.Dummy;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class DummyFunction implements Function<Dummy, Dummy> {
    @Override
    public Dummy apply(Dummy dummy) {
        return new Dummy("Hello " + dummy.getName());
    }
}
