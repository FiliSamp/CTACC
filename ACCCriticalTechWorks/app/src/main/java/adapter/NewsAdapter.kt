package adapter

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.filisamp.acccriticaltechworks.R
import kotlinx.coroutines.*
import models.Article

class NewsAdapter(private var dataList: List<Article>, private val context: Context) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    private lateinit var tvSourceNameTitle: TextView
    private lateinit var tvSourceName: TextView
    private lateinit var tvSourceAuthorTitle: TextView
    private lateinit var tvSourceAuthor: TextView
    private lateinit var tvSourceTitleTitle: TextView
    private lateinit var tvSourceTitle: TextView
    private lateinit var tvSourceDescTitle: TextView
    private lateinit var tvSourceDesc: TextView
    private lateinit var btSourceLink: Button
    private lateinit var ivSourceImage: ImageView
    private lateinit var tvSourcePublishedAtTitle: TextView
    private lateinit var tvSourcePublishedAt: TextView
    private lateinit var tvSourceContentTitle: TextView
    private lateinit var tvSourceContent: TextView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_item, parent, false)

        tvSourceNameTitle = view.findViewById(R.id.tv_news_item_source_name)
        tvSourceName = view.findViewById(R.id.tv_news_item_source_name_value)
        tvSourceAuthorTitle = view.findViewById(R.id.tv_news_item_author)
        tvSourceAuthor = view.findViewById(R.id.tv_news_item_author_value)
        tvSourceTitleTitle = view.findViewById(R.id.tv_news_item_title)
        tvSourceTitle = view.findViewById(R.id.tv_news_item_title_value)
        tvSourceDescTitle = view.findViewById(R.id.tv_news_item_description)
        tvSourceDesc = view.findViewById(R.id.tv_news_item_description_value)
        tvSourcePublishedAtTitle = view.findViewById(R.id.tv_news_item_published_at)
        tvSourcePublishedAt = view.findViewById(R.id.tv_news_item_published_at_value)
        tvSourceContentTitle = view.findViewById(R.id.tv_news_item_content)
        tvSourceContent = view.findViewById(R.id.tv_news_item_content_value)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList

        btSourceLink = holder.itemView.findViewById(R.id.bt_news_item_link)
        ivSourceImage = holder.itemView.findViewById(R.id.iv_news_item_image)

        if (data[position].source?.name.isNullOrEmpty()) {
            tvSourceNameTitle.visibility = View.GONE
            tvSourceName.visibility = View.GONE
        } else {
            tvSourceNameTitle.visibility = View.VISIBLE
            tvSourceName.visibility = View.VISIBLE
            tvSourceName.text = data[position].source?.name
        }

        if (data[position].author.isNullOrEmpty()) {
            tvSourceAuthorTitle.visibility = View.GONE
            tvSourceAuthor.visibility = View.GONE
        } else {
            tvSourceAuthorTitle.visibility = View.VISIBLE
            tvSourceAuthor.visibility = View.VISIBLE
            tvSourceAuthor.text = data[position].author
        }

        if (data[position].title.isNullOrEmpty()) {
            tvSourceTitleTitle.visibility = View.GONE
            tvSourceTitle.visibility = View.GONE
        } else {
            tvSourceTitleTitle.visibility = View.VISIBLE
            tvSourceTitle.visibility = View.VISIBLE
            tvSourceTitle.text = data[position].title
        }

        if (data[position].description.isNullOrEmpty()) {
            tvSourceDescTitle.visibility = View.GONE
            tvSourceDesc.visibility = View.GONE
        } else {
            tvSourceDescTitle.visibility = View.VISIBLE
            tvSourceDesc.visibility = View.VISIBLE
            tvSourceDesc.text = data[position].description
        }

        if (data[position].urlLink.isNullOrEmpty()){
            btSourceLink.visibility = View.GONE
        } else {
            btSourceLink.visibility = View.VISIBLE
            btSourceLink.setOnClickListener {
                var url = data[position].urlLink
                if (!url.isNullOrEmpty()) {
                    try {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        it.context.startActivity(browserIntent)
                    } catch (e: java.lang.Exception) {
                        Log.e(
                            "LinkResolveError",
                            "No activity found to handle intent for URL: $url"
                        )
                        Toast.makeText(
                            context,
                            context.getString(R.string.news_item_link_clicked_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        if (data[position].urlToImage.isNullOrEmpty()) {
            ivSourceImage.visibility = View.GONE
            ivSourceImage.setImageDrawable(null) // Clear the image content if any
        } else {
            ivSourceImage.visibility = View.VISIBLE

            val maxHeight = ivSourceImage.height
            val maxWidth = ivSourceImage.width
            val cornerRadius = context.resources.getInteger(R.integer.news_item_image_round_corners)

            Glide.with(context)
                .load(data[position].urlToImage)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(maxWidth, maxHeight)
                .transform(CenterCrop(), RoundedCorners(cornerRadius))
                .into(ivSourceImage)
        }

        if (data[position].publishedAt.isNullOrEmpty()){
            tvSourcePublishedAtTitle.visibility = View.GONE
            tvSourcePublishedAt.visibility = View.GONE
        } else {
            tvSourcePublishedAtTitle.visibility = View.VISIBLE
            tvSourcePublishedAt.visibility = View.VISIBLE
            tvSourcePublishedAt.text = data[position].publishedAt
        }

        if (data[position].content.isNullOrEmpty()){
            tvSourceContentTitle.visibility = View.GONE
            tvSourceContent.visibility = View.GONE
        } else {
            tvSourceContentTitle.visibility = View.VISIBLE
            tvSourceContent.visibility = View.VISIBLE
            tvSourceContent.text = data[position].content
        }

    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun clearData() {
        dataList = emptyList()
        notifyDataSetChanged()
    }

    fun applyNewData(pDataList: List<Article>){
        dataList = pDataList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }
}