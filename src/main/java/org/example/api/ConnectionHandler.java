package org.example.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ConnectionHandler {

    public static String fetchData(String endpoint) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        try {
            int status = connection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK) {
                throw new HttpStatusException(status);
            }

            try (var input = connection.getInputStream()) {
                return new String(
                        input.readAllBytes(),
                        StandardCharsets.UTF_8
                );
            }
        } finally {
            connection.disconnect();
        }
    }

    public static class HttpStatusException extends IOException {

        private final int statusCode;

        public HttpStatusException(int statusCode) {
            super("Serwer zwrócił HTTP " + statusCode);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}