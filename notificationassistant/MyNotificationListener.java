package com.zfstudio.notificationassistant;


import android.app.Notification;
import android.media.MediaPlayer;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.IOException;


public class MyNotificationListener extends NotificationListenerService {


    public static String audio;
    private StatusBarNotification latestNotification;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // Implement what you want here

        if(MainActivity.voiceenable==1 || MainActivity.csound==1){
            String packageName = sbn.getPackageName();
            String title = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE);
            String text = sbn.getNotification().extras.getString(Notification.EXTRA_TEXT);
            if (!packageName.equals("com.android.systemui") && isMessageNotification(text)) {
                latestNotification = sbn;
                if(MainActivity.csound==1){
                    DbHelper myDB=new DbHelper(this);
                    myDB.getUriFromPackageName(packageName, uri -> {
                        if (uri != null) {
                            MediaPlayer mediaPlayer = new MediaPlayer();
                            try {
                                mediaPlayer.setDataSource(uri);
                                mediaPlayer.prepare();
                                mediaPlayer.start();

                                mediaPlayer.setOnCompletionListener(mp -> {
                                    stopSelf(); // Stop the service when playback completes
                                });
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            // Handle case where URI is not found
                            Log.e("URI", "URI not found");
                        }
                    });
                    myDB.close();

                }
                if (MainActivity.voiceenable == 1) {
                    processLatestNotification(packageName);

                }
            }

        }


    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Implement what you want here
    }

    private void processLatestNotification(String pckgName) {
        if (latestNotification != null) {
            String title = latestNotification.getNotification().extras.getString(Notification.EXTRA_TITLE);
            String text = latestNotification.getNotification().extras.getString(Notification.EXTRA_TEXT);
            DbHelper myDB=new DbHelper(this);
            myDB.isAppPresent(pckgName, 1, isPresent -> {
                // Handle the result here
                if (isPresent) {
                    Notificationspeaker.getInstance().speaker(title, text, this);
                }
                myDB.close();

            });

        }
    }

    private boolean isMessageNotification(String text) {
        // Check if the notification text contains message-related keywords
        return text != null && !(text.toLowerCase().contains("new message"));
    }

}