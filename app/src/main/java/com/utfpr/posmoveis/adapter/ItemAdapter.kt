package com.utfpr.posmoveis.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.utfpr.posmoveis.R
import com.utfpr.posmoveis.model.Item
import com.utfpr.posmoveis.ui.loadUrl

class ItemAdapter(items: List<Item>,
                  private val onItemClick: (Item) -> Unit,
    ) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>()

{
    class ItemViewHolder (view: View) : RecyclerView.ViewHolder(view){

        val imageViewCar = view.findViewById<ImageView>(R.id.imageViewCar)
        val textViewName = view.findViewById<TextView>(R.id.textViewName)
        val textViewYear = view.findViewById<TextView>(R.id.textViewYear)
        val textViewLicence = view.findViewById<TextView>(R.id.textViewLicence)
       // val textViewPlace = view.findViewById<TextView>(R.id.textViewPlace)

    }

    private val items: List<Item> = items


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemAdapter.ItemViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemAdapter.ItemViewHolder, position: Int) {
        val item = items[position]
        holder.textViewName.text = item.name
        holder.textViewYear.text = item.year
        holder.textViewLicence.text = item.licence
      //  holder.textViewPlace.text = item.place.toString()
        holder.imageViewCar.loadUrl(item.imageUrl)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

}
