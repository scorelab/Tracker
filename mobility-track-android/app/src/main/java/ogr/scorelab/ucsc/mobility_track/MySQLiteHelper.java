package ogr.scorelab.ucsc.mobility_track;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {
    public static final String COLUMN_TIMESTAMP = "timestamp";  // long
    public static final String COLUMN_LATITUDE = "latitude";    // double
    public static final String COLUMN_LONGITUDE = "longitude";  // double
    public static final String COLUMN_DIRECTION = "direction";  // float
    public static final String COLUMN_SPEED = "speed";          // float
    public static final String TABLE_GEO = "geo";

    private static final String DATABASE_NAME = "geodata";
    private static final int DATABASE_VERSION = 1;

    private static final String mDatabaseCreate = "create table "
            + TABLE_GEO + "(" + COLUMN_TIMESTAMP + " integer, "
            + COLUMN_LATITUDE + " real, "
            + COLUMN_LONGITUDE + " real, "
            + COLUMN_DIRECTION + " real, "
            + COLUMN_SPEED + " real);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(mDatabaseCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_GEO);
        onCreate(db);
    }
}
