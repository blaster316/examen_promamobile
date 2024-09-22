package com.eva4

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.eva4.databinding.ItemPlaceBinding

class PlaceAdapter(
    private var list : MutableList<Place>,
    private val onDeleteClick: (String) -> Unit,
    private val onEditClick: (String) -> Unit,
    private val onLocationClick: (String) -> Unit,
    private val openDetail : (String) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = list[position]
        holder.bind(place)
    }

    fun update(list : List<Place>) {
        this.list = list.toMutableList()
        notifyDataSetChanged()
    }

    inner class PlaceViewHolder(private val binding: ItemPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(place: Place) {
            binding.textNamePlace.text = place.name
            binding.textCost.text = "${place.cost} USD"
            binding.textTrans.text = "${place.costTrans} USD"
            // Asignar imagen desde un recurso o URL si lo manejas
            binding.imgPlace.load(place.imageRef)
            // Listeners para los botones
            binding.btnDelete.setOnClickListener { onDeleteClick(place.id) }
            binding.btnEdit.setOnClickListener { onEditClick(place.id) }
            binding.containerPlace.setOnClickListener { openDetail(place.id) }
        }
    }
}
