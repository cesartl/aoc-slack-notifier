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

/**
 * This function is responsible for reading a {@link AocCompareEvent} from SQS, check if there are has been
 * any changes in the leaderboard, and write to Slack via {@link SlackLeaderboardNotifier} if needed.
 */
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
        logger.info("Processing message " + message.getMessageId());
        try {
            final AocCompareEvent compareEvent = mapper.readValue(message.getBody(), AocCompareEvent.class);
            final LeaderboardChangeEvent leaderboardChangeEvent = LeaderboardChangeProcessor.computeChanges(compareEvent);
            logger.info(String.format("There are %d changes for [year=%s, leaderBoard=%s]",
                    leaderboardChangeEvent.getMemberEvents().size()),
                    leaderboardChangeEvent.getYearEvent(),
                    leaderboardChangeEvent.getLeaderboardId());
            if (!leaderboardChangeEvent.getMemberEvents().isEmpty()) {
                slackLeaderboardNotifier.notifyLeaderboardChange(leaderboardChangeEvent);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not parse JSON object as AocCompareEvent.class", e);
        }
    }

    @Override
    public Void apply(Flux<SQSEvent> sqsEventFlux) {
        final SQSEvent sqsEvent = sqsEventFlux.blockFirst();
        logger.info(String.format("Received SQS event, will process %d messages", sqsEvent.getRecords().size()));
        Optional.ofNullable(sqsEvent).map(SQSEvent::getRecords).ifPresent(x -> x.forEach(this::processMessage));
        return null;
    }
}
