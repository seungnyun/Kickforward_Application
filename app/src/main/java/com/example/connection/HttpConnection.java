package com.example.connection;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.net.URL;

public class HttpConnection extends AsyncTask<String, Integer, String> {

    private HttpConnectionListener listener;

    public HttpConnection(HttpConnectionListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... strings) {
        String data = "";
        if (strings.length == 0) {
            return "URL is empty.";
        }
        try {
            URL url = new URL(strings[0]);
            String postData = strings[1]; // JSON 데이터를 받아옴

            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            data = connection.request(url, postData);

        } catch (Exception e) {
            data = e.getMessage();
        }
        return data;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String data) {
        super.onPostExecute(data);
        Log.d("HttpConnection", "Response data: " + data);
        try {
            listener.onResult(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface HttpConnectionListener {
        void onResult(String data) throws JSONException;
    }
}
