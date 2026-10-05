package com.androidtv.bhagavadgita.adapter;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.androidtv.bhagavadgita.calendar.Language;
import com.androidtv.bhagavadgita.fragment.CalendarMonthFragment;

import java.lang.ref.WeakReference;
import java.time.YearMonth;
import java.util.List;

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter; // MUST BE THIS IMPORT

import com.androidtv.bhagavadgita.calendar.Language;
import com.androidtv.bhagavadgita.fragment.CalendarMonthFragment;

import java.lang.ref.WeakReference;
import java.time.YearMonth;

public class CalendarPagerAdapter extends FragmentStateAdapter {

    public static final int CENTER_POSITION = 600;
    public static final int TOTAL_PAGES = 12;
    private final YearMonth baseMonth;
    private final int lastPosition;

    private final SparseArray<WeakReference<CalendarMonthFragment>> fragments = new SparseArray<>();

    public CalendarPagerAdapter(@NonNull FragmentActivity fa, YearMonth baseMonth) {
        super(fa);
        this.baseMonth = baseMonth;

        int monthsUntilDec = TOTAL_PAGES - baseMonth.getMonthValue();
        this.lastPosition = CENTER_POSITION + monthsUntilDec;
    }

    public int getFirstPosition() {
        return CENTER_POSITION - (baseMonth.getMonthValue() - 1);
    }

    public int getLastPosition() {
        return lastPosition;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        YearMonth month = baseMonth.plusMonths(position - CENTER_POSITION);
        CalendarMonthFragment fragment = CalendarMonthFragment.newInstance(month);
        fragments.put(position, new WeakReference<>(fragment));
        return fragment;
    }

    @Override
    public int getItemCount() {
        return lastPosition + 1; // pages 0..lastPosition
    }
}