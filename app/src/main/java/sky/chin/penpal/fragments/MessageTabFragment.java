package sky.chin.penpal.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import sky.chin.penpal.R;
import sky.chin.penpal.adapters.ChatAdapter;
import sky.chin.penpal.interfaces.OnRecyclerViewItemClickListener;
import sky.chin.penpal.models.Chat;
import sky.chin.penpal.server.Server;
import sky.chin.penpal.server.interfaces.ServerResponseListener;
import sky.chin.penpal.server.requests.AllMessagesRequest;
import sky.chin.penpal.utils.AuthManager;

public class MessageTabFragment extends Fragment implements OnRecyclerViewItemClickListener{

    public interface OnChatSelectedListener {
        void onChatSelected(String id);
    }

    private static final String LOG = MessageTabFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<Chat> mChats = new ArrayList<>();
    private OnChatSelectedListener mListener;

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
                findChats();
            }
        });

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ChatAdapter(getActivity(), mChats, this);
        mRecyclerView.setAdapter(mAdapter);

        findChats();

        return v;
    }

    private void findChats() {
        mSwipeRefreshLayout.setRefreshing(true);

        AuthManager authManager = AuthManager.getInstance(getActivity());

        Server.getInstance(getActivity()).sendRequest(
                new AllMessagesRequest.Builder()
                        .userId(authManager.getUserId())
                        .userPassword(authManager.getUserPassword())
                        .build(),
                new ServerResponseListener() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            JSONArray message = data.getJSONArray("message")
                                    .getJSONArray(0);

                            // Clear old records
                            mChats.clear();

                            for (int k = 0; k < message.length(); k++) {
                                JSONArray item = message.getJSONArray(k);
                                mChats.add(new Chat(item.getString(1),
                                        item.getString(0),
                                        item.getString(2),
                                        item.getString(4)));
                            }

                            mAdapter.notifyDataSetChanged();
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

    @Override
    public void onRecyclerViewItemClicked(int position) {
        Toast.makeText(getActivity(), "Clicked " + position, Toast.LENGTH_SHORT).show();
        mListener.onChatSelected(mChats.get(position).getId());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnChatSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnChatSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
