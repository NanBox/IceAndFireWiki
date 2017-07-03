package com.southernbox.infwiki.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.southernbox.infwiki.R
import com.southernbox.infwiki.adapter.MainAdapter
import com.southernbox.infwiki.entity.Page
import com.southernbox.infwiki.entity.WikiResponse
import com.southernbox.infwiki.util.RequestUtil
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.item_list.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * Created by SouthernBox on 2016/3/27.
 * 首页Fragment
 */

@Suppress("NAME_SHADOWING")
class MainFragment : Fragment() {

    lateinit var type: String
    lateinit var title: String
    lateinit var mAdapter: MainAdapter
    var pageList = ArrayList<Page>()
    var mCmcontinue = ""

    companion object {

        /**
         * 获取对应的首页Fragment

         * @param type  一级分类
         * *
         *  @param title  分类标题
         * *
         * @return 对应的Fragment
         */
        fun newInstance(type: String?, title: String?): MainFragment {
            val fragment = MainFragment()
            val bundle = Bundle()
            bundle.putString("type", type)
            bundle.putString("title", title)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        type = bundle.getString("type")
        title = bundle.getString("title")
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        initView()
        initRefreshLayout()
    }

    private fun initView() {
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        //静止 item 交换位置
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        recycler_view.layoutManager = layoutManager
        mAdapter = MainAdapter(activity, pageList)
        recycler_view.adapter = mAdapter
        recycler_view.addOnScrollListener(MyScrollListener())
    }

    /**
     * 初始化SwipeRefreshLayout
     */
    private fun initRefreshLayout() {
        swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimaryDark)
        val refreshListener = SwipeRefreshLayout.OnRefreshListener {
            mCmcontinue = ""
            getData()
        }
        swipe_refresh_layout.setOnRefreshListener(refreshListener)
        swipe_refresh_layout.post({ getData() })
        swipe_refresh_layout.isRefreshing = true
    }

    /**
     * 获取数据
     */
    fun getData() {
        if (isRemoving) {
            return
        }
        val call: Call<WikiResponse>
        if (mCmcontinue.isEmpty()) {
            call = RequestUtil.wikiRequestServes.getCategoryMembers("Category:" + title)
        } else {
            call = RequestUtil.wikiRequestServes.getCategoryMembers("Category:" + title, mCmcontinue)
        }
        call.enqueue(object : Callback<WikiResponse> {
            override fun onResponse(call: Call<WikiResponse>?, response: Response<WikiResponse>) {
                if (response.body() != null) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val list = responseBody.query.categorymembers
                        if (list.size > 0) {
                            if (mCmcontinue.isEmpty()) {
                                pageList.clear()
                            }
                            pageList.addAll(list)
                            var titles = ""
                            for (page in list) {
                                titles += page.title + "|"
                            }
                            titles = titles.substring(0, titles.length - 1)
                            if (mCmcontinue.isEmpty()) {
                                getImage(titles, false)
                            } else {
                                getImage(titles, true)
                            }
                        }
                        if (responseBody.next != null) {
                            mCmcontinue = responseBody.next!!.cmcontinue
                        } else {
                            mCmcontinue = ""
                        }
                    }
                }
            }

            override fun onFailure(call: Call<WikiResponse>?, t: Throwable?) {
                stopLoading()
            }
        })
    }

    /**
     * 获取封面图片
     */
    fun getImage(titles: String, nextPage: Boolean) {
        val call = RequestUtil.wikiRequestServes.getPageImages(titles)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>) {
                stopLoading()
                if (response.body() != null) {
                    val jsonObject = JSONObject(response.body())
                    val pageObject = jsonObject.optJSONObject("query").optJSONObject("pages")
                    val keys = pageObject.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val page = pageObject.optJSONObject(key)
                        val thumbnail = page.optJSONObject("thumbnail")
                        if (thumbnail != null) {
                            val title = page.optString("title")
                            val coverImg = thumbnail.optString("source")
                            for (mPage in pageList) {
                                if (title == mPage.title) {
                                    mPage.coverImg = coverImg
                                    break
                                }
                            }
                        }
                    }
                    if (!nextPage) {
                        mAdapter.notifyItemChanged(0, pageList.size)
                    } else {
                        mAdapter.notifyItemRangeInserted(mAdapter.itemCount, pageList.size)
                        mAdapter.notifyItemChanged(mAdapter.itemCount, pageList.size)
                    }
                }
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                stopLoading()
            }
        })
    }

    fun stopLoading() {
        if (swipe_refresh_layout != null) {
            swipe_refresh_layout.isRefreshing = false
        }
    }

    fun refreshUI() {
        if (!isAdded) {
            return
        }
        val theme = context.theme
        val pagerBackground = TypedValue()
        theme.resolveAttribute(R.attr.pagerBackground, pagerBackground, true)
        val colorBackground = TypedValue()
        theme.resolveAttribute(R.attr.colorBackground, colorBackground, true)
        val darkTextColor = TypedValue()
        theme.resolveAttribute(R.attr.darkTextColor, darkTextColor, true)

        //更新背景颜色
        fl_content.setBackgroundResource(pagerBackground.resourceId)
        //更新Item的背景及字体颜色
        val childCount = recycler_view.childCount
        for (position in 0..childCount - 1) {
            val item = recycler_view.getChildAt(position)
            item.ll_content.setBackgroundResource(colorBackground.resourceId)
            item.tv_name.setTextColor(
                    ContextCompat.getColor(context, darkTextColor.resourceId))
        }
        //让 RecyclerView 缓存在 Pool 中的 Item 失效
        val recyclerViewClass = RecyclerView::class.java
        try {
            val declaredField = recyclerViewClass.getDeclaredField("mRecycler")
            declaredField.isAccessible = true
            val declaredMethod = Class.forName(RecyclerView.Recycler::class.java.name)
                    .getDeclaredMethod("clear")
            declaredMethod.isAccessible = true
            declaredMethod.invoke(declaredField.get(recycler_view))
            val recycledViewPool = recycler_view.recycledViewPool
            recycledViewPool.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class MyScrollListener : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            val layoutManager = recycler_view.layoutManager as StaggeredGridLayoutManager

            //防止第一行顶部留空
            layoutManager.invalidateSpanAssignments()

            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE ->
                    if (recycler_view.canScrollVertically(-1) && mCmcontinue.isNotEmpty()) {
                        getData()
                    }
            }

        }
    }
}

















