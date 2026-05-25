package com.di.architecture;

import com.di.annotations.Configuration;
import com.di.annotations.http.GET;
import com.di.annotations.http.POST;
import com.di.annotations.http.PathVariable;
import com.di.annotations.http.RequestParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jspecify.annotations.Nullable;

@Configuration
public class Server {
    private static final int PORT_NUMBER = 8080;
    private final List<Route> routes = new ArrayList<>();

    public void registerRoute(final Object bean, final Method... methods) {
        Arrays.stream(methods).forEach(method -> {
            if (method.isAnnotationPresent(GET.class)) {
                final var path = method.getAnnotation(GET.class).value();
                routes.add(new Route("GET", path, bean, method));
                System.out.println("Registered route: GET " + path);
            } else if (method.isAnnotationPresent(POST.class)) {
                final var path = method.getAnnotation(POST.class).value();
                routes.add(new Route("POST", path, bean, method));
                System.out.println("Registered route: POST " + path);
            }
        });
    }

    public void run() {
        try (ServerSocket server = new ServerSocket(PORT_NUMBER)) {
            System.out.println("Listening on http://localhost:8080");

            while (true) {
                try (Socket socket = server.accept()) {
                    final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final var out = socket.getOutputStream();

                    var line = in.readLine();
                    if (line == null || line.isEmpty()) continue;

                    System.out.println("Request: " + line);
                    final var requestParts = line.split(" ");
                    final var httpMethod = requestParts[0];
                    final var rawPath = requestParts[1];

                    while (!(line = in.readLine()).isEmpty()) { }

                    var statusCode = 200;
                    var statusText = "OK";
                    String body;

                    final var matched = findRoute(httpMethod, rawPath);

                    if (matched != null) {
                        try {
                            final var args = convertArgs(matched.route().method(), matched.pathParams(), matched.queryParams());
                            final var result = matched.route().method().invoke(matched.route().bean(), args);
                            body = result != null ? result.toString() : "";
                        } catch (Exception e) {
                            e.printStackTrace();
                            statusCode = 500;
                            statusText = "Internal Server Error";
                            body = "Error: " + e.getMessage();
                        }
                    } else {
                        statusCode = 404;
                        statusText = "Not Found";
                        body = "404 Not Found";
                    }

                    final var response =
                            "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "Content-Length: " + body.length() + "\r\n" +
                            "\r\n" +
                            body;

                    out.write(response.getBytes());
                } catch (Exception e) {
                    System.err.println("Error handling request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + PORT_NUMBER);
            System.exit(-1);
        }
    }

    private @Nullable MatchedRoute findRoute(final String httpMethod, final String rawPath) {
        final var pathParts = rawPath.split("\\?", 2);
        final var path = pathParts[0];
        final var queryParams = pathParts.length < 2 ? Map.<String, String>of() :
            Arrays.stream(pathParts[1].split("&"))
                .map(pair -> pair.split("=", 2))
                .collect(Collectors.toMap(
                    parts -> parts[0],
                    parts -> parts.length > 1 ? parts[1] : "",
                    (v1, _) -> v1
                ));

        final var pathSegments = path.split("/");
        return routes.stream()
                .filter(route -> route.httpMethod().equals(httpMethod))
                .map(route -> {
                    final var routeSegments = route.path().split("/");
                    if (pathSegments.length != routeSegments.length) {
                        return null;
                    }
                    final var pathParams = new HashMap<String, String>();
                    final var matches = IntStream.range(0, routeSegments.length)
                            .allMatch(i -> {
                                if (routeSegments[i].startsWith("{") && routeSegments[i].endsWith("}")) {
                                    final var key = routeSegments[i].substring(1, routeSegments[i].length() - 1);
                                    pathParams.put(key, pathSegments[i]);
                                    return true;
                                }
                                return routeSegments[i].equals(pathSegments[i]);
                            });
                    return matches ? new MatchedRoute(route, pathParams, queryParams) : null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private Object[] convertArgs(final Method method, final Map<String, String> pathParams, final Map<String, String> queryParams) {
        return Arrays.stream(method.getParameters())
                .map(parameter -> {
                    final var val = parameter.isAnnotationPresent(PathVariable.class)
                            ? pathParams.get(parameter.getAnnotation(PathVariable.class).value())
                            : parameter.isAnnotationPresent(RequestParam.class)
                            ? queryParams.get(parameter.getAnnotation(RequestParam.class).value())
                            : null;
                    if (parameter.getType() == int.class || parameter.getType() == Integer.class) {
                        return val != null ? Integer.parseInt(val) : 0;
                    }
                    return val;
                })
                .toArray();
    }

    private record Route(String httpMethod, String path, Object bean, Method method) {}
    private record MatchedRoute(Route route, Map<String, String> pathParams, Map<String, String> queryParams) {}
}
