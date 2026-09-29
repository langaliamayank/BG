package com.androidtv.bhagavadgita.comman;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.media3.common.util.UnstableApi;

import com.androidtv.bhagavadgita.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@UnstableApi
public class ToggleButtonBase extends LinearLayout {

    private final List<OnCheckedChangeListener> mCheckedListeners = new ArrayList<>();
    public TextView mDescView, clickCounterView;
    public ImageButton mImageButton;
    protected Drawable mImage;
    protected Drawable mImageOn;
    protected Drawable mImageOff;
    protected String mDescText;
    protected boolean mIsChecked;
    protected String mTextOn;
    protected String mTextOff;
    protected ViewGroup mToggleButtonWrapper;
    private int mBindToId;
    private boolean mIsDisabled;
    private int mSpace;
    private int clickCounter = 0;

    public ToggleButtonBase(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public ToggleButtonBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ToggleButtonBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ToggleButtonBase,
                0, 0);

        try {
            mImage = a.getDrawable(R.styleable.ToggleButtonBase_image);
            mImageOn = a.getDrawable(R.styleable.ToggleButtonBase_imageOn);
            mImageOff = a.getDrawable(R.styleable.ToggleButtonBase_imageOff);
            mTextOn = a.getString(R.styleable.ToggleButtonBase_textOn);
            mTextOff = a.getString(R.styleable.ToggleButtonBase_textOff);
            mDescText = a.getString(R.styleable.ToggleButtonBase_desc);
            mBindToId = a.getResourceId(R.styleable.ToggleButtonBase_bindTo, 0);
            mSpace = a.getDimensionPixelSize(R.styleable.ToggleButtonBase_left_right_space, 0);

            String handlerName = a.getString(R.styleable.ToggleButtonBase_onCheckedChanged);
            if (handlerName != null) {
                setOnCheckedChangeListener(new DeclaredOnCheckedChangeListener(this, handlerName));
            }
        } finally {
            a.recycle();
        }

        init();
    }

    public ToggleButtonBase(Context context) {
        super(context);
        init();
    }

    private void init() {
        inflate();
        applyCommonProps();
        setOnFocus();
        setOnClick();
        initElems();
        makeUnfocused();
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        // NOTE: android doesn't allow multiple onClick listeners
        if (mCheckedListeners.size() > 0) {
            return;
        }
        super.setOnClickListener(l);
    }

    private void setOnClick() {
        // NOTE: android doesn't allow multiple onClick listeners
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
    }

    private void setOnFocus() {
        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    makeFocused();
                } else {
                    makeUnfocused();
                }
            }
        });
    }

    private void inflate() {
        inflate(getContext(), R.layout.toggle_button, this);
        mDescView = findViewById(R.id.description);
        mImageButton = findViewById(R.id.exo_icon);
        mToggleButtonWrapper = findViewById(R.id.toggle_button_wrapper);

        if (mSpace > 0) {
            mToggleButtonWrapper.setPadding(mSpace, 0, mSpace, 0);
        }
    }

    private void applyCommonProps() {
        setOrientation(LinearLayout.VERTICAL);
        setClickable(true);
        setFocusable(true);
        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        // NOTE: disable background when using style attribute
        setBackgroundDrawable(getResources().getDrawable(R.color.colorTransparent));
    }

    private void makeUnfocused() {
        mDescView.setText(mDescText);
        onButtonUnfocused();
    }

    private void makeFocused() {
        mDescView.setText(mDescText);
        onButtonFocused();

    }

    private void initElems() {
        if (getChecked()) {
            onButtonChecked();
            callCheckedListener(true);
            uncheckBindToView();
        } else {
            onButtonUnchecked();
            callCheckedListener(false);
        }
    }

    private void uncheckBindToView() {
        View bindToView = getRootView().findViewById(mBindToId);
        if (bindToView != null)
            ((ToggleButtonBase) bindToView).uncheck();
    }

    private void uncheck() {
        mIsChecked = false;
        initElems();
    }

    protected void callCheckedListener(boolean isChecked) {
        if (mIsDisabled) {
            return;
        }

        for (OnCheckedChangeListener listener : mCheckedListeners) {
            if (listener != null) {
                listener.onCheckedChanged(this, isChecked);
            }
        }
    }

    public boolean getChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean isChecked) {
        mIsChecked = isChecked;
        initElems();
    }

    protected void toggle() {
        mIsChecked = !mIsChecked;
        initElems();
    }

    public void resetState() {
        mIsChecked = false;
        initElems();
    }

    public boolean isDisabled() {
        return mIsDisabled;
    }

    public void enableEvents() {
        mIsDisabled = false;
    }

    public void disableEvents() {
        mIsDisabled = true;
    }

    public void disable() {
//        setFocusable(false);
//        setClickable(false);
        makeTextDisabled();
        makeImageDisabled();
        mIsDisabled = true;
    }

    public void enable() {
        setFocusable(true);
        setClickable(true);
        makeTextEnabled();
        makeImageEnabled();
        mIsDisabled = false;
    }

    private void makeTextEnabled() {
//        if (mTextButton != null)
//            mTextButton.setTextColor(Color.WHITE);
    }

    private void makeTextDisabled() {
//        if (mTextButton != null)
//            mTextButton.setTextColor(Color.DKGRAY);
    }

    private void makeImageEnabled() {
        if (mImageButton == null) {
            return;
        }

        Drawable image = mImageButton.getDrawable();
        if (image == null) {
            return;
        }
        image.setAlpha(255);
    }

    private void makeImageDisabled() {
        if (mImageButton == null) {
            return;
        }

        Drawable image = mImageButton.getDrawable();
        if (image == null) {
            return;
        }
        image.setAlpha(80);
    }

    protected void onButtonFocused() {
        if (mDescView != null)
            mDescView.setVisibility(VISIBLE);
        if (mImageButton != null) {
            mImageButton.setBackgroundResource(R.drawable.ic_button_focus);
        }
    }

    protected void onButtonUnfocused() {
        if (mDescView != null)
            mDescView.setVisibility(INVISIBLE);

        if (clickCounterView != null)
            clickCounterView.setVisibility(INVISIBLE);

        if (mImageButton != null) {
            mImageButton.setBackgroundResource(R.drawable.ic_button_normal);
        }
    }

    protected void onButtonChecked() {
        if (mImageButton != null)
            mImageButton.setImageDrawable(mImageOn);
    }

    protected void onButtonUnchecked() {
//        if (mDescView != null)
//            mDescView.setText(mTextOff);

        if (mImageButton != null)
            mImageButton.setImageDrawable(mImageOff);
    }

    public CharSequence getText() {
        return getText();
    }

    public void setText(int label) {
//        mDescView.setText(label);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mCheckedListeners.add(listener);
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(ToggleButtonBase button, boolean isChecked);
    }

    /**
     * An implementation of Listener that attempts to lazily load a
     * named handling method from a parent or ancestor context.
     */
    private class DeclaredOnCheckedChangeListener implements OnCheckedChangeListener {
        private final View mHostView;
        private final String mMethodName;

        private Method mResolvedMethod;
        private Object mResolvedContext;

        public DeclaredOnCheckedChangeListener(@NonNull View hostView, @NonNull String methodName) {
            mHostView = hostView;
            mMethodName = methodName;
        }

        @Override
        public void onCheckedChanged(@NonNull ToggleButtonBase compoundButton, boolean b) {
            if (mResolvedMethod == null) {
                resolveMethod(mHostView.getContext(), mMethodName);
            }

            try {
                mResolvedMethod.invoke(mResolvedContext, compoundButton, b);
            } catch (Exception e) {
                LogTag.e(e.getMessage());
            }
        }

        @NonNull
        private void resolveMethod(@Nullable Context context, @NonNull String name) {
            while (context != null) {
                // activity case
                if (!context.isRestricted()) {
                    Method method = getMethod(context.getClass(), mMethodName, ToggleButtonBase.class, boolean.class);
                    if (method != null) {
                        mResolvedMethod = method;
                        mResolvedContext = context;
                        return;
                    }
                }

                // fragment case
                if (context instanceof FragmentActivity) { // search inside fragments
                    FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
                    for (Fragment fragment : manager.getFragments()) {
                        Method method = getMethod(fragment.getClass(), mMethodName, ToggleButtonBase.class, boolean.class);
                        if (method != null) {
                            mResolvedMethod = method;
                            mResolvedContext = fragment;
                            return;
                        }
                    }
                }

                // next
                if (context instanceof ContextWrapper) {
                    context = ((ContextWrapper) context).getBaseContext();
                } else {
                    // Can't search up the hierarchy, null out and fail.
                    context = null;
                }
            }

            final int id = mHostView.getId();
            final String idText = id == NO_ID ? "" : " with id '"
                    + mHostView.getContext().getResources().getResourceEntryName(id) + "'";
            throw new IllegalStateException("Could not find method " + mMethodName
                    + "(ToggleButtonBase, boolean) in a parent or ancestor Context for app:onCheckedChanged "
                    + "attribute defined on view " + mHostView.getClass() + idText);
        }

        private Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
            try {
                Method method = clazz.getMethod(methodName, parameterTypes);
                if (method != null) {
                    return method;
                }
            } catch (NoSuchMethodException e) {
                // Failed to find method, keep searching up the hierarchy.
                return null;
            }
            return null;
        }
    }
}
