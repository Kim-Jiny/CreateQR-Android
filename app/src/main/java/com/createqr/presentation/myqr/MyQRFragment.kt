package com.createqr.presentation.myqr

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.createqr.R
import com.createqr.databinding.FragmentMyQrBinding
import com.createqr.domain.model.QRItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyQRFragment : Fragment() {

    private var _binding: FragmentMyQrBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyQRViewModel by viewModels()
    private lateinit var adapter: MyQRAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = MyQRAdapter(
            onItemClick = { item ->
                showQRDetailDialog(item)
            },
            onDeleteClick = { item ->
                showDeleteConfirmation(item)
            }
        )

        binding.rvMyQr.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@MyQRFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.qrItems.collect { items ->
                    adapter.submitList(items)
                    binding.tvEmpty.isVisible = items.isEmpty()
                    binding.rvMyQr.isVisible = items.isNotEmpty()
                }
            }
        }
    }

    private fun showQRDetailDialog(item: QRItem) {
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(item.title)
            .setMessage(item.qrData)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.edit_title) { _, _ ->
                showEditTitleDialog(item)
            }

        if (isUrl(item.qrData)) {
            builder.setNeutralButton(R.string.open_in_browser) { _, _ ->
                openInBrowser(item.qrData)
            }
        }

        builder.show()
    }

    private fun showEditTitleDialog(item: QRItem) {
        val container = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        val margin = (20 * resources.displayMetrics.density).toInt()
        params.setMargins(margin, 0, margin, 0)

        val textInputLayout = TextInputLayout(requireContext()).apply {
            layoutParams = params
            hint = getString(R.string.title)
        }

        val editText = TextInputEditText(textInputLayout.context).apply {
            setText(item.title)
        }

        textInputLayout.addView(editText)
        container.addView(textInputLayout)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_title)
            .setView(container)
            .setPositiveButton(R.string.save) { _, _ ->
                val newTitle = editText.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    viewModel.updateTitle(item, newTitle)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun isUrl(text: String): Boolean {
        return text.startsWith("http://") || text.startsWith("https://")
    }

    private fun openInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun showDeleteConfirmation(item: QRItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteItem(item)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
