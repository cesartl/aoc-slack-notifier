package com.ctl.aoc.slacknotifier.functions;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class CompareFunction implements Function<Flux<SNSEvent>, Void> {
    private static final Logger logger = LogManager.getLogger(CompareFunction.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private void processRecord(SNSEvent.SNSRecord record) {
        logger.info("Processing " + record.getEventSource());
    }

    @Override
    public Void apply(Flux<SNSEvent> snsEventFlux) {
        final SNSEvent snsEvent = snsEventFlux.blockFirst();
        logger.info(String.format("Received SNS event %s", Optional.ofNullable(snsEvent).map(SNSEvent::toString).orElse("null")));
        Optional.ofNullable(snsEvent).map(SNSEvent::getRecords).ifPresent(x -> x.forEach(this::processRecord));
        return null;
    }
}
