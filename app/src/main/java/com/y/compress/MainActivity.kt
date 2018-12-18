package com.y.compress

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import com.y.compresslib.CompressCallback
import com.y.compresslib.CompressCat
import com.y.compresslib.CompressCat2
import com.y.compresslib.bean.Image
import com.y.compresslib.config.CompressConfig
import com.y.permissionlib.PermCat
import com.y.permissionlib.PermissionCat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_STORAGE_READ = Manifest.permission.READ_EXTERNAL_STORAGE
        const val PERMISSION_STORAGE_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE
        const val PERMISSION_CAMERA = Manifest.permission.CAMERA

        const val CAMERA_REQUEST_CODE = 100
        const val ALBUM_REQUEST_CODE = 100

        private lateinit var ORIGINAL_PATH: String
        private lateinit var COMPRESS_PATH: String
    }

    private lateinit var context: Context
    private val list = ArrayList<String>()
    private var cameraCachePath: String? = null // 拍照源文件路径
    val data = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        ORIGINAL_PATH = "${externalCacheDir?.absolutePath}/compress/original/"
        COMPRESS_PATH = "${externalCacheDir?.absolutePath}/compress/compress/"

        val parent = File(externalCacheDir?.absolutePath + "/compress/compress/")

        btnWatch.text = parent.absolutePath

        btnCamera.setOnClickListener {
            openCamera()
        }

        btnPhoto.setOnClickListener {
            openAlbum(false)
        }
        btnPhoto2.setOnClickListener {
            openAlbum(true)
        }

        init()
    }

    private fun openCamera() {
        // Android 7.0 File 路径的变更，需要使用 FileProvider 来做
        val dir = File(ORIGINAL_PATH)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "${System.currentTimeMillis()}.jpg")
        val outputUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 版本大于6.0
            UriParseUtils.getCameraOutPutUri(this, file)
        } else {
            Uri.fromFile(file)
        }
        cameraCachePath = file.absolutePath
        CommonUtils.hasCamera(this, CommonUtils.getCameraIntent(outputUri), CAMERA_REQUEST_CODE)
    }

    private fun openAlbum(mulit:Boolean) {
//        CommonUtils.openAlbum(this, ALBUM_REQUEST_CODE)

        list.clear()

        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160202_124912.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160202_124926.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160202_124937.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160203_185043.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160213_125714.jpg")

        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160213_125722.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160213_125828.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160213_130013.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160213_130015.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160213_130036.jpg")

        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160213_130455.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160213_130510.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160213_130554.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160213_130733.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160213_130829.jpg")

        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160213_131023.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160223_131222.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160223_131319.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160225_124025.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160326_144513.jpg")

        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160326_144527.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160326_173357.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160326_173410.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160403_115812.jpg")
        list.add("/storage/emulated/0/DCIM/Camera/IMG_20160408_154627.jpg")

        preCompress(false,mulit)
    }

    @PermCat(PERMISSION_STORAGE_READ)
    private fun init() {
        val perms = arrayOf(PERMISSION_STORAGE_READ, PERMISSION_STORAGE_WRITE, PERMISSION_CAMERA)
        if (!PermissionCat.has(this, *perms)) {
            Log.e("init()", "----")
            PermissionCat.request("给我权限", this, null, *perms)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        list.clear()

        // 拍照返回
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (cameraCachePath != null) {
                list.add(cameraCachePath!!)
                preCompress(true)
            }
        }

        // 相册返回
        if (requestCode == ALBUM_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                // 测试多张图片同时压缩


                val uri = data.data
                val path = UriParseUtils.getPath(this, uri)
                list.add(path)
                preCompress(false)
            }
        }
    }

    private fun preCompress(isDelete: Boolean = false,isMulit: Boolean = false) {
        val config = CompressConfig
                .get(this)
                .maxPixel(1000)
                .maxSize(200)
                .form(Bitmap.Config.ARGB_8888)
                .cacheDir(COMPRESS_PATH)
                .enablePixelCompress(true)
                .enableQualityCompress(true)
                .enableDeleteOriginalImage(isDelete)
                .enableShowLoading(true)

        val time = System.currentTimeMillis()
        val callback = object : CompressCallback {
            override fun compressSuccess(list: java.util.ArrayList<Image>) {
                val cur = (System.currentTimeMillis() - time).toInt()
                Log.e("----", "压缩完成${list.size};耗时 = $cur")
                for (it in list) {
                    if (!TextUtils.isEmpty(it.compressPath)) {

                    } else {
                        Log.e("----", "压缩失败,compressPath有null值${it.isCompress}")
                    }
                }
            }

            override fun compressFailed(list: java.util.ArrayList<Image>, msg: String) {
                Log.e("----", "压缩失败${list.size}---$msg")
            }
        }

        if(isMulit){
            CompressCat2
                    .get(this)
                    .config(config)
                    .callback(callback)
                    .compress(list)
        }else{
            CompressCat
                    .get(this)
                    .config(config)
                    .callback(callback)
                    .compress(list)
        }


    }

}
