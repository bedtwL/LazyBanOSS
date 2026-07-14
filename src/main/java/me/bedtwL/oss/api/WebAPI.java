package me.bedtwL.oss.api;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class WebAPI {
    public void startServer(int port)
    {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/api/v1", new Handler());
            server.setExecutor(null);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
