package com.androidtv.bhagavadgita.comman;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.RequiresApi;
import androidx.leanback.widget.HorizontalGridView;
import androidx.recyclerview.widget.RecyclerView;

import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.model.ActionModel;

import java.util.ArrayList;

public class ActionWidgetAdapter extends RecyclerView.Adapter {

    private final ArrayList<ActionModel> mActionsList;
    private Context mCtx;
    private HorizontalGridView mHorizontalGridView;
    public int selectedItem;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ActionWidgetAdapter(Context context, HorizontalGridView inputHorizontalGridView) {
        this(inputHorizontalGridView, new ArrayList<ActionModel>());
        this.mCtx = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ActionWidgetAdapter(HorizontalGridView inputHorizontalGridView,
                               ArrayList<ActionModel> actions) {
        mHorizontalGridView = inputHorizontalGridView;

        mHorizontalGridView.setWindowAlignment(HorizontalGridView.WINDOW_ALIGN_BOTH_EDGE);
//        mHorizontalGridView.setWindowAlignmentOffsetPercent(0f);
//        mHorizontalGridView.setWindowAlignmentOffset(10);
//        mHorizontalGridView.setItemAlignmentOffsetPercent(0f);

        mHorizontalGridView.setItemSpacing(0);
        mHorizontalGridView.setGravity(Gravity.CENTER);
        mHorizontalGridView.setAdapter(this);
        mActionsList = new ArrayList<>();

        if (actions != null) {
            addActions(actions);
        }
    }

    @MainThread
    public void addActions(ArrayList<ActionModel> inputActions) {
        mActionsList.addAll(inputActions);
        notifyDataSetChanged();
    }

    public ArrayList<ActionModel> getActions() {
        return mActionsList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View contactView = LayoutInflater.from(mCtx).inflate(R.layout.card_action_item, parent, false);
        return new ViewHolder(contactView);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder baseHolder, @SuppressLint("RecyclerView") final int position) {

//        final ViewHolder holder = (ViewHolder) baseHolder;
//        final ActionModel action = mActionsList.get(position);
//        holder.actionButton.setText(action.getTitle());
//        holder.actionImage.setImageResource(action.getIcon());
//
//        if (action.isSelected()) {
//            holder.actionButton.setTextColor(mCtx.getResources().getColor(R.color.colorSelected));
//            holder.actionButton.setVisibility(View.VISIBLE);
//        } else {
//            holder.actionButton.setVisibility(View.GONE);
//        }
//
//        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
//            if (hasFocus) {
//                holder.actionButton.setTextColor(mCtx.getResources().getColor(R.color.colorTextPrimary));
//                holder.actionButton.setVisibility(View.VISIBLE);
//
//            } else if (action.isSelected()) {
//                holder.actionButton.setTextColor(mCtx.getResources().getColor(R.color.colorSelected));
//                holder.actionButton.setVisibility(View.VISIBLE);
//
//            } else {
//                holder.actionButton.setVisibility(View.GONE);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mActionsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
//        protected ImageView actionImage;
//        protected TextView actionButton;
//        protected LinearLayout actionLayout;

        public ViewHolder(View itemView) {
            super(itemView);
//            actionImage = itemView.findViewById(R.id.action_icon);
//            actionButton = itemView.findViewById(R.id.action_button);
//            actionLayout = itemView.findViewById(R.id.action_layout);
//
//            itemView.setFocusable(true);
//            itemView.setFocusableInTouchMode(true);
        }
    }
}