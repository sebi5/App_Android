package sky.chin.penpal.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import sky.chin.penpal.R;
import sky.chin.penpal.configs.Url;
import sky.chin.penpal.interfaces.OnRecyclerViewItemClickListener;
import sky.chin.penpal.models.Chat;
import sky.chin.penpal.server.Server;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private ArrayList<Chat> mDataset;
    private Context mContext;

    private OnRecyclerViewItemClickListener mOnClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public NetworkImageView mProfilePhotos;
        public TextView mTitle;
        public ViewHolder(View v) {
            super(v);
            mProfilePhotos = (NetworkImageView) v.findViewById(R.id.profilePhotos);
            mTitle = (TextView) v.findViewById(R.id.title);
        }
    }

    public ChatAdapter(Context myContext, ArrayList<Chat> myDataset
            , OnRecyclerViewItemClickListener myOnClickListener) {
        mContext = myContext;
        mDataset = myDataset;
        mOnClickListener = myOnClickListener;
    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Chat c = mDataset.get(position);
        holder.mTitle.setText(c.getTitle());

        if (c.hasProfilePhoto())
            holder.mProfilePhotos.setImageUrl(Url.PROFILE_PHOTOS + "/" + c.getProfilePhoto(),
                    Server.getInstance(mContext).getImageLoader());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null)
                    mOnClickListener.onRecyclerViewItemClicked(position);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
