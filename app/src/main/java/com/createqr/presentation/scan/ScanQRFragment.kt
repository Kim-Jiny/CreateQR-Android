package com.createqr.presentation.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.createqr.R
import com.createqr.databinding.FragmentScanQrBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class ScanQRFragment : Fragment() {

    private var _binding: FragmentScanQrBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScanQRViewModel by viewModels()

    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService
    private var isScanning = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(context, R.string.camera_permission_required, Toast.LENGTH_LONG).show()
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { scanImageFromGallery(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        setupButtons()
        observeViewModel()
        checkCameraPermission()
    }

    private fun setupButtons() {
        binding.btnGallery.setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    private fun observeViewModel() {
        viewModel.scannedResult.observe(viewLifecycleOwner) { result ->
            result?.let { showResultDialog(it) }
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, R.string.save_success, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QRCodeAnalyzer { result ->
                        if (isScanning) {
                            isScanning = false
                            requireActivity().runOnUiThread {
                                viewModel.onQRScanned(result)
                            }
                        }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun scanImageFromGallery(uri: Uri) {
        try {
            val image = InputImage.fromFilePath(requireContext(), uri)
            val scanner = BarcodeScanning.getClient()

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        barcodes.first().rawValue?.let { result ->
                            viewModel.onQRScanned(result)
                        }
                    } else {
                        Toast.makeText(context, R.string.no_qr_found, Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, R.string.scan_failed, Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showResultDialog(result: String) {
        when {
            viewModel.isVCard(result) -> showVCardDialog(result)
            viewModel.isInstagram(result) -> showInstagramDialog(result)
            viewModel.isYouTube(result) -> showYouTubeDialog(result)
            viewModel.isTikTok(result) -> showTikTokDialog(result)
            else -> showDefaultDialog(result)
        }
    }

    private fun showDefaultDialog(result: String) {
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.qr_content)
            .setMessage(result)
            .setPositiveButton(R.string.ok) { _, _ -> resumeScanning() }
            .setNegativeButton(R.string.save_to_app) { _, _ ->
                viewModel.saveScannedQR(result)
                resumeScanning()
            }

        // If it's a URL, add open option
        if (result.startsWith("http://") || result.startsWith("https://")) {
            builder.setNeutralButton(R.string.open_in_browser) { _, _ ->
                openUrl(result)
                resumeScanning()
            }
        }

        builder.show()
    }

    private fun showVCardDialog(result: String) {
        val name = Regex("FN:(.+)").find(result)?.groupValues?.get(1) ?: ""
        val phone = Regex("TEL:(.+)").find(result)?.groupValues?.get(1) ?: ""
        val email = Regex("EMAIL:(.+)").find(result)?.groupValues?.get(1) ?: ""

        val message = buildString {
            if (name.isNotEmpty()) append("Name: $name\n")
            if (phone.isNotEmpty()) append("Phone: $phone\n")
            if (email.isNotEmpty()) append("Email: $email")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.contact_found)
            .setMessage(message)
            .setPositiveButton(R.string.add_to_contacts) { _, _ ->
                addToContacts(name, phone, email)
                resumeScanning()
            }
            .setNegativeButton(R.string.save_to_app) { _, _ ->
                viewModel.saveScannedQR(result)
                resumeScanning()
            }
            .setNeutralButton(R.string.cancel) { _, _ -> resumeScanning() }
            .show()
    }

    private fun showInstagramDialog(result: String) {
        val username = viewModel.extractInstagramUsername(result) ?: return showDefaultDialog(result)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Instagram")
            .setMessage("@$username")
            .setPositiveButton(R.string.open_in_app) { _, _ ->
                openInstagram(username)
                resumeScanning()
            }
            .setNegativeButton(R.string.open_in_browser) { _, _ ->
                openUrl(result)
                resumeScanning()
            }
            .setNeutralButton(R.string.cancel) { _, _ -> resumeScanning() }
            .show()
    }

    private fun showYouTubeDialog(result: String) {
        val channel = viewModel.extractYouTubeChannel(result) ?: return showDefaultDialog(result)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("YouTube")
            .setMessage(channel)
            .setPositiveButton(R.string.open_in_app) { _, _ ->
                openYouTube(result)
                resumeScanning()
            }
            .setNegativeButton(R.string.open_in_browser) { _, _ ->
                openUrl(result)
                resumeScanning()
            }
            .setNeutralButton(R.string.cancel) { _, _ -> resumeScanning() }
            .show()
    }

    private fun showTikTokDialog(result: String) {
        val username = viewModel.extractTikTokUsername(result) ?: return showDefaultDialog(result)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("TikTok")
            .setMessage("@$username")
            .setPositiveButton(R.string.open_in_app) { _, _ ->
                openTikTok(username)
                resumeScanning()
            }
            .setNegativeButton(R.string.open_in_browser) { _, _ ->
                openUrl(result)
                resumeScanning()
            }
            .setNeutralButton(R.string.cancel) { _, _ -> resumeScanning() }
            .show()
    }

    private fun resumeScanning() {
        viewModel.clearResult()
        isScanning = true
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun openInstagram(username: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("instagram://user?username=$username"))
            startActivity(intent)
        } catch (e: Exception) {
            openUrl("https://instagram.com/$username")
        }
    }

    private fun openYouTube(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setPackage("com.google.android.youtube")
            startActivity(intent)
        } catch (e: Exception) {
            openUrl(url)
        }
    }

    private fun openTikTok(username: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("snssdk1233://user/profile/$username"))
            startActivity(intent)
        } catch (e: Exception) {
            openUrl("https://tiktok.com/@$username")
        }
    }

    private fun addToContacts(name: String, phone: String, email: String) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = ContactsContract.Contacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, name)
            putExtra(ContactsContract.Intents.Insert.PHONE, phone)
            putExtra(ContactsContract.Intents.Insert.EMAIL, email)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}
