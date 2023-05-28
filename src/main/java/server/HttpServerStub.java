package server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HttpServerStub {
    private static HttpServer httpServer;
    private final int millisLag;
    private static final int RESULT_SIZE = 10;

    public HttpServerStub(int millisLag) {
        this.millisLag = millisLag;
        try {
            httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
            httpServer.createContext("/google/search", getHandler("google"));
            httpServer.createContext("/yandex/search", getHandler("yandex"));
            httpServer.start();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create http stub server:" + e.getMessage());
        }
    }

    private HttpHandler getHandler(String engineName) {
        return exchange -> {
            try {
                Thread.sleep(millisLag);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Unable to sleep selected time: " + millisLag);
            }
            String query = exchange.getRequestHeaders().get("query").get(0);
            JSONObject jsonResult = constructJson(engineName, query);
            byte[] response = jsonResult.toString().getBytes();
            exchange.getResponseHeaders().put("Content-Type", Collections.singletonList("text/json"));
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);

            exchange.getResponseBody().write(response);
            exchange.close();
        };
    }

    private JSONObject constructJson(String engineName, String query) {
        JSONObject jsonResult = new JSONObject();
        JSONArray queryResults = new JSONArray();
        List<String> resultList = new ArrayList<>();
        for (int i = 0; i < RESULT_SIZE; i++) {
            resultList.add(query + "_" + engineName + "_stub_result_" + i);
        }
        queryResults.putAll(resultList);
        jsonResult.put(query, queryResults);
        return jsonResult;
    }

    public void stop() {
        httpServer.stop(0);
    }
}
