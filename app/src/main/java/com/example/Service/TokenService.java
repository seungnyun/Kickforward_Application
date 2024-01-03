package com.example.Service;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connection.HttpConnection;

import org.json.JSONException;
import org.json.JSONObject;

public class TokenService extends AppCompatActivity {

    public void checkTokenExp(String token, final TokenCheckListener listener){
        JSONObject Token = new JSONObject();
        try {
            Token.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String TokenData = Token.toString();
        String urlStr = "http://192.168.0.24:8080" + "/checkTokenExp";

        HttpConnection httpConnection = new HttpConnection(new HttpConnection.HttpConnectionListener() {
            @Override
            public void onResult(String data) {
                boolean isTokenValid = data.equals("1");
                listener.onTokenCheckResult(isTokenValid);
            }
        });
        httpConnection.execute(urlStr, TokenData);
    }

    public interface TokenCheckListener {
        void onTokenCheckResult(boolean isTokenValid);
    }
}

