# CatCompress
图片压缩实现


## 使用：
```
        val config = CompressConfig
                .get(this)
                .maxPixel(1000) //较短边的长度限制
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

        if (isMulit) {
            //多线程方案
            CompressCat2
                    .get(this)
                    .config(config)
                    .callback(callback)
                    .compress(list)
        } else {
            //单线程方案
            CompressCat
                    .get(this)
                    .config(config)
                    .callback(callback)
                    .compress(list)
        }
```











