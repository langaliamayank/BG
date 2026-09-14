package com.androidtv.bhagavadgita.comman;

//import static in.aeongroup.aeontv.MasterActivity.mWatchDataList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.widget.TitleViewAdapter;
import androidx.leanback.widget.VerticalGridView;

import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.model.ChapterModel;
import com.androidtv.bhagavadgita.model.DarshanModel;
import com.androidtv.bhagavadgita.model.HistoryModel;
import com.androidtv.bhagavadgita.model.PushtimargModel;
import com.androidtv.bhagavadgita.model.VallabhacharyaModel;
import com.androidtv.bhagavadgita.model.VersesModel;
import com.androidtv.bhagavadgita.network.APIClient;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class HeaderViewChapter extends RelativeLayout implements TitleViewAdapter.Provider {

    private final TitleViewAdapter mTitleViewAdapter = createAdapter();
    private TextView textOther, textChapterTitle, textChapterInfo;
    public Button buttonReadChapter;
    private OnItemClickListener mItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public HeaderViewChapter(@NonNull Context context) {
        super(context);
        init(context);
    }

    public HeaderViewChapter(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public HeaderViewChapter(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private TitleViewAdapter createAdapter() {
        return new TitleViewAdapter() {
            @NonNull
            @Override
            public View getSearchAffordanceView() {
                // Ensure a valid dummy View is returned
                return new View(getContext());
            }

            @Override
            public void setTitle(CharSequence titleText) {
            }

            @Override
            public void setBadgeDrawable(Drawable drawable) {
            }

            @Override
            public void setOnSearchClickedListener(OnClickListener listener) {
            }

            @Override
            public void updateComponentsVisibility(int flags) {
                Log.e("Header", "updateComponentsVisibility: " + flags);

                if ((flags & BRANDING_VIEW_VISIBLE) == BRANDING_VIEW_VISIBLE) {
                    HeaderViewChapter.this.setVisibility(View.VISIBLE);
                } else {
                    HeaderViewChapter.this.setVisibility(View.GONE);
                }
            }
        };
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.header_view_chapter, this);

        getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                if (newFocus != null) {
                    String className = newFocus.getClass().getSimpleName();
                    int id = newFocus.getId();
                    String name = (id != View.NO_ID) ? getResources().getResourceEntryName(id) : "NO_ID";
                }
            }
        });

        textOther = findViewById(R.id.textOther);
        textChapterTitle = findViewById(R.id.textChapterTitle);
        textChapterInfo = findViewById(R.id.textChapterInfo);
        buttonReadChapter = findViewById(R.id.buttonReadChapter);
    }

    public void setData(MasterActivity activity, Object object, List<Object> objectList, RowHeaderItem headerItem) {
        if (object == null) return;

        if (object instanceof VersesModel) {
            VersesModel versesModel = (VersesModel) object;
            getChapterInformation(activity, versesModel);
        }

        buttonReadChapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(v, 0);
                }
            }
        });
    }

    private void getChapterInformation(MasterActivity activity, VersesModel versesModel) {
        if (versesModel == null || activity == null) {
            return;
        }

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getChapters();

        APIClient.callAPI(activity, loginCall, new APIClient.APICallback() {

            @Override
            public void onSuccess(String response) {
                try {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<ChapterModel>>() {
                    }.getType();
                    List<ChapterModel> chapterList = gson.fromJson(response, listType);

                    if (chapterList == null || chapterList.isEmpty()) {
                        return;
                    }

                    // Find matching chapter efficiently
                    ChapterModel selectedChapterModel = null;
                    for (ChapterModel chapterModel : chapterList) {
                        if (chapterModel != null &&
                                Objects.equals(versesModel.getChapterNumber(), chapterModel.getChapterNumber())) {
                            selectedChapterModel = chapterModel;
                            break; // Stop iteration once matched
                        }
                    }

                    if (selectedChapterModel == null) {
                        return;
                    }

                    // Final reference to use inside nested async callback
                    final ChapterModel chapterModel = selectedChapterModel;

                    textChapterTitle.setText("Chapter " + chapterModel.getChapterNumber() + ": " + chapterModel.getNameTranslation());
                    textChapterInfo.setText(chapterModel.getChapterSummary());
                    textOther.setText(chapterModel.getVersesCount() + " Verses");

                    Glide.with(getContext())
                            .load(activity.getImagePath(chapterModel, false))
                            .into(((ImageView) findViewById(R.id.imageChapter)));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String error, int responseCode) {
                // Log error or inform UI
            }

            @Override
            public void onError(String error) {
                // Log error or inform UI
            }
        });
    }

    @NonNull
    @Override
    public TitleViewAdapter getTitleViewAdapter() {
        return mTitleViewAdapter;
    }
}


