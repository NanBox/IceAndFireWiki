package com.southernbox.infwiki.fragment


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
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
import com.southernbox.infwiki.util.BaseUrl
import com.southernbox.infwiki.util.RequestServes
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.item_list.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*

/**
 * Created by SouthernBox on 2016/3/27.
 * 首页Fragment
 */

class MainFragment : Fragment() {

    var mContext: Context? = null
    var type: String? = null
    var title: String? = null
    var adapter: MainAdapter? = null
    //    private val contentList = ArrayList<ContentDTO>()
    var pageList = ArrayList<Page>()
    //    var mRealm: Realm? = null
    lateinit var retrofit: Retrofit
    lateinit var requestServes: RequestServes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = activity
        val bundle = arguments
        type = bundle.getString("type")
        title = bundle.getString("title")
//        Realm.init(context)
//        val realmConfig = RealmConfiguration.Builder().build()
//        mRealm = Realm.getInstance(realmConfig)
        retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl.WIKI_URL)
//                .baseUrl("http://zh.asoiaf.wikia.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
//                .client(getOkHttpClient())
                .build()
        requestServes = retrofit.create(RequestServes::class.java)

    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        initView()
        getData()
    }

    private fun initView() {
        recycler_view.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        adapter = MainAdapter(activity, pageList)
        recycler_view.adapter = adapter
    }

    /**
     * 展示数据
     */
    fun getData() {
        if (isRemoving) {
            return
        }
        val call = requestServes.getCategoryMembers("Category:" + title)
        call.enqueue(object : Callback<WikiResponse> {
            override fun onResponse(call: Call<WikiResponse>?, response: Response<WikiResponse>) {
                if (response.body() != null) {
                    val list = response.body()!!.query.categorymembers
                    if (list.size > 0) {
                        pageList.clear()
                        pageList.addAll(list)
                        var titles = ""
                        for (page in list) {
                            titles += page.title + "|"
                        }
                        titles = titles.substring(0, titles.length - 1)
                        getImage(titles)
                    }
                }
            }

            override fun onFailure(call: Call<WikiResponse>?, t: Throwable?) {

            }
        })
    }

    fun getImage(titles: String) {
        val call = requestServes.getPageImages(titles)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>) {
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
                    adapter!!.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {

            }
        })
    }

    fun refreshUI() {
        if (isRemoving) {
            return
        }
        if (mContext != null) {
            val theme = mContext!!.theme
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
                        ContextCompat.getColor(mContext!!, darkTextColor.resourceId))
                item.tv_desc.setTextColor(
                        ContextCompat.getColor(mContext!!, darkTextColor.resourceId))
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
    }

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
}

















