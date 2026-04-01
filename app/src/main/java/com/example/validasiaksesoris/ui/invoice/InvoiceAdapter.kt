package com.example.validasiaksesoris.ui.invoice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.validasiaksesoris.data.model.invoice.FrameNumber
import com.example.validasiaksesoris.databinding.ItemFrameNumberBinding

class InvoiceAdapter(
    private val onCheckedChange: (Int) -> Unit
) : ListAdapter<FrameNumber, InvoiceAdapter.ViewHolder>(DIFF_CALLBACK) {

    class ViewHolder(val binding: ItemFrameNumberBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemFrameNumberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.cb.text = item.frameNumber
        holder.binding.cb.setOnCheckedChangeListener(null)
        holder.binding.cb.isChecked = item.isSelected

        holder.binding.cb.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked
            onCheckedChange(position)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FrameNumber>() {
            override fun areItemsTheSame(oldItem: FrameNumber, newItem: FrameNumber): Boolean = oldItem.frameNumber == newItem.frameNumber

            override fun areContentsTheSame(oldItem: FrameNumber, newItem: FrameNumber): Boolean = oldItem == newItem
        }
    }
}