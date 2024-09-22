package com.eva4
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class PhotoAdapterSave(
    private var photoList: MutableList<PhotosSaved>
) : RecyclerView.Adapter<PhotoAdapterSave.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val uri = photoList[position].imgUriString
        holder.bind(Uri.parse(uri))
    }

    override fun getItemCount(): Int = photoList.size

    fun update(list : MutableList<PhotosSaved>) {
        this.photoList = list
        notifyDataSetChanged()
    }

    fun addPhoto(photo: PhotosSaved) {
        photoList.add(photo)
        notifyDataSetChanged()
    }

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(uri: Uri) {
            imageView.setImageURI(uri)
        }
    }
}
