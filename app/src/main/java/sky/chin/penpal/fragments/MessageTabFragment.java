package sky.chin.penpal.fragments;

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
import sky.chin.penpal.adapters.ChatAdapter;
import sky.chin.penpal.models.Chat;
import sky.chin.penpal.server.Server;
import sky.chin.penpal.server.interfaces.ServerResponseListener;
import sky.chin.penpal.server.requests.AllMessagesRequest;
import sky.chin.penpal.utils.AuthManager;
import sky.chin.penpal.widgets.EndlessRecyclerOnScrollListener;

public class MessageTabFragment extends Fragment{

    private static final String LOG = MessageTabFragment.class.getSimpleName();

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_message_tab, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                findChats(1);
            }
        });

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                findChats(current_page);
            }
        };
        mRecyclerView.addOnScrollListener(mRecyclerOnScrollListener);

        mAdapter = new ChatAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        findChats(1);

        return v;
    }

    final private int LIMIT = 10;

    private void findChats(int page) {
        mSwipeRefreshLayout.setRefreshing(true);

        AuthManager authManager = AuthManager.getInstance(getActivity());

        String skip = ((page-1) * page) + "";

        Server.getInstance(getActivity()).sendRequest(
                new AllMessagesRequest.Builder()
                        .userId(authManager.getUserId())
                        .userPassword(authManager.getUserPassword())
                        .limit(LIMIT+"")
                        .skip(skip)
                        .build(),
                new ServerResponseListener() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            JSONArray message = data.getJSONArray("message")
                                    .getJSONArray(0);

                            for (int k = 0; k < message.length(); k++) {
                                JSONArray item = message.getJSONArray(k);
                                mAdapter.addChat(new Chat(item.getString(1),
                                        item.getString(0),
                                        item.getString(2),
                                        item.getString(4)));

                                mAdapter.addChat(new Chat(item.getString(1),
                                        item.getString(0),
                                        item.getString(2),
                                        item.getString(4)));

                                mAdapter.addChat(new Chat(item.getString(1),
                                        item.getString(0),
                                        item.getString(2),
                                        item.getString(4)));
                                mAdapter.addChat(new Chat(item.getString(1),
                                        item.getString(0),
                                        item.getString(2),
                                        item.getString(4)));

                                mAdapter.addChat(new Chat(item.getString(1),
                                        item.getString(0),
                                        item.getString(2),
                                        item.getString(4)));

                                mAdapter.addChat(new Chat(item.getString(1),
                                        item.getString(0),
                                        item.getString(2),
                                        item.getString(4)));
                            }

                            mAdapter.notifyDataSetChanged();

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
                        // TODO show error message
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
    }
}
