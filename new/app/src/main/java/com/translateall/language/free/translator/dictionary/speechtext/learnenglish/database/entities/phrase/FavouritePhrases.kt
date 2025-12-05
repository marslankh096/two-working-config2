package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.phrase

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity
data class FavouritePhrases(
    @PrimaryKey var id: Long? = null,
    @ColumnInfo(name = "fav_id") var favId: String? = null
)
