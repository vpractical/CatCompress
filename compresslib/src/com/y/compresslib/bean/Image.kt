package com.y.compresslib.bean

class Image(var originalPath: String) {

    var isCompress: Boolean = false
    var compressPath: String? = null


    override fun equals(other: Any?): Boolean {

        if (other is Image) {
            return other.originalPath == originalPath
        }

        return super.equals(other)
    }
}