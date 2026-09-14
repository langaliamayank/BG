package com.androidtv.bhagavadgita.comman;

import android.view.View;

public interface NavigableFragment {
    View getmFirstPosterView();
    // Helper to check if we are at the top row
    boolean isAtTopRow();
}