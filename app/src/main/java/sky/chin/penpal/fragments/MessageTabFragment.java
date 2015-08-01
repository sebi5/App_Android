package sky.chin.penpal.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import sky.chin.penpal.R;
import sky.chin.penpal.activities.BaseActivity;
import sky.chin.penpal.adapters.ThreadAdapter;
import sky.chin.penpal.core.databases.DbHelper;
import sky.chin.penpal.core.databases.ThreadReaderContract;
import sky.chin.penpal.models.Thread;
import sky.chin.penpal.server.Server;
import sky.chin.penpal.server.interfaces.ServerResponseListener;
import sky.chin.penpal.server.requests.AllMessagesRequest;
import sky.chin.penpal.utils.AuthManager;
import sky.chin.penpal.utils.NetworkConnectivityListener;
import sky.chin.penpal.widgets.EndlessRecyclerOnScrollListener;

public class MessageTabFragment extends Fragment{

    private static final String LOG = MessageTabFragment.class.getSimpleName();

    final private int MAX_RECENT_THREADS = 8;

    private RecyclerView mRecyclerView;
    private ThreadAdapter mAdapter;
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
                getThreadsFromServer();
            }
        });

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore() {
                getThreadsFromServer();
            }
        };
        mRecyclerView.addOnScrollListener(mRecyclerOnScrollListener);

        mAdapter = new ThreadAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        if (((BaseActivity)getActivity()).isOffline())
            getThreadsFromDatabase();
        else
            getThreadsFromServer();

        return v;
    }

    private int mLimit = 10;
    private int mSkip = 0;

    private void resetCounter() {
        mLimit = 10;
        mSkip = 0;
    }

    private void getThreadsFromServer() {
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
                        .limit(mLimit +"")
                        .skip(mSkip+"")
                        .lastId("1") // Dummy value
                        .build(),
                new ServerResponseListener() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            if (mSkip == 0)
                                mAdapter.clearThreads();

                            JSONArray message = data.getJSONArray("message")
                                    .getJSONArray(0);

                            ArrayList<Thread> threadArrayList = new ArrayList<>();

                            for (int k = 0; k < message.length(); k++) {
                                JSONObject item = message.getJSONObject(k);
                                Thread newThread = new Thread(item.getString("master_thread_id"),
                                        item.getString("text"),
                                        item.getString("message_date"),
                                        item.getString("user_photo"),
                                        item.getString("username"),
                                        item.getString("poster_id"),
                                        item.getInt("read") == 1 ? true : false);
                                mAdapter.addThread(newThread);
                                threadArrayList.add(newThread);
                            }

                            mAdapter.notifyDataSetChanged();
                            saveRecentThreads(threadArrayList);

                            // update skip
                            mLimit += message.length();

                            // Remove OnScrollListener if no next page
                            if (message.length() < mLimit)
                                mRecyclerView.removeOnScrollListener(mRecyclerOnScrollListener);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onError(String content) {
                        Log.d("Message", content);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void getThreadsFromDatabase() {
        DbHelper mDbHelper = new DbHelper(getActivity());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                ThreadReaderContract.ThreadEntry._ID,
                ThreadReaderContract.ThreadEntry.COLUMN_NAME_THREAD_ID,
                ThreadReaderContract.ThreadEntry.COLUMN_NAME_TEXT,
                ThreadReaderContract.ThreadEntry.COLUMN_NAME_MESSAGE_DATE,
                ThreadReaderContract.ThreadEntry.COLUMN_NAME_USER_PHOTO,
                ThreadReaderContract.ThreadEntry.COLUMN_NAME_USERNAME,
                ThreadReaderContract.ThreadEntry.COLUMN_NAME_POSTER_ID,
                ThreadReaderContract.ThreadEntry.COLUMN_NAME_READ,
        };

        Cursor c = db.query(
                ThreadReaderContract.ThreadEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        if (c.moveToFirst()) {
            do {
                mAdapter.addThread(new Thread(c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4),
                        c.getString(5),
                        c.getString(6),
                        c.getInt(7) == 1 ? true : false));
            } while (c.moveToNext());
        }
    }

    private void saveRecentThreads(ArrayList<Thread> threads) {
        int offset = 0;
        if (threads.size() > MAX_RECENT_THREADS)
            offset = threads.size() - MAX_RECENT_THREADS - 1;

        DbHelper mDbHelper = new DbHelper(getActivity());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.delete(ThreadReaderContract.ThreadEntry.TABLE_NAME, null, null);

        for (int i = offset; i < threads.size(); i++) {
            Thread c = threads.get(i);
            insertThreadToDatabase(db,
                    c.getId(),
                    c.getText(),
                    c.getMessageDate(),
                    c.getUserPhoto(),
                    c.getUsername(),
                    c.getPosterId(),
                    c.isRead() ? 1 : 0);
        }
    }

    private void insertThreadToDatabase(SQLiteDatabase db,
                                        String id,
                                        String text,
                                        String messageDate,
                                        String userPhoto,
                                        String username,
                                        String posterId,
                                        int read) {
        ContentValues values = new ContentValues();
        values.put(ThreadReaderContract.ThreadEntry.COLUMN_NAME_THREAD_ID, id);
        values.put(ThreadReaderContract.ThreadEntry.COLUMN_NAME_TEXT, text);
        values.put(ThreadReaderContract.ThreadEntry.COLUMN_NAME_MESSAGE_DATE, messageDate);
        values.put(ThreadReaderContract.ThreadEntry.COLUMN_NAME_USER_PHOTO, userPhoto);
        values.put(ThreadReaderContract.ThreadEntry.COLUMN_NAME_USERNAME, username);
        values.put(ThreadReaderContract.ThreadEntry.COLUMN_NAME_POSTER_ID, posterId);
        values.put(ThreadReaderContract.ThreadEntry.COLUMN_NAME_READ, read);

        db.insert(
                ThreadReaderContract.ThreadEntry.TABLE_NAME,
                null,
                values);
    }
}
