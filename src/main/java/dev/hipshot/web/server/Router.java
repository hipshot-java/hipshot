package dev.hipshot.web.server;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Router implements BiConsumer<WebServerRequest, WebServerResponse> {
    private final Route root = new Route("");

    @Override
    public void accept(WebServerRequest request, WebServerResponse response) {
        root.handle(request, response, request.path().split("/"), 0);
    }

    public Router post(String path, BiConsumer<WebServerRequest, WebServerResponse> handler) {
        return route("POST", path, handler);
    }

    public Router get(String path, BiConsumer<WebServerRequest, WebServerResponse> handler) {
        return route("GET", path, handler);
    }

    public Router put(String path, BiConsumer<WebServerRequest, WebServerResponse> handler) {
        return route("PUT", path, handler);
    }

    public Router delete(String path, BiConsumer<WebServerRequest, WebServerResponse> handler) {
        return route("DELETE", path, handler);
    }

    public Router patch(String path, BiConsumer<WebServerRequest, WebServerResponse> handler) {
        return route("PATCH", path, handler);
    }

    public Router head(String path, BiConsumer<WebServerRequest, WebServerResponse> handler) {
        return route("HEAD", path, handler);
    }

    public Router options(String path, BiConsumer<WebServerRequest, WebServerResponse> handler) {
        return route("OPTIONS", path, handler);
    }

    public Router trace(String path, BiConsumer<WebServerRequest, WebServerResponse> handler) {
        return route("TRACE", path, handler);
    }

    public Router route(String method, String path, BiConsumer<WebServerRequest, WebServerResponse> handler) {
        var tokens = path.split("/");
        var route = root;
        for (var token : tokens) {
            if (token.startsWith("{") && token.endsWith("}")) {
                route = route.routes.computeIfAbsent("{*}", (k) -> new Route(token));
            } else {
                route = route.routes.computeIfAbsent(token, (k) -> new Route(token));
            }
        }
        route = route.routes.computeIfAbsent(method, (k) -> new Route(method));
        route.handler = handler;
        return this;
    }

    @RequiredArgsConstructor
    public static class Route {
        private final String token;
        private BiConsumer<WebServerRequest, WebServerResponse> handler;
        private final Map<String, Route> routes = new HashMap<>();

        void handle(WebServerRequest request, WebServerResponse response, String[] pathTokens, int position) {
            if (position == pathTokens.length) {
                if (!routes.containsKey(request.method())) {
                    response.status(404).body("Not found").send();
                    return;
                }
                routes.get(request.method()).handler.accept(request, response);
                return;
            }

            // handle path parameters
            if (routes.containsKey("{*}")) {
                var route = routes.get("{*}");
                var key = route.token.substring(1, route.token.length() - 1);
                request.pathParams().put(key, pathTokens[position]);
                route.handle(request, response, pathTokens, position + 1);
                return;
            }

            // if wildcard is present, route to it
            if (routes.containsKey("*")) {
                routes.get("*").handle(request, response, pathTokens, position + 1);
            }

            // otherwise route to the exact path
            var token = pathTokens[position];
            if (!routes.containsKey(token)) {
                response.status(404).body("Not found").send();
                return;
            }

            routes.get(token).handle(request, response, pathTokens, position + 1);
        }
    }
}
