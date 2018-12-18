package com.y.compresslib

import com.y.compresslib.bean.Image
import java.util.*

interface CompressCallback {

    fun compressSuccess(list: ArrayList<Image>)

    fun compressFailed(list: ArrayList<Image>,msg: String)

}