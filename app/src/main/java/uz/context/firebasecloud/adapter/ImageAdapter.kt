package uz.context.firebasecloud.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uz.context.firebasecloud.databinding.ItemLayoutBinding

class ImageAdapter(
    private val urls: List<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ImageViewHolder(
            ItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val url = urls[position]

        if (holder is ImageViewHolder) {
            Glide.with(holder.itemView)
                .load(url)
                .into(holder.binding.imageView)
        }
    }

    override fun getItemCount(): Int {
        return urls.size
    }

    inner class ImageViewHolder(val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}