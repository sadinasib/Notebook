package com.android.notebook.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NotebookDbHelper extends SQLiteOpenHelper {
    private static final String TAG = NotebookDbHelper.class.getSimpleName();
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "dictionary.db";

    private static final String SQL_CREATE_WORDS_TABLE =
            "CREATE TABLE "
                    + NotebookContract.NotebookEntry.TABLE_NAME + " ("
                    + NotebookContract.NotebookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + NotebookContract.NotebookEntry.COLUMN_WORD + " TEXT NOT NULL, "
                    + NotebookContract.NotebookEntry.COLUMN_TRANSLATION + " TEXT NOT NULL);";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
            + NotebookContract.NotebookEntry.TABLE_NAME + ";";

    public NotebookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate");
        db.execSQL(SQL_CREATE_WORDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade");
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onDowngrade");
        onUpgrade(db, oldVersion, newVersion);
    }
}
