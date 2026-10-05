package com.androidtv.bhagavadgita.adapter;

import static com.androidtv.bhagavadgita.CalendarActivity.verticalGridView;
import static com.androidtv.bhagavadgita.CalendarActivity.viewPagerCalendar;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.androidtv.bhagavadgita.CalendarActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.calendar.CalendarUtils;
import com.androidtv.bhagavadgita.calendar.Language;
import com.androidtv.bhagavadgita.calendar.PanchangCalculator;
import com.androidtv.bhagavadgita.comman.ColorUtils;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.fragment.CalendarMonthFragment;
import com.androidtv.bhagavadgita.model.DayModel;
import com.androidtv.bhagavadgita.model.FestivalModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class UpcomingUtsavAdapter extends RecyclerView.Adapter<UpcomingUtsavAdapter.UtsavViewHolder> {

    private List<FestivalModel> mList;
    private CalendarActivity mContext;

    public UpcomingUtsavAdapter(CalendarActivity context, List<FestivalModel> listItems) {
        mContext = context;
        mList = listItems;
    }

    @Override
    public UtsavViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.card_calendar_event_item, viewGroup, false);
        return new UtsavViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UtsavViewHolder viewHolder, @SuppressLint("RecyclerView") int position) {
        FestivalModel festivalModel = mList.get(position);
        viewHolder.textTitle.setText(festivalModel.getTitle());

        String tithi;
        int defaultColor = ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.colorKrushna);

        try {
            LocalDate localDate = LocalDate.parse(festivalModel.getDate()); // expects "yyyy-MM-dd"
            tithi = PanchangCalculator.getPanchang(
                    localDate, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET).getCompactDescription();

            boolean isShuklaPaksh = PanchangCalculator.getPanchang(
                    localDate, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET
            ).isShuklaPaksh();

            defaultColor = ContextCompat.getColor(
                    viewHolder.itemView.getContext(),
                    isShuklaPaksh ? R.color.colorShukla : R.color.colorKrushna
            );

            viewHolder.defaultColor = defaultColor;
            edgeColor(viewHolder, defaultColor);

        } catch (Exception e) {
            tithi = "";
        }

        viewHolder.textTithi.setText(tithi);
        viewHolder.textDescription.setText(festivalModel.getDescription());
        viewHolder.textDate.setText(formatToOrdinalDate(festivalModel.getDate()));

        viewHolder.itemView.setNextFocusLeftId(R.id.selector);
        viewHolder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                int colorToApply = hasFocus
                        ? ContextCompat.getColor(mContext, R.color.colorWhite)
                        : viewHolder.defaultColor;
                edgeColor(viewHolder, colorToApply);

                if (mContext instanceof CalendarActivity) {
                    if (hasFocus) {
                        ((CalendarActivity) mContext).highlightCalendarDate(festivalModel.getDate());
                    } else {
                        ((CalendarActivity) mContext).clearCalendarHighlight();
                    }
                }
            }
        });

        viewHolder.itemView.setOnKeyListener((v, keyCode, event) -> {

            if (event.getAction() == KeyEvent.ACTION_DOWN) {

                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    if (position == getItemCount() - 1 && getItemCount() > 0) {
                        verticalGridView.setSelectedPositionSmooth(0);
                        // Use post to ensure focus shifts after layout passes
                        verticalGridView.post(() -> {
                            RecyclerView.ViewHolder firstVh = verticalGridView.findViewHolderForAdapterPosition(0);
                            if (firstVh != null) {
                                firstVh.itemView.requestFocus();
                            }
                        });
                        return true;
                    }
                }

                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    if (position == 0 && getItemCount() > 0) {
                        verticalGridView.setSelectedPositionSmooth(getItemCount() - 1);
                        // Use post to ensure focus shifts after layout passes
                        verticalGridView.post(() -> {
                            RecyclerView.ViewHolder firstVh = verticalGridView.findViewHolderForAdapterPosition(0);
                            if (firstVh != null) {
                                firstVh.itemView.requestFocus();
                            }
                        });
                        return true;
                    }
                }

                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (mContext instanceof CalendarActivity) {
                        ((CalendarActivity) mContext).focusFirstDayOfCurrentPage();
                        return true;
                    }
                }

                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (viewPagerCalendar != null && viewPagerCalendar.getAdapter() != null) {
                        int currentItem = viewPagerCalendar.getCurrentItem();
                        int totalItems = viewPagerCalendar.getAdapter().getItemCount();
                        if (currentItem + 1 < totalItems) {
                            viewPagerCalendar.setCurrentItem(currentItem + 1, false);
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void edgeColor(UtsavViewHolder holder, @ColorInt int color) {
        LayerDrawable layerDrawable = (LayerDrawable) holder.itemView.getBackground();
        GradientDrawable leftEdge = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.leftEdge);
        leftEdge.mutate();
        leftEdge.setColor(color);

        String rightEdgeColor = SharePreferenceManager.getString("KEY_THEME_COLOR");
        GradientDrawable rightEdge = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.rightEdge);
        rightEdge.mutate();
        rightEdge.setColor(ColorUtils.whiten(Color.parseColor(rightEdgeColor), 0.1f));
    }

    class UtsavViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle, textTithi, textDescription, textDate;
        int defaultColor;

        public UtsavViewHolder(View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textTithi = itemView.findViewById(R.id.textTithi);
            textDescription = itemView.findViewById(R.id.textDescription);
            textDate = itemView.findViewById(R.id.textDate);

            itemView.setBackgroundResource(R.drawable.bg_calendar);
            itemView.setFocusable(true);
        }
    }

    public static String formatToOrdinalDate(String dateString) {
        LocalDate date = LocalDate.parse(dateString);
        int day = date.getDayOfMonth();
        String suffix = getDaySuffix(day);

        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        return day + suffix + "\n" + date.format(monthYearFormatter);
    }

    private static String getDaySuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }
}
