package com.y.compresslib.util

import android.app.Activity
import android.app.ProgressDialog

internal class ProgressLoading{

    private var dialog: ProgressDialog? = null

    fun show(activity: Activity){
        if(dialog == null){
            dialog = ProgressDialog(activity)
            dialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
//            dialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
//            dialog!!.max = 100
//            dialog!!.progress = 0
            dialog!!.setCancelable(false)
        }

        dialog!!.show()
    }

    fun cancel(){
        if(dialog != null && dialog!!.isShowing){
            dialog!!.cancel()
            dialog = null
        }
    }

    fun title(title: String){
        dialog?.setTitle(title)
    }

    fun message(msg: String){
        dialog?.setMessage(msg)
    }

    fun progress(progress: Int){
//        dialog?.progress = progress
    }
}