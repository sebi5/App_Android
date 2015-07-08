package sky.chin.penpal.core.databases;

import android.provider.BaseColumns;

public class MessageReaderContract {

    public MessageReaderContract() {}

    public static abstract class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "messages";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_IMAGE = "image";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }
}
