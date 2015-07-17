package sky.chin.penpal.activities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import sky.chin.penpal.R;
import sky.chin.penpal.adapters.MessageAdapter;
import sky.chin.penpal.core.databases.MessageReaderContract;
import sky.chin.penpal.core.databases.MessageReaderDbHelper;
import sky.chin.penpal.models.Message;
import sky.chin.penpal.server.Server;
import sky.chin.penpal.server.interfaces.ServerResponseListener;
import sky.chin.penpal.server.requests.GetMessagesRequest;
import sky.chin.penpal.server.requests.SendMessageRequest;
import sky.chin.penpal.utils.AuthManager;
import sky.chin.penpal.utils.TimestampUtils;

public class MessageActivity extends BaseActivity{

    private static final String LOG = MessageActivity.class.getSimpleName();

    final private int MAX_RECENT_MESSAGES = 20;
    final private int LIMIT = 10;

    private RecyclerView mRecyclerView;
    private MessageAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EditText messageBox;
    private Button sendButton;

    private String id;

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        if (getIntent() != null) {
            id = getIntent().getStringExtra("id");
        }

        authManager = new AuthManager(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                get(id, authManager.getUserId(), authManager.getUserPassword());
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MessageAdapter();
        mRecyclerView.setAdapter(mAdapter);

        messageBox = (EditText) findViewById(R.id.messageBox);
        sendButton = (Button) findViewById(R.id.btnSendMessage);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = messageBox.getText().toString();

                if ("".equals(text)) return;

                send(text, id, authManager.getUserId(), authManager.getUserPassword());
                messageBox.setText("");
            }
        });

        get(id, authManager.getUserId(), authManager.getUserPassword());
    }

    public void get(String id, String userId, String userPassword) {
        if (isOffline())
            getMessagesFromDatabase(id);
        else
            getMessagesFromServer(id, userId, userPassword, 1);
    }

    private void getMessagesFromServer(final String id, String userId, String userPassword, int page) {
        String skip = ((page-1) * page) + "";

        Server.getInstance(this).sendRequest(
                new GetMessagesRequest.Builder()
                        .id(id)
                        .userId(userId)
                        .userPassword(userPassword)
                        .limit(LIMIT+"")
                        .skip(skip)
                        .build(),
                new ServerResponseListener() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            JSONArray messages = data.getJSONArray("messages")
                                    .getJSONArray(0);

                            for (int k = 0; k < messages.length(); k++) {
                                JSONArray item = messages.getJSONArray(k);
                                mAdapter.addMessage(new Message(item.getString(1),
                                                                item.getString(0),
                                                                item.getString(2)));
                            }

                            saveRecentMessages(mAdapter.getMessages());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String content) {
                        getMessagesFromDatabase(id);
                    }
                }
        );
    }

    private void getMessagesFromDatabase(String id) {
        MessageReaderDbHelper mDbHelper = new MessageReaderDbHelper(this);
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
            do {
                mAdapter.addMessage(new Message(c.getString(2),
                        c.getString(3),
                        c.getString(1)));
            } while (c.moveToNext());
        }
    }

    private void saveRecentMessages(ArrayList<Message> messages) {
        int offset = 0;
        if (messages.size() > MAX_RECENT_MESSAGES)
            offset = messages.size() - MAX_RECENT_MESSAGES - 1;

        MessageReaderDbHelper mDbHelper = new MessageReaderDbHelper(this);
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

        final Message newMessage = new Message(text, TimestampUtils.generateTimestamp(), userId);
        mAdapter.addMessage(newMessage);

//        if (ConnectivityUtils.isConnected(this)) {
            Server.getInstance(this).sendRequest(
                    new SendMessageRequest.Builder()
                            .id(id)
                            .userId(userId)
                            .userPassword(userPassword)
                            .message(text)
                            .build(),
                    new ServerResponseListener() {
                        @Override
                        public void onSuccess(JSONObject data) {
                            // TODO do something?
                        }

                        @Override
                        public void onError(String content) {
                            mAdapter.removeMessage(newMessage);
                        }
                    }
            );
//        }
    }
}
