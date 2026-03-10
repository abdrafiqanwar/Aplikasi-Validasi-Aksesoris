package com.example.validasiaksesoris.ui.invoice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.validasiaksesoris.data.model.invoice.FrameNumber
import com.example.validasiaksesoris.databinding.ItemFrameNumberBinding

class InvoiceAdapter(
    private val frameNumbers: List<FrameNumber>,
    private val onCheckedChange: (Int) -> Unit
) : RecyclerView.Adapter<InvoiceAdapter.ViewHolder>() {

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
        val item = frameNumbers[position]
        holder.binding.cb.text = item.frameNumber
        holder.binding.cb.setOnCheckedChangeListener(null)
        holder.binding.cb.isChecked = item.isSelected

        holder.binding.cb.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked
            onCheckedChange(position)
        }
    }

    override fun getItemCount(): Int = frameNumbers.size
}