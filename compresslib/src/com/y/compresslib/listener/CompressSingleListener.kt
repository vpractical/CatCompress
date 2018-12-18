package com.y.compresslib.listener

internal interface CompressSingleListener {

    fun compressSuccess(path: String)

    fun compressFailed(path: String, msg: String)

}