package com.indieweb.indigenous.timeline;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.post.LikeActivity;
import com.indieweb.indigenous.post.ReplyActivity;

import at.blogc.android.views.ExpandableTextView;

import java.util.List;

/**
 * Timeline items list adapter.
 */
public class TimelineListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<TimelineItem> items;
    private LayoutInflater mInflater;

    public TimelineListAdapter(Context context, List<TimelineItem> items) {
        this.context = context;
        this.items = items;
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return items.size();
    }

    public TimelineItem getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void onClick(View view) {}

    public static class ViewHolder {
        public TextView author;
        public ImageView authorPhoto;
        public TextView name;
        public Button expand;
        public ExpandableTextView content;
        public ImageView image;
        public CardView card;
        public LinearLayout row;
        public Button reply;
        public Button like;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.timeline_list_item, null);
            holder = new ViewHolder();
            holder.author = convertView.findViewById(R.id.timeline_author);
            holder.authorPhoto = convertView.findViewById(R.id.timeline_author_photo);
            holder.name = convertView.findViewById(R.id.timeline_name);
            holder.content = convertView.findViewById(R.id.timeline_content);
            holder.expand = convertView.findViewById(R.id.timeline_content_more);
            holder.image = convertView.findViewById(R.id.timeline_image);
            holder.card = convertView.findViewById(R.id.timeline_card);
            holder.row = convertView.findViewById(R.id.timeline_item_row);
            holder.reply = convertView.findViewById(R.id.itemReply);
            holder.like = convertView.findViewById(R.id.itemLike);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        final TimelineItem item = items.get(position);
        if (item != null) {

            String color = ((position % 2) == 0) ? "#f8f7f1" :  "#ffffff";
            holder.row.setBackgroundColor(Color.parseColor(color));

            // Author.
            if (item.getAuthorName().length() > 0) {
                holder.author.setVisibility(View.VISIBLE);
                holder.author.setText(item.getAuthorName());
            }
            else {
                holder.author.setVisibility(View.GONE);
            }

            // Author photo.
            Glide.with(context)
                    .load(item.getAuthorPhoto())
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.avatar_small))
                    .into(holder.authorPhoto);

            // Name.
            if (item.getName().length() > 0) {
                holder.name.setVisibility(View.VISIBLE);
                holder.name.setText(item.getName());
            }
            else {
                holder.name.setVisibility(View.GONE);
            }

            // Content.
            if (item.getHtmlContent().length() > 0 || item.getTextContent().length() > 0) {

                holder.content.setVisibility(View.VISIBLE);

                if (item.getHtmlContent().length() > 0) {
                    String html = item.getHtmlContent();
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        holder.content.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
                    }
                    else {
                        holder.content.setText(Html.fromHtml(html));
                    }
                }
                else {
                    holder.content.setText(item.getTextContent());
                }

                if (item.getTextContent().length() > 400) {
                    holder.expand.setVisibility(View.VISIBLE);
                    holder.expand.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(final View v) {
                            if (holder.content.isExpanded()) {
                                holder.content.collapse();
                                holder.expand.setText(R.string.read_more);
                            }
                            else {
                                holder.content.expand();
                                holder.expand.setText(R.string.close);
                            }
                        }
                    });

                }
                else {
                    holder.expand.setVisibility(View.GONE);
                }
            }
            else {
                holder.content.setVisibility(View.GONE);
                holder.expand.setVisibility(View.GONE);
            }

            // Image.
            if (item.getPhoto().length() > 0) {
                Glide.with(context)
                        .load(item.getPhoto())
                        .into(holder.image);
                holder.image.setVisibility(View.VISIBLE);
                holder.card.setVisibility(View.VISIBLE);
                holder.image.setOnClickListener(new OnImageClickListener(position));
            }
            else {
                holder.image.setVisibility(View.GONE);
                holder.card.setVisibility(View.GONE);
            }

            holder.reply.setOnClickListener(new OnReplyClickListener(position));
            holder.like.setOnClickListener(new OnLikeClickListener(position));
        }

        return convertView;
    }

    class OnReplyClickListener implements OnClickListener {

        int position;

        OnReplyClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, ReplyActivity.class);
            TimelineItem item = items.get(this.position);
            i.putExtra("incomingText", item.getUrl());
            i.putExtra("fromTimeline", true);
            context.startActivity(i);
        }

    }

    class OnLikeClickListener implements OnClickListener {

        int position;

        OnLikeClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, LikeActivity.class);
            TimelineItem item = items.get(this.position);
            i.putExtra("incomingText", item.getUrl());
            i.putExtra("fromTimeline", true);
            context.startActivity(i);
        }

    }

    class OnImageClickListener implements OnClickListener {

        int position;

        OnImageClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, TimelineImageActivity.class);
            TimelineItem item = items.get(this.position);
            i.putExtra("imageUrl", item.getPhoto());
            context.startActivity(i);
        }

    }
}