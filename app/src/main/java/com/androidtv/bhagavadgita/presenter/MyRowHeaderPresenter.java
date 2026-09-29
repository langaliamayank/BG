package com.androidtv.bhagavadgita.presenter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.RowHeaderPresenter;

import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;

public class MyRowHeaderPresenter extends RowHeaderPresenter {
    private static final String TAG = MyRowHeaderPresenter.class.getSimpleName();
    private final MasterActivity mCtx;
    private float mUnselectedAlpha;

    public MyRowHeaderPresenter(MasterActivity ctx) {
        this.mCtx = ctx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        mUnselectedAlpha = viewGroup.getResources().getFraction(androidx.leanback.R.fraction.lb_browse_header_unselect_alpha, 1, 1);
        LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row_header_item, null);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, final Object item) {
        View rootView = viewHolder.view;
        final HeaderItem headerItem = ((ListRow) item).getHeaderItem();

        TextView headerTitle = rootView.findViewById(R.id.headerTitle);
        headerTitle.setText(headerItem.getName());

        TextView headerCategory = rootView.findViewById(R.id.headerCategory);
        headerCategory.setText(headerItem.getDescription());
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
    }

    // TODO: This is a temporary fix. Remove me when leanback onCreateViewHolder no longer sets the
    // mUnselectAlpha, and also assumes the xml inflation will return a RowHeaderView.
    @Override
    protected void onSelectLevelChanged(ViewHolder holder) {
        holder.view.setAlpha(mUnselectedAlpha + holder.getSelectLevel() *
                (1.0f - mUnselectedAlpha));
    }
}
