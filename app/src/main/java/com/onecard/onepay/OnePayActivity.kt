package com.onecard.onepay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import com.onecard.onepay.databinding.ActivityOnePayBinding
import com.onecard.onepay.gpay.RecentTransAdapter
import com.onecard.onepay.gpay.RecentTransViewModel
import com.onecard.onepay.gpay.RecentTransactionItem
import com.onecard.onepay.util.Status
import com.onecard.onepay.util.hideKeyboard
import kotlinx.android.synthetic.main.activity_one_pay.*
import org.json.JSONException
import org.json.JSONObject
import org.koin.android.viewmodel.ext.android.viewModel

class OnePayActivity : AppCompatActivity() {

    //GPay wallet
    private lateinit var onePayBinding: ActivityOnePayBinding
    private lateinit var paymentsClient: PaymentsClient
    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 99
    private var addHideMoney = false
    private var walletAmount = 0f

    //recent trans data
    private lateinit var recentTransAdapter: RecentTransAdapter
    private var recentTransData = ArrayList<RecentTransactionItem>()

    //viewmodel
    private val recentTransViewModel: RecentTransViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onePayBinding = DataBindingUtil.setContentView(this, R.layout.activity_one_pay)

        //recent transactions
        recentTransAdapter = RecentTransAdapter(context = this, dataList = recentTransData)
        rcvRecentTrasnsaction.layoutManager = LinearLayoutManager(this)
        rcvRecentTrasnsaction.adapter = recentTransAdapter

        //viewmodel
        recentTransViewModel.recentTransData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    if (recentTransData.isNotEmpty()) recentTransData.clear()
                    recentTransData.addAll(it.data!!.recentTransactionData)
                    recentTransAdapter.notifyDataSetChanged()
                }
                Status.ERROR -> {

                }
            }
        })

        //current wallet amount
        onePayBinding.tvWalletBalance.text = getString(R.string.format_dollar, walletAmount)
        //create wallet client
        paymentsClient = PaymentsUtil.createPaymentsClient(this)

        onePayBinding.btnAddMoney.setOnClickListener {
            if (addHideMoney) {
                addHideMoney = false
                onePayBinding.btnAddMoney.text = getString(R.string.action_add_money)
                onePayBinding.tilAmount.visibility = View.GONE
                onePayBinding.tietAmount.text = null
                if (onePayBinding.btnGPay.isVisible) onePayBinding.btnGPay.visibility = View.GONE
            } else {
                addHideMoney = true
                onePayBinding.tilAmount.visibility = View.VISIBLE
                onePayBinding.btnAddMoney.text = getString(R.string.action_hide_add_money)
            }
        }

        onePayBinding.tietAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.isEmpty()) onePayBinding.btnGPay.visibility = View.GONE
                else {
                    if (!onePayBinding.btnGPay.isVisible) {
                        possiblyShowGooglePayButton()
                        /*  onePayBinding.btnGPay.visibility =
                              View.VISIBLE*/
                    }
                }
            }
        })

        onePayBinding.btnGPay.setOnClickListener {
            it.hideKeyboard()
            requestMoney(tietAmount.text.toString().trim())
        }
    }

    private fun possiblyShowGooglePayButton() {
        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString()) ?: return
        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)?.let(::setGooglePayAvailable)
            } catch (exception: ApiException) {
                // Process error
                Log.w("isReadyToPay failed", exception)
            }
        }
    }

    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
            btnGPay.visibility = View.VISIBLE
        } else {
            Toast.makeText(
                this,
                "Unfortunately, Google Pay is not available on this device",
                Toast.LENGTH_LONG
            ).show();
        }
    }

    private fun requestMoney(amount: String) {
        // Disables the button to prevent multiple clicks.
        onePayBinding.btnGPay.isClickable = false

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.

        val paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(amount.toString())
        if (paymentDataRequestJson == null) {
            Log.e("RequestPayment", "Can't fetch payment data request")
            return
        }
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        if (request != null) {
            AutoResolveHelper.resolveTask(
                paymentsClient.loadPaymentData(request), this, LOAD_PAYMENT_DATA_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // value passed in AutoResolveHelper
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK ->
                        data?.let { intent ->
                            PaymentData.getFromIntent(intent)?.let(::handlePaymentSuccess)
                        }
                    Activity.RESULT_CANCELED -> {
                        // Nothing to do here normally - the user simply cancelled without selecting a
                        // payment method.
                    }

                    AutoResolveHelper.RESULT_ERROR -> {
                        AutoResolveHelper.getStatusFromIntent(data)?.let {
                            handleError(it.statusCode)
                        }
                    }
                }
                // Re-enables the Google Pay payment button.
                onePayBinding.btnGPay.isClickable = true
            }
        }
    }

    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson() ?: return

        try {
            // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
            val paymentMethodData =
                JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            println("GPay Payment Info $paymentMethodData")
            // If the gateway is set to "example", no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".
            if (paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("type") == "PAYMENT_GATEWAY" && paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token") == "examplePaymentMethodToken"
            ) {

                AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage(
                        "Gateway name set to \"example\" - please modify " +
                                "Constants.java and replace it with your own gateway."
                    )
                    .setPositiveButton("OK", null)
                    .create()
                    .show()
            }

            val billingName = paymentMethodData.getJSONObject("info")
                .getJSONObject("billingAddress").getString("name")
            Log.d("BillingName", billingName)
            updateAmount()
            Toast.makeText(
                this,
                getString(R.string.payments_show_name, billingName),
                Toast.LENGTH_LONG
            ).show()
            println(
                "GPay Token ${
                    paymentMethodData
                        .getJSONObject("tokenizationData")
                        .getString("token")
                }"
            )

        } catch (e: JSONException) {
            Log.e("handlePaymentSuccess", "Error: $e")
        }

    }

    private fun updateAmount() {
        walletAmount += tietAmount.text.toString().trim().toFloat()
        onePayBinding.tvWalletBalance.text = getString(R.string.format_dollar, walletAmount)
        addHideMoney = true
        onePayBinding.btnAddMoney.callOnClick()
    }

    /**
     * At this stage, the user has already seen a popup informing them an error occurred. Normally,
     * only logging is required.
     *
     * @param statusCode will hold the value of any constant from CommonStatusCode or one of the
     * WalletConstants.ERROR_CODE_* constants.
     * @see [
     * Wallet Constants Library](https://developers.google.com/android/reference/com/google/android/gms/wallet/WalletConstants.constant-summary)
     */
    private fun handleError(statusCode: Int) {
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode))
    }
}