package ogr.scorelab.ucsc.mobility_track;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

public class DBAccess {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_TIMESTAMP,
            MySQLiteHelper.COLUMN_LATITUDE,
            MySQLiteHelper.COLUMN_LONGITUDE,
            MySQLiteHelper.COLUMN_DIRECTION,
            MySQLiteHelper.COLUMN_SPEED
    };

    public DBAccess(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public synchronized void open() {
        database = dbHelper.getWritableDatabase();
    }

    public synchronized void close() {
        dbHelper.close();
    }

    public synchronized void push(Location location) {
        ContentValues values = new ContentValues();

        values.put(MySQLiteHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());
        values.put(MySQLiteHelper.COLUMN_LATITUDE, location.getLatitude());
        values.put(MySQLiteHelper.COLUMN_LONGITUDE, location.getLongitude());
        values.put(MySQLiteHelper.COLUMN_DIRECTION, location.getSpeed());
        values.put(MySQLiteHelper.COLUMN_SPEED, location.getBearing());

        database.beginTransaction();
        database.insert(MySQLiteHelper.TABLE_GEO, null, values);
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    // Returns the number of entries in the database
    public synchronized int getCount()
    {
        if (!database.isOpen())
            return 0;

        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + MySQLiteHelper.TABLE_GEO, null);
        cursor.moveToFirst();

        int count = cursor.getInt(0);

        cursor.close();

        return count;
    }

    public synchronized Location2 get() {
        if (!database.isOpen())
            return null;

        Cursor cursor = database.query(MySQLiteHelper.TABLE_GEO, allColumns, null, null, null, null, null);
        if (!cursor.moveToFirst()) {
            return null;
        }
        Location2 location2 = cursorToLocation2(cursor);

        cursor.close();
        return location2;
    }

    public synchronized int delete(long timestamp) {
        return database.delete(MySQLiteHelper.TABLE_GEO, MySQLiteHelper.COLUMN_TIMESTAMP + " = " + timestamp, null);
    }

    private Location2 cursorToLocation2(Cursor cursor) {
        Location2 location2 = new Location2();

        location2.timestamp = cursor.getLong(0);
        location2.latitude = cursor.getDouble(1);
        location2.longitude = cursor.getDouble(2);
        location2.direction = cursor.getFloat(3);
        location2.speed = cursor.getFloat(4);

        return location2;
    }
}
