package com.createqr.presentation.create.types

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import com.createqr.R
import com.createqr.databinding.ViewTypeBankTransferBinding

class BankTransferTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseQRTypeView(context, attrs, defStyleAttr) {

    private lateinit var inputBinding: ViewTypeBankTransferBinding

    private val banks = listOf(
        "KB국민은행", "신한은행", "우리은행", "하나은행",
        "NH농협은행", "IBK기업은행", "SC제일은행", "카카오뱅크",
        "토스뱅크", "케이뱅크"
    )

    override fun setupInputView() {
        inputBinding = ViewTypeBankTransferBinding.inflate(
            LayoutInflater.from(context),
            baseBinding.inputContainer,
            true
        )

        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, banks)
        inputBinding.spinnerBank.adapter = adapter
    }

    override fun getQRData(): String {
        val bank = banks[inputBinding.spinnerBank.selectedItemPosition]
        val account = inputBinding.etAccount.text.toString().trim()
        val holder = inputBinding.etHolder.text.toString().trim()
        val amount = inputBinding.etAmount.text.toString().trim()

        val sb = StringBuilder()
        sb.append("Bank: $bank\n")
        sb.append("Account: $account\n")
        sb.append("Holder: $holder")
        if (amount.isNotEmpty()) {
            sb.append("\nAmount: ${amount}원")
        }

        return sb.toString()
    }

    override fun validateInput(data: String): Boolean {
        if (inputBinding.etAccount.text.toString().trim().isEmpty()) {
            Toast.makeText(context, R.string.enter_account_number, Toast.LENGTH_SHORT).show()
            return false
        }
        if (inputBinding.etHolder.text.toString().trim().isEmpty()) {
            Toast.makeText(context, R.string.enter_account_holder, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
