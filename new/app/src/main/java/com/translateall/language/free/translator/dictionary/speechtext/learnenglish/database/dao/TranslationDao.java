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

import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.TranslationHistory;
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants;

import java.util.List;

@Keep
@Dao
public interface TranslationDao {
    @Transaction
    @Insert
    void insertAll(List<TranslationHistory> historyList);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TranslationHistory history);

    @Transaction
    @Update
    void update(TranslationHistory history);

    @Query("update translation set isFavorite = :isFavorite where primaryId = :primaryId")
    void update(String primaryId, Boolean isFavorite);

    @Transaction
    @Delete
    void delete(TranslationHistory history);

    @Transaction
    @Query("SELECT * FROM " + Constants.TABLE_NAME_TRANSLATION)
    List<TranslationHistory> getAll();

    @Transaction
    @Query("UPDATE translation SET isFavorite= :isFavorite WHERE primaryId LIKE :primaryId ")
    int updateItem(String primaryId, boolean isFavorite);

    @Transaction
    @Query("SELECT * FROM " + Constants.TABLE_NAME_TRANSLATION + " WHERE isFavorite LIKE :isFavorite")
    LiveData<List<TranslationHistory>> getFavorites(boolean isFavorite);

    @Transaction
    @Query("DELETE FROM " + Constants.TABLE_NAME_TRANSLATION)
    void deleteAll();

    @Transaction
    @Query("DELETE FROM translation WHERE primaryId LIKE :primaryId")
    void deleteById(String primaryId);

    @Query("SELECT * FROM " + Constants.TABLE_NAME_TRANSLATION)
    LiveData<List<TranslationHistory>> getAllHistory();

    @Query("SELECT isFavorite FROM translation WHERE id LIKE :favoriteId ")
    LiveData<Boolean> getFavorite(int favoriteId);

    @Query("SELECT isFavorite FROM translation WHERE id LIKE :favoriteId ")
    boolean getFavoriteById(int favoriteId);

    @Transaction
    @Query("SELECT * FROM " + Constants.TABLE_NAME_TRANSLATION + " ORDER BY id DESC LIMIT :limit  ")
    LiveData<List<TranslationHistory>> getLimitList(int limit);
}
