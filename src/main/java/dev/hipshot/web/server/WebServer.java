package dev.hipshot.web.server;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import sh.blake.niouring.util.OsVersionCheck;

import java.util.function.BiConsumer;

@Slf4j
public abstract class WebServer {
    public abstract WebServer start();
    public abstract WebServer stop();

    public static WebServer create(Options options, BiConsumer<WebServerRequest, WebServerResponse> handler) {
        OsVersionCheck.verifySystemRequirements();
        return new NioUringWebServer(options, handler);
    }

    public static WebServer create(BiConsumer<WebServerRequest, WebServerResponse> handler) {
        return create(Options.builder().build(), handler);
    }

    @Data
    @Builder
    @Accessors(fluent = true, chain = true)
    public static class Options {
        @Builder.Default private final String host = "0.0.0.0";
        @Builder.Default private final int port = 8080;
        @Builder.Default private final int requestBufferSize = 8 * 1024;
        @Builder.Default private final int responseBufferSize = 8 * 1024;
        @Builder.Default private final int threads = Runtime.getRuntime().availableProcessors();
        @Builder.Default private final int ttl = 60000;
    }
}
