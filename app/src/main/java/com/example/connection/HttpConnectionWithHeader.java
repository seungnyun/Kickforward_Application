package com.example.connection;

import android.os.AsyncTask;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class HttpConnectionWithHeader extends AsyncTask<String, Integer, HttpConnectionWithHeader.Result> {

    private HttpConnectionListener listener;

    public HttpConnectionWithHeader(HttpConnectionListener listener) {
        this.listener = listener;
    }

    @Override
    protected Result doInBackground(String... strings) {
        Result result = new Result();
        if (strings.length == 0) {
            result.data = "URL is empty.";
            return result;
        }
        try {
            URL url = new URL(strings[0]);
            String postData = strings[1]; // JSON 데이터를 받아옴

            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            result.data = connection.request(url, postData);
            result.jwtToken = connection.getJwtToken(); // 헤더에서 JWT 토큰을 받아옴

        } catch (Exception e) {
            result.data = e.getMessage();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        Log.d("HttpConnection", "Response data: " + result.data);
        listener.onResult(result);
    }

    public interface HttpConnectionListener {
        void onResult(Result result);
    }

    public class Result {
        public String data;
        public String jwtToken;
    }
}
