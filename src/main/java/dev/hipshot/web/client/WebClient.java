package dev.hipshot.web.client;

import lombok.RequiredArgsConstructor;
import sh.blake.niouring.IoUring;
import sh.blake.niouring.IoUringSocket;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class WebClient {
    private final IoUring ring;

    public static void main(String[] args) {
        var request = WebClientRequest.builder()
                .url("http://hello:test23/world")
                .build();
        new WebClient(null).send(request, (response) -> {

        });
    }

    public void send(WebClientRequest request, Consumer<WebClientResponse> consumer) {
        var url = request.url();
        validateUrl(url);

        var withoutProtocol = url.substring(7);
        var firstSegment = withoutProtocol.substring(0, withoutProtocol.indexOf("/"));
        System.out.println(firstSegment);
    }

    private void validateUrl(String url) {
        if (url.startsWith("https://")) {
            throw new UnsupportedOperationException("HTTPS not supported yet");
        }
        if (!url.startsWith("http://")) {
            throw new IllegalArgumentException("URL must start with http://");
        }
    }
}
