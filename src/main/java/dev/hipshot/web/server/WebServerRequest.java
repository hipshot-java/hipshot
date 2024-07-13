package dev.hipshot.web.server;

import lombok.Builder;
import sh.blake.niouring.IoUring;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Builder
public class WebServerRequest {
    private final String method;
    private final String path;
    private final String protocol;
    private final ByteBuffer body;
    private Consumer<ByteBuffer> chunkHandler;
    private IoUring ring;

    @Builder.Default private final Map<String, List<String>> headers = new HashMap<>();
    @Builder.Default private final Map<String, List<String>> queryParams = new HashMap<>();
    @Builder.Default private final Map<String, String> pathParams = new HashMap<>();

    public void ring(IoUring ring) {
        this.ring = ring;
    }

    public IoUring ring() {
        return ring;
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    public Map<String, List<String>> queryParams() {
        return queryParams;
    }

    public List<String> queryParam(String key) {
        return queryParams.get(key);
    }

    public String protocol() {
        return protocol;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public ByteBuffer body() {
        return body;
    }

    public Consumer<ByteBuffer> chunkHandler() {
        return chunkHandler;
    }

    public WebServerRequest chunkHandler(Consumer<ByteBuffer> chunkHandler) {
        this.chunkHandler = chunkHandler;
        return this;
    }

    public Map<String, String> pathParams() {
        return pathParams;
    }

    public String pathParam(String key) {
        return pathParams.get(key);
    }
}
