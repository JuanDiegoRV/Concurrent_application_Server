package co.edu.escuelaing.framework;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Small framework server with a route registry, virtual-thread concurrency and
 * graceful shutdown. It deliberately has no Spring dependency.
 */
public final class MiniHttpServer {
    private static final Duration SHUTDOWN_GRACE_PERIOD = Duration.ofSeconds(10);

    private final HttpServer httpServer;
    private final ExecutorService executor;
    private final Map<String, RouteHandler> getRoutes = new ConcurrentHashMap<>();

    private MiniHttpServer(int port) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        executor = Executors.newVirtualThreadPerTaskExecutor();
        httpServer.setExecutor(executor);
        httpServer.createContext("/", this::dispatch);
    }

    public static MiniHttpServer create(int port) throws IOException {
        return new MiniHttpServer(port);
    }

    public MiniHttpServer get(String path, RouteHandler handler) {
        getRoutes.put(path, handler);
        return this;
    }

    public void start() {
        httpServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopGracefully, "graceful-shutdown"));
    }

    private void dispatch(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "Method Not Allowed");
            return;
        }
        RouteHandler handler = getRoutes.get(exchange.getRequestURI().getPath());
        if (handler == null) {
            send(exchange, 404, "Not Found");
            return;
        }
        try {
            send(exchange, 200, handler.handle(new Request(exchange.getRequestURI())));
        } catch (Exception exception) {
            send(exchange, 500, "Internal Server Error");
        }
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(status, response.length);
        try (var output = exchange.getResponseBody()) {
            output.write(response);
        }
    }

    private void stopGracefully() {
        System.out.println("Shutdown requested: allowing active requests to finish.");
        httpServer.stop((int) SHUTDOWN_GRACE_PERIOD.toSeconds());
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_GRACE_PERIOD.toSeconds(), TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Server stopped cleanly.");
    }
}
