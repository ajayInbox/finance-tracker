package com.tracker.finance_app.data.remote

import com.tracker.finance_app.domain.model.*
import kotlinx.serialization.Serializable
import retrofit2.http.*

@Serializable
data class SignInRequest(val email: String, val password: String)

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val name: String? = null,
    val lastName: String? = null
)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class PasswordResetRequest(val email: String)

@Serializable
data class SmsMessagePayload(
    val body: String,
    val sender: String,
    val timestamp: Long
)

interface FinanceApiService {
    // Auth
    @POST("/auth/login")
    suspend fun signIn(@Body request: SignInRequest): AuthTokens

    @POST("/auth/register")
    suspend fun signUp(@Body request: SignUpRequest): AuthTokens

    @POST("/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): AuthTokens

    @POST("/auth/logout")
    suspend fun logout(@Body refreshToken: String)

    @GET("/auth/user")
    suspend fun getUserProfile(): UserProfile

    @POST("/auth/password-reset")
    suspend fun sendPasswordReset(@Body email: String)

    @DELETE("/auth/user")
    suspend fun deleteUserAccount()

    // Accounts
    @GET("/api/v1/accounts")
    suspend fun getAccounts(): List<Account>

    @POST("/api/v1/account")
    suspend fun createAccount(@Body request: AccountCreateUpdateRequest): Account

    @PUT("/api/v1/accounts/{id}")
    suspend fun updateAccount(@Path("id") id: String, @Body request: AccountCreateUpdateRequest): Account

    @DELETE("/api/v1/accounts/{id}")
    suspend fun deleteAccount(@Path("id") id: String)

    @GET("/api/v1/networth")
    suspend fun getNetWorthSummary(): NetWorthSummary

    @POST("/api/v1/accounts/initialize-defaults")
    suspend fun initializeDefaults(): Account

    // Categories
    @GET("/api/v1/categories")
    suspend fun getCategories(): List<Category>

    @POST("/api/v1/categories")
    suspend fun createCategory(@Body category: Category): Category

    @DELETE("/api/v1/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String)

    @GET("/api/v1/categories/subcategories")
    suspend fun getSubcategories(): List<Category>

    // Transactions
    @GET("/api/v1/transactions")
    suspend fun getTransactions(@Query("version") version: Int = 1): List<Transaction>

    @POST("/api/v1/transactions")
    suspend fun createTransaction(@Body transaction: Transaction): Transaction

    @PUT("/api/v1/transactions/{id}")
    suspend fun updateTransaction(@Path("id") id: String, @Body transaction: Transaction): Transaction

    @DELETE("/api/v1/transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String)

    @GET("/api/v1/transactions/summary")
    suspend fun getTransactionSummary(): TransactionSummary

    @GET("/api/v1/transactions/breakdown")
    suspend fun getCategoryBreakdown(): List<CategoryBreakdown>

    @POST("/api/v1/transactions/avg-daily")
    suspend fun getAverageDailyExpense(): AverageDailyExpense

    @POST("/api/v1/transactions/analysis")
    suspend fun getExpenseReport(@Body params: ExpenseReportRequest): ExpenseReport

    @POST("/api/v1/transactions/export-messages")
    suspend fun exportSmsMessages(@Body messages: List<SmsMessagePayload>)

    // SMS Drafts
    @GET("/api/v1/transactions")
    suspend fun getDrafts(@Query("version") version: Int = 3): List<TransactionDraft>

    @POST("/api/v1/transactions/drafts/batch-delete")
    suspend fun deleteDraftsBatch(@Body ids: List<String>)

    // Sync
    @GET("/api/sync/latest-timestamp")
    suspend fun getSyncLatestTimestamp(): SyncTimestamp

    @POST("/api/sync/batch-upload")
    suspend fun syncBatchUpload(@Body messages: List<SmsMessagePayload>)
}
