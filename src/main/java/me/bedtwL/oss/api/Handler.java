package me.bedtwL.oss.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import me.bedtwL.oss.BanCommand;
import me.bedtwL.oss.LazyBanOSS;
import me.bedtwL.oss.utils.BanEntry;
import me.bedtwL.oss.utils.DataUtils;
import net.md_5.bungee.api.ProxyServer;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Handler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryParams(query);
            String key=params.get("keys");
            if (!Objects.equals(key, LazyBanOSS.getConfig().getString("web-api.auth"))) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }
            String playerUUID = params.get("player");
            String action = params.get("action");
            Map<String, Object> response = new HashMap<>();
            if (playerUUID == null || action==null) {
                response.put("error","Query args missing!");
            }
            switch (action.toString()) {
                case "unban":
                    BanEntry e= DataUtils.getBan(playerUUID);
                    e.setBanEnd(0);
                    DataUtils.saveBan(e);
                    break;
                case "ban":
                    BanCommand.banCmd(LazyBanOSS.getInstance().getProxy().getConsole(),new String[] {playerUUID,params.get("time"),params.get("reason")});
            }
            String jsonResponse = toJson(response);
            ProxyServer.getInstance().getLogger().info(jsonResponse);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        return params;
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        map.forEach((key, value) -> json.append("\"").append(key).append("\":")
                .append(value instanceof String ? "\"" + value + "\"" : value).append(","));
        if (json.charAt(json.length() - 1) == ',') {
            json.setLength(json.length() - 1);
        }
        json.append("}");
        return json.toString();
    }
}
