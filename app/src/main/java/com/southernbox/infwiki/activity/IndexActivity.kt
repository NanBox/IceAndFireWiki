package com.southernbox.infwiki.activity

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import com.southernbox.infwiki.R
import com.southernbox.infwiki.entity.ContentDTO
import com.southernbox.infwiki.entity.Tab
import com.southernbox.infwiki.entity.TabDTO
import com.southernbox.infwiki.util.ToastUtil
import kotlinx.android.synthetic.main.activity_index.*
import retrofit2.Call
import retrofit2.Callback

/**
 * Created SouthernBox on 2016/3/27.
 * 启动页面
 */

class IndexActivity : BaseActivity() {

    var animationComplete = false
    var loadTabComplete = false
//    var loadContentComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)
        showAnimation()
    }

    /**
     * 显示启动页动画
     */
    private fun showAnimation() {
        val animation = AnimationUtils.loadAnimation(this,
                R.anim.anim_valar_morghulis)
        animation.setAnimationListener(object : AnimationListener {

            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                SystemClock.sleep(500)
                animationComplete = true
                goMainPage()
            }

            override fun onAnimationRepeat(animation: Animation) {

            }

        })
        SystemClock.sleep(200)
        tv_index.startAnimation(animation)
        loadTabData()
//        loadContentData()
    }

    /**
     * 获取标签数据
     */
    private fun loadTabData() {
        val call = requestServes.tab
        call.enqueue(object : Callback<List<Tab>> {
            override fun onResponse(call: Call<List<Tab>>,
                                    response: retrofit2.Response<List<Tab>>) {
                val tabList = response.body()
                if (tabList != null) {
                    //缓存到数据库
                    mRealm.beginTransaction()
                    mRealm.copyToRealmOrUpdate(tabList)
                    mRealm.commitTransaction()
                }
                loadTabComplete = true
                goMainPage()
            }

            override fun onFailure(call: Call<List<Tab>>, t: Throwable) {
                val tabList = mRealm.where(TabDTO::class.java).findAll()
                //有缓存数据可正常跳转，没有则提示点击重试
                if (tabList != null) {
                    ToastUtil.show(mContext, "网络连接失败")
                    loadTabComplete = true
                    goMainPage()
                } else {
                    netError()
                }
            }
        })
    }

//    /**
//     * 获取内容数据
//     */
//    private fun loadContentData() {
//        val call = requestServes.content
//        call.enqueue(object : Callback<List<ContentDTO>> {
//            override fun onResponse(call: Call<List<ContentDTO>>,
//                                    response: retrofit2.Response<List<ContentDTO>>) {
//                val contentList = response.body()
//                if (contentList != null) {
//                    //缓存到数据库
//                    mRealm.beginTransaction()
//                    mRealm.copyToRealmOrUpdate(contentList)
//                    mRealm.commitTransaction()
//                }
//                loadContentComplete = true
//                goMainPage()
//            }
//
//            override fun onFailure(call: Call<List<ContentDTO>>, t: Throwable) {
//                val contentList = mRealm.where(ContentDTO::class.java).findAll()
//                //有缓存数据可正常跳转，没有则提示点击重试
//                if (contentList != null) {
//                    ToastUtil.show(mContext, "网络连接失败")
//                    loadContentComplete = true
//                    goMainPage()
//                } else {
//                    netError()
//                }
//            }
//        })
//    }

    private fun goMainPage() {
        if (animationComplete && loadTabComplete ) {
            startActivity(Intent(this@IndexActivity,
                    MainActivity::class.java))
            finish()
        }
    }

    private fun netError() {
        ToastUtil.show(mContext, "网络连接失败，请点击重试")
        tv_index.setOnClickListener({
            loadTabData()
            tv_index.isClickable = false
        })
    }

}
