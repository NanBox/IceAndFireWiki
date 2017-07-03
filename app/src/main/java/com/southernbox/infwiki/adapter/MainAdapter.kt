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
import kotlinx.android.synthetic.main.item_list.view.*


/**
 * Created by SouthernBox on 2017/6/25 0025.
 * 首页列表适配器
 */

class MainAdapter(content: Context, list: ArrayList<Page>) : RecyclerView.Adapter<MainAdapter.MyViewHolder>() {

    val mContext: Context = content
    var mList = ArrayList<Page>()

    init {
        mList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
        val rootView = LayoutInflater.from(mContext).inflate(R.layout.item_list, parent, false)
        return MyViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: MainAdapter.MyViewHolder, position: Int) {
        val page = mList[position]

        holder.ivName.text = page.title

        //设置图片高度
        if (page.coverImgWidth > 0 && page.coverImgHeight > 0) {
            Glide
                    .with(mContext)
                    .load(page.coverImg)
                    .override(page.coverImgWidth, page.coverImgHeight)
                    .crossFade()
                    .into(holder.ivImg)
        } else {
            Glide
                    .with(mContext)
                    .load(page.coverImg)
                    .crossFade()
                    .into(ImageViewTarget(page, holder.ivImg))
        }

        holder.itemView.setOnClickListener { _ -> onItemClick(page) }
    }

    override fun getItemCount(): Int = mList.size

    fun onItemClick(content: Page) {
        DetailActivity.Companion.show(mContext, content.title!!)
    }

    class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        val ivImg: ImageView = itemView.iv_img
        val ivName: TextView = itemView.tv_name
    }

    inner class ImageViewTarget(page: Page, view: ImageView) : GlideDrawableImageViewTarget(view) {

        var mPage: Page = page

        override fun onResourceReady(resource: GlideDrawable?, animation: GlideAnimation<in GlideDrawable>?) {
            super.onResourceReady(resource, animation)
            val viewWidth = view.measuredWidth
            if (resource != null) {
                val scale = viewWidth / resource.minimumWidth
                val viewHeight = (resource.minimumHeight * scale)
                mPage.coverImgHeight = viewHeight
                mPage.coverImgWidth = viewWidth
            }
        }
    }
}