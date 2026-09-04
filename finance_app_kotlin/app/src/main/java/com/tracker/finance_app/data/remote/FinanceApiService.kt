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
    val timestamp: Long,
    val uniqueIdentifier: String? = null
)

@Serializable
data class SyncBatchUploadRequest(
    val smsList: List<SmsMessagePayload>,
    val fromTimestamp: Long
)

@Serializable
data class SyncBatchUploadResponse(
    val newCount: Int = 0
)

@Serializable
data class BatchUpdateTransactionRequest(
    val id: String,
    val transactionName: String,
    val amount: Double,
    val type: String,
    val categoryId: String,
    val accountId: String,
    val occurredAt: String,
    val merchant: String? = null,
    val notes: String? = null,
    val tags: List<String> = emptyList(),
    val currency: String = "INR"
)

@Serializable
data class CreateTransactionRequest(
    val transactionName: String,
    val amount: Double,
    val type: String,
    val categoryId: String?,
    val accountId: String? = null,
    val occurredAt: String,
    val merchant: String? = null,
    val notes: String? = null,
    val tags: List<String> = emptyList(),
    val currency: String = "INR"
)

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val type: String,
    val parentId: String? = null,
    val iconKey: String? = "default-folder",
    val colorCode: String? = "#087B3D",
    val description: String? = null
)

@Serializable
data class UpdateCategoryRequest(
    val name: String,
    val description: String? = null,
    val isActive: Boolean? = true,
    val parentId: String? = null,
    val iconKey: String? = "default-folder",
    val colorCode: String? = "#808080"
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
    suspend fun createCategory(@Body request: CreateCategoryRequest): Category

    @PUT("/api/v1/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body request: UpdateCategoryRequest
    ): Category

    @DELETE("/api/v1/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String)

    @GET("/api/v1/categories/subcategories")
    suspend fun getSubcategories(): List<Category>

    // Transactions
    @GET("/api/v1/transactions")
    suspend fun getTransactions(@Query("version") version: Int = 1): List<Transaction>

    @POST("/api/v1/transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): Transaction

    @PUT("/api/v1/transactions/{id}")
    suspend fun updateTransaction(@Path("id") id: String, @Body transaction: Transaction): Transaction

    @DELETE("/api/v1/transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String)

    @POST("/api/v1/transactions/analysis")
    suspend fun getExpenseReport(@Body params: ExpenseReportRequest): MonthlyExpenseResponse

    @POST("/api/v1/transactions/export-messages")
    suspend fun exportSmsMessages(@Body messages: List<SmsMessagePayload>)

    // SMS Drafts
    @GET("/api/v1/transactions")
    suspend fun getDrafts(@Query("version") version: Int = 3): List<TransactionDraft>

    @PUT("/api/v1/transactions/batch")
    suspend fun batchUpdateTransactions(@Body requests: List<BatchUpdateTransactionRequest>)

    @POST("/api/v1/transactions/drafts/batch-delete")
    suspend fun deleteDraftsBatch(@Body ids: List<String>)

    // Sync — 2-step protocol: watermark handshake, then batch upload
    @GET("/api/sync/latest-timestamp")
    suspend fun getSyncLatestTimestamp(): SyncTimestamp

    @POST("/api/sync/batch-upload")
    suspend fun syncBatchUpload(@Body request: SyncBatchUploadRequest): SyncBatchUploadResponse
}
