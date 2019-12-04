package com.example.runningtracker.Provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.runningtracker.DbHelper;

public class MyContentProvider extends ContentProvider {
    private DbHelper dbHelper;

    private static final String AUTHORITY =
            "com.example.runningtracker.Provider.MyContentProvider";
    private static final String PRODUCTS_TABLE = "sessions";
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + PRODUCTS_TABLE);

    public static final int PRODUCTS = 1;
    public static final int PRODUCTS_ID = 2;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        new UriMatcher(UriMatcher.NO_MATCH);
        sURIMatcher.addURI(AUTHORITY, PRODUCTS_TABLE, PRODUCTS);
        sURIMatcher.addURI(AUTHORITY, PRODUCTS_TABLE + "/#", PRODUCTS_ID);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case PRODUCTS:
                rowsDeleted = sqlDB.delete(dbHelper.TABLE_PRODUCTS, selection,
                        selectionArgs);
                break;
            case PRODUCTS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(dbHelper.TABLE_PRODUCTS, dbHelper.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(dbHelper.TABLE_PRODUCTS,
                            dbHelper.COLUMN_ID + "=" + id + " and " + selection,
                            selectionArgs);

                }
                break;

            default:

                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        long id = 0;
        switch (uriType) {

            case PRODUCTS:

                id = sqlDB.insert(dbHelper.TABLE_PRODUCTS, null, values);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(PRODUCTS_TABLE + "/" + id);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext(), null, null, 1);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(dbHelper.TABLE_PRODUCTS);
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case PRODUCTS_ID:
                queryBuilder.appendWhere(dbHelper.COLUMN_ID + "="
                        + uri.getLastPathSegment());

                break;

            case PRODUCTS:

                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
                projection, selection, selectionArgs, null, null,
                sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(),
                uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case PRODUCTS:
                rowsUpdated =
                        sqlDB.update(dbHelper.TABLE_PRODUCTS, values,
                                selection,
                                selectionArgs);
                break;
            case PRODUCTS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(dbHelper.TABLE_PRODUCTS, values,
                            dbHelper.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated =
                            sqlDB.update(dbHelper.TABLE_PRODUCTS, values,
                                    dbHelper.COLUMN_ID + "=" + id + " and "
                                            + selection,
                                    selectionArgs);
                }
                break;
            default:

                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,
                null);
        return rowsUpdated;

    }
}