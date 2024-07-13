package dev.hipshot.web.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WebServerResponse {
    private final Map<String, List<String>> headers = new HashMap<>();
    private int status = 200;
    private ByteBuffer body;
    private Consumer<WebServerResponse> sender;

    void onSend(Consumer<WebServerResponse> sender) {
        this.sender = sender;
    }

    public WebServerResponse send() {
        sender.accept(this);
        return this;
    }

    public WebServerResponse body(ByteBuffer body) {
        this.body = body;
        return this;
    }

    public WebServerResponse body(String body) {
        this.body = ByteBuffer.wrap(body.getBytes());
        return this;
    }

    public ByteBuffer body() {
        return body;
    }

    public int status() {
        return status;
    }

    public WebServerResponse status(int status) {
        this.status = status;
        return this;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public WebServerResponse header(String key, String value) {
        headers.computeIfAbsent(key, (k) -> new ArrayList<>()).add(value);
        return this;
    }
}
