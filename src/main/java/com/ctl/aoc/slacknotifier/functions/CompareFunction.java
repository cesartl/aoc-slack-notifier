package com.ctl.aoc.slacknotifier.functions;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.ctl.aoc.slacknotifier.client.SlackLeaderboardNotifier;
import com.ctl.aoc.slacknotifier.misc.LeaderboardChangeProcessor;
import com.ctl.aoc.slacknotifier.model.AocCompareEvent;
import com.ctl.aoc.slacknotifier.model.LeaderboardChangeEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.function.Function;

@Component
public class CompareFunction implements Function<Flux<SQSEvent>, Void> {
    private static final Logger logger = LogManager.getLogger(CompareFunction.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private final SlackLeaderboardNotifier slackLeaderboardNotifier;

    @Autowired
    public CompareFunction(SlackLeaderboardNotifier slackLeaderboardNotifier) {
        this.slackLeaderboardNotifier = slackLeaderboardNotifier;
    }

    private void processMessage(SQSEvent.SQSMessage message) {
        try {
            final AocCompareEvent compareEvent = mapper.readValue(message.getBody(), AocCompareEvent.class);
            final LeaderboardChangeEvent leaderboardChangeEvent = LeaderboardChangeProcessor.computeChanges(compareEvent);
            if (!leaderboardChangeEvent.getMemberEvents().isEmpty()) {
                slackLeaderboardNotifier.notify(leaderboardChangeEvent);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not parse JSON object as AocCompareEvent.class", e);
        }
    }

    @Override
    public Void apply(Flux<SQSEvent> sqsEventFlux) {
        final SQSEvent sqsEvent = sqsEventFlux.blockFirst();
        logger.info("Received SQS event");
        Optional.ofNullable(sqsEvent).map(SQSEvent::getRecords).ifPresent(x -> x.forEach(this::processMessage));
        return null;
    }
}
