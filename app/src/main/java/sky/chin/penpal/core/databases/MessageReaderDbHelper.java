package sky.chin.penpal.core.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessageReaderDbHelper extends SQLiteOpenHelper {

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MessageReaderContract.MessageEntry.TABLE_NAME + " (" +
                    MessageReaderContract.MessageEntry._ID + " INTEGER PRIMARY KEY," +
                    MessageReaderContract.MessageEntry.COLUMN_NAME_USER_ID + TEXT_TYPE + COMMA_SEP +
                    MessageReaderContract.MessageEntry.COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                    MessageReaderContract.MessageEntry.COLUMN_NAME_IMAGE + TEXT_TYPE + COMMA_SEP +
                    MessageReaderContract.MessageEntry.COLUMN_NAME_TIMESTAMP + TEXT_TYPE +
            " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MessageReaderContract.MessageEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MessageReader.db";

    public MessageReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
