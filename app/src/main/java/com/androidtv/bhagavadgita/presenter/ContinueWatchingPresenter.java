package com.androidtv.bhagavadgita.presenter;


import android.graphics.Outline;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.BaseCardView;
import androidx.leanback.widget.ImageCardView;

import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.AudioUtils;
import com.androidtv.bhagavadgita.model.ChapterModel;
import com.androidtv.bhagavadgita.model.VersesModel;
import com.androidtv.bhagavadgita.network.APIClient;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class ContinueWatchingPresenter extends AbstractBasePresenter<BaseCardView> {
    private MasterActivity mContext;
    private ArrayList<VersesModel> mVersesList = new ArrayList<>();
    private int mSelectedBackgroundColor = -1;
    private int mDefaultBackgroundColor = -1;

    public ContinueWatchingPresenter(MasterActivity context) {
        super(context);
        mContext = context;
    }

    @Override
    protected BaseCardView onCreateView(ViewGroup parent) {
        mDefaultBackgroundColor =
                ContextCompat.getColor(getContext(), R.color.colorWhite25);
        mSelectedBackgroundColor =
                ContextCompat.getColor(getContext(), R.color.colorBlack50);

        BaseCardView cardView = new BaseCardView(mContext, null, R.style.SideInfoCardStyle) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.addView(LayoutInflater.from(mContext).inflate(R.layout.card_continue_watching_item, null));
        cardView.addOnLayoutChangeListener(sLayoutChangeListener);
        updateCardBackgroundColor(cardView, false);
        return cardView;
    }

    private void updateCardBackgroundColor(BaseCardView view, boolean selected) {
        int color = selected ? mSelectedBackgroundColor : mDefaultBackgroundColor;
        view.setBackgroundColor(color);
    }

    private View.OnLayoutChangeListener sLayoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                   int oldLeft, int oldTop, int oldRight, int oldBottom) {
            v.setPivotY(v.getMeasuredHeight());
        }
    };

    @Override
    public void onBindViewHolder(Object object, BaseCardView cardView) {

        if (object instanceof VersesModel) {
            VersesModel versesModel = (VersesModel) object;

            String audioUrl = APIInterface.VERSES_BASE_PATH + versesModel.getChapterNumber() + "/" + versesModel.getVerseNumber() + ".mp3";
            AudioUtils.getAudioDurationFromUrl(getContext(), audioUrl, new AudioUtils.DurationCallback() {
                @Override
                public void onDurationRetrieved(String formattedDuration) {
                    ((TextView) cardView.findViewById(R.id.textDuration)).setText(formattedDuration);
                }
            });

            getChapterInformation(mContext, versesModel, new VersesOfTheDayPresenter.ChapterCallback() {
                @Override
                public void onChapterLoaded(ChapterModel chapterModel) {
                    ((TextView) cardView.findViewById(R.id.textChapterNumber)).setText(chapterModel.getNameTranslation());
                    ((TextView) cardView.findViewById(R.id.textVerseNumber)).setText(
                            "BG " + chapterModel.getChapterNumber() + "." + versesModel.getVerseNumber());
                }

                @Override
                public void onError(String message) {

                }
            });

            String singleSpacedText = versesModel.getText().replaceAll("(\r?\n)+", "\n").trim();
            ((TextView) cardView.findViewById(R.id.textVerse)).setText(singleSpacedText);
        }
    }

    private void getChapterInformation(MasterActivity activity, VersesModel versesModel, VersesOfTheDayPresenter.ChapterCallback callback) {
        if (versesModel == null || activity == null) {
            if (callback != null) callback.onError("Activity or VersesModel is null");
            return;
        }

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getChapters();

        APIClient.callAPI(activity, loginCall, new APIClient.APICallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<ChapterModel>>() {}.getType();
                    List<ChapterModel> chapterList = gson.fromJson(response, listType);

                    if (chapterList == null || chapterList.isEmpty()) {
                        if (callback != null) callback.onError("Chapter list is empty");
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
                        if (callback != null) callback.onError("Chapter not found");
                        return;
                    }

                    // Deliver the returned object
                    if (callback != null) {
                        callback.onChapterLoaded(selectedChapterModel);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) callback.onError(e.getMessage());
                }
            }

            @Override
            public void onFailure(String error, int responseCode) {
                if (callback != null) callback.onError(error);
            }

            @Override
            public void onError(String error) {
                if (callback != null) callback.onError(error);
            }
        });
    }

    private void setChildRounded(ImageCardView cardView) {
        View mainImageView = cardView.getMainImageView();
        mainImageView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int cornerRadius = 13;
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
            }
        });
    }

    @Override
    public void onUnbindViewHolder(BaseCardView cardView) {
        super.onUnbindViewHolder(cardView);
    }

    public void setList(ArrayList<VersesModel> listFromAdapter) {
        mVersesList = (listFromAdapter != null) ? new ArrayList<>(listFromAdapter) : new ArrayList<>();
    }

    public ArrayList<VersesModel> getList() {
        return mVersesList;
    }

}
