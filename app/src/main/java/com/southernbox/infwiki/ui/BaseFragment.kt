package com.southernbox.infwiki.ui

import android.os.Bundle
import android.support.v4.app.Fragment

/**
 * Created by SouthernBox on 2017/2/20 0020.
 * Fragment 基类
 */

abstract class BaseFragment : Fragment() {

    private var mIsViewInitiated: Boolean = false
    private var mIsVisibleToUser: Boolean = false
    protected var mIsDataInitiated: Boolean = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mIsViewInitiated = true
        initGetData()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        // 监听 Fragment 是否可见，实现懒加载
        mIsVisibleToUser = isVisibleToUser
        initGetData()
    }

    private fun initGetData() {
        if (mIsViewInitiated && mIsVisibleToUser && !mIsDataInitiated) {
            getData()
            mIsDataInitiated = true
        }
    }

    protected abstract fun getData()

}
