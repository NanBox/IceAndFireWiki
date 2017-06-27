package com.southernbox.infwiki.util

import android.content.Context
import android.content.SharedPreferences

class DayNightHelper(context: Context) {

    companion object {
        private val FILE_NAME = "ice_and_fire"
        private val MODE = "day_night_mode"
    }

    private val mSharedPreferences: SharedPreferences

    init {
        this.mSharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 保存模式设置

     * @param mode 模式
     */
    fun setMode(mode: DayNight) {
        val editor = mSharedPreferences.edit()
        editor.putString(MODE, mode.name).apply()
    }

    /**
     * 夜间模式

     * @return 是否夜间模式
     */
    val isNight: Boolean
        get() {
            val mode = mSharedPreferences.getString(MODE, DayNight.DAY.name)
            return DayNight.NIGHT.name == mode
        }

    /**
     * 日间模式

     * @return 是否白天模式
     */
    val isDay: Boolean
        get() {
            val mode = mSharedPreferences.getString(MODE, DayNight.DAY.name)
            return DayNight.DAY.name == mode
        }

    enum class DayNight {
        DAY, NIGHT
    }
}
