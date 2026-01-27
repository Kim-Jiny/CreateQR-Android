package com.createqr.presentation.myqr

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.jiny.createqr.R
import com.jiny.createqr.databinding.DialogQrDetailBinding
import com.createqr.domain.model.LogoStyle
import com.createqr.domain.model.QRItem
import com.createqr.domain.usecase.GenerateQRCodeUseCase
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class QRDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogQrDetailBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var generateQRCodeUseCase: GenerateQRCodeUseCase

    private var qrItem: QRItem? = null
    private var qrBitmap: Bitmap? = null

    companion object {
        private const val ARG_QR_ITEM = "qr_item"

        fun newInstance(item: QRItem): QRDetailBottomSheet {
            return QRDetailBottomSheet().apply {
                arguments = Bundle().apply {
                    putLong("id", item.id)
                    putString("title", item.title)
                    putString("qrImagePath", item.qrImagePath)
                    putString("qrType", item.qrType.name)
                    putString("qrData", item.qrData)
                    putString("qrColor", item.qrColor)
                    putString("backgroundColor", item.backgroundColor)
                    putString("logoPath", item.logoPath)
                    putString("logoStyle", item.logoStyle.name)
                    putLong("createdAt", item.createdAt)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogQrDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadQRItem()
        setupViews()
        generateQRCode()
    }

    private fun loadQRItem() {
        arguments?.let { args ->
            qrItem = QRItem(
                id = args.getLong("id"),
                title = args.getString("title", ""),
                qrImagePath = args.getString("qrImagePath"),
                qrType = com.createqr.domain.model.CreateType.valueOf(args.getString("qrType", "URL")),
                qrData = args.getString("qrData", ""),
                qrColor = args.getString("qrColor", "FF000000"),
                backgroundColor = args.getString("backgroundColor", "FFFFFFFF"),
                logoPath = args.getString("logoPath"),
                logoStyle = LogoStyle.valueOf(args.getString("logoStyle", "SQUARE")),
                createdAt = args.getLong("createdAt")
            )
        }
    }

    private fun setupViews() {
        qrItem?.let { item ->
            binding.tvTitle.text = item.title
            binding.tvQrContent.text = item.qrData
        }

        binding.btnSave.setOnClickListener {
            saveToGallery()
        }

        binding.btnShare.setOnClickListener {
            shareQR()
        }

        binding.btnReadQr.setOnClickListener {
            toggleQRContent()
        }

        binding.cardQrContent.setOnLongClickListener {
            copyToClipboard()
            true
        }
    }

    private fun generateQRCode() {
        qrItem?.let { item ->
            val qrColor = Color.parseColor("#${item.qrColor}")
            val bgColor = Color.parseColor("#${item.backgroundColor}")

            val logo = item.logoPath?.let { path ->
                try {
                    BitmapFactory.decodeFile(path)
                } catch (e: Exception) {
                    null
                }
            }

            qrBitmap = generateQRCodeUseCase(
                data = item.qrData,
                size = 512,
                qrColor = qrColor,
                backgroundColor = bgColor,
                logo = logo,
                logoStyle = item.logoStyle
            )

            binding.ivQrCode.setImageBitmap(qrBitmap)
        }
    }

    private fun toggleQRContent() {
        binding.cardQrContent.isVisible = !binding.cardQrContent.isVisible

        if (binding.cardQrContent.isVisible) {
            binding.btnReadQr.text = getString(R.string.ok)
        } else {
            binding.btnReadQr.text = getString(R.string.read_qr)
        }
    }

    private fun copyToClipboard() {
        qrItem?.let { item ->
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("QR Content", item.qrData)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToGallery() {
        qrBitmap?.let { bitmap ->
            val savedUri = MediaStore.Images.Media.insertImage(
                requireContext().contentResolver,
                bitmap,
                "QR_${System.currentTimeMillis()}",
                "QR Code generated by CreateQR"
            )
            if (savedUri != null) {
                Toast.makeText(context, R.string.save_success, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, R.string.save_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareQR() {
        qrBitmap?.let { bitmap ->
            try {
                val cachePath = File(requireContext().cacheDir, "images")
                cachePath.mkdirs()
                val file = File(cachePath, "qr_share_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                val contentUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share)))
            } catch (e: Exception) {
                // Fallback to old method
                val path = MediaStore.Images.Media.insertImage(
                    requireContext().contentResolver,
                    bitmap,
                    "QR_Share",
                    null
                )
                val uri = Uri.parse(path)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share)))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
