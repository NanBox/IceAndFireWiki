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
import com.southernbox.infwiki.R
import com.southernbox.infwiki.activity.DetailActivity
import com.southernbox.infwiki.entity.Page
import kotlinx.android.synthetic.main.item_list.view.*

/**
 * Created by SouthernBox on 2017/6/25 0025.
 * 首页列表适配器
 */

class MainAdapter(content: Context, list: List<Page>) : RecyclerView.Adapter<MainAdapter.MyViewHolder>() {

    var mContext: Context? = null
    var mList: List<Page>? = null

    init {
        mContext = content
        mList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
        val rootView = LayoutInflater.from(mContext).inflate(R.layout.item_list, parent, false)
        return MyViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: MainAdapter.MyViewHolder, position: Int) {
        val content = mList!![position]

        holder.ivName!!.text = content.title

        Glide
                .with(mContext)
                .load(content.coverImg)
                .crossFade()
                .into(holder.ivImg)

        holder.itemView.setOnClickListener { _ -> onItemClick(content) }
    }

    override fun getItemCount(): Int = if (mList != null) (mList as List<Page>).size else 0

    fun onItemClick(content: Page) {
        DetailActivity.Companion.show(mContext!!, content.title!!)
    }

    class MyViewHolder(itemView: View) : ViewHolder(itemView) {

        var ivImg: ImageView? = null
        var ivName: TextView? = null

        init {
            ivImg = itemView.iv_img
            ivName = itemView.tv_name
        }

    }
}