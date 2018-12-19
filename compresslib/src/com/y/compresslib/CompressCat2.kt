package com.y.compresslib

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.y.compresslib.bean.Image
import com.y.compresslib.config.CompressConfig
import com.y.compresslib.core.Compress
import com.y.compresslib.listener.CompressSingleListener
import com.y.compresslib.util.ProgressLoading
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 压缩框架流程控制器-多线程
 */
class CompressCat2 private constructor() {

    private lateinit var compress: Compress
    private lateinit var activity: Activity
    private var config: CompressConfig? = null
    private var callback: CompressCallback? = null
    private val images = ArrayList<Image>()
    private var loading: ProgressLoading? = null

    private val executorService: ExecutorService = Executors.newFixedThreadPool(3)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var count = 0

    companion object {
        fun get(activity: Activity): CompressCat2 {
            val cat = CompressCat2()
            cat.activity = activity
            return cat
        }
    }

    fun config(config: CompressConfig): CompressCat2 {
        this.config = config
        return this
    }

    fun callback(callback: CompressCallback): CompressCat2 {
        this.callback = callback
        return this
    }

    /**
     * 外部调用，传入待压缩图片组
     */
    fun compress(paths: ArrayList<String>) {

        if (config == null) {
            config = CompressConfig.get(activity)
        }

        val dir = File(config!!.cacheDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        if (!dir.isDirectory) {
            throw IllegalArgumentException("compress cacheDir must be a directory")
        }

        images.clear()
        count = 0
        compress = Compress(config!!)

        if (paths.isEmpty()) {
            callback?.compressFailed(images, "未传入待压缩图片")
            return
        }

        if (config!!.enableShowLoading) {
            loading = ProgressLoading()
            loading?.show(activity)
            loading?.title("压缩中...")
        }

        for (path in paths) {
            val image = Image(path)
            images.add(image)
            if (TextUtils.isEmpty(image.originalPath)) {
                compressedSingle(image, false)
                return
            }

            val file = File(image.originalPath)
            if (!file.exists() || !file.isFile) {
                compressedSingle(image, false)
                return
            }
        }

        for (image in images){
            compress(image)
        }

    }

    /**
     * 压缩一张图片
     */
    private fun compress(image: Image) {
        val task = Runnable {
            compress.compress(image.originalPath, object : CompressSingleListener {
                override fun compressSuccess(path: String) {
//                    Log.e("---Success", "ori=${image.originalPath};pressed=$path")
                    compressedSingle(image, true, path = path)
                }

                override fun compressFailed(path: String, msg: String) {
//                    Log.e("---Failed", "path=$path;msg=$msg")
                    compressedSingle(image, false, msg = msg)
                }
            })
        }
        executorService.execute(task)
    }

    /**
     * 压缩完一张的结果
     */
    private fun compressedSingle(image: Image, succ: Boolean, msg: String? = null, path: String? = null) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            mainHandler.post {
                compressedSingle(image, succ, msg, path)
            }
            return
        }
        if (!succ) {
            loading?.cancel()
            callback?.compressFailed(images, msg ?: "压缩失败，请检查传入的图片组地址")
            callback = null
            return
        }

        count++
        image.isCompress = succ
        image.compressPath = path

//        loading?.progress(count * 100 / images.size)
        loading?.message("已压缩  $count / ${images.size} 张")

        if (config!!.enableDeleteOriginalImage) {
            val file = File(image.originalPath)
            file.delete()
        }

        if (count == images.size) {
            loading?.cancel()
            callback?.compressSuccess(images)
            callback = null
            return
        }
    }
}