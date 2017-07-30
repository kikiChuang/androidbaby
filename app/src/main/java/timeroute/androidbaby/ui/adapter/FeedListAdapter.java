package timeroute.androidbaby.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import timeroute.androidbaby.R;
import timeroute.androidbaby.bean.feed.Feed;
import timeroute.androidbaby.bean.feed.FeedTimeLine;
import timeroute.androidbaby.ui.activity.FeedDetailActivity;
import timeroute.androidbaby.ui.activity.ImageViewActivity;
import timeroute.androidbaby.ui.view.RecyclerViewClickListener;
import timeroute.androidbaby.util.RoundTransform;
import timeroute.androidbaby.util.ScreenUtil;
import timeroute.androidbaby.widget.HorizontalListView;

/**
 * Created by chinesejar on 17-7-8.
 */

public class FeedListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "FeedListAdapter";
    private RecyclerViewClickListener listener;
    private Context context;
    private FeedTimeLine feedTimeLine;
    private int status = 1;

    private List<Map<String, Object>> list;
    private FeedPicAdapter feedPicAdapter;

    public static final int LOAD_MORE = 0;
    public static final int LOAD_PULL_TO = 1;
    public static final int LOAD_NONE = 2;
    public static final int LOAD_END = 3;
    private static final int TYPE_FOOTER = -1;

    private float downX;
    private float downY;
    private float upX;
    private float upY;
    private boolean isUpOrDown = true;

    public FeedListAdapter(Context context, FeedTimeLine feedTimeLine, RecyclerViewClickListener listener) {
        this.listener = listener;
        this.context = context;
        this.feedTimeLine = feedTimeLine;
    }

    @Override
    public int getItemViewType(int position) {
        if (feedTimeLine.getFeeds() != null) {
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

    public void updateLikeStatus(Feed feed) {
        List<Feed> feeds = feedTimeLine.getFeeds();
        int position = feeds.indexOf(feed);
        feeds.get(position).setLike_count(feed.getLikeCount()+1);
        notifyDataSetChanged();
    }

    class FeedViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.card_feeds)
        CardView cardView;
        @Bind(R.id.avatar)
        ImageView avatar;
        @Bind(R.id.username)
        TextView username;
        @Bind(R.id.content)
        TextView content;
        @Bind(R.id.feed_pic)
        HorizontalListView horizontalListViewFeedPic;
        @Bind(R.id.like)
        TextView like;
        @Bind(R.id.imageButtonLike)
        ImageButton imageButtonLike;
        @Bind(R.id.comment)
        TextView comment;
        @Bind(R.id.imageButtonComment)
        ImageButton imageButtonComment;
        @Bind(R.id.create_time)
        TextView create_time;

        public FeedViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            ScreenUtil screenUtil = ScreenUtil.instance(context);
            int screenWidth = screenUtil.getScreenWidth();
            cardView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

        }

        public void bindItem(Feed feed) {
            cardView.setOnClickListener(view -> {
                Log.d(TAG, "card click "+feed.getFeedId());
                listener.onCardViewClick(feed);
            });
            loadCirclePic(context, feed.getUser().getAvatar(), avatar);
            avatar.setOnClickListener(view -> {
                if(listener != null){
                    listener.onAvatarClicked(feed.getUser().getId(), feed.getUser().getNickname(), feed.getUser().getAssignment(), feed.getUser().getAvatar());
                }
            });
            username.setText(feed.getUser().getNickname());
            content.setText(feed.getContent());
            if (feed.getFeedPic().size() > 0) {
                horizontalListViewFeedPic.setVisibility(View.VISIBLE);
                list = new ArrayList<Map<String, Object>>();
                String[] images = new String[feed.getFeedPic().size()];
                for (int i = 0; i < feed.getFeedPic().size(); i++) {
                    images[i] = feed.getFeedPic().get(i).getUrl();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("url", feed.getFeedPic().get(i).getUrl());
                    list.add(map);
                }
                feedPicAdapter = new FeedPicAdapter(context,
                        list,
                        R.layout.layout_feed_pic,
                        new String[]{"url"},
                        new int[]{R.id.image_view_pic});
                horizontalListViewFeedPic.setAdapter(feedPicAdapter);
                horizontalListViewFeedPic.setOnTouchListener((view, motionEvent) -> {
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
                    int action = motionEvent.getAction();
                    switch (action){
                        case MotionEvent.ACTION_DOWN:
                            downX = x;
                            downY = y;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            upX = x;
                            upY = y;
                            if(isUpOrDown){
                                if(Math.abs(upX-downX)>8&&Math.abs(upY-downY)>8){
                                    if(Math.abs(upX-downX)>Math.abs(upY-downY)){
                                        view.getParent().requestDisallowInterceptTouchEvent(true);
                                        isUpOrDown = false;
                                    }
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            isUpOrDown = true;
                            break;
                    }
                    return false;
                });
                horizontalListViewFeedPic.setOnItemClickListener((adapterView, view, i, l) -> {
                    listener.onImageViewClick(i, images);
                });
            }
            like.setText(String.valueOf(feed.getLikeCount()));
            imageButtonLike.setOnClickListener(view -> {
                listener.onLikeClicked(feed);
            });
            comment.setText(String.valueOf(feed.getCommentCount()));
            imageButtonComment.setOnClickListener(view -> {
                listener.onCommentClicked(feed);
            });
            create_time.setText(String.valueOf(feed.getCreate_time()));
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
                    tv_load_prompt.setText("正在加载...");
                    itemView.setVisibility(View.VISIBLE);
                    break;
                case LOAD_PULL_TO:
                    progress.setVisibility(View.GONE);
                    tv_load_prompt.setText("上拉加载更多");
                    itemView.setVisibility(View.VISIBLE);
                    break;
                case LOAD_NONE:
                    System.out.println("LOAD_NONE----");
                    progress.setVisibility(View.GONE);
                    tv_load_prompt.setText("已无更多加载");
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
        return feedTimeLine == null ? 0 : feedTimeLine.getFeeds().size() + 1;
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
            View view = View.inflate(parent.getContext(), R.layout.layout_feed, null);
            return new FeedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FeedViewHolder) {
            FeedViewHolder feedHolder = (FeedViewHolder) holder;
            feedHolder.bindItem(feedTimeLine.getFeeds().get(position));
        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            footerViewHolder.bindItem();
        }
    }

    public static void loadCirclePic(final Context context, String url, ImageView imageView) {
        if(url != null){
            Picasso.with(context)
                    .load(url)
                    .placeholder(R.drawable.avatar)
                    .transform(new RoundTransform())
                    .into(imageView);
        }

    }
}
