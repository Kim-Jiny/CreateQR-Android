package com.createqr.presentation.create.types

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import com.createqr.R
import com.createqr.databinding.ViewTypeWifiBinding

class WiFiTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseQRTypeView(context, attrs, defStyleAttr) {

    private lateinit var inputBinding: ViewTypeWifiBinding

    override fun setupInputView() {
        inputBinding = ViewTypeWifiBinding.inflate(
            LayoutInflater.from(context),
            baseBinding.inputContainer,
            true
        )

        val securityTypes = arrayOf("WPA/WPA2", "WEP", "None")
        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, securityTypes)
        inputBinding.spinnerSecurity.adapter = adapter
    }

    override fun getQRData(): String {
        val ssid = inputBinding.etSsid.text.toString().trim()
        val password = inputBinding.etPassword.text.toString()
        val securityType = when (inputBinding.spinnerSecurity.selectedItemPosition) {
            0 -> "WPA"
            1 -> "WEP"
            else -> "nopass"
        }
        val hidden = if (inputBinding.cbHidden.isChecked) "true" else "false"

        return "WIFI:T:$securityType;S:$ssid;P:$password;H:$hidden;;"
    }

    override fun validateInput(data: String): Boolean {
        if (inputBinding.etSsid.text.toString().trim().isEmpty()) {
            Toast.makeText(context, R.string.enter_wifi_name, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
