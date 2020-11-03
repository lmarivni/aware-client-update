package com.aware.plugin.smokeregistration;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import androidx.annotation.Nullable;


import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

public class Provider extends ContentProvider {
    public static String AUTHORITY = "com.aware.plugin.smokeregistration.provider.smokeregistration";

    public static final int DATABASE_VERSION = 4; //increase this if you make changes to the database structure, i.e., rename columns, etc.

    public static final String DATABASE_NAME = "smoke_events.db"; //the database filename

    public static final String DB_TBL_SMOKE_EVENTS = "smoke_events";
//    public static final String DB_TBL_SMOKE_EVENTS_DATA = "smoke_events_data";

    //For each table, add two indexes: DIR and ITEM. The index needs to always increment. Next one is 3, and so on.
    private static final int SE_DIR = 1;
    private static final int SE_ITEM = 2;
//    private static final int SE_DATA_DIR = 3;
//    private static final int SE_DATA_ITEM = 4;

    //Put tables names in this array so AWARE knows what you have on the database
    public static final String[] DATABASE_TABLES = {
            DB_TBL_SMOKE_EVENTS,
//            DB_TBL_SMOKE_EVENTS_DATA
    };

    //These are columns that we need to sync data, don't change this!
    public interface AWAREColumns extends BaseColumns {
        String _ID = "_id";
        String TIMESTAMP = "timestamp";
        String DEVICE_ID = "device_id";
    }

    /**
     * Create one of these per database table
     * In this example, we are adding example columns
     */
    public static final class Smoke_Events implements AWAREColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_TBL_SMOKE_EVENTS);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.aware.plugin.smokeregistration";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.aware.plugin.smokeregistration";

        public static final String DATE = "date";
        public static final String TIME = "time";
    }

//    public static final class Smoke_Events_Data implements AWAREColumns {
//        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_TBL_SMOKE_EVENTS_DATA);
//        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.${applicationId}.provider.smoke_events.data";
//        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.${applicationId}.provider.smoke_events.data";
//
//        public static final String DATE = "date";
//        public static final String TIME = "time";
//    }

    /**
     * Share the fields with AWARE so we can replicate the table schema on the server
     */
    private static final String DB_TBL_SMOKE_EVENTS_FIELDS =
            Smoke_Events._ID + " integer primary key autoincrement," +
                    Smoke_Events.TIMESTAMP + " real default 0," +
                    Smoke_Events.DEVICE_ID + " text default ''," +
                    Smoke_Events.DATE + " text default ''," +
                    Smoke_Events.TIME + " text default ''";

//    private static final String DB_TBL_SMOKE_EVENTS_DATA_FIELDS =
//            Smoke_Events_Data._ID + " integer primary key autoincrement," +
//                    Smoke_Events_Data.TIMESTAMP + " real default 0," +
//                    Smoke_Events_Data.DEVICE_ID + " text default ''," +
//                    Smoke_Events_Data.DATE + "real default 0," +
//                    Smoke_Events_Data.TIME + "real default 0,";

    public static final String[] TABLES_FIELDS = {
            DB_TBL_SMOKE_EVENTS_FIELDS
//            DB_TBL_SMOKE_EVENTS_DATA_FIELDS
    };

    //Helper variables for ContentProvider - don't change me
    private static UriMatcher sUriMatcher;
    private DatabaseHelper dbHelper;
    private static SQLiteDatabase database;

    private void initialiseDatabase() {
        if (dbHelper == null)
            dbHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        if (database == null)
            database = dbHelper.getWritableDatabase();
    }

    //For each table, create a hashmap needed for database queries
    private static HashMap<String, String> seHash = null;
//    private static HashMap<String, String> seDataHash = null;

    /**
     * Returns the provider authority that is dynamic
     *
     * @return
     */
    public static String getAuthority(Context context) {
        AUTHORITY = context.getPackageName() + ".provider.smokeregistration";
        return AUTHORITY;
    }

    @Override
    public boolean onCreate() {
        //This is a hack to allow providers to be reusable in any application/plugin by making the authority dynamic using the package name of the parent app

        AUTHORITY = getContext().getPackageName() + ".provider.smokeregistration";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], SE_DIR);
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", SE_ITEM);
//        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[1], SE_DATA_DIR);
//        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[1] + "/#", SE_DATA_ITEM);

        //Create each table hashmap so Android knows how to insert data to the database. Put ALL table fields.
        seHash = new HashMap<>();
        seHash.put(Smoke_Events._ID, Smoke_Events._ID);
        seHash.put(Smoke_Events.TIMESTAMP, Smoke_Events.TIMESTAMP);
        seHash.put(Smoke_Events.DEVICE_ID, Smoke_Events.DEVICE_ID);
        seHash.put(Smoke_Events.DATE, Smoke_Events.DATE);
        seHash.put(Smoke_Events.TIME, Smoke_Events.TIME);

//        seDataHash = new HashMap<>();
//        seDataHash.put(Smoke_Events_Data._ID, Smoke_Events_Data._ID);
//        seDataHash.put(Smoke_Events_Data.TIMESTAMP, Smoke_Events_Data.TIMESTAMP);
//        seDataHash.put(Smoke_Events_Data.DEVICE_ID, Smoke_Events_Data.DEVICE_ID);
//        seDataHash.put(Smoke_Events_Data.DATE, Smoke_Events_Data.DATE);
//        seDataHash.put(Smoke_Events_Data.TIME, Smoke_Events_Data.TIME);

        return true;
    }

    @Override
    public synchronized int delete(Uri uri, String selection, String[] selectionArgs) {
        initialiseDatabase();

        database.beginTransaction();

        int count;
        switch (sUriMatcher.match(uri)) {
            case SE_DIR:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
//            case SE_DATA_DIR:
//                count = database.delete(DATABASE_TABLES[1], selection, selectionArgs);
//                break;
            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        getContext().getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    @Nullable
    @Override
    public synchronized Uri insert(Uri uri, ContentValues new_values) {

        initialiseDatabase();

        ContentValues values = (new_values != null) ? new ContentValues(new_values) : new ContentValues();

        database.beginTransaction();

        switch (sUriMatcher.match(uri)) {

            //Add each table DIR case
            case SE_DIR:
                long _id = database.insert(DATABASE_TABLES[0], Smoke_Events.DEVICE_ID, values);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(Smoke_Events.CONTENT_URI, _id);
                    getContext().getContentResolver().notifyChange(dataUri, null);
                    return dataUri;
                }
                database.endTransaction();
                throw new SQLException("Failed to insert row into " + uri);
//            case SE_DATA_DIR:
//                long _id = database.insert(DATABASE_TABLES[1], Smoke_Events_Data.DEVICE_ID, values);
//                database.setTransactionSuccessful();
//                database.endTransaction();
//                if (_id > 0) {
//                    Uri dataUri = ContentUris.withAppendedId(Smoke_Events_Data.CONTENT_URI, _id);
//                    getContext().getContentResolver().notifyChange(dataUri, null);
//                    return dataUri;
//                }
//                database.endTransaction();
//                throw new SQLException("Failed to insert row into " + uri);
            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        initialiseDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {

            //Add all tables' DIR entries, with the right table index
            case SE_DIR:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(seHash); //the hashmap of the table
                break;
//            case SE_DATA_DIR:
//                qb.setTables(DATABASE_TABLES[1]);
//                qb.setProjectionMap(seDataHash); //the hashmap of the table
//                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        //Don't change me
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());
            return null;
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            //Add each table indexes DIR and ITEM
            case SE_DIR:
                return Smoke_Events.CONTENT_TYPE;
            case SE_ITEM:
                return Smoke_Events.CONTENT_ITEM_TYPE;
//            case SE_DATA_DIR:
//                return Smoke_Events_Data.CONTENT_TYPE;
//            case SE_DATA_DIR:
//                return Smoke_Events_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public synchronized int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        initialiseDatabase();

        database.beginTransaction();

        int count;
        switch (sUriMatcher.match(uri)) {

            //Add each table DIR case
            case SE_DIR:
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                break;

//            case SE_DATA_DIR:
//                count = database.update(DATABASE_TABLES[1], values, selection, selectionArgs);
//                break;

            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        database.setTransactionSuccessful();
        database.endTransaction();

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
