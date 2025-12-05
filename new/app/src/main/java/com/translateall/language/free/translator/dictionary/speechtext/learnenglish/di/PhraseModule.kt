package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.di

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.PhraseDatabase
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.PhrasesDbHelper
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao.PhraseFavoriteDao
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.repo.PhraseRepository
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.phrase.PhraseViewModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.TinyDB
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val phraseDatabaseModule = module {
    fun provideDatabase(application: Application): PhraseDatabase {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `FavouritePhrases` (`id` INTEGER,`fav_id` TEXT, PRIMARY KEY(`id`))")
            }
        }

        return Room.databaseBuilder(application, PhraseDatabase::class.java, "hazel_translator")
            .addMigrations(MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()
    }

    fun provideTransHistoryDao(database: PhraseDatabase): PhraseFavoriteDao {
        return database.phraseDao
    }
    single { provideDatabase(androidApplication()) }
    single { provideTransHistoryDao(get()) }
}

val phraseModules = module {
    single {
        PhrasesDbHelper(androidApplication())
    }
    single {
        PhraseRepository(TinyDB(androidApplication()), get(), get())
    }
    viewModel {
        PhraseViewModel(get())
    }
}