package com.zfstudio.notificationassistant;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;


public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "NotificationAssistant.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME1 = "apps_tts";
    private static final String COLUMN_ID1 = "_id";
    private static final String COLUMN_PACKAGE1 = "package";

    private static final String TABLE_NAME2 = "apps_custom";
    private static final String COLUMN_ID2 = "_id";
    private static final String COLUMN_PACKAGE2 = "package";
    private static final String COLUMN_STATUS2 = "status";
    private static final String COLUMN_URI2="sound_uri";

    private Context context;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query1 = "CREATE TABLE " + TABLE_NAME1 + " (" + COLUMN_ID1 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PACKAGE1 + " TEXT )";
        db.execSQL(query1);

        String query2=
                "CREATE TABLE "+TABLE_NAME2+" ("+COLUMN_ID2+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        COLUMN_PACKAGE2+" TEXT, "+COLUMN_STATUS2+" INTEGER DEFAULT 0, "+COLUMN_URI2+" TEXT )";
        db.execSQL(query2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Upgrade database schema if needed
    }

    public void insertApp(String packageName, int table) {
        new InsertAppTask().execute(packageName, table);
    }

    public void deleteApp(String packageName, int table) {
        new DeleteAppTask().execute(packageName, table);

    }
    public void insertAppWithUri(String packageName, String uri) {
        new InsertAppWithUriTask().execute(packageName, uri);
    }

    public void isAppPresent(String packageName, int table, AppPresentCallback callback) {
        new IsAppPresentTask(callback).execute(packageName, table);
    }
    public void getUriFromPackageName(String packageName, UriCallback callback) {
        new GetUriFromPackageNameTask(callback).execute(packageName);
    }


    // AsyncTask to insert app into the database
    private class InsertAppTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {

            ContentValues values = new ContentValues();
            String packageName = (String) params[0];
            int table = (int) params[1];
            if (table == 1) {
                SQLiteDatabase db = getWritableDatabase();
                values.put(COLUMN_PACKAGE1, packageName);
                db.insert(TABLE_NAME1, null, values);
                db.close();
            } else {

                values.put(COLUMN_PACKAGE2, packageName);
                values.put(COLUMN_STATUS2,1);
                isAppPresent(packageName, 2, isPresent -> {
                    SQLiteDatabase db = getWritableDatabase();
                    // Handle the result here
                    if (!isPresent) {
                        db.insert(TABLE_NAME2, null, values);
                        db.close();
                    }

                });


            }

            return null;
        }
    }

    // AsyncTask to delete app from the database
    private class DeleteAppTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            SQLiteDatabase db = getWritableDatabase();
            String packageName = (String) params[0];
            int table = (int) params[1];
            if (table == 1) {
                String selection = COLUMN_PACKAGE1 + " = ?";
                String[] selectionArgs = {packageName};
                db.delete(TABLE_NAME1, selection, selectionArgs);
            } else {
                ContentValues values = new ContentValues();
                values.put(COLUMN_STATUS2, 0);

                String selection = COLUMN_PACKAGE2 + " = ?";
                String[] selectionArgs = {packageName};

                db.update(TABLE_NAME2, values, selection, selectionArgs);
            }
            db.close();
            return null;
        }
    }

    private class InsertAppWithUriTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {

            ContentValues values = new ContentValues();
            String selection = COLUMN_PACKAGE2 + " = ?";
            String packageName = (String) params[0];
            String[] selectionArgs = {packageName};
            String uri = (String) params[1];
            values.put(COLUMN_URI2, uri);
            isAppPresent(packageName, 2, isPresent -> {
                SQLiteDatabase db = getWritableDatabase();
                // Handle the result here
                if (isPresent) {
                    db.update(TABLE_NAME2, values, selection, selectionArgs);
                }else{
                    values.put(COLUMN_PACKAGE2, packageName);
                    // Insert the URI into the appropriate column
                    db.insert(TABLE_NAME2, null, values);
                }
                db.close();
            });

            return null;
        }
    }


    // AsyncTask to check if app is present in the database
    private class IsAppPresentTask extends AsyncTask<Object, Void, Boolean> {
        private AppPresentCallback callback;

        public IsAppPresentTask(AppPresentCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = null;
            String packageName = (String) params[0];
            int table = (int) params[1];
            try {
                if (table == 1) {
                    String query = "SELECT * FROM " + TABLE_NAME1 + " WHERE " + COLUMN_PACKAGE1 + " = ?";
                    cursor = db.rawQuery(query, new String[]{packageName});
                } else {
                    String query = "SELECT * FROM " + TABLE_NAME2 + " WHERE " + COLUMN_PACKAGE2 + " = ?";
                    cursor = db.rawQuery(query, new String[]{packageName});
                }
                // If the cursor has any rows, the app is present in the database
                return cursor.getCount() > 0;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                db.close();
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            callback.onAppPresent(result);
        }
    }

    // Interface to handle callback when app presence is checked
    public interface AppPresentCallback {
        void onAppPresent(boolean isPresent);
    }

    private class GetUriFromPackageNameTask extends AsyncTask<String, Void, String> {
        private UriCallback callback;

        public GetUriFromPackageNameTask(UriCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... params) {
            String packageName = params[0];
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = null;
            try {
                // Query the appropriate table for the URI based on the package name
                String[] columns = {COLUMN_URI2}; // Assuming the URI column is in TABLE_NAME2
                String selection = COLUMN_PACKAGE2 + " = ?";
                String[] selectionArgs = {packageName};
                cursor = db.query(TABLE_NAME2, columns, selection, selectionArgs, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(COLUMN_URI2);
                    if(columnIndex>=0  ){
                    return cursor.getString(columnIndex);}
                } else {
                    return null; // No URI found for the given package name
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                db.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String uri) {
            callback.onUriReceived(uri);
        }
    }

    // Interface to handle callback when URI is received
    public interface UriCallback {
        void onUriReceived(String uri);
    }
}



