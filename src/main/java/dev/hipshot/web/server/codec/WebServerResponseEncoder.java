package dev.hipshot.web.server.codec;

import dev.hipshot.web.server.WebServerResponse;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebServerResponseEncoder {
    private static final Map<Integer, byte[]> ENCODED_STATUSES = new ConcurrentHashMap<>();
    private static final Map<String, byte[]> ENCODED_HEADER_KEYS = new ConcurrentHashMap<>();
    private static final byte[] HTTP_VERSION = "HTTP/1.1 ".getBytes(StandardCharsets.UTF_8);
    private static final byte[] CARRIAGE_RETURN = "\r\n".getBytes(StandardCharsets.UTF_8);
    private static final byte[] SERVER_HEADER = "Server: Hipshot\r\n".getBytes(StandardCharsets.UTF_8);

    public static void encode(WebServerResponse response, ByteBuffer buffer) {
        if (!response.headers().containsKey("Content-Length")) {
            var contentLength = response.body() != null ? response.body().remaining() : 0;
            response.header("Content-Length", contentLength + "");
        }

        if (!ENCODED_STATUSES.containsKey(response.status())) {
            byte[] statusBytes = (response.status() + "\r\n").getBytes(StandardCharsets.UTF_8);
            ENCODED_STATUSES.put(response.status(), statusBytes);
        }

        // Encode the response status line
        buffer
            .put(HTTP_VERSION)
            .put(ENCODED_STATUSES.get(response.status()));

        // Encode any response headers
        buffer.put(SERVER_HEADER);
        if (!response.headers().isEmpty()) {
            for (Map.Entry<String, List<String>> entry : response.headers().entrySet()) {
                for (String headerValue : entry.getValue()) {
                    if (!ENCODED_HEADER_KEYS.containsKey(entry.getKey())) {
                        byte[] encodedKey = (entry.getKey() + ": ").getBytes(StandardCharsets.UTF_8);
                        ENCODED_HEADER_KEYS.put(entry.getKey(), encodedKey);
                    }
                    buffer.put(ENCODED_HEADER_KEYS.get(entry.getKey()))
                            .put(headerValue.getBytes(StandardCharsets.UTF_8))
                            .put(CARRIAGE_RETURN);
                }
            }
        }
        buffer.put(CARRIAGE_RETURN);

        // And finally encode the body
        if (response.body() != null) {
            buffer.put(response.body());
        }

        buffer.flip();
    }
}
