package com.example.android.guardiannews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class NewsAdapter extends ArrayAdapter<News> {

    private static final String TIME_SEPARATOR = "T";

    public NewsAdapter(Context context, List<News> news) {
        super(context, 0, news);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.news_list_item, parent, false);
        }

        News currentNews = getItem(position);

        TextView authorView = (TextView) convertView.findViewById(R.id.section);
        authorView.setText(currentNews.getSection());

        TextView titleView = (TextView) convertView.findViewById(R.id.title);
        titleView.setText(currentNews.getTitle());

        String[] parts = currentNews.getDate().split(TIME_SEPARATOR);
        String date = parts[0];
        String time = parts[1].substring(0,5);
        TextView dateView = (TextView) convertView.findViewById(R.id.date);
        dateView.setText(date);
        TextView timeView = (TextView) convertView.findViewById(R.id.time);
        timeView.setText(time);

        return convertView;
    }


}
