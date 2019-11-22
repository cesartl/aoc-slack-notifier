package com.ctl.aoc.slacknotifier.client;

import com.ctl.aoc.slacknotifier.model.AocLeaderboardResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Component
public class HttpAocClient implements AocClient {

    static final Logger logger = LogManager.getLogger(HttpAocClient.class);

    private final RestTemplate restTemplate;

    public HttpAocClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public AocLeaderboardResponse pollLeaderboard(String year, String leaderboardId, String sessionId) {
        final String url = buildLeaderboardUrl(year, leaderboardId);
        try {
            logger.info("Using url: " + url);
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.COOKIE, "session=" + sessionId);
            final HttpEntity<AocLeaderboardResponse> httpEntity = new HttpEntity<AocLeaderboardResponse>(null, headers);
            final ResponseEntity<AocLeaderboardResponse> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, AocLeaderboardResponse.class);
            logger.info("Response " + response.getStatusCodeValue());
            return response.getBody();
        } catch (RestClientException e) {
            logger.error("Could not fetch from " + url, e);
            throw new IllegalArgumentException("Could not fetch " + url);
        }
    }

    public static String buildLeaderboardUrl(String year, String leaderboardId) {
        return String.format("https://adventofcode.com/%s/leaderboard/private/view/%s.json", year, leaderboardId);
    }
}
