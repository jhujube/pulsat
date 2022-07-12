package com.example.pulsat.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulsat.R;
import com.example.pulsat.event.DateTouchStateEvent;

import org.greenrobot.eventbus.EventBus;

public class ViewHolderHorizontal  extends RecyclerView.ViewHolder{

    private final TextView date;
    private final RecyclerView mRecyclerView;
    private final static String TAG = "VIEWHOLDERHORIZONTAL";

    @SuppressLint("ClickableViewAccessibility")
    public ViewHolderHorizontal(@NonNull View itemView) {
        super(itemView);
        date = itemView.findViewById(R.id.date);
        mRecyclerView = itemView.findViewById(R.id.recyclerVertical);
        date.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:           //appui sur date
                    EventBus.getDefault().post(new DateTouchStateEvent(true));
                    break;
                case MotionEvent.ACTION_UP:             //relache date
                    EventBus.getDefault().post(new DateTouchStateEvent(false));
                    break;
            }
            return true;
        });
    }
    public TextView getDate(){return date;}
    public RecyclerView getChildRecyclerview(){return mRecyclerView;}

}
