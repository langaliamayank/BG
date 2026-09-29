package com.androidtv.bhagavadgita.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TranslationModel implements Serializable {
    @SerializedName("authorName")
    @Expose
    private String authorName;
    @SerializedName("author_id")
    @Expose
    private Integer authorId;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("lang")
    @Expose
    private String lang;
    @SerializedName("language_id")
    @Expose
    private Integer languageId;
    @SerializedName("verseNumber")
    @Expose
    private Integer verseNumber;
    @SerializedName("verse_id")
    @Expose
    private Integer verseId;
    private final static long serialVersionUID = -5945676401716794298L;

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
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
}
