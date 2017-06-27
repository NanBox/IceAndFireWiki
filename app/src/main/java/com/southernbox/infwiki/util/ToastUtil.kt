package com.southernbox.infwiki.util

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

/**
 * Created by SouthernBox on 2016/3/28.
 * 吐司工具类
 */

object ToastUtil {

    private var toast: Toast? = null

    @SuppressLint("ShowToast")
    fun show(context: Context, text: String) {
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
        } else {
            toast!!.setText(text)
        }
        toast!!.show()
    }

    fun cancel() {
        if (toast != null) {
            toast!!.cancel()
        }
    }

}
