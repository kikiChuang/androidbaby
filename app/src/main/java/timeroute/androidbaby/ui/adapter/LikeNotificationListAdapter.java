package timeroute.androidbaby.ui.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import timeroute.androidbaby.R;
import timeroute.androidbaby.bean.feed.Like;
import timeroute.androidbaby.bean.feed.LikeTimeLine;
import timeroute.androidbaby.ui.view.RecyclerViewClickListener;
import timeroute.androidbaby.util.RoundTransform;
import timeroute.androidbaby.util.ScreenUtil;

/**
 * Created by chinesejar on 17-8-8.
 */

public class LikeNotificationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "LikeNotificationListAdapter";
    private Context context;
    private RecyclerViewClickListener listener;
    private LikeTimeLine likeTimeLine;
    private int status = 1;

    public static final int LOAD_MORE = 0;
    public static final int LOAD_PULL_TO = 1;
    public static final int LOAD_NONE = 2;
    public static final int LOAD_END = 3;
    private static final int TYPE_FOOTER = -1;

    public LikeNotificationListAdapter(Context context, LikeTimeLine likeTimeLine, RecyclerViewClickListener listener) {
        this.context = context;
        this.likeTimeLine = likeTimeLine;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (likeTimeLine.getLikes() != null) {
            if (position + 1 == getItemCount()) {
                return TYPE_FOOTER;
            } else {
                return position;
            }
        } else if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return position;
        }
    }

    public void updateLoadStatus(int status) {
        this.status = status;
        notifyDataSetChanged();
    }

    class LikeViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.linearLayoutLikeComment)
        LinearLayout linearLayoutLikeComment;
        @Bind(R.id.avatar)
        ImageView avatar;
        @Bind(R.id.nickname)
        TextView nickname;
        @Bind(R.id.create_time)
        TextView create_time;

        public LikeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            ScreenUtil screenUtil = ScreenUtil.instance(context);
            int screenWidth = screenUtil.getScreenWidth();
            linearLayoutLikeComment.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        public void bindItem(Like like) {
            loadCirclePic(context, like.getCreator().getAvatar(), avatar);
            avatar.setOnClickListener(view -> {

            });
            nickname.setText(String.format(context.getResources().getString(R.string.like_notification), like.getCreator().getNickname()));
            create_time.setText(String.valueOf(like.getCreate_time()));
            linearLayoutLikeComment.setOnClickListener(view -> {
                listener.onCardViewClick(like.getFeed_id());
            });
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_load_prompt)
        TextView tv_load_prompt;
        @Bind(R.id.progress)
        ProgressBar progress;

        public FooterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtil.instance(context).dip2px(40));
            itemView.setLayoutParams(params);
        }

        private void bindItem() {
            switch (status) {
                case LOAD_MORE:
                    progress.setVisibility(View.VISIBLE);
                    tv_load_prompt.setText(context.getString(R.string.loading));
                    itemView.setVisibility(View.VISIBLE);
                    break;
                case LOAD_PULL_TO:
                    progress.setVisibility(View.GONE);
                    tv_load_prompt.setText(context.getString(R.string.load_more));
                    itemView.setVisibility(View.VISIBLE);
                    break;
                case LOAD_NONE:
                    progress.setVisibility(View.GONE);
                    tv_load_prompt.setText(context.getString(R.string.load_none));
                    break;
                case LOAD_END:
                    itemView.setVisibility(View.GONE);
                default:
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        // TODO Auto-generated method stub
        return likeTimeLine == null ? 0 : likeTimeLine.getLikes().size() + 1;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View view = View.inflate(parent.getContext(), R.layout.activity_view_footer, null);
            return new FooterViewHolder(view);
        } else {
            View view = View.inflate(parent.getContext(), R.layout.layout_notification_like_comment, null);
            return new LikeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LikeViewHolder) {
            LikeViewHolder likeViewHolder = (LikeViewHolder) holder;
            likeViewHolder.bindItem(likeTimeLine.getLikes().get(position));
        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            footerViewHolder.bindItem();
        }
    }

    public static void loadCirclePic(final Context context, String url, ImageView imageView) {
        if(url != null){
            Picasso.with(context)
                    .load(url)
                    .placeholder(R.drawable.ic_android)
                    .transform(new RoundTransform())
                    .into(imageView);
        }

    }
}
