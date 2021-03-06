package com.y.compresslib.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.y.compresslib.BuildConfig
import com.y.compresslib.config.CompressConfig
import com.y.compresslib.listener.CompressSingleListener
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * 压缩过程实现类
 */
internal class Compress(private var config: CompressConfig) {

    fun compress(path: String, listener: CompressSingleListener) {

        if (config.enablePixelCompress) {
            compressByPixel(path, listener)
        } else {
            compressByQuality(path, BitmapFactory.decodeFile(path), listener)
        }
    }

    /**
     * 像素压缩
     */
    private fun compressByPixel(path: String, listener: CompressSingleListener) {
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, options)
            options.inJustDecodeBounds = false
            val w = options.outWidth
            val h = options.outHeight
            val max = config.maxPixel
            var ratio = 1 //图片大小与期望大小的比例
            if (w > max && h in max..w) {
                ratio = (max + h) / max
            } else if (h > max && w in max..h) {
                ratio = (max + w) / max
            }

            if (ratio < 1) {
                ratio = 1
            }

            options.inSampleSize = ratio
            options.inPreferredConfig = config.form
            options.inPurgeable = true
            options.inInputShareable = true // 当系统内存不够时候图片自动被回收,和inPurgeable同时设置有效
            val bitmap = BitmapFactory.decodeFile(path, options)

            if(BuildConfig.DEBUG){
                Log.e("----core:pixel----", "w=$w;h=$h;ration=$ratio;-----w=${bitmap.width};h=${bitmap.height};size=${bitmap.byteCount / 1024}")
            }

            if (config.enableQualityCompress) {
                compressByQuality(path, bitmap, listener)
            } else {
                val file = getCacheFile(File(path))
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
                listener.compressSuccess(path)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            listener.compressFailed(path, "压缩像素异常: ${e.printStackTrace()}")
        }
    }

    /**
     * 质量压缩
     */
    private fun compressByQuality(path: String, bitmap: Bitmap, listener: CompressSingleListener) {
        val baos = ByteArrayOutputStream()
        val file = getCacheFile(File(path))
        val fos = FileOutputStream(file)
        try {
            var option = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, option, baos)
            while (baos.toByteArray().size > config.maxSize) {
                baos.reset()
                option -= 4
                bitmap.compress(Bitmap.CompressFormat.JPEG, option, baos)
                if (option - 4 <= 0) {
                    //已经质量压缩到这个比例下最小
                    break
                }
            }
            if(BuildConfig.DEBUG) {
                Log.e("----core:quality----", "option=$option;-----size=${baos.toByteArray().size / 1024}")
            }
            fos.write(baos.toByteArray())
            listener.compressSuccess(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            listener.compressFailed(path, "压缩质量异常: ${e.printStackTrace()}")
        } finally {
            fos.flush()
            fos.close()
            baos.flush()
            baos.close()
            bitmap.recycle()
        }
    }


    private fun getCacheFile(file: File): File {
        return File(config.cacheDir, "/compress_" + file.name)
    }
}