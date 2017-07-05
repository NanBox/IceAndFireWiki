package com.southernbox.infwiki.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
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
class MainFragment : Fragment() {

    private lateinit var type: String
    //分类标题
    private lateinit var categoryTitle: String
    private lateinit var mAdapter: MainAdapter
    private lateinit var pageList: List<Page>
    private lateinit var mRealm: Realm

    private var mCmcontinue = ""
    private val pageSize = 50

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
        type = bundle.getString("type")
        categoryTitle = bundle.getString("categoryTitle")
        mRealm = Realm.getDefaultInstance()
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        initRecyclerView()
        initRefreshLayout()
    }

    private fun initRecyclerView() {
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        //禁止 item 交换位置
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        recycler_view.layoutManager = layoutManager
        //从数据库读取数据
        pageList = mRealm.where(Page::class.java)
                .contains("categories", categoryTitle)
                .findAll()
        mAdapter = MainAdapter(activity, pageList)
        //设置适配器
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
        swipe_refresh_layout.post({ getData() })
        swipe_refresh_layout.isRefreshing = true
    }

    /**
     * 获取数据
     */
    private fun getData() {
        if (isRemoving) {
            return
        }
        val call: Call<WikiResponse>
        if (mCmcontinue.isNotEmpty()) {
            call = RequestUtil.wikiRequestServes.getCategoryMembers("Category:" + categoryTitle, mCmcontinue)
            mCmcontinue = ""
            //显示缓存数据
            val displayItemCount = mAdapter.itemCount
            mAdapter.setMaxItemCount(mAdapter.getMaxItemCount() + pageSize)
            mAdapter.notifyItemRangeInserted(displayItemCount, mAdapter.itemCount - displayItemCount)
            mAdapter.notifyItemRangeChanged(displayItemCount, mAdapter.itemCount - displayItemCount)
        } else {
            call = RequestUtil.wikiRequestServes.getCategoryMembers("Category:" + categoryTitle)
            //显示缓存数据
            mAdapter.setMaxItemCount(pageSize)
            mAdapter.notifyDataSetChanged()
        }
        if (recycler_view.visibility != View.VISIBLE) {
            recycler_view.postDelayed({
                recycler_view ?: return@postDelayed
                recycler_view.visibility = View.VISIBLE
            }, 700)
        }
        call.enqueue(object : Callback<WikiResponse> {
            override fun onResponse(call: Call<WikiResponse>?, response: Response<WikiResponse>) {
                stopLoading()
                val responseBody = response.body() ?: return
                Log.d("response", responseBody.toString())
                val query = responseBody.query ?: return
                val list = query.categorymembers
                if (list.size > 0) {
                    var titles = ""
                    for (page in list) {
                        titles += page.title + "|"
                        if (!page.categories.contains(categoryTitle)) {
                            page.categories += categoryTitle + "|"
                        }
                    }
                    titles = titles.substring(0, titles.length - 1)
                    getImage(titles)
                }
                if (responseBody.next != null) {
                    mCmcontinue = responseBody.next.cmcontinue
                } else {
                    mCmcontinue = ""
                }
            }

            override fun onFailure(call: Call<WikiResponse>?, t: Throwable?) {
                stopLoading()
                ToastUtil.show(context, "网络连接失败")
            }
        })
    }

    /**
     * 获取封面图片
     */
    private fun getImage(titles: String) {
        val call = RequestUtil.wikiRequestServes.getPageImages(titles)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>) {
                stopLoading()
                val responseBody = response.body() ?: return
                Log.d("response", responseBody)
                val jsonObject = JSONObject(responseBody)
                val queryObject = jsonObject.optJSONObject("query") ?: return
                val pageObject = queryObject.optJSONObject("pages") ?: return
                val keys = pageObject.keys()
                val list = ArrayList<Page>()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val page = pageObject.optJSONObject(key)
                    val thumbnail = page.optJSONObject("thumbnail")
                    if (thumbnail != null) {
                        val pageid = page.optString("pageid")
                        val coverImg = thumbnail.optString("source")
                        val cachePage = mRealm.where(Page::class.java)
                                .equalTo("pageid", pageid)
                                .findFirst()
                        if (cachePage.coverImg != coverImg) {
                            cachePage.coverImg = coverImg
                            cachePage.coverImgHeight = 0
                            cachePage.coverImgWidth = 0
                        }
                        list.add(cachePage)
                    }
                }

                //保存到数据库
                mRealm.beginTransaction()
                mRealm.copyToRealmOrUpdate(list)
                mRealm.commitTransaction()

                pageList = mRealm.where(Page::class.java)
                        .contains("categories", categoryTitle)
                        .findAll()
                if (mAdapter.itemCount - list.size > 0) {
                    //展示更新后的数据（最后一页）
                    mAdapter.notifyItemRangeChanged(mAdapter.itemCount - list.size, list.size)
                } else {
                    mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount)
                }
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                stopLoading()
                ToastUtil.show(context, "网络连接失败")
            }
        })
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

















