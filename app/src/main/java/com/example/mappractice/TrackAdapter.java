package com.example.mappractice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class TrackAdapter extends ArrayAdapter<TrackMeta> {
    //这个类是对轨迹列表的适配器
    //它负责将轨迹元数据转换为可显示的列表项

    public TrackAdapter(Context context, List<TrackMeta> tracks) {
        super(context, 0, tracks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //获取当前轨迹元数据
        TrackMeta track = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.track_item, parent, false);
        }
        //设置轨迹信息到视图中
        TextView filePathTextView = convertView.findViewById(R.id.text_file_path);
        TextView startTimeTextView = convertView.findViewById(R.id.text_start_time);
        TextView endTimeTextView = convertView.findViewById(R.id.text_end_time);

        filePathTextView.setText(track.getFilePath());
        startTimeTextView.setText(String.valueOf(track.getStartTime()));
        endTimeTextView.setText(String.valueOf(track.getEndTime()));

        return convertView;
    }
}
