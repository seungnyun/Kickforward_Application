package com.example.kick_login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.example.Service.MP3Service;

public class ValidationActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태
    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private BluetoothSocket bluetoothSocket = null; //블루투스 소켓
    private OutputStream outputStream = null; //블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; //블루투스에 데이터를 입력하기 위한 입력 스트림
    private Thread workerThread = null; //문자열 수신에 사용되는 쓰레드
    private byte[] readBuffer; //수신된 문자열 저장 버퍼
    private int readBufferPosition; //버퍼  내 문자 저장 위치

    private Button buttonSend; // 송신하기 위한 버튼

    String[] array = {"0"};


    int pairedDeviceCount; //페어링 된 기기의 크기를 저장할 변수


    private TextView textViewDeviceName;


    String markerName, usageRecordId;
    long rentalDateTime;

    MP3Service mp3Player = new MP3Service();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validation);


        mp3Player.playMP3WithName(getApplicationContext(), "start");




        // 대여 시작 직후의 데이터 받아옴
        markerName = getIntent().getStringExtra("markerName");
        rentalDateTime = getIntent().getLongExtra("timeStamp", 0);
        usageRecordId = getIntent().getStringExtra("usageRecordId");


        //위치권한 허용 코드
        String[] permission_list = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(this, permission_list, 1);


        textViewDeviceName = findViewById(R.id.textView_device_name);

        buttonSend = findViewById(R.id.button_send);


        //블루투스 활성화 코드
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //블루투스 어댑터를 디폴트 어댑터로 설정

        if (bluetoothAdapter == null) { //기기가 블루투스를 지원하지 않을때
            Toast.makeText(getApplicationContext(), "Bluetooth 미지원 기기입니다.", Toast.LENGTH_SHORT).show();
            //처리코드 작성
        } else { // 기기가 블루투스를 지원할 때
            if (bluetoothAdapter.isEnabled()) { // 기기의 블루투스 기능이 켜져있을 경우
                selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
            } else { // 기기의 블루투스 기능이 꺼져있을 경우
                // 블루투스를 활성화 하기 위한 대화상자 출력
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // 선택 값이 onActivityResult함수에서 콜백
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivityForResult(intent, REQUEST_ENABLE_BT);
                selectBluetoothDevice();
            }

        }

        // 검사시작 버튼 클릭시 아두이노로 A 전송
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDataToArduino("A");
            }
        });

    }
    private void showToastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void sendDataToArduino(String data) {
        if (outputStream != null) {
            try {
                outputStream.write(data.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void selectBluetoothDevice() {
        //이미 페어링 되어있는 블루투스 기기를 탐색
        devices = bluetoothAdapter.getBondedDevices();
        //페어링 된 디바이스 크기 저장
        pairedDeviceCount = devices.size();
        //페어링 된 장치가 없는 경우
        if (pairedDeviceCount == 0) {
            //페어링 하기 위한 함수 호출
            Toast.makeText(getApplicationContext(), "먼저 Bluetooth 설정에 들어가 페어링을 진행해 주세요.", Toast.LENGTH_SHORT).show();
        }
        //페어링 되어있는 장치가 있는 경우
        else {
//            //디바이스를 선택하기 위한 대화상자 생성
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("페어링 된 블루투스 디바이스 목록");
//            //페어링 된 각각의 디바이스의 이름과 주소를 저장
//            List<String> list = new ArrayList<>();
//            //모든 디바이스의 이름을 리스트에 추가
//            for (BluetoothDevice bluetoothDevice : devices) {
//                list.add(bluetoothDevice.getName());
//            }
//
//            //list를 Charsequence 배열로 변경
//            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
//            list.toArray(new CharSequence[list.size()]);
//
//            //해당 항목을 눌렀을 때 호출되는 이벤트 리스너
//            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    //해당 디바이스와 연결하는 함수 호출
//                    connectDevice(charSequences[which].toString());
//                }
//            });
//            //뒤로가기 버튼 누를때 창이 안닫히도록 설정
//            builder.setCancelable(false);
//            //다이얼로그 생성
//            AlertDialog alertDialog = builder.create();
//             alertDialog.show();
//        }
            devices = bluetoothAdapter.getBondedDevices();
            pairedDeviceCount = devices.size();

            if (pairedDeviceCount > 0) { // 페어링된 장치가 하나라도 있을 때
                for (BluetoothDevice device : devices) {
                    // 여기에서 연결하고자 하는 특정 기기의 이름을 확인하고 연결하도록 설정
                    if (device.getName().equals("HC-06")) {
                        connectDevice(device.getName());
                        return;
                    }
                }
            }
            Toast.makeText(getApplicationContext(), "원하는 디바이스를 찾을 수 없거나 페어링되지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        //비활성화
    }

    @SuppressLint("MissingPermission")
    public void connectDevice(String deviceName) {
        //페어링 된 디바이스 모두 탐색
        for (BluetoothDevice tempDevice : devices) {
            //사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if (deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }

        }
        Toast.makeText(getApplicationContext(), bluetoothDevice.getName() + " 연결 완료!", Toast.LENGTH_SHORT).show();
        //UUID생성
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        //Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성

        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();

            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            receiveData();
        } catch (IOException e) {
            e.printStackTrace();
        }
// 연결된 블루투스 기기명을 textViewDeviceName에 설정
        textViewDeviceName.setText(markerName);



    }


    public void receiveData() {
        final Handler handler = new Handler();
        //데이터 수신을 위한 버퍼 생성
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        //데이터 수신을 위한 쓰레드 생성
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        //데이터 수신 확인
                        int byteAvailable = inputStream.available();
                        //데이터 수신 된 경우
                        if (byteAvailable > 0) {
                            //입력 스트림에서 바이트 단위로 읽어옴
                            byte[] bytes = new byte[byteAvailable];
                            inputStream.read(bytes);
                            //입력 스트림 바이트를 한 바이트씩 읽어옴
                            for (int i = 0; i < byteAvailable; i++) {
                                byte tempByte = bytes[i];
                                //개행문자를 기준으로 받음 (한줄)
                                if (tempByte == '\n') {
                                    //readBuffer 배열을 encodeBytes로 복사
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    //인코딩 된 바이트 배열을 문자열로 변환
                                    final String text = new String(encodedBytes, "UTF-8");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // textView_alcohol에 텍스트 설정

                                            array = text.split(",", 3);



                                            //정수형으로 변환

                                            int alcohol = 0; // 기본값 설정
                                            try {
                                                alcohol = Integer.parseInt(array[0]);


                                            } catch (NumberFormatException e) {
                                                // text가 숫자로 변환할 수 없는 형식인 경우
                                                e.printStackTrace(); // 로그에 오류 메시지 기록

                                            }

                                            int humidity=0;
                                            try {
                                                humidity = Integer.parseInt(array[1]);


                                            } catch (NumberFormatException e) {
                                                // text가 숫자로 변환할 수 없는 형식인 경우
                                                e.printStackTrace(); // 로그에 오류 메시지 기록

                                            }
                                            // && humidity>=60
                                            if(alcohol>=300){
                                                Toast.makeText(getApplicationContext(), "수치가 높아 운전이 불가합니다", Toast.LENGTH_LONG).show();
                                                mp3Player.playMP3WithName(getApplicationContext(), "denied");
                                            }

                                            //humidity>=60
                                            if(alcohol<=300){
                                                Toast.makeText(getApplicationContext(), "정상수치입니다 운전이 가능합니다", Toast.LENGTH_LONG).show();
                                               mp3Player.playMP3WithName(getApplicationContext(), "valid");
                                               Intent intent = new Intent(getApplicationContext(), RentalActivity.class);
                                                intent.putExtra("markerName", markerName);
                                                intent.putExtra("timeStamp", rentalDateTime);
                                               intent.putExtra("usageRecordId",usageRecordId);
                                                startActivity(intent);//
                                                finish();
                                            }
                                            if(humidity<60){
                                                Toast.makeText(getApplicationContext(), "측정이 불가능 합니다 다시 시도하세요", Toast.LENGTH_LONG).show();
                                                mp3Player.playMP3WithName(getApplicationContext(), "retry");
                                            }




                                        }
                                    });
                                } // 개행문자가 아닐경우
                                else {
                                    readBuffer[readBufferPosition++] = tempByte;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }
                try {
                    //1초 마다 받아옴
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        workerThread.start();
    }



}
