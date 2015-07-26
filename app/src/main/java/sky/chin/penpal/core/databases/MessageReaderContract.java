package sky.chin.penpal.core.databases;

import android.provider.BaseColumns;

public class MessageReaderContract {

    public MessageReaderContract() {}

    public static abstract class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "messages";
        public static final String COLUMN_NAME_MESSAGE_ID = "message_id";
        public static final String COLUMN_NAME_POSTER_ID = "poster_id";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_USER_PHOTO = "user_photo";
        public static final String COLUMN_NAME_MESSAGE_DATE = "message_date";
        public static final String COLUMN_NAME_MASTER_ID = "master_id";
    }
}
