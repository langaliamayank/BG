package com.androidtv.bhagavadgita.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.calendar.CalendarUtils;
import com.androidtv.bhagavadgita.calendar.PanchangCalculator;
import com.androidtv.bhagavadgita.comman.ColorUtils;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.fragment.CalendarMonthFragment;
import com.androidtv.bhagavadgita.model.DayModel;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private CalendarMonthFragment calendarMonthFragment;
    private final List<DayModel> daysList;

    private OnDayClickListener onDayClickListener;

    public interface OnDayClickListener {
        void onDayClick(DayModel day);
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.onDayClickListener = listener;
    }

    public interface OnPageChangeListener {
        void onNextPageRequested(int focusedRow);

        void onPrevPageRequested(int focusedRow);
    }

    private OnPageChangeListener onPageChangeListener;

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.onPageChangeListener = listener;
    }

    public CalendarAdapter(List<DayModel> daysList, CalendarMonthFragment cmf) {
        this.daysList = daysList;
        this.calendarMonthFragment = cmf;
    }

    public void updateDays(List<DayModel> newDays) {
        daysList.clear();
        daysList.addAll(newDays);
        notifyDataSetChanged();
    }

    /**
     * Read-only use only — mutate via updateDays(), not this reference.
     */
    public List<DayModel> getDaysList() {
        return daysList;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_calendar_item, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        DayModel day = daysList.get(position);

        // --- Reset every time: recycled views must not carry over stale state ---
        holder.selector.setOnKeyListener(null);
        holder.selector.setOnClickListener(null);
        holder.imageAP.setImageDrawable(null);
        holder.imageEvent.setImageDrawable(null);

        holder.tvPrimaryDate.setText(day.getPrimaryDate());

        boolean isShuklaPaksh = PanchangCalculator.getPanchang(
                day.getDate(), CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET
        ).isShuklaPaksh();

        int color = ContextCompat.getColor(
                holder.itemView.getContext(),
                isShuklaPaksh ? R.color.colorShukla : R.color.colorKrushna
        );
        edgeColor(holder, color);

        holder.tvDescription.setText(PanchangCalculator.getPanchang(
                day.getDate(), CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET
        ).getTithi());

        // --- Current-month cells: always focusable, so grid nav works everywhere ---
        holder.selector.setFocusable(true);
        holder.selector.setNextFocusRightId(R.id.verticalGridView);

        if (!day.isCurrentMonth()) {
            holder.itemView.setVisibility(View.GONE);
        }

        boolean hasFestival = day.getFestivalTitle() != null && !day.getFestivalTitle().trim().isEmpty();
        if (hasFestival) {
            holder.tvDescription.setText(day.getFestivalTitle().trim());
            holder.imageEvent.setImageResource(R.drawable.bg_calendar_event);
        }

        if (day.isSunday()) {
            holder.tvPrimaryDate.setTextColor(Color.RED);
        }

        if (day.isToday()) {
            todayEdgeColor(holder, ContextCompat.getColor(holder.childLayout.getContext(), R.color.colorFocus));
            holder.tvPrimaryDate.setTextColor(Color.WHITE);
            holder.tvDescription.setTextColor(Color.WHITE);
        }

        if (day.isPunam()) {
            holder.imageAP.setImageResource(R.drawable.ic_action_poonam);

        } else if (day.isAmavas()) {
            holder.imageAP.setImageResource(R.drawable.ic_action_amavasya);

        } else {
            holder.imageAP.setImageDrawable(null);
        }

        holder.selector.setOnClickListener(v -> {
            if (day.getDescription() != null && !day.getDescription().trim().isEmpty()) {
                Toast.makeText(v.getContext(), day.getDescription(), Toast.LENGTH_SHORT).show();
            }
            if (onDayClickListener != null) {
                onDayClickListener.onDayClick(day);
            }
        });

        boolean isLastColumn = (position % 7) == 6;
        boolean isFirstColumn = (position % 7) == 0;
        int row = position / 7;

        if (isLastColumn) {
            holder.selector.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {

                    int nextPos = position + 1;
                    boolean nextCellIsGoneOrEnd = (nextPos >= daysList.size()) || !daysList.get(nextPos).isCurrentMonth();

                    if (isLastColumn || nextCellIsGoneOrEnd) {
                        View verticalGrid = holder.itemView.getRootView().findViewById(R.id.verticalGridView);
                        if (verticalGrid != null) {
                            verticalGrid.requestFocus();
                            return true;
                        }
                    }

                    if (onPageChangeListener != null) {
                        onPageChangeListener.onNextPageRequested(row);
                    }
                    return true; // Consume event immediately
                }
                return false;
            });
        } else if (isFirstColumn) {
            holder.selector.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (onPageChangeListener != null) {
                        onPageChangeListener.onPrevPageRequested(row);
                    }
                    return true; // Consume event immediately
                }
                return false;
            });
        }
    }

    @Override
    public int getItemCount() {
        return daysList != null ? daysList.size() : 0;
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder {
        final TextView tvPrimaryDate, tvDescription;
        final ImageView selector, imageAP, imageEvent;
        final RelativeLayout childLayout;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            selector = itemView.findViewById(R.id.selector);
            childLayout = itemView.findViewById(R.id.childLayout);

            tvPrimaryDate = itemView.findViewById(R.id.tvPrimaryDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);

            imageAP = itemView.findViewById(R.id.imageAP);
            imageEvent = itemView.findViewById(R.id.imageEvent);
        }
    }

    public void edgeColor(CalendarViewHolder holder, @ColorInt int color) {
        LayerDrawable layerDrawable = (LayerDrawable) holder.childLayout.getBackground();
        GradientDrawable leftEdge = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.leftEdge);
        leftEdge.mutate();
        leftEdge.setColor(color);

        String rightEdgeColor = SharePreferenceManager.getString("KEY_THEME_COLOR");
        GradientDrawable rightEdge = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.rightEdge);
        rightEdge.mutate();
        rightEdge.setColor(ColorUtils.darken(Color.parseColor(rightEdgeColor), 0.1f));
    }

    public void todayEdgeColor(CalendarViewHolder holder, @ColorInt int color) {
        LayerDrawable layerDrawable = (LayerDrawable) holder.childLayout.getBackground();
        GradientDrawable leftEdge = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.leftEdge);
        leftEdge.mutate();
        leftEdge.setColor(ColorUtils.lighten(color, 0.1f));

        GradientDrawable rightEdge = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.rightEdge);
        rightEdge.mutate();
        rightEdge.setColor(ColorUtils.lighten(color, 0.5f));
    }

    public int getFirstDayPosition() {
        if (daysList == null) return -1;
        for (int i = 0; i < daysList.size(); i++) {
            DayModel model = daysList.get(i);
            if (model != null && model.isCurrentMonth()) {
                if (model.isFirstDayOfMonth() || "1".equals(model.getPrimaryDate())) {
                    return i;
                }
            }
        }
        return -1;
    }
}