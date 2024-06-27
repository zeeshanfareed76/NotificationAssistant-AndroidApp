package com.zfstudio.notificationassistant;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AllAps extends AppCompatActivity {
    public static String appClicked;
    private RecyclerView recyclerView;
    private static final int PICK_AUDIO_REQUEST_CODE = 123;
    RecyclerView.LayoutManager layoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    DbHelper myDB;
    ArrayList<String> app_name = new ArrayList<>();
    ArrayList<Drawable> app_icon = new ArrayList<>();
    Intent intent;
    int table;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_aps);
        intent=getIntent();
        table=intent.getIntExtra("table",1);
        Log.e("Table", String.valueOf(table));
        SetRecyclerView();
        //storeDataInArray();

    }

    void storeDataInArray(){
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        // Iterate through the installed applications
        for (ApplicationInfo appInfo : installedApplications) {
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {

                // Get the application name
                String appName = appInfo.packageName;
                //String appName = (String) packageManager.getApplicationLabel(appInfo);
                app_name.add(appName);


                // Get the application icon
                Drawable appIcon = packageManager.getApplicationIcon(appInfo);
                app_icon.add(appIcon);
            }


        }
    }

    void SetRecyclerView(){
        myDB=new DbHelper(AllAps.this);
        app_name=new ArrayList<String>();
        app_icon=new ArrayList<Drawable>();
        storeDataInArray();

        recyclerView=findViewById(R.id.recycleView);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerViewAdapter=new RecyclerViewAdapter(this,app_name,app_icon,table);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setHasFixedSize(true);

        myDB.insertAppWithUri("a","b");
        myDB.getUriFromPackageName("a", uri -> {
            if (uri != null) {
                // URI received, do something with it
                Log.e("URI", uri);
            } else {
                // Handle case where URI is not found
                Log.e("URI", "URI not found");
            }
        });
        myDB.close();


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
                Log.e("AudioPath:",appClicked);
                MyNotificationListener.audio=audiopath;
                myDB=new DbHelper(AllAps.this);
                myDB.insertAppWithUri(appClicked,audiopath);
                myDB.close();



            }

        }
    }

}