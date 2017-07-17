package com.southernbox.infwiki.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.SwitchCompat
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.southernbox.infwiki.R
import com.southernbox.infwiki.adapter.MainFragmentPagerAdapter
import com.southernbox.infwiki.entity.Search
import com.southernbox.infwiki.entity.Tab
import com.southernbox.infwiki.entity.WikiResponse
import com.southernbox.infwiki.util.DayNightHelper
import com.southernbox.infwiki.util.RequestUtil
import com.southernbox.infwiki.util.ToastUtil
import com.southernbox.infwiki.widget.MaterialSearchView.MaterialSearchView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.view.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * Created SouthernBox on 2016/3/27.
 * 主页
 */

@Suppress("NAME_SHADOWING")
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var tabList: List<Tab>
    private val fragmentList = ArrayList<MainFragment>()
    private lateinit var switchCompat: SwitchCompat
    private var searchList = ArrayList<Search>()
    private var currentFirstType = TYPE_PERSON

    private companion object {
        private val TYPE_PERSON = "person"
        private val TYPE_HOUSE = "house"
        private val TYPE_HISTORY = "history"
        private val TYPE_SITE = "site"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initToolbar()
        initSearchView()
        initDrawerLayout()
        initNavigationView()
        initViewPager(currentFirstType)
    }

    /**
     * 初始化Toolbar
     */
    private fun initToolbar() {
        setSupportActionBar(app_bar.main_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        app_bar.main_toolbar.post({
            //设置Toolbar的标题及图标颜色
            app_bar.main_toolbar.title = resources.getString(R.string.person)
            refreshToolbarIcon()
        })
    }

    /**
     * 初始化搜索控件
     */
    private fun initSearchView() {
        //设置搜索控件
        search_view.setEllipsize(true)
        search_view.setHint("搜索")

        //设置搜索结果
        search_view.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                //隐藏键盘
                search_view.hideKeyboard(search_view)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                search_view.dismissSuggestions()
                if (newText == null || newText.isEmpty()) {
                    return false
                }
                val call = RequestUtil.wikiRequestServes.search(newText)
                call.enqueue(object : Callback<WikiResponse> {

                    override fun onResponse(call: Call<WikiResponse>?, response: Response<WikiResponse>) {
                        val responseBody = response.body() ?: return
                        val query = responseBody.query ?: return
                        searchList = query.search
                        val searchTitles = Array(searchList.size, { i ->
                            var searchTitle = searchList[i].title
                            val category = searchList[i].categorysnippet
                            if (category != null && category.isNotEmpty()) {
                                val category = category
                                        .replace("<span class=\"searchmatch\">", "")
                                        .replace("</span>", "")
                                searchTitle += " 「$category」分类"
                            }
                            val section = searchList[i].sectionsnippet
                            if (section != null && section.isNotEmpty()) {
                                val section = section
                                        .replace("<span class=\"searchmatch\">", "")
                                        .replace("</span>", "")
                                searchTitle += " 「$section」章节"
                            }
                            searchTitle
                        })
                        search_view.setSuggestions(searchTitles)
                        search_view.showSuggestions()
                    }

                    override fun onFailure(call: Call<WikiResponse>?, t: Throwable?) {
                        ToastUtil.show(this@MainActivity, "网络请求失败")
                    }
                })

                return true
            }
        })

        //监听搜索结果点击事件
        var clickable = true //防止多次点击
        search_view.setOnItemClickListener { _, _, position, _ ->
            if (clickable) {
                clickable = false
                //延时以展示水波纹效果
                search_view.postDelayed({
                    search_view.closeSearch()
                    if (searchList.size - 1 >= position) {
                        DetailActivity.show(
                                mContext,
                                searchList[position].title)
                    }
                }, 150)
                search_view.postDelayed({
                    clickable = true
                }, 200)
            }
        }
    }

    /**
     * 初始化DrawerLayout
     */
    private fun initDrawerLayout() {
        val toggle = ActionBarDrawerToggle(
                this,
                drawer_layout,
                app_bar.main_toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
    }

    /**
     * 初始化侧边菜单
     */
    private fun initNavigationView() {
        navigation_view.setNavigationItemSelectedListener(this)

        val navigationHeader = navigation_view.getHeaderView(0)
        if (mDayNightHelper.isDay) {
            navigationHeader.setBackgroundResource(R.drawable.side_nav_bar_day)
        } else {
            navigationHeader.setBackgroundResource(R.drawable.side_nav_bar_night)
        }

        val menu = navigation_view.menu
        val nightItem = menu.findItem(R.id.nav_night)
        val nightView = MenuItemCompat.getActionView(nightItem)
        switchCompat = nightView.findViewById(R.id.switch_compat) as SwitchCompat
        //设置夜间模式开关
        switchCompat.isChecked = !mDayNightHelper.isDay
        //监听夜间模式点击事件
        switchCompat.setOnCheckedChangeListener { _, b ->
            showAnimation()
            if (b) {
                mDayNightHelper.setMode(DayNightHelper.DayNight.NIGHT)
                setTheme(R.style.NightTheme)
                refreshUI(false)
            } else {
                mDayNightHelper.setMode(DayNightHelper.DayNight.DAY)
                setTheme(R.style.DayTheme)
                refreshUI(true)
            }
        }
    }

    /**
     * 初始化ViewPager
     *
     * @param type 要展示的类型
     */
    private fun initViewPager(type: String) {
        tabList = mRealm.where(Tab::class.java)
                .equalTo("type", type)
                .findAll()
        if (tabList.isNotEmpty()) {
            initFragments()
            app_bar.view_pager.offscreenPageLimit = 2
            app_bar.view_pager.adapter = MainFragmentPagerAdapter(
                    supportFragmentManager,
                    fragmentList, tabList)
            app_bar.tab_layout.setupWithViewPager(app_bar.view_pager)
        }
    }

    /**
     * 初始化fragment
     */
    private fun initFragments() {
        fragmentList.clear()
        tabList.mapTo(fragmentList) {
            MainFragment.newInstance(it.type, it.title)
        }
    }

    /**
     * 展示一个切换动画
     */
    private fun showAnimation() {
        val decorView = window.decorView
        val cacheBitmap = getCacheBitmapFromView(decorView)
        if (decorView is ViewGroup && cacheBitmap != null) {
            val view = View(this)
            view.background = BitmapDrawable(resources, cacheBitmap)
            val layoutParam = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            decorView.addView(view, layoutParam)
            val objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
            objectAnimator.duration = 300
            objectAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    decorView.removeView(view)
                }
            })
            objectAnimator.start()
        }
    }

    /**
     * 获取 View 的缓存视图
     *
     * @param view 对应的View
     * *
     * @return 对应View的缓存视图
     */
    private fun getCacheBitmapFromView(view: View): Bitmap? {
        val drawingCacheEnabled = true
        view.isDrawingCacheEnabled = drawingCacheEnabled
        view.buildDrawingCache(drawingCacheEnabled)
        val drawingCache = view.drawingCache
        val bitmap: Bitmap?
        if (drawingCache != null) {
            bitmap = Bitmap.createBitmap(drawingCache)
            view.isDrawingCacheEnabled = false
        } else {
            bitmap = null
        }
        return bitmap
    }

    /**
     * 刷新界面UI
     */
    private fun refreshUI(isDay: Boolean) {
        val theme = theme
        val colorPrimary = TypedValue()
        theme.resolveAttribute(R.attr.colorPrimary, colorPrimary, true)
        val colorPrimaryDark = TypedValue()
        theme.resolveAttribute(R.attr.colorPrimaryDark, colorPrimaryDark, true)
        val colorAccent = TypedValue()
        theme.resolveAttribute(R.attr.colorAccent, colorAccent, true)
        val colorBackground = TypedValue()
        theme.resolveAttribute(R.attr.colorBackground, colorBackground, true)
        val darkTextColor = TypedValue()
        theme.resolveAttribute(R.attr.darkTextColor, darkTextColor, true)
        val lightTextColor = TypedValue()
        theme.resolveAttribute(R.attr.lightTextColor, lightTextColor, true)

        //更新Toolbar的背景、标题、图标颜色
        app_bar.main_toolbar.setBackgroundResource(colorPrimary.resourceId)
        app_bar.main_toolbar.setTitleTextColor(
                ContextCompat.getColor(mContext, lightTextColor.resourceId))
        refreshToolbarIcon()

        //更新TabLayout的背景及标识线颜色
        app_bar.tab_layout.setBackgroundResource(colorPrimary.resourceId)
        app_bar.tab_layout.setSelectedTabIndicatorColor(
                ContextCompat.getColor(mContext, colorAccent.resourceId))
        //更新侧滑菜单标题栏背景及字体颜色
        val navigationHeader = navigation_view.getHeaderView(0)
        if (isDay) {
            navigationHeader.setBackgroundResource(R.drawable.side_nav_bar_day)
        } else {
            navigationHeader.setBackgroundResource(R.drawable.side_nav_bar_night)
        }
        navigationHeader.textView.setTextColor(ContextCompat.getColor(mContext, lightTextColor.resourceId))
        //更新侧滑菜单背景
        navigation_view.setBackgroundResource(colorBackground.resourceId)
        //更新侧滑菜单字体颜色
        navigation_view.itemTextColor = ContextCompat.getColorStateList(mContext, darkTextColor.resourceId)
        navigation_view.itemIconTintList = ContextCompat.getColorStateList(mContext, darkTextColor.resourceId)
        //更新ViewPagerUI
        for (fragment in fragmentList) {
            fragment.refreshUI()
        }

        refreshStatusBar()
    }

    /**
     * 刷新StatusBar
     */
    private fun refreshStatusBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            val typedValue = TypedValue()
            val theme = theme
            theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true)
            window.statusBarColor = ContextCompat.getColor(mContext, typedValue.resourceId)
        }
    }

    /**
     * 刷新Toolbar图标
     */
    private fun refreshToolbarIcon() {
        val navigationIcon = app_bar.main_toolbar.navigationIcon
        if (navigationIcon != null) {
            if (mDayNightHelper.isDay) {
                navigationIcon.alpha = 255
            } else {
                navigationIcon.alpha = 128
            }
        }
        val toolbarMenu = app_bar.main_toolbar.menu
        val searchIcon = toolbarMenu.getItem(0).icon
        if (searchIcon != null) {
            if (mDayNightHelper.isDay) {
                searchIcon.alpha = 255
            } else {
                searchIcon.alpha = 128
            }
        }
    }

    private var mExitTime: Long = 0

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else if (search_view.isSearchOpen) {
            search_view.closeSearch()
        } else if (System.currentTimeMillis() - mExitTime > 2000) {
            ToastUtil.show(this, "再按一次退出")
            mExitTime = System.currentTimeMillis()
        } else {
            ToastUtil.cancel()
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main_toolbar, menu)
        //设置搜索框
        val item = menu.findItem(R.id.action_search)
        search_view.setMenuItem(item)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_person -> {
                if (TYPE_PERSON != currentFirstType) {
                    currentFirstType = TYPE_PERSON
                    initViewPager(currentFirstType)
                    app_bar.main_toolbar.title = resources.getString(R.string.person)
                }
                drawer_layout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_house -> {
                if (TYPE_HOUSE != currentFirstType) {
                    currentFirstType = TYPE_HOUSE
                    initViewPager(currentFirstType)
                    app_bar.main_toolbar.title = resources.getString(R.string.house)
                }
                drawer_layout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_history -> {
                if (TYPE_HISTORY != currentFirstType) {
                    currentFirstType = TYPE_HISTORY
                    initViewPager(currentFirstType)
                    app_bar.main_toolbar.title = resources.getString(R.string.history)
                }
                drawer_layout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_castles -> {
                if (TYPE_SITE != currentFirstType) {
                    currentFirstType = TYPE_SITE
                    initViewPager(currentFirstType)
                    app_bar.main_toolbar.title = resources.getString(R.string.site)
                }
                drawer_layout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_night -> {
                val isChecked = switchCompat.isChecked
                switchCompat.isChecked = !isChecked
            }
            R.id.nav_about -> {
                startActivity(Intent(mContext, AboutActivity::class.java))
                drawer_layout.closeDrawer(GravityCompat.START)
            }
        }
        return true
    }
}
