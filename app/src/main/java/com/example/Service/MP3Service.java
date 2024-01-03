package com.example.Service;

import android.content.Context;
import android.media.MediaPlayer;

public class MP3Service {
    private MediaPlayer mediaPlayer;

    public void playMP3WithName(Context context, String mp3FileName) {
        int resID = context.getResources().getIdentifier(mp3FileName, "raw", context.getPackageName());
        mediaPlayer = MediaPlayer.create(context, resID);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopMP3();
                // 다음 MP3 파일 이름을 전달하여 여기서 playMP3WithName() 함수를 호출할 수 있습니다.
            }
        });
    }

    public void stopMP3() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

