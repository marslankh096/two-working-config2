package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.languageSelection;

import android.text.TextUtils;

import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel;

import java.util.ArrayList;
import java.util.Collections;

public class LanguageUtils {

    public static ArrayList<LanguageModel> getHeaderListLatter(ArrayList<LanguageModel> langList) {

        ArrayList<LanguageModel> headersList = new ArrayList<>();

        Collections.sort(langList, (user1, user2) -> String.valueOf(user1.getLanguageName().charAt(0)).toUpperCase().compareTo(String.valueOf(user2.getLanguageName().charAt(0)).toUpperCase()));

        String lastHeader = "";

        int size = langList.size();

        for (int i = 0; i < size; i++) {

            LanguageModel user = langList.get(i);
            String header = String.valueOf(user.getLanguageName().charAt(0)).toUpperCase();

            if (!TextUtils.equals(lastHeader, header)) {
                lastHeader = header;
                headersList.add(new LanguageModel(header, true));
            }

            headersList.add(user);

        }

        return headersList;

    }

}
