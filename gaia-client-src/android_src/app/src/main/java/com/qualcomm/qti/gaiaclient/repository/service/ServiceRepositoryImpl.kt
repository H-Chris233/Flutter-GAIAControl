/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.qualcomm.qti.gaiaclient.BuildConfig
import com.qualcomm.qti.gaiaclient.core.GaiaClientService
import com.qualcomm.qti.gaiaclient.core.tasks.TaskManager
import com.qualcomm.qti.gaiaclient.repository.service.download.DownloadFileRequest
import com.qualcomm.qti.gaiaclient.repository.service.download.DownloadProgress
import com.qualcomm.qti.gaiaclient.repository.service.feedback.FeedbackError
import com.qualcomm.qti.gaiaclient.repository.service.feedback.FeedbackParameters
import com.qualcomm.qti.gaiaclient.repository.service.feedback.FeedbackResponse
import com.qualcomm.qti.gaiaclient.repository.service.feedback.Issue
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.FileData
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.GetFilesError
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.GetFilesParameters
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.GetFilesResponse
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ServiceRepositoryImpl"

@Singleton
class ServiceRepositoryImpl @Inject constructor() : ServiceRepositoryData() {

    private object Server {
        const val token: String = BuildConfig.SERVICE_TOKEN
        val interceptor = HttpLoggingInterceptor().setLevel(
            if (BuildConfig.SERVICE_LOGGING) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        )
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .retryOnConnectionFailure(true)
            .hostnameVerifier { _, _ -> true } // no verification: url is hardcoded
            .connectTimeout(SERVER_TIME_OUT_DEFAULT, TimeUnit.SECONDS)
            .readTimeout(SERVER_TIME_OUT_DEFAULT, TimeUnit.SECONDS)
            .writeTimeout(SERVER_TIME_OUT_DEFAULT, TimeUnit.SECONDS)
            .build()
        lateinit var retrofit: Retrofit
    }

    private lateinit var retrofitService: Service

    private val downloadRequest = AtomicReference<Request?>(null)

    private val taskManager: TaskManager
        get() {
            return GaiaClientService.getTaskManager()
        }

    override fun init() {
        super.init()
        Server.retrofit = Retrofit.Builder()
            .client(Server.client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.SERVICE_BASE_URL)
            .callbackExecutor(taskManager.backgroundExecutor)
            .build()

        retrofitService = Server.retrofit.create(Service::class.java)
    }

    override fun executeSendFeedback(
        liveData: MutableLiveData<Result<Unit, Issue, ServiceError>>,
        parameters: FeedbackParameters
    ) {
        if (!::retrofitService.isInitialized) {
            Log.w(TAG, "[executeSendFeedback] must call ServiceRepository.init() first")
            liveData.value = Result.Error(ServiceError.Internal.NotInitialised)
            return
        }

        val call: Call<FeedbackResponse?>? = retrofitService.sendFeedback(Server.token, parameters)
        val request = SingleRequest(call,
            onError = { taskManager.runOnMain { liveData.value = Result.Error(it) } },
            onSuccess = { taskManager.runOnMain { liveData.value = Result.Success(it.issue ?: Issue()) } },
            parseErrorId = { FeedbackError.valueOf(it) }
        )

        request.execute()
    }

    override fun executeGetFiles(
        liveData: MutableLiveData<Result<Unit, List<FileData>, ServiceError>>,
        parameters: GetFilesParameters
    ) {
        if (!::retrofitService.isInitialized) {
            Log.w(TAG, "[executeGetFiles] must call ServiceRepository.init() first")
            liveData.value = Result.Error(ServiceError.Internal.NotInitialised)
            return
        }

        val call: Call<GetFilesResponse?>? = retrofitService.getFiles(Server.token, parameters)
        val request = SingleRequest(call,
            onError = { taskManager.runOnMain { liveData.value = Result.Error(it) } },
            onSuccess = { taskManager.runOnMain { liveData.value = Result.Success(it.builds) } },
            parseErrorId = { GetFilesError.valueOf(it) }
        )

        request.execute()
    }

    override fun executeDownloadFile(
        liveData: MutableLiveData<Result<DownloadProgress, Uri, ServiceError>>,
        downloadId: String, file: File, uri: Uri
    ) {
        if (!::retrofitService.isInitialized) {
            Log.w(TAG, "[executeDownloadFile] must call ServiceRepository.init() first")
            liveData.value = Result.Error(ServiceError.Internal.NotInitialised)
            return
        }

        val call: Call<ResponseBody?>? = retrofitService.downloadFile(Server.token, downloadId)
        val request = DownloadFileRequest(call, file,
            onProgress = {
                taskManager.runOnMain {
                    val result = Result.Progress<DownloadProgress, Uri, ServiceError>(DownloadProgress(it))
                    if (it < 100) {
                        liveData.postValue(result)
                    } else {
                        liveData.value = result // force 100% update to liveData
                    }
                }
            },
            onError = { taskManager.runOnMain { liveData.value = Result.Error(it) } },
            onSuccess = { taskManager.runOnMain { liveData.value = Result.Success(uri) } },
            onComplete = { downloadRequest.set(null) }
        )

        // cancel previous if existing
        val previous = downloadRequest.getAndSet(request)
        previous?.cancel()

        // start new request
        request.execute()
    }

    override fun cancelDownload() {
        val call = downloadRequest.getAndSet(null)
        call?.cancel()
    }

    companion object {
        private const val SERVER_TIME_OUT_DEFAULT = 60L
    }
}
