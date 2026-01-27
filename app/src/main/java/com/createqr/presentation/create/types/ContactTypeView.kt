package com.createqr.presentation.create.types

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Toast
import com.jiny.createqr.R
import com.jiny.createqr.databinding.ViewTypeContactBinding

class ContactTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseQRTypeView(context, attrs, defStyleAttr) {

    private lateinit var inputBinding: ViewTypeContactBinding

    override fun setupInputView() {
        inputBinding = ViewTypeContactBinding.inflate(
            LayoutInflater.from(context),
            baseBinding.inputContainer,
            true
        )
    }

    override fun getQRData(): String {
        val name = inputBinding.etName.text.toString().trim()
        val phone = inputBinding.etPhone.text.toString().trim()
        val email = inputBinding.etEmail.text.toString().trim()
        val company = inputBinding.etCompany.text.toString().trim()

        val vCard = StringBuilder()
        vCard.append("BEGIN:VCARD\n")
        vCard.append("VERSION:3.0\n")
        vCard.append("N:;$name;;;\n")
        vCard.append("FN:$name\n")
        vCard.append("TEL:$phone\n")
        if (email.isNotEmpty()) {
            vCard.append("EMAIL:$email\n")
        }
        if (company.isNotEmpty()) {
            vCard.append("ORG:$company\n")
        }
        vCard.append("END:VCARD")

        return vCard.toString()
    }

    override fun validateInput(data: String): Boolean {
        if (inputBinding.etName.text.toString().trim().isEmpty()) {
            Toast.makeText(context, R.string.enter_name, Toast.LENGTH_SHORT).show()
            return false
        }
        if (inputBinding.etPhone.text.toString().trim().isEmpty()) {
            Toast.makeText(context, R.string.enter_phone, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
