package com.southernbox.infwiki.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.southernbox.infwiki.R
import com.southernbox.infwiki.entity.Page
import com.southernbox.infwiki.ui.DetailActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.item_list.view.*
import kotlinx.android.synthetic.main.view_footer.view.*


/**
 * Created by SouthernBox on 2017/6/25 0025.
 * 首页列表适配器
 */

class MainAdapter(content: Context, list: List<Page>) : RecyclerView.Adapter<ViewHolder>() {

    private val mContext: Context = content
    private val mList: List<Page> = list

    // 分页提示文字
    private var footerText = ""
    // item类型
    private val ITEM_TYPE_CONTENT = 1
    private val ITEM_TYPE_FOOTER = 2

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder.layoutPosition >= itemCount - 1) {
            val params = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            params.isFullSpan = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            ITEM_TYPE_CONTENT -> {
                val rootView = LayoutInflater.from(mContext).inflate(R.layout.item_list, parent, false)
                ContentViewHolder(rootView)
            }
            else -> {
                val rootView = LayoutInflater.from(mContext).inflate(R.layout.view_footer, parent, false)
                FooterViewHolder(rootView)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_TYPE_CONTENT -> {
                holder as ContentViewHolder
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
                val options = RequestOptions().centerCrop()
                if (page.coverImgWidth > 0 && page.coverImgHeight > 0) {
                    options.override(page.coverImgWidth, page.coverImgHeight)
                    Glide
                            .with(mContext)
                            .load(page.coverImg)
                            .apply(options)
                            .into(ImageViewTarget(page, holder))
                } else {
                    Glide
                            .with(mContext)
                            .load(page.coverImg)
                            .into(ImageViewTarget(page, holder))
                }
            }
            ITEM_TYPE_FOOTER -> {
                holder as FooterViewHolder
                holder.tvFooter.text = footerText
            }
        }

    }

    override fun getItemCount(): Int = if (mList.isNotEmpty()) mList.size + 1 else 0

    override fun getItemViewType(position: Int): Int {
        return if (itemCount > 1 && position >= itemCount - 1) {
            //底部View
            ITEM_TYPE_FOOTER
        } else {
            //内容View
            ITEM_TYPE_CONTENT
        }
    }

    private fun onItemClick(content: Page) {
        DetailActivity.Companion.show(mContext, content.title)
    }

    fun setFooterText(text: String) {
        footerText = if (mList.isNotEmpty()) text else ""
        notifyItemChanged(itemCount - 1)
    }

    inner class ContentViewHolder(itemView: View) : ViewHolder(itemView) {
        val ivImg: ImageView = itemView.iv_img
        val ivName: TextView = itemView.tv_name
    }

    inner class FooterViewHolder(itemView: View) : ViewHolder(itemView) {
        val tvFooter: TextView = itemView.tv_footer
    }

    inner class ImageViewTarget(page: Page, holder: ContentViewHolder) : SimpleTarget<Drawable>() {

        private val mPage = page
        private val mHolder = holder

        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
            val viewWidth = mHolder.ivImg.measuredWidth
            if (mPage.coverImgWidth > 0 && mPage.coverImgHeight > 0) {
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