package com.pintertamas.befake.database.repository

import android.util.Log
import android.widget.ImageView
import com.pintertamas.befake.R
import com.pintertamas.befake.database.ImageCacheDatabase
import com.pintertamas.befake.database.converter.ImageBitmapString
import com.pintertamas.befake.network.response.UserResponse
import com.pintertamas.befake.network.service.RetrofitService
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody


object CacheService {

    private var instance: CacheService? = null

    fun getInstance(): CacheService? {
        if (instance == null) {
            synchronized(CacheService::class.java) {
                if (instance == null) {
                    instance = CacheService
                }
            }
        }
        return instance
    }

    private lateinit var network: RetrofitService
    private val picasso: Picasso by lazy { Picasso.get() }

    private val cache: HashMap<String, String> = HashMap()
    //private lateinit var db: ImageCacheDao

    private fun put(key: String, value: String) {
        //db.put(key, value)
        cache[key] = value
    }

    private fun get(key: String): String? {
        //return db.get(key)
        return cache[key]
    }

    private fun update(key: String, value: String) {
        //db.update(key, value)
        cache[key] = value
    }

    fun clear() {
        //db.clear()
        cache.clear()
    }

    fun resetKey(key: String) {
        cache.remove(key)
    }

    fun cacheProfilePicture(user: UserResponse, target: ImageView) {
        val tag = user.profilePicture ?: return
        val uri = get(tag)
        if (uri == null) {
            Log.d("CACHE_PROFILE_PICTURE", "Cache missed, downloading image with tag: $tag")
            loadProfilePictureIntoView(user, target)
        } else {
            Log.d("CACHE_PROFILE_PICTURE", "Cache hit, loading image with tag: $tag")
            val bitmap = ImageBitmapString.StringToBitMap(uri)
            //target.setImageBitmap(bitmap)
            picasso
                .load(uri)
                .placeholder(R.color.primaryAccent)
                .into(target)
        }
    }

    private fun cache(tag: String, target: ImageView, fn: (String, ImageView) -> Unit) {
        val uri = get(tag)
        if (uri == null) {
            Log.d("CACHE_PROFILE_PICTURE_F", "Cache missed, downloading image with tag: $tag")
            fn(tag, target)
        } else {
            Log.d("CACHE_PROFILE_PICTURE_F", "Cache hit, loading image with tag: $tag")
            picasso
                .load(uri)
                .placeholder(R.color.primaryAccent)
                .into(target)
        }
    }

    fun cacheProfilePicture(userId: Long, target: ImageView) {
        val tag = userId.toString()
        cache(tag, target, this::loadProfilePictureIntoView)
    }

    fun cachePostImage(filename: String, target: ImageView) {
        cache(filename, target, this::loadPostImageIntoView)
    }

    fun cacheReactionImage(filename: String, target: ImageView) {
        cache(filename, target, this::loadReactionImageIntoView)
    }

    private fun loadProfilePictureIntoView(user: UserResponse, view: ImageView) {
        if (user.profilePicture == null) return
        network.getProfilePictureUrl(
            user = user,
            view = view,
            onSuccess = this::getImageUrlSuccess,
            onError = this::genericError
        )
    }

    private fun loadProfilePictureIntoView(id: String, view: ImageView) {
        network.loadProfilePictureUrlIntoView(
            id = java.lang.Long.decode(id),
            view = view,
            onSuccess = this::getImageUrlSuccess,
            onError = this::genericError
        )
    }

    private fun loadPostImageIntoView(filename: String, view: ImageView) {
        network.loadPostImageUrlIntoView(
            filename = filename,
            view = view,
            onSuccess = this::getImageUrlSuccess,
            onError = this::genericError
        )
    }

    private fun loadReactionImageIntoView(filename: String, view: ImageView) {
        network.loadReactionImageUrlIntoView(
            filename = filename,
            view = view,
            onSuccess = this::getImageUrlSuccess,
            onError = this::genericError
        )
    }

    private fun getImageUrlSuccess(
        statusCode: Int,
        responseBody: ResponseBody,
        tag: String,
        view: ImageView
    ) {
        Log.d(
            "GET_IMAGE_URL",
            "Successfully got image url: $responseBody Status code: $statusCode"
        )
        val uri = responseBody.string()
        println(uri)
        put(tag, uri)
        picasso
            .load(uri)
            .placeholder(R.color.primaryAccent)
            .into(
                view
            )
    }

    private fun genericError(statusCode: Int, e: Throwable) {
        Log.e("API_ERROR", "Error $statusCode during API call")
        e.printStackTrace()
    }

    fun setNetworkService(network: RetrofitService) {
        CacheService.network = network
    }

    fun initDatabase(db: ImageCacheDatabase) {
        //CacheService.db = db.imageCacheDao()
    }
}