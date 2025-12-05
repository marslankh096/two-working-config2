package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.clipboard.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R;
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel;

import java.util.List;

public class CustomAdapter extends BaseAdapter {

    private List<LanguageModel> langs;
    private Context context;

    public CustomAdapter(Context context, List<LanguageModel> langs) {
        this.langs = langs;
        this.context = context;
    }

    @Override
    public int getCount() {
        return langs.size();
    }

    @Override
    public Object getItem(int position) {
        return langs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint("ViewHolder") View view =  View.inflate(context, R.layout.layout_lang_spinner, null);
        AppCompatTextView textView = view.findViewById(R.id.tv_spinner_lang);
//        textView.setText(langs.get(position).getLanguageName() /*+ " ("+ langs.get(position).getLangMean()+")"*/);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View view;
        view =  View.inflate(context, R.layout.layout_lang_spinner_items, null);
        final TextView textView = view.findViewById(R.id.tv_spinner_lang);
        textView.setText(langs.get(position).getLanguageName());
        return view;
    }
}
