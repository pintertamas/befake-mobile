package com.pintertamas.befake.network.service

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import com.pintertamas.befake.network.request.JwtRequest
import com.pintertamas.befake.network.request.UserRequest
import com.pintertamas.befake.network.response.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Url
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import kotlin.concurrent.thread
import kotlin.reflect.KFunction2

class RetrofitService() {

    private val networkService: NetworkService
    private var token: String = ""

    constructor(token: String) : this() {
        this.token = token
    }

    init {
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val newRequest: Request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        }).build()
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(NetworkService.ENDPOINT_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        this.networkService = retrofit.create(NetworkService::class.java)
    }

    private fun <T> runCallOnBackgroundThread(
        call: Call<T>,
        onSuccess: (Int, T) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        Log.d("RETROFIT_SERVICE", call.request().toString())
        val handler = Handler(Looper.getMainLooper()!!)
        thread {
            var response: Response<T>? = null
            try {
                response = call.execute()
                Log.d("RESPONSE_CODE", response.code().toString())
                if (response == null || !response.code().toString()
                        .startsWith("2")
                ) throw Exception()
                val responseBody = response.body()!!
                handler.post { onSuccess(response.code(), responseBody) }
            } catch (e: Exception) {
                val code: Int = response?.code() ?: 500
                e.printStackTrace()
                handler.post { onError(code, e) }
            }
        }
    }

    private fun <T, R> runCallOnBackgroundThreadWithParameterReturn(
        param: R,
        call: Call<T>,
        onSuccess: (Int, T, R) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        Log.d("RETROFIT_SERVICE", call.request().toString())
        val handler = Handler(Looper.getMainLooper()!!)
        thread {
            var response: Response<T>? = null
            try {
                response = call.execute()
                Log.d("RESPONSE_CODE", response.code().toString())
                if (response == null || !response.code().toString()
                        .startsWith("2")
                ) throw Exception()
                val responseBody = response.body()!!
                handler.post { onSuccess(response.code(), responseBody, param) }
            } catch (e: Exception) {
                val code: Int = response?.code() ?: 500
                e.printStackTrace()
                handler.post { onError(code, e) }
            }
        }
    }

    private fun <T> loadImageIntoViewOnBackgroundThread(
        call: Call<T>,
        filename: String,
        view: ImageView,
        onSuccess: (Int, T, String, ImageView) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        Log.d("RETROFIT_SERVICE", call.request().toString())
        val handler = Handler(Looper.getMainLooper()!!)
        thread {
            var response: Response<T>? = null
            try {
                response = call.execute()
                Log.d("RESPONSE_CODE", response.code().toString())
                if (response == null || !response.code().toString()
                        .startsWith("2")
                ) throw Exception()
                val responseBody = response.body()!!
                handler.post { onSuccess(response.code(), responseBody, filename, view) }
            } catch (e: Exception) {
                val code: Int = response?.code() ?: 500
                e.printStackTrace()
                handler.post { onError(code, e) }
            }
        }
    }

    fun login(
        username: String,
        password: String,
        onSuccess: (Int, JwtResponse) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val loginRequest = networkService.login(JwtRequest(username, password))
        runCallOnBackgroundThread(loginRequest, onSuccess, onError)
    }

    fun register(
        username: String,
        password: String,
        fullName: String?,
        email: String,
        bibliography: String?,
        location: String?,
        onSuccess: (Int, UserResponse) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val registerRequest = networkService.register(
            UserRequest(
                username,
                password,
                fullName,
                email,
                bibliography,
                location
            )
        )
        runCallOnBackgroundThread(registerRequest, onSuccess, onError)
    }

    fun getUser(
        userId: Long,
        onSuccess: (Int, UserResponse) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val getUserRequest = networkService.getUser(userId)
        runCallOnBackgroundThread(getUserRequest, onSuccess, onError)
    }

    fun getPostsFromFriends(
        onSuccess: (Int, List<PostResponse>) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val getPostsFromFriendsRequest = networkService.getPostsFromFriends()
        runCallOnBackgroundThread(getPostsFromFriendsRequest, onSuccess, onError)
    }

    fun getCommentsOnPost(
        postId: Long,
        onSuccess: (Int, List<CommentResponse>) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val getCommentsOnPostRequest = networkService.getCommentsOnPost(postId)
        runCallOnBackgroundThread(getCommentsOnPostRequest, onSuccess, onError)
    }

    fun getReactionsOnPost(
        postId: Long,
        onSuccess: (Int, List<ReactionResponse>) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val getReactionsOnPostRequest = networkService.getReactionsOnPost(postId)
        runCallOnBackgroundThread(getReactionsOnPostRequest, onSuccess, onError)
    }

    fun getTodaysPostByUser(
        userId: Long,
        onSuccess: (Int, PostResponse) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val getTodaysPostRequest = networkService.getTodaysPostByUser(userId)
        runCallOnBackgroundThread(getTodaysPostRequest, onSuccess, onError)
    }

    fun getProfilePictureUrl(
        user: UserResponse,
        view: ImageView,
        onSuccess: (Int, ResponseBody, String, ImageView) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        println("Creating getProfilePictureUrl request with userid: ${user.id}")
        val getProfilePictureUrlRequest = networkService.getProfilePictureUrl(user.id)
        loadImageIntoViewOnBackgroundThread(
            getProfilePictureUrlRequest,
            user.profilePicture ?: "",
            view,
            onSuccess,
            onError
        )
    }

    fun editProfilePicture(
        file: MultipartBody.Part,
        onSuccess: (Int, UserResponse) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val getProfilePictureUrlRequest = networkService.uploadProfilePicture(file)
        runCallOnBackgroundThread(
            getProfilePictureUrlRequest,
            onSuccess,
            onError
        )
    }

    fun loadProfilePictureUrlIntoView(
        id: Long,
        view: ImageView,
        onSuccess: (Int, ResponseBody, String, ImageView) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val getProfilePictureUrlRequest = networkService.getProfilePictureUrl(id)
        loadImageIntoViewOnBackgroundThread(
            getProfilePictureUrlRequest,
            id.toString(),
            view,
            onSuccess,
            onError
        )
    }

    fun loadPostImageUrlIntoView(
        filename: String,
        view: ImageView,
        onSuccess: (Int, ResponseBody, String, ImageView) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val loadImageRequest = networkService.getImageUrl(filename)
        loadImageIntoViewOnBackgroundThread(loadImageRequest, filename, view, onSuccess, onError)
    }

    fun loadReactionImageUrlIntoView(
        filename: String,
        view: ImageView,
        onSuccess: (Int, ResponseBody, String, ImageView) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val loadImageRequest = networkService.getReactionImageUrl(filename)
        loadImageIntoViewOnBackgroundThread(loadImageRequest, filename, view, onSuccess, onError)
    }

    fun canUserPost(
        onSuccess: (Int, Boolean) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val getLastPostByUserRequest = networkService.canUserPost()
        runCallOnBackgroundThread(getLastPostByUserRequest, onSuccess, onError)
    }

    fun getBeFakeTime(
        onSuccess: (Int, String) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val getBeFakeTimeRequest = networkService.getBeFakeTime()
        runCallOnBackgroundThread(getBeFakeTimeRequest, onSuccess, onError)
    }

    fun loadUserList(
        onSuccess: (Int, List<UserResponse>) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val userListRequest = networkService.loadUserList()
        runCallOnBackgroundThread(userListRequest, onSuccess, onError)
    }

    fun loadPendingRequests(
        onSuccess: (Int, List<FriendshipResponse>) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val pendingListRequest = networkService.getPendingRequests()
        runCallOnBackgroundThread(pendingListRequest, onSuccess, onError)
    }

    fun loadFriendList(
        onSuccess: (Int, List<Long>) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val pendingListRequest = networkService.getListOfFriends()
        runCallOnBackgroundThread(pendingListRequest, onSuccess, onError)
    }

    fun addFriend(
        userId: Long,
        onSuccess: (Int, FriendshipResponse, Long) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val addFriendRequest = networkService.addFriend(userId)
        runCallOnBackgroundThreadWithParameterReturn(userId, addFriendRequest, onSuccess, onError)
    }

    fun acceptRequest(
        userId: Long,
        onSuccess: (Int, FriendshipResponse, Long) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val acceptFriendRequest = networkService.acceptFriendRequest(userId)
        runCallOnBackgroundThreadWithParameterReturn(
            userId,
            acceptFriendRequest,
            onSuccess,
            onError
        )
    }

    fun removeFriend(
        userId: Long,
        onSuccess: (Int, Boolean, Long) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val removeFriendRequest = networkService.rejectFriend(userId)
        runCallOnBackgroundThreadWithParameterReturn(
            userId,
            removeFriendRequest,
            onSuccess,
            onError
        )
    }

    fun getUserByUserId(
        userId: Long,
        onSuccess: (Int, UserResponse) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val pendingListRequest = networkService.getUserByUserId(userId)
        runCallOnBackgroundThread(pendingListRequest, onSuccess, onError)
    }

    fun editUser(
        userRequest: UserRequest,
        onSuccess: (Int, UserResponse) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val editUserRequest = networkService.editUser(userRequest)
        runCallOnBackgroundThread(editUserRequest, onSuccess, onError)
    }

    fun createPost(
        mainPhoto: MultipartBody.Part,
        selfiePhoto: MultipartBody.Part,
        location: RequestBody,
        onSuccess: (Int, PostResponse) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val editUserRequest = networkService.createPost(mainPhoto, selfiePhoto, location)
        runCallOnBackgroundThread(editUserRequest, onSuccess, onError)
    }

    fun react(
        reaction: MultipartBody.Part,
        postId: Long,
        position: Int,
        onSuccess: (Int, ReactionResponse, Int) -> Unit,
        onError: (Int, Throwable) -> Unit
    ) {
        val reactionRequest = networkService.react(reaction, postId)
        runCallOnBackgroundThreadWithParameterReturn(position, reactionRequest, onSuccess, onError)
    }

    /*fun downloadIMageFromUrl(url: URL): ByteArray {
        val baos = ByteArrayOutputStream()
        var inputStream: InputStream? = null
        try {
            inputStream = url.openStream()
            val byteChunk = ByteArray(4096)
            var n: Int
            while (inputStream.read(byteChunk).also { n = it } > 0) {
                baos.write(byteChunk, 0, n)
            }
        } catch (e: IOException) {
            System.err.printf(
                "Failed while reading bytes from %s: %s",
                url.toExternalForm(),
                e.message
            )
            e.printStackTrace()
            // Perform any other exception handling that's appropriate.
        } finally {
            inputStream?.close()
        }
        return baos.toByteArray()
    }*/
}