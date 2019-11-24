package com.ctl.aoc.slacknotifier.functions;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.ctl.aoc.slacknotifier.model.AocCompareEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.function.Function;

@Component
public class CompareFunction implements Function<Flux<SQSEvent>, Void> {
    private static final Logger logger = LogManager.getLogger(CompareFunction.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private void processMessage(SQSEvent.SQSMessage message) {
        try {
            final AocCompareEvent compareEvent = mapper.readValue(message.getBody(), AocCompareEvent.class);

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not parse JSON object as AocCompareEvent.class", e);
        }
    }

    @Override
    public Void apply(Flux<SQSEvent> sqsEventFlux) {
        final SQSEvent sqsEvent = sqsEventFlux.blockFirst();
        logger.info(String.format("Received SQS event %s", Optional.ofNullable(sqsEvent).map(SQSEvent::toString).orElse("null")));
        Optional.ofNullable(sqsEvent).map(SQSEvent::getRecords).ifPresent(x -> x.forEach(this::processMessage));
        return null;
    }
}
