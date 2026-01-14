package com.createqr.presentation.myqr

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.createqr.databinding.ItemMyQrBinding
import com.createqr.domain.model.QRItem
import java.text.SimpleDateFormat
import java.util.*

class MyQRAdapter(
    private val onItemClick: (QRItem) -> Unit,
    private val onDeleteClick: (QRItem) -> Unit
) : ListAdapter<QRItem, MyQRAdapter.MyQRViewHolder>(QRItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyQRViewHolder {
        val binding = ItemMyQrBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyQRViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyQRViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MyQRViewHolder(
        private val binding: ItemMyQrBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())

        fun bind(item: QRItem) {
            binding.apply {
                tvTitle.text = item.title
                tvType.text = item.qrType.name
                tvDate.text = dateFormat.format(Date(item.createdAt))

                root.setOnClickListener { onItemClick(item) }
                btnDelete.setOnClickListener { onDeleteClick(item) }
            }
        }
    }

    private class QRItemDiffCallback : DiffUtil.ItemCallback<QRItem>() {
        override fun areItemsTheSame(oldItem: QRItem, newItem: QRItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: QRItem, newItem: QRItem): Boolean {
            return oldItem == newItem
        }
    }
}
