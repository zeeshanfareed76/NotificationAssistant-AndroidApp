package com.zfstudio.notificationassistant;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    Button enbtn, sndBtn, select1, select2;
    private static final int PICK_AUDIO_REQUEST_CODE = 123;

    public static int voiceenable,csound=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (!isNotificationAccessGranted()) {
            requestNotificationAccessPermission();
        }
        enbtn=findViewById(R.id.enablebutton);
        enbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (voiceenable==0){
                    voiceenable=1;
                    enbtn.setText("Turn Off");
                }else{
                    voiceenable=0;
                    Notificationspeaker.getInstance().shutdown();
                    enbtn.setText("NS Turn On");
                }
            }
        });

        sndBtn=findViewById(R.id.soundbutton);
        sndBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (csound==0){
                    csound=1;
                    sndBtn.setText("Turn Off");
                }else{
                    csound=0;
                    Notificationspeaker.getInstance().shutdown();
                    sndBtn.setText("CS Turn On");
                }
            }
        });

        select1=findViewById(R.id.selection1);
        select1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to VaultActivity
                Intent intent1 = new Intent(MainActivity.this, AllAps.class);
                intent1.putExtra("table",1);
                startActivity(intent1);
            }
        });

        select2=findViewById(R.id.selection2);
        select2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to VaultActivity
                Intent intent2 = new Intent(MainActivity.this, AllAps.class);
                intent2.putExtra("table",2);
                startActivity(intent2);
            }
        });

    }

    private boolean isNotificationAccessGranted() {
        ContentResolver contentResolver = getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getPackageName();

        return enabledNotificationListeners != null && enabledNotificationListeners.contains(packageName);
    }
    private void requestNotificationAccessPermission() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(intent);
        intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    public void selectAudioFile() {
        Intent i= new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i,PICK_AUDIO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            Uri selectedAudio = data.getData();
            String[] filepath = {MediaStore.Audio.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedAudio, filepath, null, null, null);
            if(cursor!=null){
                cursor.moveToFirst();
                int columIndex = cursor.getColumnIndex(filepath[0]);
                String audiopath = cursor.getString(columIndex);
                //MyNotificationListener.uri=audiopath;
                Log.e("AudioPath:",audiopath);
                MyNotificationListener.audio=audiopath;


            }

        }
    }

    }