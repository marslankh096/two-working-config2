package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.phrase.FavouritePhrases

@Dao
interface PhraseFavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavPhrase(fav: FavouritePhrases): Long

    @Query("SELECT * FROM FavouritePhrases WHERE fav_id = :id")
    fun getFavoritePhrases(id: Int): LiveData<List<FavouritePhrases>>

    @Query("SELECT fav_id FROM FavouritePhrases WHERE fav_id = :id")
    fun getFavoritePhrase(id: String): String?

    @Query("DELETE FROM FavouritePhrases WHERE fav_id =:id")
    fun deleteFavPhrase(id: String)

}