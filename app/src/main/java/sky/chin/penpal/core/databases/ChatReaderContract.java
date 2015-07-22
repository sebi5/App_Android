package sky.chin.penpal.core.databases;

import android.provider.BaseColumns;

public class ChatReaderContract {

    public ChatReaderContract() {}

    public static abstract class ChatEntry implements BaseColumns {
        public static final String TABLE_NAME = "chats";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_IMAGE = "image";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_READ = "read";
    }
}
