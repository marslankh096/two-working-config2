package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities;


import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants;

@Keep
@Entity(tableName = Constants.TABLE_NAME_FAVORITE)
public class FavoriteWordsEntity {

    @ColumnInfo(name = "input")
    private String inputWord;
    @ColumnInfo(name = "translated")
    private String translatedWord;
    @ColumnInfo(name = "isFavorite")
    private boolean isFavorite;
    @ColumnInfo(name = "targetLang")
    private String targetLang;
    @ColumnInfo(name = "srcLang")
    private String srcLang;
    @ColumnInfo(name="srcCode")
    private String srcCode;
    @ColumnInfo(name = "trCode")
    private String trCode;
    @PrimaryKey
    @NonNull
    private String primaryId;;


    boolean isSelected;


    public FavoriteWordsEntity() {
    }



    public String getInputWord() {
        return inputWord;
    }

    public void setInputWord(String inputWord) {
        this.inputWord = inputWord;
    }

    public String getTranslatedWord() {
        return translatedWord;
    }

    public void setTranslatedWord(String translatedWord) {
        this.translatedWord = translatedWord;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getTargetLang() {
        return targetLang;
    }

    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
    }

    public String getSrcLang() {
        return srcLang;
    }

    public void setSrcLang(String srcLang) {
        this.srcLang = srcLang;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getSrcCode() {
        return srcCode;
    }

    public void setSrcCode(String srcCode) {
        this.srcCode = srcCode;
    }

    public String getTrCode() {
        return trCode;
    }

    public void setTrCode(String trCode) {
        this.trCode = trCode;
    }

    @NonNull
    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(@NonNull String primaryId) {
        this.primaryId = primaryId;
    }
}
