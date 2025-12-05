package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class PhrasesDbHelper (private val myContext: Context) : SQLiteOpenHelper(
    myContext, DB_NAME, null, DB_VERSION
) {
    //    private val dbPath = "/data/data/${myContext.packageName}/abc/databases/"
    private val dbPath = "/data/data/${myContext.packageName}/abc/databases/"
    private val dbPathAlternative = myContext.applicationInfo.dataDir + "/databases/"

    private var myDataBase: SQLiteDatabase? = null

    //Create a empty database on the system
    @Throws(IOException::class)
    fun createDatabase2() {
        if (!isDatabaseExists()) {
            this.readableDatabase
            try {
                close()
                copyDataBase()
            } catch (e: IOException) {
                throw Error("Error copying database")
            }
        }
    }

    @Throws(IOException::class)
    fun createDatabase() {
        if (!isDatabaseExists()) {
            var db: SQLiteDatabase? = null
            try {
                db = this.readableDatabase
            } finally {
                db?.close()
            }

            try {
                copyDataBase()
            } catch (e: IOException) {
                try {
                    copyDataBaseAlternativePath()
                } catch (ex: IOException) {
                    throw IOException("Error copying database ", ex)
                }
            }
        }
    }

    //Check database already exist or not
    private fun isDatabaseExists(): Boolean {
        var checkDB = false
        try {
            val myPath = dbPath + DB_NAME
            val dbFile = File(myPath)
            checkDB = dbFile.exists()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return checkDB
    }

    //Copies your database from your local assets-folder to the just created empty database in the system folder
    @Throws(IOException::class)
    private fun copyDataBase() {
        val mInput = myContext.assets.open("$DB_NAME.db")
        val outFileName = dbPath + DB_NAME
        val mOutput: OutputStream = FileOutputStream(outFileName)
        val mBuffer = ByteArray(2024)

        var mLength: Int
        try {
            while (mInput.read(mBuffer).also { mLength = it } > 0) {
                mOutput.write(mBuffer, 0, mLength)
            }
            mOutput.flush()
        } finally {
            mOutput.close()
            mInput.close()
        }

    }

    @Throws(IOException::class)
    private fun copyDataBaseAlternativePath() {
        val mInput = myContext.assets.open("$DB_NAME.db")
        val outFileNameAlternative = dbPathAlternative + DB_NAME
        val mOutput: OutputStream = FileOutputStream(outFileNameAlternative)
        val mBuffer = ByteArray(2024)

        var mLength: Int
        try {
            while (mInput.read(mBuffer).also { mLength = it } > 0) {
                mOutput.write(mBuffer, 0, mLength)
            }
            mOutput.flush()
        } finally {
            mOutput.close()
            mInput.close()
        }
    }


    private fun deleteDb() {
        val file = File(dbPath + DB_NAME)
        if (file.exists()) {
            file.delete()
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (newVersion > oldVersion) {
            deleteDb()
        }
    }

    companion object {
        private const val DB_NAME = "Phrasebook"
        const val DB_VERSION = 1
    }

}