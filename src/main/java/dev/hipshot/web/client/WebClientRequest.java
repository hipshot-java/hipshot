package dev.hipshot.web.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
public class WebClientRequest {
    private final String method;
    private final String url;
    @Builder.Default private final Map<String, List<String>> queryParams = new HashMap<>();
    @Builder.Default private final Map<String, List<String>> headers = new HashMap<>();

    public String url() {
        return url;
    }

    public WebClientRequest queryParam(String key, String value) {
        queryParams.computeIfAbsent(key, (k) -> new ArrayList<>()).add(value);
        return this;
    }

    public WebClientRequest header(String key, String value) {
        headers.computeIfAbsent(key, (k) -> new ArrayList<>()).add(value);
        return this;
    }
}
