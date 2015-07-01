package sky.chin.penpal.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sky.chin.penpal.R;
import sky.chin.penpal.adapters.ChatAdapter;
import sky.chin.penpal.configs.Url;
import sky.chin.penpal.interfaces.OnRecyclerViewItemClickListener;
import sky.chin.penpal.models.Chat;
import sky.chin.penpal.utils.AuthManager;
import sky.chin.penpal.server.Server;

public class MessageTabFragment extends Fragment implements OnRecyclerViewItemClickListener {

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

        StringRequest jsObjRequest = new StringRequest
                (Request.Method.POST, Url.MESSAGES_ALL, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG, "Response: " + response);

                        try {
                            JSONObject jsonResp = new JSONObject(response);
                            JSONArray dataArray = jsonResp.getJSONArray("data");

                            JSONObject data;
                            for (int i = 0; i < dataArray.length(); i++) {
                                data = dataArray.getJSONObject(i);
                                String code = data.getString("code");
                                if ("0".equals(code)) {
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
                                } else {
                                    // TODO show error message
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG, "Error: " + error.getMessage());
                        // TODO show error message
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                AuthManager authManager = AuthManager.getInstance(getActivity());

                params.put("u_id", authManager.getUserId());
                params.put("u_pass", authManager.getUserPassword());
                params.put("p_chk", "key");

                return params;
            }
        };

        Server.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
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

    public interface OnChatSelectedListener {
        void onChatSelected(String id);
    }
}
