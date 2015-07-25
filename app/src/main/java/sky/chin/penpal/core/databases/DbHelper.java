package sky.chin.penpal.core.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_TABLE_MESSAGE =
            "CREATE TABLE " + MessageReaderContract.MessageEntry.TABLE_NAME + " (" +
                    MessageReaderContract.MessageEntry._ID + " INTEGER PRIMARY KEY," +
                    MessageReaderContract.MessageEntry.COLUMN_NAME_MESSAGE_ID + TEXT_TYPE + COMMA_SEP +
                    MessageReaderContract.MessageEntry.COLUMN_NAME_USER_ID + TEXT_TYPE + COMMA_SEP +
                    MessageReaderContract.MessageEntry.COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                    MessageReaderContract.MessageEntry.COLUMN_NAME_IMAGE + TEXT_TYPE + COMMA_SEP +
                    MessageReaderContract.MessageEntry.COLUMN_NAME_TIMESTAMP + TEXT_TYPE +
            " )";


    private static final String SQL_CREATE_TABLE_CHAT =
            "CREATE TABLE " + ThreadReaderContract.ThreadEntry.TABLE_NAME + " (" +
                    ThreadReaderContract.ThreadEntry._ID + " INTEGER PRIMARY KEY," +
                    ThreadReaderContract.ThreadEntry.COLUMN_NAME_THREAD_ID + TEXT_TYPE + COMMA_SEP +
                    ThreadReaderContract.ThreadEntry.COLUMN_NAME_USERNAME + TEXT_TYPE + COMMA_SEP +
                    ThreadReaderContract.ThreadEntry.COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                    ThreadReaderContract.ThreadEntry.COLUMN_NAME_USER_PHOTO + TEXT_TYPE + COMMA_SEP +
                    ThreadReaderContract.ThreadEntry.COLUMN_NAME_MESSAGE_DATE + TEXT_TYPE + COMMA_SEP +
                    ThreadReaderContract.ThreadEntry.COLUMN_NAME_POSTER_ID + TEXT_TYPE + COMMA_SEP +
                    ThreadReaderContract.ThreadEntry.COLUMN_NAME_READ + INT_TYPE +
                    " )";

    private static final String SQL_DELETE_TABLE_MESSAGE =
        "DROP TABLE IF EXISTS " + MessageReaderContract.MessageEntry.TABLE_NAME;

    private static final String SQL_DELETE_TABLE_CHAT =
        "DROP TABLE IF EXISTS " + ThreadReaderContract.ThreadEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "InterLocalReader.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_MESSAGE);
        db.execSQL(SQL_CREATE_TABLE_CHAT);
    }

    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DELETE_TABLE_MESSAGE);
        db.execSQL(SQL_DELETE_TABLE_CHAT);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
