package com.jiny.createqr.presentation.myqr

import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jiny.createqr.databinding.ItemMyQrBinding
import com.jiny.createqr.domain.model.QRItem
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.SimpleDateFormat
import java.util.*

class MyQRAdapter(
    private val onItemClick: (QRItem) -> Unit,
    private val onDeleteClick: (QRItem) -> Unit
) : ListAdapter<QRItem, MyQRAdapter.MyQRViewHolder>(QRItemDiffCallback()) {

    private val thumbnailCache = mutableMapOf<Long, Bitmap>()

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

    private fun generateThumbnail(item: QRItem): Bitmap? {
        thumbnailCache[item.id]?.let { return it }

        return try {
            val size = 128
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(item.qrData, BarcodeFormat.QR_CODE, size, size)
            val qrColor = Color.parseColor("#${item.qrColor}")
            val bgColor = Color.parseColor("#${item.backgroundColor}")

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) qrColor else bgColor)
                }
            }
            thumbnailCache[item.id] = bitmap
            bitmap
        } catch (e: Exception) {
            null
        }
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

                // QR Thumbnail
                val thumbnail = generateThumbnail(item)
                ivQrThumbnail.setImageBitmap(thumbnail)

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
