package sky.chin.penpal.core.databases;

import android.provider.BaseColumns;

public class ThreadReaderContract {

    public ThreadReaderContract() {}

    public static abstract class ThreadEntry implements BaseColumns {
        public static final String TABLE_NAME = "threads";
        public static final String COLUMN_NAME_THREAD_ID = "thread_id";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_USER_PHOTO = "user_photo";
        public static final String COLUMN_NAME_MESSAGE_DATE = "message_date";
        public static final String COLUMN_NAME_POSTER_ID = "poster_id";
        public static final String COLUMN_NAME_READ = "read";
    }
}
