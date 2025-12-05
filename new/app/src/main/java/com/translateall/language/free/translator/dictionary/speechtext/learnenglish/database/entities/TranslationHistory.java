package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants;

@Keep
@Entity(tableName = Constants.TABLE_NAME_TRANSLATION)
public class TranslationHistory implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "input_word")
    private String inputWord;
    @ColumnInfo(name = "translated_word")
    private String translatedWord;
    @ColumnInfo(name = "isFavorite")
    private boolean isFavorite;
    @ColumnInfo(name = "srcLang")
    private String srcLang;
    @ColumnInfo(name = "targetLang")
    private String targetLang;
    @ColumnInfo(name = "srcCode")
    private String srcCode;
    @ColumnInfo(name = "trCode")
    private String trCode;
    @ColumnInfo(name = "primaryId")
    private String primaryId;

    boolean isSelected;

    public TranslationHistory() {
    }


    protected TranslationHistory(Parcel in) {
        id = in.readInt();
        inputWord = in.readString();
        translatedWord = in.readString();
        isFavorite = in.readByte() != 0;
        srcLang = in.readString();
        targetLang = in.readString();
        srcCode = in.readString();
        trCode = in.readString();
        primaryId = in.readString();
        isSelected = in.readByte() != 0;
    }

    public static final Creator<TranslationHistory> CREATOR = new Creator<TranslationHistory>() {
        @Override
        public TranslationHistory createFromParcel(Parcel in) {
            return new TranslationHistory(in);
        }

        @Override
        public TranslationHistory[] newArray(int size) {
            return new TranslationHistory[size];
        }
    };

    @NonNull
    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(@NonNull String primaryId) {
        this.primaryId = primaryId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getSrcLang() {
        return srcLang;
    }

    public void setSrcLang(String srcLang) {
        this.srcLang = srcLang;
    }

    public String getTargetLang() {
        return targetLang;
    }

    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(inputWord);
        dest.writeString(translatedWord);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeString(srcLang);
        dest.writeString(targetLang);
        dest.writeString(srcCode);
        dest.writeString(trCode);
        dest.writeString(primaryId);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass())
            return false;
        TranslationHistory conversationModel = (TranslationHistory) obj;
        return this.inputWord.equals(conversationModel.inputWord);
    }
}
