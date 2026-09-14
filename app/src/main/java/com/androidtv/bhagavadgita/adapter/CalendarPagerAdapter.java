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
    public static final int TOTAL_PAGES = 1200;

    private final YearMonth baseMonth;
    private final Language language;
    private int pendingFocusRow = 0;

    // Track created fragments so the Activity can grab the CURRENT one on page selection
    // without relying on FragmentManager tag lookups.
    private final SparseArray<WeakReference<CalendarMonthFragment>> fragments = new SparseArray<>();

    public CalendarPagerAdapter(@NonNull FragmentActivity fa, YearMonth baseMonth, Language language) {
        super(fa);
        this.baseMonth = baseMonth;
        this.language = language;
    }

    public void setPendingFocusRow(int row) {
        this.pendingFocusRow = row;
    }

    public int consumePendingFocusRow() {
        int row = pendingFocusRow;
        pendingFocusRow = 0;
        return row;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        YearMonth month = baseMonth.plusMonths(position - CENTER_POSITION);
        CalendarMonthFragment fragment = CalendarMonthFragment.newInstance(month, language);
        fragments.put(position, new WeakReference<>(fragment));
        return fragment;
    }

    @Nullable
    public CalendarMonthFragment getFragment(int position) {
        WeakReference<CalendarMonthFragment> ref = fragments.get(position);
        return ref != null ? ref.get() : null;
    }

    @Override
    public int getItemCount() {
        return TOTAL_PAGES;
    }
}