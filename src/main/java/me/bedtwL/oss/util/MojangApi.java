package me.bedtwL.oss.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Mojang API 工具類別 by deepseek v4 flash
 * 用法：
 *   MojangApi.getUuid("Notch")          -> Optional<UUID>
 *   MojangApi.getProfile(uuid)          -> Optional<JsonObject>
 *   MojangApi.getProfile("Notch")       -> Optional<JsonObject>（先查 UUID 再查 Profile）
 */
public final class MojangApi {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String UUID_API = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final String PROFILE_API = "https://sessionserver.mojang.com/session/minecraft/profile/%s";

    private MojangApi() {}

    /** 以用戶名取得 UUID（不帶連字號） */
    public static Optional<UUID> getUuid(String username) {
        return getJson(String.format(UUID_API, username))
                .map(json -> json.get("id").getAsString())
                .map(MojangApi::parseUuid);
    }

    /** 以 UUID 取得完整 profile（含 skin、cape 等 properties） */
    public static Optional<JsonObject> getProfile(UUID uuid) {
        return getProfile(uuid.toString().replace("-", ""));
    }

    /** 以無連字號 UUID 字串取得完整 profile */
    public static Optional<JsonObject> getProfile(String uuidNoDash) {
        return getJson(String.format(PROFILE_API, uuidNoDash));
    }

    /** 以用戶名直接取得完整 profile（內部先查 UUID） */
    public static Optional<JsonObject> getProfileByUsername(String username) {
        return getUuid(username).flatMap(MojangApi::getProfile);
    }

    /** 從 profile 中取出 skin 的 texture URL（若存在） */
    public static Optional<String> getSkinUrl(JsonObject profile) {
        if (!profile.has("properties")) return Optional.empty();
        for (com.google.gson.JsonElement prop : profile.getAsJsonArray("properties")) {
            JsonObject obj = prop.getAsJsonObject();
            if ("textures".equals(obj.get("name").getAsString())) {
                String decodedJson = new String(java.util.Base64.getDecoder().decode(obj.get("value").getAsString()));
                JsonObject decoded = JsonParser.parseString(decodedJson).getAsJsonObject();
                JsonObject textures = decoded.getAsJsonObject("textures");
                if (textures != null && textures.has("SKIN")) {
                    return Optional.of(textures.getAsJsonObject("SKIN").get("url").getAsString());
                }
            }
        }
        return Optional.empty();
    }

    // ---- 內部工具 ----

    private static Optional<JsonObject> getJson(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Optional.empty(); // 404 = 用戶不存在，429 = 被限流
            }
            return Optional.of(JsonParser.parseString(response.body()).getAsJsonObject());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static UUID parseUuid(String uuidNoDash) {
        return UUID.fromString(uuidNoDash.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }
}