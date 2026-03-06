package com.example.validasiaksesoris.ui.accessory

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.validasiaksesoris.R
import com.example.validasiaksesoris.data.model.AccessoryItem
import com.example.validasiaksesoris.databinding.ItemAccessoryBinding

class AccessoryAdapter (
    private val accessories: MutableList<AccessoryItem>
) : RecyclerView.Adapter<AccessoryAdapter.ViewHolder>() {

    private val selections = mutableMapOf<String, Boolean>()
    private var enable = true

    class ViewHolder(val binding: ItemAccessoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemAccessoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = accessories[position]
        holder.binding.tvName.text = item.label
        holder.binding.accessory.apply {
            isEnabled  = enable
            isClickable = enable
        }

        val spinnerItems = holder.itemView.context.resources.getStringArray(R.array.spinner_accessoryItem)

        val adapter = object : ArrayAdapter<String>(
            holder.itemView.context,
            R.layout.selected_item,
            spinnerItems
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView

                if (position == 0) {
                    tv.setTextColor(ContextCompat.getColor(context, R.color.gray))
                } else {
                    val typedValue = TypedValue()
                    context.theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
                    tv.setTextColor(ContextCompat.getColor(context, typedValue.resourceId))
                }
                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.item_dropdown)
        holder.binding.accessory.adapter = adapter

        val previousSelection = selections[item.key]
        when (previousSelection) {
            true -> holder.binding.accessory.setSelection(1)
            false -> holder.binding.accessory.setSelection(2)
            null -> holder.binding.accessory.setSelection(0)
        }

        holder.binding.accessory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    when (pos) {
                        1 -> selections[item.key] = true
                        2 -> selections[item.key] = false
                        else -> selections.remove(item.key)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    override fun getItemCount(): Int = accessories.size

    fun getSelection(): Map<String, Boolean> = selections

    fun clearSelection() = selections.clear()

    fun updateData(newData: List<AccessoryItem>) {
        accessories.clear()
        accessories.addAll(newData)
        notifyDataSetChanged()
    }

    fun setEnabled(enabled: Boolean) {
        enable = enabled
        notifyDataSetChanged()
    }

    fun hasUnselected(): Boolean = selections.size < accessories.size
}