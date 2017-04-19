package com.example.android.project1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by abspk on 25/11/2016.
 */

public class MovieDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "Movie";
    private static final int DB_VER = 1;

    static final String DB_MOV_TABLE = "movie_object";
    static final String DB_MOV_COL_OBJ = "mObj";
    static final String DB_MOV_COL_FAV = "isFav";
    static final String DB_MOV_COL_NAME = "mName";
    static final String DB_MOV_COL_ID = "_id";

    //constructor
    public MovieDatabase(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + DB_MOV_TABLE + "(" +
                "_id INTEGER NOT NULL PRIMARY KEY, " +
                DB_MOV_COL_OBJ + " TEXT NOT NULL, " +
                DB_MOV_COL_NAME + " TEXT NOT NULL, " +
                DB_MOV_COL_FAV + " INTEGER NOT NULL DEFAULT 0);";
        Log.d("SQL", "........................................" + CREATE_TABLE);
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //onCreate(db);
    }
}
