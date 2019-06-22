package com.dev.pranay.friendlychat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MesssageAdapter extends ArrayAdapter<Message> {

    private Context mContext;
    private List<Message> mMessageList;
    private int mResource;

    // View lookup cache
    private static class ViewHolder {
        TextView text;
        TextView name;
        ImageView image;
    }

    public MesssageAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.mMessageList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Message message = mMessageList.get(position);
        View view = convertView;
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (view == null){
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            viewHolder.image = view.findViewById(R.id.ivImageMessage);
            viewHolder.name = view.findViewById(R.id.tvMessageAuthor);
            viewHolder.text = view.findViewById(R.id.tvTextMessage);
            // Cache the viewHolder object inside the fresh view
            view.setTag(viewHolder);
        }else{
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) view.getTag();
        }

        if(message.getImageUrl() != null){
            viewHolder.image.setVisibility(View.VISIBLE);
            viewHolder.text.setVisibility(View.GONE);
            Glide.with(viewHolder.image.getContext())
                    .load(message.getImageUrl())
                    .into(viewHolder.image);
        }else{
            viewHolder.image.setVisibility(View.GONE);
            viewHolder.text.setVisibility(View.VISIBLE);
            viewHolder.text.setText(message.getText());
        }
        //viewHolder.image.setImageBitmap(message.getImageUrl());
        viewHolder.name.setText(message.getUser());

        return view;
    }
}
