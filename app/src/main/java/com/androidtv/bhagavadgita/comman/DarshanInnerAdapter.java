package com.androidtv.bhagavadgita.comman;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.model.DarshanModel;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class DarshanInnerAdapter extends RecyclerView.Adapter<DarshanInnerAdapter.ViewHolder> {

    private final List<DarshanModel> mDarshanList;
    private DarshanModel mDarshanModel;
    private Context mContext;

    public DarshanInnerAdapter(Context context, List<DarshanModel> darshanList, DarshanModel dm) {
        this.mContext = context;
        this.mDarshanList = darshanList;
        this.mDarshanModel = dm;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_timetable_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DarshanModel model = mDarshanList.get(position);

        String status = model.getStatus();
        int colorRes;

        if (model != null && model.equals(mDarshanModel)) {
            // 1. Prioritize currently focused/selected item on TV UI
            colorRes = R.color.colorFocus;

        } else if (model != null && model.isOpen()) {
            // 2. Active Darshan ("Open (X Min Left)")
            colorRes = R.color.colorFocus; // Or dedicated active green/gold accent

        } else if (model != null && model.isUpcoming()) {
            // 3. Upcoming Darshan awaiting start
            colorRes = R.color.colorTextPrimary;

        } else if (model != null && (model.isClosed() || "Darshan closed".equalsIgnoreCase(status))) {
            // 4. Closed Darshan (Matches both "Closed" and legacy "Darshan closed")
            colorRes = R.color.colorTextSecondary;

        } else {
            // 5. N/A, invalid time, or missing status
            colorRes = R.color.colorTextSecondary;
        }

        // Resolve the color once
        int resolvedColor = ContextCompat.getColor(mContext, colorRes);

        // Apply color and typeface in a single loop
        TextView[] textViews = {
                holder.textDarshanName,
                holder.textDarshanTime,
                holder.textDarshanStatus
        };

        for (TextView tv : textViews) {
            tv.setTextColor(resolvedColor);
        }

        holder.textDarshanName.setText(model.getTitle() + "/" + model.getTitleHi());
        holder.textDarshanTime.setText(model.getStartTime().equalsIgnoreCase("") ? "-" : model.getStartTime() + " to " + model.getEndTime());
        holder.textDarshanStatus.setText(model.getStatus());
    }

    @Override
    public int getItemCount() {
        return mDarshanList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDarshanName, textDarshanTime, textDarshanStatus;

        ViewHolder(View itemView) {
            super(itemView);
            textDarshanName = itemView.findViewById(R.id.textDarshanName);
            textDarshanTime = itemView.findViewById(R.id.textDarshanTime);
            textDarshanStatus = itemView.findViewById(R.id.textDarshanStatus);
        }
    }
}