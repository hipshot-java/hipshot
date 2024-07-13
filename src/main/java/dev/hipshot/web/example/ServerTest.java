package dev.hipshot.web.example;

import dev.hipshot.web.server.WebServer;

public class ServerTest {
    public static void main(String[] args) {
        WebServer.create((request, response) -> {
            response.header("Content-Type", "text/plain")
                    .body("Hello from Hipshot!")
                    .send();
        }).start();
    }
}
