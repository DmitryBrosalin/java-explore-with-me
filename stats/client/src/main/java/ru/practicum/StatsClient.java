package ru.practicum;

import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class StatsClient {
    private final RestTemplate rest = new RestTemplate();

    public ResponseEntity<Object> postHit(HitDto hitDto) {
        return makeAndSendRequest(HttpMethod.POST, "http://localhost:9090/hit", hitDto);
    }

    public ResponseEntity<Object> getStats(String start,
                                           String end,
                                           List<String> uris,
                                           Boolean unique) {
        StringBuilder path = new StringBuilder("http://localhost:9090/stats?start=" + start + "&end=" + end);
        if (uris != null) {
            for (String uri : uris) {
                path.append("&uri=").append(uri);
            }
        }
        if (unique != null) {
            path.append("&unique=").append(unique);
        }
        return makeAndSendRequest(HttpMethod.GET, path.toString(), null);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());
        ResponseEntity<Object> responseEntity;
        try {
            responseEntity = rest.exchange(path, method, requestEntity, Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepare(responseEntity);
    }

    private static ResponseEntity<Object> prepare(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());
        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }
        return responseBuilder.build();
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
