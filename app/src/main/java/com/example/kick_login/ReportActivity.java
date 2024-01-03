package com.example.kick_login;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.connection.HttpConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private Button btn_rentLog, btn_report;

    private EditText reportTitle,reportContent;
    private String userNo;

    private TextView selectedLog;

    private ImageView imageView;
    private Bitmap capturedImage;

    byte[] imageData;

    private String location;
    private String kbnumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);


        selectedLog = findViewById(R.id.selectedLog);
        imageView = findViewById(R.id.imageView);

        reportTitle = findViewById(R.id.reportTitle);
        reportContent = findViewById(R.id.reportContent);

//        Button captureButton = findViewById(R.id.captureButton);
//        captureButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dispatchTakePictureIntent();
//            }
//        });


        // 빌링키 및 아이디 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("KickForward", Context.MODE_PRIVATE);
        String userData = sharedPreferences.getString("data","");
        try {
            // userData 문자열을 JSON 객체로 파싱
            JSONObject jsonUserData = new JSONObject(userData);
            userNo = jsonUserData.optString("no", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btn_rentLog = findViewById(R.id.btn_rentLog);
        btn_rentLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("writerId", userNo);

                    String jsonData = jsonObject.toString();
                    String path = getResources().getString(R.string.server_url) + "/rentLogList";
                    HttpConnection httpConnection = new HttpConnection(new HttpConnection.HttpConnectionListener() {
                        @Override
                        public void onResult(String data) {
                            try {
                                // 넘어온 데이터를 JSON 배열로 파싱
                                JSONArray jsonArray = new JSONArray(data);

                                // 항목을 저장할 배열을 생성
                                CharSequence[] items = new CharSequence[jsonArray.length()];

                                CharSequence[] kbnumberList = new CharSequence[jsonArray.length()];

                                // JSON 배열에서 항목을 추출하여 배열에 저장
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject item = jsonArray.getJSONObject(i);
                                    String rentalDatetime = item.optString("rentalDatetime");
                                    double rentalLatitude = item.optDouble("rentalLatitude");
                                    double rentalLongitude = item.optDouble("rentalLongitude");
                                    String rentalKbnumber = item.optString("kickboardId");

                                    // 반납 위치를 주소로 변환
                                    String returnAddress = getLocationAddress(rentalLatitude, rentalLongitude);

                                    // items 배열에 추가
                                    if (returnAddress != null) {
                                        items[i] = "대여 일시: " + rentalDatetime + "\n반납 주소: " + returnAddress;
                                        kbnumberList[i] = rentalKbnumber;
                                    } else {
                                        items[i] = "대여 일시: " + rentalDatetime + "\n반납 위치 주소를 찾을 수 없습니다.";
                                    }
                                }

                                // AlertDialog 표시
                                AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this, R.style.AppAlertDialogTheme);
                                builder.setTitle("대여/반납 정보 목록");
                                builder.setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                       selectedLog.setText(kbnumberList[i].toString());
                                       kbnumber = kbnumberList[i].toString();
                                       location = items[i].toString();
                                    }
                                });
                                builder.show();

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                    httpConnection.execute(path, jsonData);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });


        btn_report = findViewById(R.id.btn_report);
        btn_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("writerId", userNo);
                    jsonObject.put("title", reportTitle.getText().toString());
                    jsonObject.put("content", reportContent.getText().toString());
                    jsonObject.put("location", location);
                    jsonObject.put("kbnumber",kbnumber);
                    jsonObject.put("imageData", Base64.encodeToString(imageData, Base64.DEFAULT));
                    String jsonData = jsonObject.toString();

                    String path = getResources().getString(R.string.server_url) + "/insertBoard";
                    HttpConnection httpConnection = new HttpConnection(new HttpConnection.HttpConnectionListener() {
                        @Override
                        public void onResult(String data) {
                            /* loadingBar.setVisibility(View.INVISIBLE); */
                            if (data.trim().equals("1")) {
                                Toast.makeText(ReportActivity.this, "신고접수 완료!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ReportActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }
                    });

                    httpConnection.execute(path, jsonData);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });



    }


    //위도 경도를 일반 주소로 변경
    private String getLocationAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                return address.getAddressLine(0); // 첫 번째 주소 라인을 반환
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    // 촬영 후 시작되는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            setCapturedImage(imageBitmap);
            imageData = convertBitmapToByteArray(imageBitmap);
        }
    }


    //이미지뷰에 촬영한 이미지 보이기
    private void setCapturedImage(Bitmap imageBitmap) {
        if (imageBitmap != null) {
            capturedImage = imageBitmap;
            imageView.setImageBitmap(imageBitmap);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    //json으로 이미지 전송을 위한 바이트 전환
    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }


    public void captureButton(View view){
        dispatchTakePictureIntent();
    }


}
