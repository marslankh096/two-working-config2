package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao.TranslationDao;
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.FavoriteWordsEntity;
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel;
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.TranslationHistory;
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants;


@Database(entities = {TranslationHistory.class, FavoriteWordsEntity.class, LanguageModel.class}, version = 2, exportSchema = false)
public abstract class MyDataBase extends RoomDatabase {
    // DAO classes
    public abstract TranslationDao translationDao();
//    public abstract TestDao testDao();






    private static MyDataBase dataBase;


    public static MyDataBase getInstance(Context context){
        if (null== dataBase){
            dataBase= buildDatabaseInstance(context);
        }
        return dataBase;
    }



    private static MyDataBase buildDatabaseInstance(Context context) {
        return Room.databaseBuilder(context,
                MyDataBase.class,
                Constants.DB_NAME)
//                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries().build();
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

            String createTable = "CREATE TABLE IF NOT EXISTS 'conversation' (id  INTEGER NOT NULL PRIMARY KEY, inputWord TEXT, translatedWord TEXT, origin TEXT, targetLangCode TEXT, isSpeaking INTEGER NOT NULL)";

            database.execSQL(createTable);
        }
    };

//    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
//        @Override
//        public void migrate(SupportSQLiteDatabase database) {
//
//            database.execSQL("ALTER TABLE Test "
//                    + " ADD COLUMN isSpeaking INTEGER");
//        }
//    };
}
