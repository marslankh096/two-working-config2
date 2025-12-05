package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.di

import android.app.Application
import androidx.room.Room
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.WordDictionaryDatabase
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.WordSuggestionDatabase
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao.BookmarkWordDao
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao.RecentWordDao
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.dao.WordSuggestionDao
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.repo.WordDictionaryRepository
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.dictionary.WordDictionaryClient
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.dictionary.WordDictionaryService
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.dictionary.DictionaryViewModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.dictionaryBookmark.BookmarkViewModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.dictionaryDetail.DictionaryDetailViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dictionaryDatabaseModule = module {
    fun getDatabase(application: Application): WordDictionaryDatabase {
        return Room.databaseBuilder(
            application,
            WordDictionaryDatabase::class.java,
            "word_dictionary"
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    fun getRecentDao(database: WordDictionaryDatabase): RecentWordDao {
        return database.recentWordDao
    }

    fun getBookmarkDao(database: WordDictionaryDatabase): BookmarkWordDao {
        return database.bookmarkWordDao
    }

    single { getDatabase(androidApplication()) }
    single { getRecentDao(get()) }
    single { getBookmarkDao(get()) }
}

val dictionaryRepositoryModule = module {
    fun getDictionaryRepository(
        dictionaryService: WordDictionaryService,
        recentWordDao: RecentWordDao,
        bookmarkWordDao: BookmarkWordDao,
        wordSuggestionDao: WordSuggestionDao
    ): WordDictionaryRepository {
        return WordDictionaryRepository(
            dictionaryService,
            recentWordDao,
            bookmarkWordDao,
            wordSuggestionDao
        )
    }

    single {
        getDictionaryRepository(
            WordDictionaryClient.wordDictionaryService,
            get(),
            get(),
            get()
        )
    }

}

val suggestedWordsDatabaseModule = module {
    fun getSuggestionDatabase(application: Application): WordSuggestionDatabase {
        return Room.databaseBuilder(
            application,
            WordSuggestionDatabase::class.java,
            "words_suggestion"
        )
            .createFromAsset("words_suggestion.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    fun getSuggestedWordsDao(database: WordSuggestionDatabase): WordSuggestionDao {
        return database.suggestedWordDao
    }

    single { getSuggestionDatabase(androidApplication()) }
    single { getSuggestedWordsDao(get()) }
}

private val _viewModelModule = module {
    viewModel {
        DictionaryViewModel(get())
    }
    viewModel {
        DictionaryDetailViewModel(get())
    }
    viewModel {
        BookmarkViewModel(get())
    }
}

val viewModelModule get() = _viewModelModule