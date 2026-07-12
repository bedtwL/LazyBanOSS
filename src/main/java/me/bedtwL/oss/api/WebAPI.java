package me.bedtwL.oss.api;

import com.sun.net.httpserver.HttpServer;
import lombok.Getter;
import me.bedtwL.oss.LazyBanOSS;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class WebAPI {
    @Getter
    private static Logger logger=null;
    public static void setLogger(Logger iLogger)
    {
        if (logger==null)
            logger=iLogger;
    }
    public void startServer()
    {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(LazyBanOSS.getConfig().getInt("web-api.port")), 0);
            server.createContext("/api/v1", new Handler());
            server.setExecutor(null);
            server.start();
        } catch (Exception e) {
            getLogger().severe("Failed to start HTTP server: " + e.getMessage());
        }
    }
}
