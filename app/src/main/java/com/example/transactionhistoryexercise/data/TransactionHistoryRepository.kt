package com.example.transactionhistoryexercise.data

import com.example.transactionhistoryexercise.MainApplication
import com.example.transactionhistoryexercise.api.ApiService
import com.example.transactionhistoryexercise.data.model.TransactionHistory
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionHistoryRepository @Inject constructor(
    private val apiService: ApiService) {

    // fetch transaction data in IO thread
    suspend fun getTransactionHistory(): TransactionHistory? {
        return withContext(Dispatchers.IO) {
            val response = apiService.fetchLatestDataWithFixedUrl()
            if (response.isSuccessful) {
                val file = async { fileSavedToLocal(response) }
                return@withContext parseJsonFile(file.await())
            } else {
                throw TransactionHistoryRefreshError(Throwable(response.message()))
            }
        }
    }

    // parse data.json file
    private fun parseJsonFile(file: File): TransactionHistory? {
        return jsonParserWithMoshi(file.readText())
    }

    private fun jsonParserWithMoshi(jsonString: String): TransactionHistory? {
        val moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<TransactionHistory> = moshi.adapter(TransactionHistory::class.java)
        return adapter.fromJson(jsonString)
    }

    // download data.json file from
    private suspend fun fileSavedToLocal(result: Response<ResponseBody>): File {
        return withContext(Dispatchers.IO) {
            val file = File(MainApplication.instance.filesDir, "data.json")
            result.body()!!.byteStream().use { input ->
                file.outputStream().use { output ->
                    var sizeRead = 0
                    val read = ByteArray(4096)
                    var len = 0
                    while (input.read(read).also { len = it } != -1) {
                        if (!isActive) {
                            return@withContext File("")
                        }
                        output.write(read, 0, len)
                        sizeRead += len
                    }
                }
            }
            return@withContext file
        }
    }

    class TransactionHistoryRefreshError(cause: Throwable) : Throwable(cause.message, cause)
}