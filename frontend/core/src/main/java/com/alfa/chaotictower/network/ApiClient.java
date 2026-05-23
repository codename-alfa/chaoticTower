package com.alfa.chaotictower.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";

    public interface ApiCallback {
        void onSuccess(JsonValue result);
        void onFailure(Throwable t);
    }

    public static void login(String username, String password, ApiCallback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest request = requestBuilder.newRequest()
            .method(Net.HttpMethods.POST)
            .url(BASE_URL + "/players/login?username=" + username + "&password=" + password)
            .build();

        sendRequest(request, callback);
    }

    public static void register(String username, String password, ApiCallback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest request = requestBuilder.newRequest()
            .method(Net.HttpMethods.POST)
            .url(BASE_URL + "/players/register?username=" + username + "&password=" + password)
            .build();

        sendRequest(request, callback);
    }

    public static void submitScore(Long playerId, String gameMode, int score, double timeRecord, double maxHeight, ApiCallback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest request = requestBuilder.newRequest()
            .method(Net.HttpMethods.POST)
            .url(BASE_URL + "/leaderboard/submit?playerId=" + playerId + "&gameMode=" + gameMode + "&score=" + score + "&timeRecord=" + timeRecord + "&maxHeight=" + maxHeight)
            .build();

        sendRequest(request, callback);
    }

    public static void getTop10Scores(String gameMode, ApiCallback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest request = requestBuilder.newRequest()
            .method(Net.HttpMethods.GET)
            .url(BASE_URL + "/leaderboard/top10?gameMode=" + gameMode)
            .build();

        sendRequest(request, callback);
    }

    private static void sendRequest(Net.HttpRequest request, ApiCallback callback) {
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                try {
                    int statusCode = httpResponse.getStatus().getStatusCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        String resultAsString = httpResponse.getResultAsString();
                        JsonReader reader = new JsonReader();
                        JsonValue json = reader.parse(resultAsString);
                        Gdx.app.postRunnable(() -> callback.onSuccess(json));
                    } else {
                        String errorBody = httpResponse.getResultAsString();
                        Gdx.app.postRunnable(() -> callback.onFailure(new Exception(errorBody != null && !errorBody.trim().isEmpty() ? errorBody.trim() : "HTTP error " + statusCode)));
                    }
                } catch (Exception e) {
                    Gdx.app.postRunnable(() -> callback.onFailure(e));
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> callback.onFailure(t));
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(() -> callback.onFailure(new Exception("Request cancelled")));
            }
        });
    }
}
