package sky.chin.penpal.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import sky.chin.penpal.R;
import sky.chin.penpal.activities.BaseActivity;
import sky.chin.penpal.adapters.ChatAdapter;
import sky.chin.penpal.core.databases.ChatReaderContract;
import sky.chin.penpal.core.databases.DbHelper;
import sky.chin.penpal.models.Chat;
import sky.chin.penpal.server.Server;
import sky.chin.penpal.server.interfaces.ServerResponseListener;
import sky.chin.penpal.server.requests.AllMessagesRequest;
import sky.chin.penpal.utils.AuthManager;
import sky.chin.penpal.utils.NetworkConnectivityListener;
import sky.chin.penpal.widgets.EndlessRecyclerOnScrollListener;

public class MessageTabFragment extends Fragment{

    private static final String LOG = MessageTabFragment.class.getSimpleName();

    final private int MAX_RECENT_CHATS = 8;

    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private EndlessRecyclerOnScrollListener mRecyclerOnScrollListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static MessageTabFragment newInstance() {
        return new MessageTabFragment();
    }

    public MessageTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NetworkConnectivityListener ncListener = new NetworkConnectivityListener();
        ncListener.startListening(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_message_tab, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetCounter();
                getChatsFromServer();
            }
        });

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore() {
                getChatsFromServer();
            }
        };
        mRecyclerView.addOnScrollListener(mRecyclerOnScrollListener);

        mAdapter = new ChatAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        if (((BaseActivity)getActivity()).isOffline())
            getChatsFromDatabase();
        else
            getChatsFromServer();

        return v;
    }

    final private int LIMIT = 10;
    private int mSkip = 0;

    private void resetCounter() {
        mSkip = 0;
    }

    private void getChatsFromServer() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        AuthManager authManager = AuthManager.getInstance(getActivity());

        Server.getInstance(getActivity()).sendRequest(
                new AllMessagesRequest.Builder()
                        .userId(authManager.getUserId())
                        .userPassword(authManager.getUserPassword())
                        .limit(LIMIT+"")
                        .skip(mSkip+"")
                        .build(),
                new ServerResponseListener() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            if (mSkip == 0)
                                mAdapter.clearChats();

                            JSONArray message = data.getJSONArray("message")
                                    .getJSONArray(0);

                            ArrayList<Chat> chatArrayList = new ArrayList<>();

                            for (int k = 0; k < message.length(); k++) {
                                JSONArray item = message.getJSONArray(k);
                                Chat newChat = new Chat(item.getString(1),
                                        item.getString(0),
                                        item.getString(2),
                                        item.getString(4),
                                        item.getString(5),
                                        item.getInt(6) == 1 ? true : false);
                                mAdapter.addChat(newChat);
                                chatArrayList.add(newChat);
                            }

                            mAdapter.notifyDataSetChanged();
                            saveRecentChats(chatArrayList);

                            // update skip
                            mSkip += message.length();

                            // Remove OnScrollListener if no next page
                            if (message.length() < LIMIT)
                                mRecyclerView.removeOnScrollListener(mRecyclerOnScrollListener);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onError(String content) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void getChatsFromDatabase() {
        DbHelper mDbHelper = new DbHelper(getActivity());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                ChatReaderContract.ChatEntry._ID,
                ChatReaderContract.ChatEntry.COLUMN_NAME_USER_ID,
                ChatReaderContract.ChatEntry.COLUMN_NAME_USERNAME,
                ChatReaderContract.ChatEntry.COLUMN_NAME_TEXT,
                ChatReaderContract.ChatEntry.COLUMN_NAME_IMAGE,
                ChatReaderContract.ChatEntry.COLUMN_NAME_TIMESTAMP,
                ChatReaderContract.ChatEntry.COLUMN_NAME_READ,
        };

        Cursor c = db.query(
                ChatReaderContract.ChatEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        if (c.moveToFirst()) {
            do {
                mAdapter.addChat(new Chat(c.getString(1),
                        c.getString(3),
                        c.getString(5),
                        c.getString(4),
                        c.getString(2),
                        c.getInt(6) == 1 ? true : false));
            } while (c.moveToNext());
        }
    }

    private void saveRecentChats(ArrayList<Chat> chats) {
        int offset = 0;
        if (chats.size() > MAX_RECENT_CHATS)
            offset = chats.size() - MAX_RECENT_CHATS - 1;

        DbHelper mDbHelper = new DbHelper(getActivity());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.delete(ChatReaderContract.ChatEntry.TABLE_NAME, null, null);

        for (int i = offset; i < chats.size(); i++) {
            Chat c = chats.get(i);
            insertChatToDatabase(db,
                    c.getId(),
                    c.getUsername(),
                    c.getTitle(),
                    c.getProfilePhoto(),
                    c.getTimestamp(),
                    c.isRead() ? 1 : 0);
        }
    }

    private void insertChatToDatabase(SQLiteDatabase db,
                                      String id,
                                      String username,
                                      String text,
                                      String image,
                                      String timestamp,
                                      int read) {
        ContentValues values = new ContentValues();
        values.put(ChatReaderContract.ChatEntry.COLUMN_NAME_USER_ID, id);
        values.put(ChatReaderContract.ChatEntry.COLUMN_NAME_USERNAME, username);
        values.put(ChatReaderContract.ChatEntry.COLUMN_NAME_TEXT, text);
        values.put(ChatReaderContract.ChatEntry.COLUMN_NAME_IMAGE, image);
        values.put(ChatReaderContract.ChatEntry.COLUMN_NAME_TIMESTAMP, timestamp);
        values.put(ChatReaderContract.ChatEntry.COLUMN_NAME_READ, read);

        db.insert(
                ChatReaderContract.ChatEntry.TABLE_NAME,
                null,
                values);
    }
}
