package com.androidtv.bhagavadgita.comman;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
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
import android.widget.LinearLayout;
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
import com.androidtv.bhagavadgita.model.FestivalModel;
import com.androidtv.bhagavadgita.model.HistoryModel;
import com.androidtv.bhagavadgita.model.PushtimargModel;
import com.androidtv.bhagavadgita.model.VallabhacharyaModel;
import com.androidtv.bhagavadgita.model.VersesModel;
import com.androidtv.bhagavadgita.network.APIClient;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class HeaderView extends RelativeLayout implements TitleViewAdapter.Provider {

    private final TitleViewAdapter mTitleViewAdapter = createAdapter();

    private TextView content_title, content_name, content_other, content_info;
    public LinearLayout content_details;
    public Button buttonSeason;
    private ToggleButtonBase toggleTrailer, toggleWatchlist, toggleInfo;
    private static final int CONTENT_IMAGE_CROSS_FADE_DURATION = 1000;
    private ImageView content_image;
    private boolean showButtons = true;

    private Handler mHandler = new Handler();
    private TextView txtTime, txtDayDate;

    // Track active request to prevent stale async callbacks from overwriting new data
    private int mCurrentRequestId = 0;
    private Call<ResponseBody> mCurrentChapterCall;

    private OnItemClickListener mItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public HeaderView(@NonNull Context context, boolean showButtons, WindowManager windowManager) {
        super(context);
        this.showButtons = showButtons;
        init(context);
    }

    public HeaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private TitleViewAdapter createAdapter() {
        return new TitleViewAdapter() {
            @NonNull
            @Override
            public View getSearchAffordanceView() {
                return new View(getContext());
            }

            @Override
            public void setTitle(CharSequence titleText) {}

            @Override
            public void setBadgeDrawable(Drawable drawable) {}

            @Override
            public void setOnSearchClickedListener(OnClickListener listener) {}

            @Override
            public void updateComponentsVisibility(int flags) {
                Log.e("Header", "updateComponentsVisibility: " + flags);
                if ((flags & BRANDING_VIEW_VISIBLE) == BRANDING_VIEW_VISIBLE) {
                    HeaderView.this.setVisibility(View.VISIBLE);
                } else {
                    HeaderView.this.setVisibility(View.GONE);
                }
            }
        };
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.header_view_comman, this);

        content_title = findViewById(R.id.content_title);
        content_name = findViewById(R.id.content_name);
        content_info = findViewById(R.id.content_info);
        content_other = findViewById(R.id.content_other);
        content_details = findViewById(R.id.content_details);

        txtTime = findViewById(R.id.txtTime);
        txtDayDate = findViewById(R.id.txtDayDate);

        setCurrentDateTime();
        mHandler.postDelayed(mUpdateClockTask, 1000);
    }

    private Runnable mUpdateClockTask = new Runnable() {
        public void run() {
            setCurrentDateTime();
            mHandler.postDelayed(this, 1000);
        }
    };

    private void setCurrentDateTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df1 = new SimpleDateFormat("hh:mma");
        String formattedDatea = df1.format(c.getTime()).toLowerCase();
        txtTime.setText(formattedDatea);

        SimpleDateFormat df2 = new SimpleDateFormat("EEEE dd MMM, yyyy");
        String formattedTime = df2.format(c.getTime());
        txtDayDate.setText(formattedTime);
    }

    /**
     * Resets all text and cancels previous animations/requests to eliminate text overlap.
     */
    public void resetPreviousData() {
        mCurrentRequestId++; // Invalidate pending async calls

        if (mCurrentChapterCall != null && !mCurrentChapterCall.isCanceled()) {
            mCurrentChapterCall.cancel();
            mCurrentChapterCall = null;
        }

        if (content_title != null) {
            content_title.animate().cancel();
            content_title.setText("");
        }
        if (content_name != null) {
            content_name.animate().cancel();
            content_name.setText("");
        }
        if (content_info != null) {
            content_info.animate().cancel();
            content_info.setText("");
        }
        if (content_other != null) {
            content_other.animate().cancel();
            content_other.setText("");
        }
    }

    public void setData(Object object, List<Object> objectList, RowHeaderItem headerItem) {
//        if (object == null) {
//            resetPreviousData();
//            return;
//        }

        // 1. Wipe previous state first so old text never bleeds into new data
//        resetPreviousData();

        // 2. Set Row Title
        if (headerItem != null && content_title != null) {
            content_title.setText(Html.fromHtml(
                    "<font color='" + getResources().getColor(R.color.colorTextPrimary) + "'>" + headerItem.getName() + "</font> " +
                            "<font color='" + getResources().getColor(R.color.colorTextSecondary) + "'>" + headerItem.getDescription() + "</font>"));
        }

        if (object instanceof HistoryModel) {
            HistoryModel historyModel = (HistoryModel) object;
            stringDecodeWithHighlight(content_info, "", historyModel.getDescriptionHi(), Color.GRAY, true);
            stringDecodeWithHighlight(content_name, "", historyModel.getTitle(), Color.GRAY, true);
            stringDecodeWithHighlight(content_other, "", "", Color.GRAY, true);
        } else if (object instanceof PushtimargModel) {
            PushtimargModel pushtimargModel = (PushtimargModel) object;
            stringDecodeWithHighlight(content_info, "", pushtimargModel.getDescription(), Color.GRAY, true);
            stringDecodeWithHighlight(content_name, "", pushtimargModel.getTitle(), Color.GRAY, true);
            stringDecodeWithHighlight(content_other, "", "", Color.GRAY, true);
        } else if (object instanceof ChapterModel) {
            ChapterModel chapterModel = (ChapterModel) object;
            stringDecodeWithHighlight(content_info, "", chapterModel.getChapterSummary(), Color.GRAY, true);
            stringDecodeWithHighlight(content_name, "", "Chapter " + chapterModel.getChapterNumber() + ": " + chapterModel.getNameTranslation(), Color.GRAY, true);
            stringDecodeWithHighlight(content_other, "", chapterModel.getVersesCount() + " Verses", Color.GRAY, true);
        } else if (object instanceof VersesModel) {
            VersesModel versesModel = (VersesModel) object;
            getChapterInformation(versesModel, mCurrentRequestId);
        } else if (object instanceof DarshanModel) {
            DarshanModel darshanModel = (DarshanModel) object;
            stringDecodeWithHighlight(content_info, "", darshanModel.getDescription(), Color.GRAY, true);
            stringDecodeWithHighlight(content_name, "", darshanModel.getTitle() + "/" + darshanModel.getTitleHi(), Color.GRAY, true);
            content_other.setText(darshanModel.getStartTime().equalsIgnoreCase("") ? "-" : darshanModel.getStartTime() + " to " + darshanModel.getEndTime()
                    + " | " + darshanModel.getStatus());
        } else if (object instanceof VallabhacharyaModel) {
            VallabhacharyaModel vallabhacharyaModel = (VallabhacharyaModel) object;
            stringDecodeWithHighlight(content_info, "", vallabhacharyaModel.getDescription(), Color.GRAY, true);
            stringDecodeWithHighlight(content_name, "", vallabhacharyaModel.getTitle(), Color.GRAY, true);
            stringDecodeWithHighlight(content_other, "", "", Color.GRAY, true);
        } else if (object instanceof FestivalModel) {
            FestivalModel festivalModel = (FestivalModel) object;
            stringDecodeWithHighlight(content_info, "", festivalModel.getDescription(), Color.GRAY, true);
            stringDecodeWithHighlight(content_name, "", festivalModel.getTitle(), Color.GRAY, true);
            stringDecodeWithHighlight(content_other, "", "", Color.GRAY, true);
        }
    }

    private void getChapterInformation(VersesModel versesModel, final int requestId) {
        if (versesModel == null || getContext() == null) {
            return;
        }

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        mCurrentChapterCall = apiInterface.getChapters();

        APIClient.callAPI((MasterActivity) getContext(), mCurrentChapterCall, new APIClient.APICallback() {
            @Override
            public void onSuccess(String response) {
                // Guard: If another item was selected before this response arrived, drop this payload
                if (requestId != mCurrentRequestId) {
                    return;
                }

                try {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<ChapterModel>>() {}.getType();
                    List<ChapterModel> chapterList = gson.fromJson(response, listType);

                    if (chapterList == null || chapterList.isEmpty()) {
                        return;
                    }

                    ChapterModel selectedChapterModel = null;
                    for (ChapterModel chapterModel : chapterList) {
                        if (chapterModel != null &&
                                Objects.equals(versesModel.getChapterNumber(), chapterModel.getChapterNumber())) {
                            selectedChapterModel = chapterModel;
                            break;
                        }
                    }

                    if (selectedChapterModel == null) {
                        return;
                    }

                    final ChapterModel finalChapter = selectedChapterModel;

                    // Double check request ID before mutating UI
                    if (requestId != mCurrentRequestId) return;

                    stringDecodeWithHighlight(content_info, "", finalChapter.getChapterSummary(), Color.GRAY, true);
                    stringDecodeWithHighlight(content_name, "", "Chapter " + finalChapter.getChapterNumber() + ": " + finalChapter.getNameTranslation(), Color.GRAY, true);

                    String audioUrl = APIInterface.VERSES_BASE_PATH + versesModel.getChapterNumber() + "/" + versesModel.getVerseNumber() + ".mp3";

                    AudioUtils.getAudioDurationFromUrl(getContext(), audioUrl, new AudioUtils.DurationCallback() {
                        @Override
                        public void onDurationRetrieved(String formattedDuration) {
                            if (requestId == mCurrentRequestId) {
                                stringDecodeWithHighlight(content_other, "", finalChapter.getVersesCount() + " Verses | " + formattedDuration, Color.GRAY, true);
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String error, int responseCode) {}

            @Override
            public void onError(String error) {}
        });
    }

    @NonNull
    @Override
    public TitleViewAdapter getTitleViewAdapter() {
        return mTitleViewAdapter;
    }

    public void stringDecodeWithHighlight(TextView textView, String label, String value, int labelColor, boolean boldLabel) {
        if (textView == null) return;

        if (value == null || value.trim().isEmpty()) {
            textView.setText("");
            textView.setVisibility(View.GONE);
            return;
        }

        try {
            String decoded = URLDecoder.decode(value.replaceAll("%(?![0-9a-fA-F]{2})", "%25"), "UTF-8");
            SpannableString span = new SpannableString(label + decoded);
            span.setSpan(new ForegroundColorSpan(labelColor), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (boldLabel) {
                span.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            textView.setText(span);
            textView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            textView.setText(label + value);
            textView.setVisibility(View.VISIBLE);
        }
    }
}