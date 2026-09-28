package co.edu.escuelaing.concurrentserver;

import co.edu.escuelaing.framework.MiniHttpServer;
import java.io.IOException;

/** Example application built on the course-framework extension. */
public final class Application {
    private static final int DEFAULT_PORT = 6000;

    private Application() {
    }

    public static void main(String[] args) throws IOException {
        int port = portFromEnvironment();
        MiniHttpServer server = MiniHttpServer.create(port)
                .get("/greeting", request -> "Hello, " + request.query("name", "World") + "!")
                .get("/health", request -> "UP");
        server.start();
        System.out.printf("Concurrent server listening on http://localhost:%d%n", port);
    }

    private static int portFromEnvironment() {
        String rawPort = System.getenv().getOrDefault("PORT", String.valueOf(DEFAULT_PORT));
        try {
            int port = Integer.parseInt(rawPort);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException("port outside range");
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("PORT must be an integer between 1 and 65535", exception);
        }
    }

}
