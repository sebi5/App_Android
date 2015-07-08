package sky.chin.penpal.core.events.message;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Observable;

import sky.chin.penpal.core.databases.MessageReaderContract;
import sky.chin.penpal.core.databases.MessageReaderDbHelper;
import sky.chin.penpal.models.Message;
import sky.chin.penpal.server.Server;
import sky.chin.penpal.server.interfaces.ServerResponseListener;
import sky.chin.penpal.server.requests.GetMessagesRequest;
import sky.chin.penpal.server.requests.SendMessageRequest;
import sky.chin.penpal.utils.ConnectivityUtils;
import sky.chin.penpal.utils.TimeUtils;

public class MessageEvent extends Observable {

    private Context mContext;
    private Server mServer;
    private ArrayList<Message> mMessages = new ArrayList<>();

    final private int MAX_RECENT_MESSAGES = 20;

    public MessageEvent(Context mContext) {
        this.mContext = mContext;

        mServer = Server.getInstance(this.mContext);
    }

    public void get(String id, String userId, String userPassword) {
        if (ConnectivityUtils.isConnected(mContext))
            getMessagesFromServer(id, userId, userPassword);
        else
            getMessagesFromDatabase(id);
    }

    private void getMessagesFromServer(final String id, String userId, String userPassword) {
        mServer.sendRequest(
                new GetMessagesRequest.Builder()
                        .id(id)
                        .userId(userId)
                        .userPassword(userPassword)
                        .build(),
                new ServerResponseListener() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        mMessages = new ArrayList<>();
                        try {
                            JSONArray messages = data.getJSONArray("messages")
                                    .getJSONArray(0);

                            for (int k = 0; k < messages.length(); k++) {
                                JSONArray item = messages.getJSONArray(k);
                                mMessages.add(new Message(item.getString(1),
                                        item.getString(0),
                                        item.getString(2)));
                            }

                            saveRecentMessages(mMessages);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        notifyObservers(mMessages);
                    }

                    @Override
                    public void onError(String content) {
                        getMessagesFromDatabase(id);
                    }
                }
        );
    }

    private void getMessagesFromDatabase(String id) {
        MessageReaderDbHelper mDbHelper = new MessageReaderDbHelper(mContext);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                MessageReaderContract.MessageEntry._ID,
                MessageReaderContract.MessageEntry.COLUMN_NAME_USER_ID,
                MessageReaderContract.MessageEntry.COLUMN_NAME_TEXT,
                MessageReaderContract.MessageEntry.COLUMN_NAME_TIMESTAMP,
        };

        String selection = MessageReaderContract.MessageEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = { id };

        Cursor c = db.query(
                MessageReaderContract.MessageEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        if (c.moveToFirst()) {
            mMessages = new ArrayList<>();
            do {
                mMessages.add(new Message(c.getString(2),
                        c.getString(3),
                        c.getString(1)));
            } while (c.moveToNext());

            notifyObservers(mMessages);
        }
    }

    private void saveRecentMessages(ArrayList<Message> messages) {
        int offset = 0;
        if (messages.size() > MAX_RECENT_MESSAGES)
            offset = messages.size() - MAX_RECENT_MESSAGES - 1;

        MessageReaderDbHelper mDbHelper = new MessageReaderDbHelper(mContext);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        for (int i = offset; i < messages.size(); i++) {
            Message m = messages.get(i);
            insertMessageToDatabase(db, m.getSenderId(), m.getText(), m.getTimestamp());
        }
    }

    private void insertMessageToDatabase(SQLiteDatabase db, String id, String text, String timestamp) {
        ContentValues values = new ContentValues();
        values.put(MessageReaderContract.MessageEntry.COLUMN_NAME_USER_ID, id);
        values.put(MessageReaderContract.MessageEntry.COLUMN_NAME_TEXT, text);
        values.put(MessageReaderContract.MessageEntry.COLUMN_NAME_TIMESTAMP, timestamp);

        db.insert(
            MessageReaderContract.MessageEntry.TABLE_NAME,
            null,
            values);
    }

    public void send(String text, String id, String userId, String userPassword) {

        final Message newMessage = new Message(text, TimeUtils.generateTimestamp(), userId);

        if (ConnectivityUtils.isConnected(mContext)) {
            mServer.sendRequest(
                    new SendMessageRequest.Builder()
                            .id(id)
                            .userId(userId)
                            .userPassword(userPassword)
                            .message(text)
                            .build(),
                    new ServerResponseListener() {
                        @Override
                        public void onSuccess(JSONObject data) {
                            mMessages.add(newMessage);
                            notifyObservers(mMessages);
                        }

                        @Override
                        public void onError(String content) {
                            mMessages.remove(newMessage);
                        }
                    }
            );
        }
    }

}
