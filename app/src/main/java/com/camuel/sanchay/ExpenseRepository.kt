package com.camuel.sanchay

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    
    val totalExpenses: Flow<Double?> = expenseDao.getTotalExpenses()

    val totalIncome: Flow<Double?> = expenseDao.getTotalIncome()

    fun getExpensesByType(type: String): Flow<List<Expense>> = expenseDao.getExpensesByType(type)

    suspend fun insert(expense: Expense) {
        expenseDao.insert(expense)
    }
    
    suspend fun update(expense: Expense) {
        expenseDao.update(expense)
    }

    suspend fun delete(expense: Expense) {
        expenseDao.delete(expense)
    }

    // --- Subscriptions ---
    val allSubscriptions: Flow<List<Subscription>> = expenseDao.getAllSubscriptions()

    suspend fun insertSubscription(subscription: Subscription) {
        expenseDao.insertSubscription(subscription)
    }

    suspend fun deleteSubscription(subscription: Subscription) {
        expenseDao.deleteSubscription(subscription)
    }

    // --- Savings Goals ---
    val allSavingsGoals: Flow<List<SavingsGoal>> = expenseDao.getAllSavingsGoals()

    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal) {
        expenseDao.insertSavingsGoal(savingsGoal)
    }

    suspend fun updateSavingsGoal(savingsGoal: SavingsGoal) {
        expenseDao.updateSavingsGoal(savingsGoal)
    }

    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal) {
        expenseDao.deleteSavingsGoal(savingsGoal)
    }

    // --- Wallets ---
    val allWallets: Flow<List<Wallet>> = expenseDao.getAllWallets()

    suspend fun insertWallet(wallet: Wallet) {
        expenseDao.insertWallet(wallet)
    }

    suspend fun updateWallet(wallet: Wallet) {
        expenseDao.updateWallet(wallet)
    }

    suspend fun deleteWallet(wallet: Wallet) {
        expenseDao.deleteWallet(wallet)
    }

    suspend fun getWalletById(id: Int): Wallet? {
        return expenseDao.getWalletById(id)
    }

    suspend fun getWalletCount(): Int = expenseDao.getWalletCount()

    // --- Debts ---
    val allDebts: Flow<List<Debt>> = expenseDao.getAllDebts()

    suspend fun insertDebt(debt: Debt) {
        expenseDao.insertDebt(debt)
    }

    suspend fun updateDebt(debt: Debt) {
        expenseDao.updateDebt(debt)
    }

    suspend fun deleteDebt(debt: Debt) {
        expenseDao.deleteDebt(debt)
    }

    // --- Investments ---
    val allInvestments: Flow<List<Investment>> = expenseDao.getAllInvestments()

    suspend fun insertInvestment(investment: Investment) {
        expenseDao.insertInvestment(investment)
    }

    suspend fun updateInvestment(investment: Investment) {
        expenseDao.updateInvestment(investment)
    }

    suspend fun deleteInvestment(investment: Investment) {
        expenseDao.deleteInvestment(investment)
    }

    // --- Categories ---
    val allCategories: Flow<List<Category>> = expenseDao.getAllCategories()

    suspend fun getCategoryCount(): Int = expenseDao.getCategoryCount()

    suspend fun insertCategory(category: Category) {
        expenseDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        expenseDao.deleteCategory(category)
    }

    // --- Category Budgets ---
    val allCategoryBudgets: Flow<List<CategoryBudget>> = expenseDao.getAllCategoryBudgets()

    suspend fun insertCategoryBudget(categoryBudget: CategoryBudget) {
        expenseDao.insertCategoryBudget(categoryBudget)
    }

    suspend fun updateCategoryBudget(categoryBudget: CategoryBudget) {
        expenseDao.updateCategoryBudget(categoryBudget)
    }

    suspend fun deleteCategoryBudget(categoryBudget: CategoryBudget) {
        expenseDao.deleteCategoryBudget(categoryBudget)
    }

    suspend fun getCategoryBudgetByName(categoryName: String): CategoryBudget? {
        return expenseDao.getCategoryBudgetByName(categoryName)
    }
}
