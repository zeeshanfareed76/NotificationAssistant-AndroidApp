
package com.zfstudio.notificationassistant;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class CustomNotification extends Service {

    private MediaPlayer mediaPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals("ACTION_PLAY_NOTIFICATION_SOUND")) {
                // Extract the sound URI from the intent extras
                Uri soundUri = intent.getParcelableExtra("soundUri");
                Log.e("Custom","mediaPlayer Log");
                if (soundUri != null) {
                    playNotificationSound(soundUri);

                }
            }
        }
        return START_NOT_STICKY;
    }

    private void playNotificationSound(Uri soundUri) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, soundUri);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopSelf(); // Stop the service when playback completes
                }
            });

            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf(); // Stop the service in case of an error
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // No binding needed
    }
}
