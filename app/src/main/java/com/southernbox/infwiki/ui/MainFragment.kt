package com.southernbox.infwiki.ui


import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.southernbox.infwiki.R
import com.southernbox.infwiki.adapter.MainAdapter
import com.southernbox.infwiki.entity.Page
import com.southernbox.infwiki.entity.WikiResponse
import com.southernbox.infwiki.util.RequestUtil
import com.southernbox.infwiki.util.ToastUtil
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.item_list.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Created by SouthernBox on 2016/3/27.
 * 首页Fragment
 */

@Suppress("NAME_SHADOWING")
class MainFragment : BaseFragment() {

    private lateinit var type: String
    //分类标题
    private lateinit var categoryTitle: String
    private lateinit var mAdapter: MainAdapter
    private lateinit var mRealm: Realm
    private var pageList = ArrayList<Page>()

    private var pageSize = 20
    private var isFirstPage = true
    private lateinit var mCmcontinue: String

    // 分页提示文字
    val FOOTER_TEXT_LOAD_MORE = "加载更多"
    val FOOTER_TEXT_LOADING = "正在加载"
    val FOOTER_TEXT_NO_MORE = "没有更多"

    companion object {

        /**
         * 获取对应的首页Fragment

         * @param type  一级分类
         * *
         *  @param categoryTitle  分类标题
         * *
         * @return 对应的Fragment
         */
        fun newInstance(type: String?, categoryTitle: String?): MainFragment {
            val fragment = MainFragment()
            val bundle = Bundle()
            bundle.putString("type", type)
            bundle.putString("categoryTitle", categoryTitle)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if (bundle != null) {
            type = bundle.getString("type")
            categoryTitle = bundle.getString("categoryTitle")
        }
        mRealm = try {
            Realm.getDefaultInstance()
        } catch (e: IllegalStateException) {
            Realm.init(context)
            Realm.getDefaultInstance()
        }
        isFirstPage = true
        mCmcontinue = ""
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRecyclerView()
        initRefreshLayout()
    }

    private fun initRecyclerView() {
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        //禁止 item 交换位置
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        recycler_view.layoutManager = layoutManager
        //获取并展示缓存数据
        val cacheList = mRealm.where(Page::class.java)
                .contains("categories", categoryTitle)
                .findAll()
        pageList.clear()
        pageList.addAll(cacheList)
        //设置适配器
        mAdapter = MainAdapter(context!!, pageList)
        recycler_view.adapter = mAdapter
        recycler_view.addOnScrollListener(MyScrollListener())
        //先隐藏，延时显示列表
        recycler_view.visibility = View.INVISIBLE
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
    }

    /**
     * 获取数据
     */
    override fun getData() {
        if (!isAdded) {
            return
        }
        val call: Call<WikiResponse>
        if (mCmcontinue.isEmpty()) {
            swipe_refresh_layout.isRefreshing = true
            call = RequestUtil.wikiRequestServes.getCategoryMembers("Category:" + categoryTitle)
            isFirstPage = true
        } else {
            call = RequestUtil.wikiRequestServes.getCategoryMembers("Category:" + categoryTitle, mCmcontinue)
            mCmcontinue = ""
            isFirstPage = false
        }
        if (recycler_view != null && recycler_view.visibility != View.VISIBLE) {
            recycler_view.postDelayed({
                recycler_view ?: return@postDelayed
                recycler_view.visibility = View.VISIBLE
            }, 500)
        }
        call.enqueue(object : Callback<WikiResponse> {
            override fun onResponse(call: Call<WikiResponse>?, response: Response<WikiResponse>) {
                if (!isAdded) {
                    return
                }
                val responseBody = response.body()
                if (responseBody == null) {
                    stopLoading()
                    return
                }
                Log.d("response", responseBody.toString())
                val query = responseBody.query
                if (query == null) {
                    stopLoading()
                    return
                }
                val list = query.categorymembers
                if (list.size > 0) {
                    var titles = ""
                    for (responsePage in list) {
                        var mPage = mRealm.where(Page::class.java)
                                .equalTo("pageid", responsePage.pageid)
                                .findFirst()
                        if (mPage == null) {
                            mRealm.beginTransaction()
                            mRealm.copyToRealmOrUpdate(responsePage)
                            mRealm.commitTransaction()
                            mPage = mRealm.where(Page::class.java)
                                    .equalTo("pageid", responsePage.pageid)
                                    .findFirst()
                        }
                        if (mPage != null) {
                            titles += mPage.title + "|"
                            if (!mPage.categories.contains(categoryTitle)) {
                                //保存到数据库
                                mRealm.beginTransaction()
                                mRealm.copyFromRealm(mPage)
                                mPage.categories += categoryTitle + "|"
                                mRealm.copyToRealmOrUpdate(mPage)
                                mRealm.commitTransaction()
                            }
                        }
                    }
                    titles = titles.substring(0, titles.length - 1)
                    getImage(titles, list)
                }
                //检查是否有下一页
                mCmcontinue = if (responseBody.next != null) responseBody.next.cmcontinue else ""
            }

            override fun onFailure(call: Call<WikiResponse>?, t: Throwable?) {
                netError()
                t!!.printStackTrace()
            }
        })
    }

    /**
     * 获取封面图片
     */
    private fun getImage(titles: String, list: List<Page>) {
        if (!isAdded) {
            return
        }
        val call = RequestUtil.wikiRequestServes.getPageImages(titles)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>) {
                if (!isAdded) {
                    return
                }
                stopLoading()
                val responseBody = response.body() ?: return
                Log.d("response", responseBody)
                val jsonObject = JSONObject(responseBody)
                val queryObject = jsonObject.optJSONObject("query") ?: return
                val pageObject = queryObject.optJSONObject("pages") ?: return
                val keys = pageObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val page = pageObject.optJSONObject(key)
                    val thumbnail = page.optJSONObject("thumbnail")
                    if (thumbnail != null) {
                        val pageid = page.optInt("pageid")
                        val coverImg = thumbnail.optString("source")
                        for(page in list){
                            if(page.pageid == pageid){
                                page.coverImg = coverImg
                                page.coverImgHeight = 0
                                page.coverImgWidth = 0
                                continue
                            }
                        }
                        val cachePage = mRealm.where(Page::class.java)
                                .equalTo("pageid", pageid)
                                .findFirst()
                        if (cachePage != null) {
                            mRealm.beginTransaction()
                            mRealm.copyFromRealm(cachePage)
                            if (cachePage.coverImg != coverImg) {
                                cachePage.coverImg = coverImg
                                cachePage.coverImgHeight = 0
                                cachePage.coverImgWidth = 0
                            }
                            mRealm.commitTransaction()
                        }
                    }
                }
                showPages(list)
                // 更新加载提示
                if (mCmcontinue.isNotEmpty()) {
                    mAdapter.setFooterText(FOOTER_TEXT_LOAD_MORE)
                } else {
                    mAdapter.setFooterText(FOOTER_TEXT_NO_MORE)
                }
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                netError()
            }
        })
    }

    private fun netError() {
        if (!isAdded) {
            return
        }
        stopLoading()
        mIsDataInitiated = false
        if (pageList.size >= pageSize) {
            mAdapter.setFooterText(FOOTER_TEXT_LOAD_MORE)
        }
        ToastUtil.show(context!!, "网络请求失败")
    }

    private fun showPages(list: List<Page>) {
        if (!isAdded) {
            return
        }
        if (isFirstPage) {
            pageList.clear()
        }
        pageList.addAll(list)
        mAdapter.notifyDataSetChanged()
    }

    private fun stopLoading() {
        if (swipe_refresh_layout != null) {
            swipe_refresh_layout.isRefreshing = false
        }
    }

    fun refreshUI() {
        if (!isAdded) {
            return
        }
        val theme = context!!.theme
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
        for (position in 0 until childCount) {
            val item = recycler_view.getChildAt(position)
            item.ll_content.setBackgroundResource(colorBackground.resourceId)
            item.tv_name.setTextColor(
                    ContextCompat.getColor(context!!, darkTextColor.resourceId))
        }
        //让 RecyclerView 缓存在 Pool 中的 Item 失效
        val recyclerViewClass = RecyclerView::class.java
        val declaredField = recyclerViewClass.getDeclaredField("mRecycler")
        declaredField.isAccessible = true
        val declaredMethod = Class.forName(RecyclerView.Recycler::class.java.name)
                .getDeclaredMethod("clear")
        declaredMethod.isAccessible = true
        declaredMethod.invoke(declaredField.get(recycler_view))
        val recycledViewPool = recycler_view.recycledViewPool
        recycledViewPool.clear()
    }

    inner class MyScrollListener : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (mCmcontinue.isEmpty()) {
                return
            }
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> {
                    if (!recycler_view.canScrollVertically(1)) {
                        mAdapter.setFooterText(FOOTER_TEXT_LOADING)
                        getData()
                    }
                }
            }
        }
    }
}

















