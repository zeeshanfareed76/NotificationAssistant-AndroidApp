package com.zfstudio.notificationassistant;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    private static final int PICK_AUDIO_REQUEST_CODE = 123;
    private Context context;
    Class<?> clazz;
    ArrayList<String> app_Name = new ArrayList<>();
    ArrayList<Drawable> app_Icon = new ArrayList<>();
    int table_no;
    DbHelper mydb;
    RecyclerViewAdapter(Context context,ArrayList<String> app_name, ArrayList<Drawable> app_icon,int table){
        this.context=context;
        this.app_Name=app_name;
        this.app_Icon=app_icon;
        this.clazz=clazz;
        mydb=new DbHelper(context);
        this.table_no=table;
    }




    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater= LayoutInflater.from(context);
        View view;
        if(table_no==1){
            view=inflater.inflate(R.layout.single_view,parent,false);
        }
        else{
            view=inflater.inflate(R.layout.single_view2,parent,false);
        }

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.textView.setText((String) app_Name.get(position)); // Set app name

        // Set app icon
        Drawable appIcon = (Drawable) app_Icon.get(position);
        holder.imageView.setImageDrawable(appIcon);
        String appName = app_Name.get(position);
        mydb.isAppPresent(appName, table_no, isPresent -> {
            // Handle the result here
            if (isPresent) {
                holder.aSwitch.setChecked(isPresent);
            }
        });

        holder.aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // If the switch is enabled, call the store() function with the app name
            if (isChecked) {
                store(appName);
            } else {
                // If the switch is disabled, call the delete() function with the app name
                deleteapp(appName);
            }
        });
        if(table_no!=1){
            holder.audioSelect.setOnClickListener(v -> {
                selectAudioFile();
                AllAps.appClicked=appName;
            });
        }


    }

    private void store(String appName) {
        mydb.isAppPresent(appName, table_no, isPresent -> {
            // Handle the result here
            if (!isPresent) {
                mydb.insertApp(appName, table_no);
                Log.e("Store","on");
            }
        });
    }
    private void deleteapp(String appName) {
        mydb.isAppPresent(appName, table_no, isPresent -> {
            // Handle the result here
            if (isPresent) {
                mydb.deleteApp(appName, table_no);
                Log.e("Store2","on");
            }
        });
    }

    @Override
    public int getItemCount() {
        return app_Name.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;
        Switch aSwitch;
        Button audioSelect;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.appicon);
            textView=itemView.findViewById(R.id.appname);
            aSwitch=itemView.findViewById(R.id.switch1);
            audioSelect=itemView.findViewById(R.id.buttonCS);



        }
    }

    public void selectAudioFile() {
        Intent i= new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        ((Activity)context).startActivityForResult(i,PICK_AUDIO_REQUEST_CODE);
    }


}

