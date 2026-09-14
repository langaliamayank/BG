package com.androidtv.bhagavadgita.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.androidtv.bhagavadgita.CalendarActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.adapter.CalendarAdapter;
import com.androidtv.bhagavadgita.adapter.CalendarPagerAdapter;
import com.androidtv.bhagavadgita.calendar.CalendarUtils;
import com.androidtv.bhagavadgita.calendar.Language;
import com.androidtv.bhagavadgita.calendar.PanchangCalculator;
import com.androidtv.bhagavadgita.comman.SpaceItemDecoration;
import com.androidtv.bhagavadgita.model.DayModel;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class CalendarMonthFragment extends Fragment {

    private static final String ARG_YEAR = "arg_year";
    private static final String ARG_MONTH = "arg_month";
    private static final String ARG_LANG = "arg_lang";

    private RecyclerView recyclerView;

    public static CalendarMonthFragment newInstance(YearMonth ym, Language lang) {
        CalendarMonthFragment f = new CalendarMonthFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_YEAR, ym.getYear());
        b.putInt(ARG_MONTH, ym.getMonthValue());
        b.putString(ARG_LANG, lang.name());
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar_month, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        YearMonth yearMonth = YearMonth.of(args.getInt(ARG_YEAR), args.getInt(ARG_MONTH));
        Language lang = Language.valueOf(args.getString(ARG_LANG));

        recyclerView = view.findViewById(R.id.recyclerViewDays);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        recyclerView.addItemDecoration(new SpaceItemDecoration());

        // Enable seamless focus delegation
        recyclerView.setFocusable(false);
        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        recyclerView.setClipChildren(false);
        recyclerView.setClipToPadding(false);
        recyclerView.setPreserveFocusAfterLayout(true);

        CalendarAdapter calendarAdapter =
                new CalendarAdapter(CalendarUtils.generateCalendarDays(yearMonth, lang), CalendarMonthFragment.this);
        recyclerView.setAdapter(calendarAdapter);

        calendarAdapter.setOnPageChangeListener(new CalendarAdapter.OnPageChangeListener() {
            @Override
            public void onNextPageRequested(int focusedRow) {
                goToPage(focusedRow, +1);
            }

            @Override
            public void onPrevPageRequested(int focusedRow) {
                goToPage(focusedRow, -1);
            }
        });

        // Request focus on the 1st day of the month
        recyclerView.post(this::requestFocusOnCurrentDay);
    }

    private void goToPage(int focusedRow, int direction) {
        ViewPager2 pager = requireActivity().findViewById(R.id.viewPagerCalendar);
        if (pager != null) {
            // MUST BE FALSE for Android TV to prevent focus drop during animation
            pager.setCurrentItem(pager.getCurrentItem() + direction, false);
        }
    }

    public void requestFocusOnCurrentDay() {
        if (recyclerView == null) return;

        CalendarAdapter adapter = (CalendarAdapter) recyclerView.getAdapter();
        if (adapter == null) return;

        List<DayModel> days = adapter.getDaysList();
        if (days == null || days.isEmpty()) return;

        LocalDate today = LocalDate.now();
        int targetPosition = -1;

        // 1. Try finding today's date in the active month
        for (int i = 0; i < days.size(); i++) {
            DayModel day = days.get(i);
            if (day != null && day.getDate() != null && day.isCurrentMonth()) {
                if (day.getDate().equals(today)) {
                    targetPosition = i;
                    break;
                }
            }
        }

        // 2. Fallback: If this page isn't the current month/year, focus the 1st day
        if (targetPosition == -1) {
            targetPosition = adapter.getFirstDayPosition();
            if (targetPosition == -1) {
                for (int i = 0; i < days.size(); i++) {
                    if (days.get(i) != null && days.get(i).isCurrentMonth()) {
                        targetPosition = i;
                        break;
                    }
                }
            }
        }

        if (targetPosition < 0) return;

        final int finalTargetPos = targetPosition;

        RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(finalTargetPos);
        if (vh != null) {
            focusSelectorOrItem(vh.itemView);
            return;
        }

        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View childView) {
                if (recyclerView.getChildAdapterPosition(childView) == finalTargetPos) {
                    childView.post(() -> focusSelectorOrItem(childView));
                    recyclerView.removeOnChildAttachStateChangeListener(this);
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View childView) { }
        });

        recyclerView.scrollToPosition(finalTargetPos);
    }

//    public void requestFocusOnFirstDay() {
//        if (recyclerView == null) return;
//
//        CalendarAdapter adapter = (CalendarAdapter) recyclerView.getAdapter();
//        if (adapter == null) return;
//
//        int targetPosition = adapter.getFirstDayPosition();
//        if (targetPosition == -1) {
//            // Fallback: locate the first day belonging to the current month
//            List<DayModel> days = adapter.getDaysList();
//            for (int i = 0; i < days.size(); i++) {
//                if (days.get(i) != null && days.get(i).isCurrentMonth()) {
//                    targetPosition = i;
//                    break;
//                }
//            }
//        }
//
//        if (targetPosition < 0) return;
//
//        final int finalTargetPos = targetPosition;
//
//        RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(finalTargetPos);
//        if (vh != null) {
//            focusSelectorOrItem(vh.itemView);
//            return;
//        }
//
//        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
//            @Override
//            public void onChildViewAttachedToWindow(@NonNull View childView) {
//                if (recyclerView.getChildAdapterPosition(childView) == finalTargetPos) {
//                    childView.post(() -> focusSelectorOrItem(childView));
//                    recyclerView.removeOnChildAttachStateChangeListener(this);
//                }
//            }
//
//            @Override
//            public void onChildViewDetachedFromWindow(@NonNull View childView) { }
//        });
//
//        recyclerView.scrollToPosition(finalTargetPos);
//    }

    private void focusSelectorOrItem(View root) {
        View selector = root.findViewById(R.id.selector);
        if (selector != null && selector.isFocusable()) {
            selector.requestFocus();
        } else {
            root.requestFocus();
        }
    }
}