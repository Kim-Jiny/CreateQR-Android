package com.createqr.presentation.create.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.createqr.R
import com.createqr.databinding.ItemQrTypeBinding
import com.createqr.domain.model.QRTypeItem

class QRTypeAdapter(
    private val onItemClick: (QRTypeItem) -> Unit
) : ListAdapter<QRTypeItem, QRTypeAdapter.QRTypeViewHolder>(QRTypeDiffCallback()) {

    private var selectedType: QRTypeItem? = null

    fun setSelectedType(type: QRTypeItem?) {
        val oldSelected = selectedType
        selectedType = type

        currentList.forEachIndexed { index, item ->
            if (item == oldSelected || item == type) {
                notifyItemChanged(index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QRTypeViewHolder {
        val binding = ItemQrTypeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QRTypeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QRTypeViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, item == selectedType)
    }

    inner class QRTypeViewHolder(
        private val binding: ItemQrTypeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: QRTypeItem, isSelected: Boolean) {
            binding.apply {
                ivIcon.setImageResource(item.iconRes)
                tvTitle.text = item.title

                // Selection state
                if (isSelected) {
                    root.setBackgroundResource(R.drawable.bg_qr_type_selected)
                } else {
                    root.setBackgroundResource(R.drawable.bg_qr_type_normal)
                }

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    private class QRTypeDiffCallback : DiffUtil.ItemCallback<QRTypeItem>() {
        override fun areItemsTheSame(oldItem: QRTypeItem, newItem: QRTypeItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: QRTypeItem, newItem: QRTypeItem): Boolean {
            return oldItem == newItem
        }
    }
}
