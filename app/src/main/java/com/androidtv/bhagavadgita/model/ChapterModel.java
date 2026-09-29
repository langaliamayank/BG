package com.androidtv.bhagavadgita.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ChapterModel implements Serializable {

    @SerializedName("chapter_number")
    @Expose
    private Integer chapterNumber;
    @SerializedName("chapter_summary")
    @Expose
    private String chapterSummary;
    @SerializedName("chapter_summary_hindi")
    @Expose
    private String chapterSummaryHindi;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("image_name")
    @Expose
    private String imageName;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("name_meaning")
    @Expose
    private String nameMeaning;
    @SerializedName("name_translation")
    @Expose
    private String nameTranslation;
    @SerializedName("name_transliterated")
    @Expose
    private String nameTransliterated;
    @SerializedName("verses_count")
    @Expose
    private Integer versesCount;
    private final static long serialVersionUID = -784122690184523669L;

    public Integer getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(Integer chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public String getChapterSummary() {
        return chapterSummary;
    }

    public void setChapterSummary(String chapterSummary) {
        this.chapterSummary = chapterSummary;
    }

    public String getChapterSummaryHindi() {
        return chapterSummaryHindi;
    }

    public void setChapterSummaryHindi(String chapterSummaryHindi) {
        this.chapterSummaryHindi = chapterSummaryHindi;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameMeaning() {
        return nameMeaning;
    }

    public void setNameMeaning(String nameMeaning) {
        this.nameMeaning = nameMeaning;
    }

    public String getNameTranslation() {
        return nameTranslation;
    }

    public void setNameTranslation(String nameTranslation) {
        this.nameTranslation = nameTranslation;
    }

    public String getNameTransliterated() {
        return nameTransliterated;
    }

    public void setNameTransliterated(String nameTransliterated) {
        this.nameTransliterated = nameTransliterated;
    }

    public Integer getVersesCount() {
        return versesCount;
    }

    public void setVersesCount(Integer versesCount) {
        this.versesCount = versesCount;
    }

}