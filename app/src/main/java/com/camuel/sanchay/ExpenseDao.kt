package com.camuel.sanchay

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<Expense>)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE isDeleted = 0 ORDER BY date DESC, id DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE isDeleted = 0 AND transactionType = :type ORDER BY date DESC, id DESC")
    fun getExpensesByType(type: String): Flow<List<Expense>>

    @Query("SELECT SUM(CAST(amount AS REAL)) FROM expenses WHERE isDeleted = 0 AND transactionType = 'EXPENSE'")
    fun getTotalExpenses(): Flow<Double?>

    @Query("SELECT SUM(CAST(amount AS REAL)) FROM expenses WHERE isDeleted = 0 AND transactionType = 'INCOME'")
    fun getTotalIncome(): Flow<Double?>

    // Sync helpers
    @Query("SELECT * FROM expenses WHERE isDirty = 1")
    suspend fun getDirtyExpenses(): List<Expense>

    @Query("SELECT * FROM expenses WHERE clientUuid = :uuid LIMIT 1")
    suspend fun getExpenseByClientUuid(uuid: String): Expense?

    @Query("UPDATE expenses SET isDirty = 0, syncedAt = :timestamp WHERE clientUuid IN (:uuids)")
    suspend fun markExpensesSynced(uuids: List<String>, timestamp: Long)

    // --- Subscriptions ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription)

    @Delete
    suspend fun deleteSubscription(subscription: Subscription)

    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<Subscription>>

    // --- Savings Goals ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal)

    @Update
    suspend fun updateSavingsGoal(savingsGoal: SavingsGoal)

    @Delete
    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal)

    @Query("SELECT * FROM savings_goals")
    fun getAllSavingsGoals(): Flow<List<SavingsGoal>>

    // --- Wallets ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: Wallet)

    @Update
    suspend fun updateWallet(wallet: Wallet)

    @Delete
    suspend fun deleteWallet(wallet: Wallet)

    @Query("SELECT * FROM wallets")
    fun getAllWallets(): Flow<List<Wallet>>

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getWalletById(id: Int): Wallet?

    @Query("SELECT COUNT(*) FROM wallets")
    suspend fun getWalletCount(): Int

    // --- Debts ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: Debt)

    @Update
    suspend fun updateDebt(debt: Debt)

    @Delete
    suspend fun deleteDebt(debt: Debt)

    @Query("SELECT * FROM debts ORDER BY isSettled ASC, id DESC")
    fun getAllDebts(): Flow<List<Debt>>

    // --- Investments ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(investment: Investment)

    @Update
    suspend fun updateInvestment(investment: Investment)

    @Delete
    suspend fun deleteInvestment(investment: Investment)

    @Query("SELECT * FROM investments")
    fun getAllInvestments(): Flow<List<Investment>>

    // --- Categories ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    // --- Category Budgets ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryBudget(categoryBudget: CategoryBudget)

    @Update
    suspend fun updateCategoryBudget(categoryBudget: CategoryBudget)

    @Delete
    suspend fun deleteCategoryBudget(categoryBudget: CategoryBudget)

    @Query("SELECT * FROM category_budgets ORDER BY categoryName ASC")
    fun getAllCategoryBudgets(): Flow<List<CategoryBudget>>

    @Query("SELECT * FROM category_budgets WHERE categoryName = :categoryName LIMIT 1")
    suspend fun getCategoryBudgetByName(categoryName: String): CategoryBudget?
}
