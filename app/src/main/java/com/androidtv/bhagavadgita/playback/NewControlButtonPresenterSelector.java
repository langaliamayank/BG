package com.androidtv.bhagavadgita.playback;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.leanback.widget.Action;
import androidx.leanback.widget.PlaybackControlsRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;

import com.androidtv.bhagavadgita.R;

/**
 * Displays primary and secondary controls for a {@link PlaybackControlsRow}.
 * <p>
 * Binds to items of type {@link Action}.
 */
public class NewControlButtonPresenterSelector extends PresenterSelector {

    private final Presenter mPrimaryPresenter =
            new ControlButtonPresenter(R.layout.lb_control_button_primary);
    private final Presenter mSecondaryPresenter =
            new ControlButtonPresenter(androidx.leanback.R.layout.lb_control_button_secondary);
    private final Presenter[] mPresenters = new Presenter[]{mPrimaryPresenter};

    /**
     * Returns the presenter for primary controls.
     */
    public Presenter getPrimaryPresenter() {
        return mPrimaryPresenter;
    }

    /**
     * Returns the presenter for secondary controls.
     */
    public Presenter getSecondaryPresenter() {
        return mSecondaryPresenter;
    }

    /**
     * Always returns the presenter for primary controls.
     */
    @Override
    public Presenter getPresenter(Object item) {
        return mPrimaryPresenter;
    }

    @Override
    public Presenter[] getPresenters() {
        return mPresenters;
    }

    static class ActionViewHolder extends Presenter.ViewHolder {
        ImageView mIcon;
        TextView mLabel;
        View mFocusableView;

        public ActionViewHolder(View view) {
            super(view);
            mIcon = (ImageView) view.findViewById(R.id.icon);
            mLabel = (TextView) view.findViewById(R.id.label);
            mFocusableView = view.findViewById(R.id.button);
        }
    }

    static class ControlButtonPresenter extends Presenter {
        private int mLayoutResourceId;

        ControlButtonPresenter(int layoutResourceId) {
            mLayoutResourceId = layoutResourceId;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(mLayoutResourceId, parent, false);
            return new ActionViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            Action action = (Action) item;
            ActionViewHolder vh = (ActionViewHolder) viewHolder;
            vh.mIcon.setImageDrawable(action.getIcon());

            if (action.getLabel1() != null) {
                if (action.getId() == 6)
                    vh.mLabel.setText("Shuffle");
                else if (action.getId() == 7)
                    vh.mLabel.setText(action.getLabel1());
            }

            vh.mLabel.setVisibility(View.INVISIBLE);
            vh.mFocusableView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    vh.mLabel.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
                }
            });

            CharSequence contentDescription = TextUtils.isEmpty(action.getLabel2())
                    ? action.getLabel1() : action.getLabel2();
            if (!TextUtils.equals(vh.mFocusableView.getContentDescription(), contentDescription)) {
                vh.mFocusableView.setContentDescription(contentDescription);
                vh.mFocusableView.sendAccessibilityEvent(
                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
            }

        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
            ActionViewHolder vh = (ActionViewHolder) viewHolder;
            vh.mIcon.setImageDrawable(null);
            if (vh.mLabel != null) {
                vh.mLabel.setText(null);
            }
            vh.mFocusableView.setContentDescription(null);
        }

        @Override
        public void setOnClickListener(ViewHolder viewHolder,
                                       View.OnClickListener listener) {
            ((ActionViewHolder) viewHolder).mFocusableView.setOnClickListener(listener);
        }
    }
}