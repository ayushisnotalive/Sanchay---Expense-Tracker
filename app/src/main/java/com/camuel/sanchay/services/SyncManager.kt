package com.camuel.sanchay.services

import android.content.Context
import com.camuel.sanchay.Expense
import com.camuel.sanchay.ExpenseDao
import com.camuel.sanchay.ExpenseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

enum class SyncState {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE,
    ERROR
}

class SyncManager(private val context: Context) {

    private val expenseDao: ExpenseDao = ExpenseDatabase.getDatabase(context).expenseDao()
    private val prefs = context.getSharedPreferences("sanchay_sync_prefs", Context.MODE_PRIVATE)

    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(prefs.getLong("last_sync_time", 0L))
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    suspend fun performSync(authToken: String? = null): Boolean = withContext(Dispatchers.IO) {
        if (_syncState.value == SyncState.SYNCING) return@withContext false

        _syncState.value = SyncState.SYNCING

        val token = authToken ?: prefs.getString("auth_token", null)
        val bearer = if (token != null && !token.startsWith("Bearer ")) "Bearer $token" else (token ?: "")

        try {
            // 1. Gather dirty local records
            val dirtyExpenses = expenseDao.getDirtyExpenses()

            if (dirtyExpenses.isNotEmpty() && bearer.isNotBlank()) {
                val dtos = dirtyExpenses.map { e ->
                    NetworkExpenseDto(
                        clientUuid = e.clientUuid,
                        amount = e.amount,
                        category = e.category,
                        date = e.date,
                        paymentMethod = e.paymentMethod,
                        note = e.note,
                        latitude = e.latitude,
                        longitude = e.longitude,
                        walletId = e.walletId.toLong(),
                        tags = e.tags,
                        deleted = e.isDeleted
                    )
                }

                val pushResponse = SanchayApiClient.syncApiService.pushDelta(bearer, NetworkPushRequest(dtos))
                if (pushResponse.success) {
                    val now = System.currentTimeMillis()
                    val uuids = dirtyExpenses.map { it.clientUuid }
                    expenseDao.markExpensesSynced(uuids, now)
                }
            }

            // 2. Pull remote delta updates
            if (bearer.isNotBlank()) {
                val pullResponse = SanchayApiClient.syncApiService.pullDelta(bearer, null)
                if (pullResponse.success && pullResponse.data != null) {
                    val serverExpenses = pullResponse.data.expenses
                    for (remote in serverExpenses) {
                        val existing = expenseDao.getExpenseByClientUuid(remote.clientUuid)
                        if (existing == null) {
                            val newEntity = Expense(
                                clientUuid = remote.clientUuid,
                                amount = remote.amount,
                                category = remote.category,
                                date = remote.date,
                                paymentMethod = remote.paymentMethod ?: "Cash",
                                note = remote.note ?: "",
                                latitude = remote.latitude,
                                longitude = remote.longitude,
                                walletId = (remote.walletId ?: 1L).toInt(),
                                tags = remote.tags ?: emptyList(),
                                isDirty = false,
                                syncedAt = System.currentTimeMillis(),
                                isDeleted = remote.deleted
                            )
                            expenseDao.insert(newEntity)
                        } else if (!existing.isDirty) {
                            // Update local record only if not locally modified
                            existing.amount = remote.amount
                            existing.category = remote.category
                            existing.date = remote.date
                            existing.paymentMethod = remote.paymentMethod ?: existing.paymentMethod
                            existing.note = remote.note ?: existing.note
                            existing.latitude = remote.latitude
                            existing.longitude = remote.longitude
                            existing.walletId = (remote.walletId ?: 1L).toInt()
                            existing.tags = remote.tags ?: existing.tags
                            existing.isDeleted = remote.deleted
                            existing.syncedAt = System.currentTimeMillis()
                            expenseDao.update(existing)
                        }
                    }
                }
            }

            val now = System.currentTimeMillis()
            prefs.edit().putLong("last_sync_time", now).apply()
            _lastSyncTimestamp.value = now
            _syncState.value = SyncState.SYNCED
            return@withContext true
        } catch (e: Exception) {
            // Offline or network error
            _syncState.value = SyncState.OFFLINE
            return@withContext false
        }
    }

    fun setAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    companion object {
        @Volatile
        private var INSTANCE: SyncManager? = null

        fun getInstance(context: Context): SyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SyncManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
