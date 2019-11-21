package com.ctl.aoc.slacknotifier.client;

import com.ctl.aoc.slacknotifier.model.PollingEvent;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class HttpAocClient implements AocClient {

    private final RestTemplate restTemplate;

    public HttpAocClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public PollingEvent pollLeaderboard(String year, String leaderboardId, String sessionId) {
        final String url = buildLeaderboardUrl(year, leaderboardId);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, "session=" + sessionId);
        final HttpEntity<PollingEvent> httpEntity = new HttpEntity<PollingEvent>(null, headers);
        final ResponseEntity<PollingEvent> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, PollingEvent.class);
        return response.getBody();
    }

    public static String buildLeaderboardUrl(String year, String leaderboardId) {
        return String.format("https://adventofcode.com/%s/leaderboard/private/view/%s.json", year, leaderboardId);
    }
}
