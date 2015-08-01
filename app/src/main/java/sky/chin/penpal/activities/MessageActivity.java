package sky.chin.penpal.activities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import sky.chin.penpal.R;
import sky.chin.penpal.adapters.MessageAdapter;
import sky.chin.penpal.core.databases.DbHelper;
import sky.chin.penpal.core.databases.MessageReaderContract;
import sky.chin.penpal.models.Message;
import sky.chin.penpal.server.Server;
import sky.chin.penpal.server.interfaces.ServerResponseListener;
import sky.chin.penpal.server.requests.GetMessagesRequest;
import sky.chin.penpal.server.requests.SendMessageRequest;
import sky.chin.penpal.utils.AuthManager;
import sky.chin.penpal.utils.PrefUtils;
import sky.chin.penpal.utils.ToastUtils;
import sky.chin.penpal.widgets.ToplessRecyclerOnScrollListener;

public class MessageActivity extends BaseActivity{

    private static final String TAG = MessageActivity.class.getSimpleName();

    final private int MAX_RECENT_MESSAGES = 20;

    final private int LIMIT = 10;
    private int mSkip = 0;
    private boolean isDatabaseDataLoaded = false;
    private boolean isFetchingOldMessage = false;

    private RecyclerView mRecyclerView;
    private MessageAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EditText messageBox;
    private Button sendButton;
    private ToplessRecyclerOnScrollListener mRecyclerOnScrollListener;

    private String mMessageId;
    public static String INTENT_MESSAGE_ID = "message_id";

    private AuthManager authManager;

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener =
            new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!isDatabaseDataLoaded) {
                        Log.d(TAG, "SwipeRefresh called fetchOldMessages");
                        fetchOldMessages();
                    }
                }
            };

    final private int FETCHING_INTERVAL = 20000;
    private Handler mFetchNewMessageHandler = new Handler();
    private Runnable mFetchNewMessageRunnable = new Runnable() {
        @Override
        public void run() {
            fetchNewMessages();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        if (getIntent() != null) {
            mMessageId = getIntent().getStringExtra(INTENT_MESSAGE_ID);
        }

        authManager = new AuthManager(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerOnScrollListener = new ToplessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore() {
                Log.d(TAG, "LoadMore called fetchOldMessages");
                fetchOldMessages();
            }
        };

        mAdapter = new MessageAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        messageBox = (EditText) findViewById(R.id.messageBox);
        sendButton = (Button) findViewById(R.id.btnSendMessage);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = messageBox.getText().toString();

                if ("".equals(text)) return;

                send(text, mMessageId, authManager.getUserId(), authManager.getUserPassword());
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        prefetch();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start fetching new message
        startFetchNewMessage();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mFetchNewMessageHandler.removeCallbacks(mFetchNewMessageRunnable);
    }

    private void prefetch() {
        getMessagesFromDatabase();

        if (!isOffline()) {
            Log.d(TAG, "prefetch called fetchOldMessages");
            fetchOldMessages();
        }
    }

    private void scrollToMostRecentMessage() {
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private void fetchOldMessages() {
        if (!isFetchingOldMessage) {
            Log.d(TAG, "called fetchOldMessages");
            isFetchingOldMessage = true;

            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });

            mSwipeRefreshLayout.setEnabled(false);

            Server.getInstance(this).sendRequest(
                    new GetMessagesRequest.Builder()
                            .id(mMessageId)
                            .userId(authManager.getUserId())
                            .userPassword(authManager.getUserPassword())
                            .limit(LIMIT + "")
                            .skip(mSkip + "")
                            .lastId(" ")
                            .build(),
                    new ServerResponseListener() {
                        @Override
                        public void onSuccess(JSONObject data) {
                            try {
                                JSONArray messages = data.getJSONArray("messages")
                                        .getJSONArray(0);

                                Log.d(TAG, data.toString());

                                if (isDatabaseDataLoaded) {
                                    isDatabaseDataLoaded = false;
                                    mAdapter.clearMessages();
                                }

                                ArrayList<Message> messageArrayList = new ArrayList<Message>();
                                for (int k = 0; k < messages.length(); k++) {
                                    JSONObject item = messages.getJSONObject(k);
                                    messageArrayList.add(0, new Message(item.getString("message_id"),
                                            item.getString("text"),
                                            item.getInt("message_date") + "",
                                            item.getString("poster_id"),
                                            item.getString("user_photo"),
                                            item.getString("master_id")));
                                }

                                mAdapter.addOldMessages(messageArrayList);

                                // Scroll to most recent message when first load and save most recent messages
                                if (mSkip == 0) {
                                    saveRecentMessages(mAdapter.getMessages());
                                    scrollToMostRecentMessage();
                                    mRecyclerView.addOnScrollListener(mRecyclerOnScrollListener);
                                }

                                // update skip
                                mSkip += messageArrayList.size();

                                // Remove OnScrollListener if no next page
                                if (messageArrayList.size() < LIMIT) {
                                    Log.d(TAG, "Remove OnScrollListener");
                                    mRecyclerView.removeOnScrollListener(mRecyclerOnScrollListener);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            mSwipeRefreshLayout.setRefreshing(false);
                            isFetchingOldMessage = false;
                        }

                        @Override
                        public void onError(String content) {
                            Log.d(TAG, "onError " + content);
                            mSwipeRefreshLayout.setRefreshing(false);
                            mSwipeRefreshLayout.setEnabled(true);
                            isFetchingOldMessage = false;
                        }
                    }
            );
        }
    }

    private void startFetchNewMessage() {
        mFetchNewMessageHandler.post(mFetchNewMessageRunnable);
    }

    private void scheduleNextFetchNewMessage() {
        mFetchNewMessageHandler.postDelayed(mFetchNewMessageRunnable, FETCHING_INTERVAL);
    }

    private void fetchNewMessages() {
        Server.getInstance(this).sendRequest(
                new GetMessagesRequest.Builder()
                        .id(mMessageId)
                        .userId(authManager.getUserId())
                        .userPassword(authManager.getUserPassword())
                        .limit(LIMIT + "")
                        .skip("0")
                        .lastId(" ")
                        .build(),
                new ServerResponseListener() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            JSONArray messages = data.getJSONArray("messages")
                                    .getJSONArray(0);

                            boolean hasNewMessage = false;
                            String userId = PrefUtils.getPrefsUserId(MessageActivity.this);
                            String messageId = mAdapter.getMostRecentMessageId();

                            ArrayList<Message> messageArrayList = new ArrayList<Message>();
                            for (int k = 0; k < messages.length(); k++) {
                                JSONObject item = messages.getJSONObject(k);

                                if (messageId.equals(item.getString("message_id")))
                                    break;

                                messageArrayList.add(0, new Message(item.getString("message_id"),
                                        item.getString("text"),
                                        item.getInt("message_date") + "",
                                        item.getString("poster_id"),
                                        item.getString("user_photo"),
                                        item.getString("master_id")));

                                if (!hasNewMessage && !userId.equals(item.getString("poster_id")))
                                    hasNewMessage = true;
                            }

                            mAdapter.addNewMessages(messageArrayList);

                            if (hasNewMessage)
                                ToastUtils.show(MessageActivity.this,
                                        getResources().getString(R.string.you_have_new_message));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        scheduleNextFetchNewMessage();
                    }

                    @Override
                    public void onError(String content) {
                        Log.d(TAG, "onError " + content);
                    }
                }
        );
    }

    private void getMessagesFromDatabase() {
        DbHelper mDbHelper = new DbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                MessageReaderContract.MessageEntry._ID,
                MessageReaderContract.MessageEntry.COLUMN_NAME_MESSAGE_ID,
                MessageReaderContract.MessageEntry.COLUMN_NAME_TEXT,
                MessageReaderContract.MessageEntry.COLUMN_NAME_MESSAGE_DATE,
                MessageReaderContract.MessageEntry.COLUMN_NAME_POSTER_ID,
                MessageReaderContract.MessageEntry.COLUMN_NAME_USER_PHOTO,
                MessageReaderContract.MessageEntry.COLUMN_NAME_MASTER_ID,
        };

        String selection = MessageReaderContract.MessageEntry.COLUMN_NAME_MESSAGE_ID + " = ?";
        String[] selectionArgs = {mMessageId};

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
                mAdapter.addMessage(new Message(c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4),
                        c.getString(5),
                        c.getString(6)));
            } while (c.moveToNext());
        }

        mAdapter.notifyDataSetChanged();

        // Scroll to most recent message
        scrollToMostRecentMessage();

        isDatabaseDataLoaded = true;
    }

    private void saveRecentMessages(ArrayList<Message> messages) {
        int offset = 0;
        if (messages.size() > MAX_RECENT_MESSAGES)
            offset = messages.size() - MAX_RECENT_MESSAGES - 1;

        DbHelper mDbHelper = new DbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.delete(MessageReaderContract.MessageEntry.TABLE_NAME, null, null);

        for (int i = offset; i < messages.size(); i++) {
            Message m = messages.get(i);
            insertMessageToDatabase(db,
                    m.getPosterId(),
                    m.getText(),
                    m.getMessageDate(),
                    m.getUserPhoto(),
                    m.getMasterId());
        }
    }

    private void insertMessageToDatabase(SQLiteDatabase db,
                                         String id,
                                         String text,
                                         String messageDate,
                                         String userPhoto,
                                         String masterId) {
        ContentValues values = new ContentValues();
        values.put(MessageReaderContract.MessageEntry.COLUMN_NAME_MESSAGE_ID, mMessageId);
        values.put(MessageReaderContract.MessageEntry.COLUMN_NAME_POSTER_ID, id);
        values.put(MessageReaderContract.MessageEntry.COLUMN_NAME_TEXT, text);
        values.put(MessageReaderContract.MessageEntry.COLUMN_NAME_MESSAGE_DATE, messageDate);
        values.put(MessageReaderContract.MessageEntry.COLUMN_NAME_USER_PHOTO, userPhoto);
        values.put(MessageReaderContract.MessageEntry.COLUMN_NAME_MASTER_ID, masterId);

        db.insert(
                MessageReaderContract.MessageEntry.TABLE_NAME,
                null,
                values);
    }

    private void updateUIOnSendingMessage() {
        enableMessageBox(false);
        scrollToMostRecentMessage();
    }

    private void updateUIOnMessageSent() {
        messageBox.setText("");
        enableMessageBox(true);
    }

    private void enableMessageBox(boolean enabled) {
        messageBox.setEnabled(enabled);
        sendButton.setEnabled(enabled);
    }

    public void send(String text, String id, String userId, String userPassword) {
        updateUIOnSendingMessage();

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
                        ToastUtils.show(MessageActivity.this, getResources().getString(R.string.sent));

                        updateUIOnMessageSent();
                    }

                    @Override
                    public void onError(String content) {
                        ToastUtils.show(MessageActivity.this, content);
                        updateUIOnMessageSent();
                    }
                }
        );
    }
}
