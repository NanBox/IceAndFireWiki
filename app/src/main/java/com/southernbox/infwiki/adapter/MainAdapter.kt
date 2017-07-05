package com.southernbox.infwiki.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.southernbox.infwiki.R
import com.southernbox.infwiki.activity.DetailActivity
import com.southernbox.infwiki.entity.Page
import io.realm.Realm
import kotlinx.android.synthetic.main.item_list.view.*


/**
 * Created by SouthernBox on 2017/6/25 0025.
 * 首页列表适配器
 */

class MainAdapter(content: Context, list: List<Page>) : RecyclerView.Adapter<MainAdapter.MyViewHolder>() {

    private val mContext: Context = content
    private val mList: List<Page> = list
    private var maxItemCount: Int = 20

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
        val rootView = LayoutInflater.from(mContext).inflate(R.layout.item_list, parent, false)
        return MyViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: MainAdapter.MyViewHolder, position: Int) {
        val page = mList[position]

        holder.ivName.text = page.title

        holder.itemView.setOnClickListener { _ -> onItemClick(page) }

        //无图片情况的处理
        if (page.coverImg.isEmpty()) {
            holder.ivImg.visibility = View.GONE
            return
        }

        //显示图片
        holder.ivImg.visibility = View.VISIBLE
        if (page.coverImgWidth > 0 && page.coverImgHeight > 0) {
            Glide
                    .with(mContext)
                    .load(page.coverImg)
                    .override(page.coverImgWidth, page.coverImgHeight)
                    .crossFade()
                    .into(ImageViewTarget(page, holder))
        } else {
            Glide
                    .with(mContext)
                    .load(page.coverImg)
                    .crossFade()
                    .into(ImageViewTarget(page, holder))
        }
    }

    override fun getItemCount(): Int = if (mList.size > maxItemCount) maxItemCount else mList.size

    fun getMaxItemCount(): Int {
        return maxItemCount
    }

    fun setMaxItemCount(maxItemCount: Int) {
        this.maxItemCount = maxItemCount
    }

    fun onItemClick(content: Page) {
        DetailActivity.Companion.show(mContext, content.title!!)
    }

    inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        val ivImg: ImageView = itemView.iv_img
        val ivName: TextView = itemView.tv_name
    }

    inner class ImageViewTarget(page: Page, holder: MyViewHolder) : GlideDrawableImageViewTarget(holder.ivImg) {

        val mPage = page

        override fun onResourceReady(resource: GlideDrawable?, animation: GlideAnimation<in GlideDrawable>?) {
            super.onResourceReady(resource, animation)
            val viewWidth = view.measuredWidth
            if (resource == null || (mPage.coverImgWidth > 0 && mPage.coverImgHeight > 0)) {
                return
            }
            val scale = viewWidth / resource.minimumWidth
            val viewHeight = (resource.minimumHeight * scale)

            val mRealm = Realm.getDefaultInstance()
            mRealm.beginTransaction()
            mRealm.copyFromRealm(mPage)
            mPage.coverImgHeight = viewHeight
            mPage.coverImgWidth = viewWidth
            mRealm.commitTransaction()
        }
    }
}