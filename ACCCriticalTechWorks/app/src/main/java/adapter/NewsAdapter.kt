package adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.filisamp.acccriticaltechworks.R
import models.Article

class NewsAdapter(private val dataList: List<Article>, private val context: Context) :
    RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList

        holder.itemView.findViewById<TextView>(R.id.tv_news_item_source_name_value).text = data[position].source?.name
        holder.itemView.findViewById<TextView>(R.id.tv_news_item_author_value).text = data[position].author
        holder.itemView.findViewById<TextView>(R.id.tv_news_item_title_value).text = data[position].title
        holder.itemView.findViewById<TextView>(R.id.tv_news_item_description_value).text = data[position].description

        holder.itemView.findViewById<Button>(R.id.bt_news_item_link).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(data[position].urlLink))
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, context.getString(R.string.news_item_link_clicked_error), Toast.LENGTH_SHORT).show()
            }
        }

        holder.itemView.findViewById<ImageView>(R.id.iv_news_item_image).post {
            val maxHeight = holder.itemView.findViewById<ImageView>(R.id.iv_news_item_image).height
            val maxWidth = holder.itemView.findViewById<ImageView>(R.id.iv_news_item_image).width
            val cornerRadius = context.resources.getInteger(R.integer.news_item_image_round_corners)

            if (context is Activity) {
                val activity: Activity = context
                if (!activity.isDestroyed && !activity.isFinishing) {
                    Glide.with(context)
                        .load(data[position].urlToImage)
                        .override(maxWidth, maxHeight)
                        .transform(CenterCrop(), RoundedCorners(cornerRadius))
                        .into(holder.itemView.findViewById<ImageView>(R.id.iv_news_item_image))
                }
            }
        }

        holder.itemView.findViewById<TextView>(R.id.tv_news_item_published_at_value).text = data[position].publishedAt
        holder.itemView.findViewById<TextView>(R.id.tv_news_item_content_value).text = data[position].content
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }
}