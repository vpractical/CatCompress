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

/**
 * 压缩框架流程控制器-单线程
 */
class CompressCat private constructor() {

    private lateinit var compress: Compress
    private lateinit var activity: Activity
    private var config: CompressConfig? = null
    private var callback: CompressCallback? = null
    private val images = ArrayList<Image>()
    private var loading: ProgressLoading? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        fun get(activity: Activity): CompressCat {
            val cat = CompressCat()
            cat.activity = activity
            return cat
        }
    }

    fun config(config: CompressConfig): CompressCat {
        this.config = config
        return this
    }

    fun callback(callback: CompressCallback): CompressCat {
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
        compress = Compress(config!!)

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

        if (images.isEmpty()) {
            callback?.compressFailed(images, "未传入待压缩图片")
            return
        }

        if (config!!.enableShowLoading) {
            loading = ProgressLoading()
            loading?.show(activity)
            loading?.title("压缩中...")
        }

        Thread {
            compress(images[0])
        }.start()

    }

    /**
     * 压缩一张图片
     */
    private fun compress(image: Image) {

        loading?.message("正在压缩第  ${images.indexOf(image) + 1} / ${images.size} 张")

        compress.compress(image.originalPath, object : CompressSingleListener {
            override fun compressSuccess(path: String) {
//                Log.e("---","ori=${image.originalPath};pressed=$path")
                compressedSingle(image, true, path = path)
            }

            override fun compressFailed(path: String, msg: String) {
                compressedSingle(image, false, msg = msg)
            }
        })

    }

    /**
     * 压缩完一张的结果
     */
    private fun compressedSingle(image: Image, succ: Boolean, msg: String? = null, path: String? = null) {

        if (!succ) {
            mainHandler.post {
                loading?.cancel()
                callback?.compressFailed(images, msg ?: "压缩失败，请检查传入的图片组地址")
                callback = null
            }
            return
        }

        image.isCompress = succ
        image.compressPath = path
        val index = images.indexOf(image)

        if (config!!.enableDeleteOriginalImage) {
            val file = File(image.originalPath)
            file.delete()
        }

        if (index == images.size - 1) {
            mainHandler.post {
                loading?.cancel()
                callback?.compressSuccess(images)
                callback = null
            }
            return
        }
        compress(images[index + 1])
    }
}