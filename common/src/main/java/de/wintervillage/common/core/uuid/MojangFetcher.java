package de.wintervillage.common.core.uuid;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MojangFetcher {

    private final HttpClient httpClient;

    public MojangFetcher(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public MojangFetcher() {
        this(HttpClient.newHttpClient());
    }

    public CompletableFuture<Optional<UUID>> lookupUniqueId(String username) {
        CompletableFuture<HttpResponse<InputStream>> future = this.httpClient.sendAsync(HttpRequest.newBuilder()
                .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + username))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(5))
                .build(), HttpResponse.BodyHandlers.ofInputStream());
        return future.thenApply((HttpResponse<InputStream> response) -> {
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                JsonObject jsonObject = this.readJson(response);

                String id = jsonObject.get("id").getAsString();
                return Optional.of(UUID.fromString(this.dashed(id)));
            } else return Optional.empty();
        });
    }

    private String dashed(String undashed) {
        if (undashed == null || undashed.length() != 32) throw new IllegalArgumentException("Invalid UUID format. UUID must be 32 characters long.");

        Pattern pattern = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
        Matcher matcher = pattern.matcher(undashed);

        if (matcher.matches()) {
            return matcher.replaceAll("$1-$2-$3-$4-$5");
        } else {
            throw new IllegalArgumentException("Invalid UUID format. UUID must consist of hexadecimal characters.");
        }
    }

    private JsonObject readJson(HttpResponse<InputStream> response) {
        Charset charset = this.charsetFromHeaders(response.headers());

        try (InputStream inputStream = response.body(); InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
            Gson GSON = new Gson();
            JsonObject jsonObject = GSON.fromJson(reader, JsonObject.class);

            if (jsonObject != null) return jsonObject;
            else throw new IllegalStateException("Failed to parse JSON response");
        } catch (IOException exception) {
            throw new RuntimeException("Could not read http response body", exception);
        } catch (JsonParseException exception) {
            throw new RuntimeException("Failed to parse JSON response", exception);
        }
    }

    private Charset charsetFromHeaders(HttpHeaders headers) {
        Optional<String> optionalContentType = headers.firstValue("Content-Type");
        if (optionalContentType.isEmpty()) return StandardCharsets.UTF_8;

        String contentType = optionalContentType.get();
        int indexOfSemicolon = contentType.indexOf(';');
        if (indexOfSemicolon == -1) return StandardCharsets.UTF_8;

        String charsetPart = contentType.substring(indexOfSemicolon + 1).trim();
        String[] charSetKeyAndValue = charsetPart.split("=", 2);

        if (charSetKeyAndValue.length == 2 && "charset".equalsIgnoreCase(charSetKeyAndValue[0])) {
            return Charset.forName(charSetKeyAndValue[1]);
        } else {
            return StandardCharsets.UTF_8;
        }
    }
}
