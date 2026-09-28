package co.edu.escuelaing.framework;

import java.net.URLDecoder;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/** Immutable request data exposed to application route handlers. */
public record Request(URI uri) {
    public String query(String name, String defaultValue) {
        String rawQuery = uri.getRawQuery();
        if (rawQuery == null || rawQuery.isBlank()) {
            return defaultValue;
        }
        for (String entry : rawQuery.split("&")) {
            String[] pair = entry.split("=", 2);
            if (pair.length == 2 && pair[0].equals(name)) {
                return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            }
        }
        return defaultValue;
    }
}
