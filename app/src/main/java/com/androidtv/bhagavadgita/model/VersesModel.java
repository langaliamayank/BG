package com.androidtv.bhagavadgita.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class VersesModel implements Serializable {

    @SerializedName("chapter_id")
    @Expose
    private Integer chapterId;
    @SerializedName("chapter_number")
    @Expose
    private Integer chapterNumber;
    @SerializedName("externalId")
    @Expose
    private Integer externalId;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("verse_number")
    @Expose
    private Integer verseNumber;
    @SerializedName("verse_id")
    @Expose
    private Integer verseId;
    @SerializedName("transliteration")
    @Expose
    private String transliteration;
    @SerializedName("word_meanings")
    @Expose
    private String wordMeanings;
    private boolean isDummy;

    private final static long serialVersionUID = -6301372345790867370L;

    public Integer getChapterId() {
        return chapterId;
    }

    public void setChapterId(Integer chapterId) {
        this.chapterId = chapterId;
    }

    public Integer getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(Integer chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getVerseNumber() {
        return verseNumber;
    }

    public void setVerseNumber(Integer verseNumber) {
        this.verseNumber = verseNumber;
    }

    public Integer getVerseId() {
        return verseId;
    }

    public void setVerseId(Integer verseId) {
        this.verseId = verseId;
    }

    public String getTransliteration() {
        return transliteration;
    }

    public void setTransliteration(String transliteration) {
        this.transliteration = transliteration;
    }

    public String getWordMeanings() {
        return wordMeanings;
    }

    public void setWordMeanings(String wordMeanings) {
        this.wordMeanings = wordMeanings;
    }

    public boolean isDummy() {
        return isDummy;
    }
    public void setDummy(boolean dummy) {
        isDummy = dummy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersesModel that = (VersesModel) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}