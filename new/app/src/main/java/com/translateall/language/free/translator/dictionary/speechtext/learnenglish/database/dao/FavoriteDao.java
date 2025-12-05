package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao;

import androidx.annotation.Keep;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.FavoriteWordsEntity;
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel;
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants;

import java.util.List;

@Keep
@Dao
public interface FavoriteDao {

    @Transaction
    @Insert
    void insertAll(List<FavoriteWordsEntity> historyList);
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteWordsEntity history);
    @Transaction
    @Update
    void update(FavoriteWordsEntity history);
    @Transaction
    @Delete
    void delete(FavoriteWordsEntity history);
    @Transaction
    @Query("SELECT * FROM "+ Constants.TABLE_NAME_FAVORITE)
    List<FavoriteWordsEntity> getAll();


    @Transaction
    @Query ("DELETE FROM favorites WHERE primaryId = (SELECT MAX(primaryId) FROM favorites)")
    void deleteLastInserted();

    @Transaction
    @Query ("DELETE FROM favorites WHERE primaryId LIKE :id")
    void deleteById(String id);
    @Transaction
    @Query("SELECT isFavorite FROM favorites WHERE primaryId LIKE :id ")
    boolean isStared(String id);
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLanguage(LanguageModel model);
    @Transaction
    @Query("SELECT * FROM " + Constants.TABLE_NAME_LANGUAGE)
    List<LanguageModel> getRecentUsedLang();
    @Transaction
    @Query("DELETE FROM "+Constants.TABLE_NAME_FAVORITE)
    void deleteAll();

    @Transaction
    @Query("SELECT * FROM "+ Constants.TABLE_NAME_FAVORITE)
    LiveData<List<FavoriteWordsEntity>> getAllFavorite();




}
