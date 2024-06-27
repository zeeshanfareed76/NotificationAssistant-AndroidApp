package com.zfstudio.notificationassistant;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class Notificationspeaker {
    TextToSpeech tts;
    private static Notificationspeaker instance;

    public void speaker(String title, String text, Context context){
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Log.e("Speaker","Working");
                tts.setPitch(1.0f); // Set pitch (optional)
                tts.setSpeechRate(1.0f); // Set speech rate (optional)
                tts.speak(title+"says : "+text, TextToSpeech.QUEUE_FLUSH, null);
            } else {

                Log.e("Speaker","Cannot initialize the speaker.");
            }
        });
    }
    public static Notificationspeaker getInstance() {
        if (instance == null) {
            instance = new Notificationspeaker();
        }
        return instance;
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop(); // Stop the TextToSpeech engine
            tts.shutdown(); // Shutdown the TextToSpeech engine to release resources
        }
    }
}
