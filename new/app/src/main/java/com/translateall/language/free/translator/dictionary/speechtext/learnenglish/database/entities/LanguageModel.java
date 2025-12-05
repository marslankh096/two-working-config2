package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants;


@Keep
@Entity(tableName = Constants.TABLE_NAME_LANGUAGE)
public class LanguageModel implements Parcelable {

    @PrimaryKey
    @NonNull
    String languageName;
    @ColumnInfo(name = "languageCode")
    String languageCode;
    @ColumnInfo(name = "langMean")
    String langMean;
    @ColumnInfo(name = "type")
    String type;
    @ColumnInfo(name = "countryCode")
    String countryCode;
    String header;
    boolean b;

    @Ignore
    boolean isSelected;


    public LanguageModel() {
    }


    public LanguageModel(String languageCode, String languageName, String langMean, String countryCode) {
        this.languageCode = languageCode;
        this.languageName = languageName;
        this.langMean = langMean;
        this.countryCode = countryCode;


    }

    public LanguageModel(String header, boolean b) {
        this.header = header;
        this.b = b;
    }


    protected LanguageModel(Parcel in) {
        languageName = in.readString();
        languageCode = in.readString();
        langMean = in.readString();
        type = in.readString();
        countryCode = in.readString();
        header = in.readString();
        b = in.readByte() != 0;
        isSelected = in.readByte() != 0;
    }

    public static final Creator<LanguageModel> CREATOR = new Creator<LanguageModel>() {
        @Override
        public LanguageModel createFromParcel(Parcel in) {
            return new LanguageModel(in);
        }

        @Override
        public LanguageModel[] newArray(int size) {
            return new LanguageModel[size];
        }
    };

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLangMean() {
        return langMean;
    }

    public void setLangMean(String langMean) {
        this.langMean = langMean;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public boolean isB() {
        return b;
    }

    public void setB(boolean b) {
        this.b = b;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
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
        dest.writeString(languageName);
        dest.writeString(languageCode);
        dest.writeString(langMean);
        dest.writeString(type);
        dest.writeString(countryCode);
        dest.writeString(header);
        dest.writeByte((byte) (b ? 1 : 0));
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }
}
