package com.y.compresslib.config

import android.app.Activity
import android.graphics.Bitmap

class CompressConfig private constructor() {

    /**
     * 较短边不超过的最大px
     */
    internal var maxPixel = 1000
    /**
     * 压缩后不超过的最大kb
     */
    internal var maxSize = 200


    internal var form: Bitmap.Config? = null
    /**
     * 是否启用像素压缩
     */
    internal var enablePixelCompress = true
    /**
     * 是否启用质量压缩
     */
    internal var enableQualityCompress = true
    /**
     * 是否删除源图片
     */
    internal var enableDeleteOriginalImage = false
    /**
     * 压缩后图片保存文件夹路径
     */
    internal lateinit var cacheDir: String
    /**
     * 是否显示压缩进度条
     */
    internal var enableShowLoading = false

    companion object {
        fun get(activity: Activity): CompressConfig {
            val config = CompressConfig()
            config.cacheDir("${activity.externalCacheDir!!.absolutePath}/compress/compress/")
            return config
        }
    }

    fun maxPixel(maxPixel: Int): CompressConfig {
        this.maxPixel = maxPixel
        return this
    }

    fun maxSize(maxSize: Int): CompressConfig {
        this.maxSize = maxSize
        return this
    }

    fun form(form: Bitmap.Config): CompressConfig {
        this.form = form
        return this
    }

    fun enablePixelCompress(enablePixelCompress: Boolean): CompressConfig {
        this.enablePixelCompress = enablePixelCompress
        return this
    }

    fun enableQualityCompress(enableQualityCompress: Boolean): CompressConfig {
        this.enableQualityCompress = enableQualityCompress
        return this
    }

    fun enableDeleteOriginalImage(enableDeleteOriginalImage: Boolean): CompressConfig {
        this.enableDeleteOriginalImage = enableDeleteOriginalImage
        return this
    }

    fun cacheDir(cacheDir: String): CompressConfig {
        this.cacheDir = cacheDir
        return this
    }

    fun enableShowLoading(enableShowLoading: Boolean): CompressConfig {
        this.enableShowLoading = enableShowLoading
        return this
    }


}