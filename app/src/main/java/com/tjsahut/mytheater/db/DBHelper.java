package com.tjsahut.mytheater.db;

import android.annotation.SuppressLint;
import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tjsahut.mytheater.objects.Theater;

import java.util.ArrayList;

public class DBHelper {
    public static final Object sDataLock = new Object();
    protected static ArrayList<String> favCodes = new ArrayList<String>();

    private static SQLiteDatabase getDatabase(Context context) {
        DB db = new DB(context);
        return db.getReadableDatabase();
    }

    /**
     * Insert new item into favorites
     *
     * @param context
     */
    @SuppressLint("NewApi")
    public static void insertFavorite(Context context, String code, String title, String location, String city) {
        synchronized (DBHelper.sDataLock) {
            SQLiteDatabase db = getDatabase(context);

            ContentValues values = new ContentValues();
            values.put("code", code);
            values.put("title", title);
            values.put("location", location);
            values.put("city", city);
            db.insert("favorites", null, values);
            db.close();
        }
        favCodes.add(code);
        new BackupManager(context).dataChanged();
    }

    /**
     * Retrieve favorites
     */
    public static ArrayList<Theater> getFavorites(Context context) {

        ArrayList<Theater> favorites = new ArrayList<Theater>();
        synchronized (DBHelper.sDataLock) {
            SQLiteDatabase db = getDatabase(context);

            favCodes = new ArrayList<String>();

            // Cursor query (boolean distinct, String table, String[] columns,
            // String selection, String[] selectionArgs, String groupBy, String
            // having, String orderBy, String limit)
            Cursor cursor = db.query(true, "favorites", new String[]{"code", "title", "location", "city"}, null, null, null, null, "_id DESC", "100");

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Theater entry = new Theater();

                entry.code = cursor.getString(0);
                entry.title = cursor.getString(1);
                entry.location = cursor.getString(2);
                entry.city = cursor.getString(3);

                favorites.add(entry);
                favCodes.add(entry.code);
                cursor.moveToNext();
            }
            cursor.close();
            db.close();
        }
        return favorites;
    }

    @SuppressLint("NewApi")
    public static void removeFavorite(Context context, String code) {
        synchronized (DBHelper.sDataLock) {
            SQLiteDatabase db = getDatabase(context);

            db.delete("favorites", "code = ?", new String[]{code});
            db.close();
        }
        favCodes.remove(code);
        new BackupManager(context).dataChanged();
    }

    public static boolean isFavorite(String code) {
        return favCodes.contains(code);
    }
}
