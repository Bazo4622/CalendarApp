package com.usj.calendarapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.usj.calendarapp.model.Account
import com.usj.calendarapp.model.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Event::class, Account::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calendar_app_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.accountDao(), database.eventDao())
                }
            }
        }

        suspend fun populateDatabase(accountDao: AccountDao, eventDao: EventDao) {
            // Create admin account
            val adminAccount = Account(
                id = 1,
                username = "admin",
                email = "admin@example.com",
                password = "admin"
            )
            accountDao.insert(adminAccount)

            // Create default account
            val defaultAccount = Account(
                id = 2,
                username = "default",
                email = "default@example.com",
                password = "default"
            )
            accountDao.insert(defaultAccount)

            // Create holidays
            val holidays = listOf(
                Event(title = "New Year's Day", description = "New Year's Day", date = 1672531200000, time = 0, accountId = 2),
                Event(title = "Independence Day", description = "Independence Day", date = 1688476800000, time = 0, accountId = 2),
                Event(title = "Christmas Day", description = "Christmas Day", date = 1704067200000, time = 0, accountId = 2),
                Event(title = "Labor Day", description = "Labor Day", date = 1693708800000, time = 0, accountId = 2),
                Event(title = "Thanksgiving Day", description = "Thanksgiving Day", date = 1700870400000, time = 0, accountId = 2),
                Event(title = "Memorial Day", description = "Memorial Day", date = 1685404800000, time = 0, accountId = 2),
                Event(title = "Veterans Day", description = "Veterans Day", date = 1700265600000, time = 0, accountId = 2),
                Event(title = "Halloween", description = "Halloween", date = 1698710400000, time = 0, accountId = 2),
                Event(title = "Valentine's Day", description = "Valentine's Day", date = 1676332800000, time = 0, accountId = 2),
                Event(title = "Easter", description = "Easter", date = 1682208000000, time = 0, accountId = 2)
            )
            holidays.forEach { eventDao.insert(it) }
        }
    }
}