package com.createqr.presentation.create

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.createqr.R
import com.createqr.databinding.FragmentCreateQrBinding
import com.createqr.domain.model.CreateType
import com.createqr.domain.model.LogoStyle
import com.createqr.domain.model.QRTypeItem
import com.createqr.presentation.create.adapter.QRTypeAdapter
import com.createqr.presentation.create.types.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateQRFragment : Fragment() {

    private var _binding: FragmentCreateQrBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateQRViewModel by viewModels()

    private lateinit var qrTypeAdapter: QRTypeAdapter
    private var currentTypeView: BaseQRTypeView? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
            showLogoStyleDialog(bitmap)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        qrTypeAdapter = QRTypeAdapter { type ->
            viewModel.selectType(type)
        }

        binding.rvQrTypes.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = qrTypeAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.qrTypes.observe(viewLifecycleOwner) { types ->
            qrTypeAdapter.submitList(types)
        }

        viewModel.selectedType.observe(viewLifecycleOwner) { type ->
            type?.let { showTypeView(it) }
            qrTypeAdapter.setSelectedType(type)
        }

        viewModel.generatedQRBitmap.observe(viewLifecycleOwner) { bitmap ->
            currentTypeView?.setQRImage(bitmap)
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, R.string.save_success, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, R.string.save_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTypeView(type: QRTypeItem) {
        currentTypeView?.let {
            binding.typeContainer.removeView(it)
        }

        currentTypeView = when (type.type) {
            CreateType.URL -> URLTypeView(requireContext())
            CreateType.WIFI -> WiFiTypeView(requireContext())
            CreateType.BANK_TRANSFER -> BankTransferTypeView(requireContext())
            CreateType.CONTACT -> ContactTypeView(requireContext())
            CreateType.INSTAGRAM -> InstagramTypeView(requireContext())
            CreateType.YOUTUBE -> YouTubeTypeView(requireContext())
            CreateType.TIKTOK -> TikTokTypeView(requireContext())
        }

        currentTypeView?.let { typeView ->
            typeView.setOnGenerateListener { data ->
                viewModel.generateQR(data)
            }
            typeView.setOnSaveListener {
                showSaveOptions()
            }
            typeView.setOnShareListener {
                shareQR()
            }
            typeView.setOnColorListener {
                showColorPicker()
            }
            typeView.setOnLogoListener {
                pickImage.launch("image/*")
            }

            binding.typeContainer.addView(typeView)
        }
    }

    private fun showSaveOptions() {
        val options = arrayOf(
            getString(R.string.save_to_gallery),
            getString(R.string.save_to_app)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.save_qr)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> saveToGallery()
                    1 -> viewModel.saveQRToApp()
                }
            }
            .show()
    }

    private fun saveToGallery() {
        viewModel.generatedQRBitmap.value?.let { bitmap ->
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
        viewModel.generatedQRBitmap.value?.let { bitmap ->
            val path = MediaStore.Images.Media.insertImage(
                requireContext().contentResolver,
                bitmap,
                "QR_Share",
                null
            )
            val uri = android.net.Uri.parse(path)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.share)))
        }
    }

    private fun showColorPicker() {
        // Color picker dialog - simplified version
        val colors = arrayOf("QR Color", "Background Color")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_color_target)
            .setItems(colors) { _, which ->
                when (which) {
                    0 -> showColorSelectionDialog(true)
                    1 -> showColorSelectionDialog(false)
                }
            }
            .show()
    }

    private fun showColorSelectionDialog(isQRColor: Boolean) {
        val colorNames = arrayOf("Black", "Red", "Blue", "Green", "Purple")
        val colorValues = intArrayOf(
            0xFF000000.toInt(),
            0xFFFF0000.toInt(),
            0xFF0000FF.toInt(),
            0xFF00FF00.toInt(),
            0xFF800080.toInt()
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (isQRColor) R.string.qr_color else R.string.background_color)
            .setItems(colorNames) { _, which ->
                if (isQRColor) {
                    viewModel.setQRColor(colorValues[which])
                } else {
                    viewModel.setBackgroundColor(colorValues[which])
                }
            }
            .show()
    }

    private fun showLogoStyleDialog(bitmap: Bitmap) {
        val styles = arrayOf(
            getString(R.string.circle),
            getString(R.string.square)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_logo_style)
            .setItems(styles) { _, which ->
                val style = if (which == 0) LogoStyle.CIRCLE else LogoStyle.SQUARE
                viewModel.setLogo(bitmap, style)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
