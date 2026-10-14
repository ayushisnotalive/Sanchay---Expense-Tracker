package com.camuel.sanchay

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.camuel.sanchay.utils.Converters

@Database(
    entities = [
        Expense::class,
        Subscription::class,
        SavingsGoal::class,
        Wallet::class,
        Debt::class,
        Category::class,
        Investment::class,
        CategoryBudget::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `wallets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `balance` REAL NOT NULL, `currency` TEXT NOT NULL, `colorHex` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `debts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `personName` TEXT NOT NULL, `amount` REAL NOT NULL, `type` TEXT NOT NULL, `dueDate` TEXT NOT NULL, `isSettled` INTEGER NOT NULL)")
                db.execSQL("ALTER TABLE expenses ADD COLUMN walletId INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN walletId INTEGER NOT NULL DEFAULT 1")
                db.execSQL("INSERT INTO wallets (id, name, balance, currency, colorHex) VALUES (1, 'Main Wallet', 0.0, '$', '#00F3FF')")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN tags TEXT NOT NULL DEFAULT '[]'")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN lastAutoPostDate TEXT")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `iconName` TEXT NOT NULL, `isCustom` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `investments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `symbol` TEXT NOT NULL, `name` TEXT NOT NULL, `quantity` REAL NOT NULL, `averageBuyPrice` REAL NOT NULL, `currentPrice` REAL NOT NULL, `walletId` INTEGER NOT NULL)")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN clientUuid TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE expenses ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE expenses ADD COLUMN syncedAt INTEGER")
                db.execSQL("ALTER TABLE expenses ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")

                db.execSQL("ALTER TABLE subscriptions ADD COLUMN clientUuid TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 1")

                db.execSQL("ALTER TABLE savings_goals ADD COLUMN clientUuid TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE savings_goals ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 1")

                db.execSQL("ALTER TABLE wallets ADD COLUMN clientUuid TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE wallets ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 1")

                db.execSQL("ALTER TABLE debts ADD COLUMN clientUuid TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE debts ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 1")

                // Create performance indices
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_date` ON `expenses` (`date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_category` ON `expenses` (`category`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_walletId` ON `expenses` (`walletId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_isDirty` ON `expenses` (`isDirty`)")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN transactionType TEXT NOT NULL DEFAULT 'EXPENSE'")
                db.execSQL("ALTER TABLE expenses ADD COLUMN transferToWalletId INTEGER")
                db.execSQL("ALTER TABLE expenses ADD COLUMN receiptImageUri TEXT")

                db.execSQL("CREATE TABLE IF NOT EXISTS `category_budgets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `categoryName` TEXT NOT NULL, `monthlyLimit` REAL NOT NULL, `budgetType` TEXT NOT NULL DEFAULT 'NEED', `clientUuid` TEXT NOT NULL DEFAULT '', `isDirty` INTEGER NOT NULL DEFAULT 1)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_category_budgets_categoryName` ON `category_budgets` (`categoryName`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_transactionType` ON `expenses` (`transactionType`)")
            }
        }

        fun getDatabase(context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_database"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .fallbackToDestructiveMigration(false)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            val walletUuid = java.util.UUID.randomUUID().toString()
                            db.execSQL("INSERT OR IGNORE INTO wallets (id, name, balance, currency, colorHex, clientUuid, isDirty) VALUES (1, 'Main Wallet', 0.0, '₹', '#10B981', '$walletUuid', 0)")
                            val defaultCategories = listOf(
                                Pair("Food", "Fastfood"),
                                Pair("Transport", "Flight"),
                                Pair("Shopping", "ShoppingBag"),
                                Pair("Bills", "Receipt"),
                                Pair("Entertainment", "Movie"),
                                Pair("Health", "LocalHospital"),
                                Pair("Education", "School"),
                                Pair("Investment", "TrendingUp"),
                                Pair("Other", "MoreHoriz")
                            )
                            defaultCategories.forEach { (catName, icon) ->
                                db.execSQL("INSERT OR IGNORE INTO categories (name, iconName, isCustom) VALUES ('$catName', '$icon', 0)")
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
