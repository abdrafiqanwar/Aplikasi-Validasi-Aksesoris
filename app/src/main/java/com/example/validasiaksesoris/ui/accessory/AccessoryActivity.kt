package com.example.validasiaksesoris.ui.accessory

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.validasiaksesoris.R
import com.example.validasiaksesoris.data.model.AccessoryItem
import com.example.validasiaksesoris.data.model.AccessoryRequest
import com.example.validasiaksesoris.databinding.ActivityAccessoryBinding
import com.example.validasiaksesoris.di.Result
import com.example.validasiaksesoris.ui.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AccessoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccessoryBinding
    private val viewModel by viewModels<AccessoryViewModel> {
        ViewModelFactory.getInstance()
    }
    private lateinit var accessoryAdapter: AccessoryAdapter
    private lateinit var vehicleName: Array<String>
    private lateinit var vehicleData: Map<String, List<AccessoryItem>>

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAccessoryBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val frameNumber = intent.getStringExtra("frameNumber")
        binding.tvFrameNumber.text = frameNumber

        vehicleName = resources.getStringArray(R.array.vehicle_name)

        vehicleData = vehicleName.associateWith { name ->
            val arrayName = "acc_" + name.lowercase().replace("[^a-z0-9]".toRegex(), "")

            val resId = resources.getIdentifier(arrayName, "array", packageName)

            if (resId != 0) {
                resources.getStringArray(resId).map { label ->
                    AccessoryItem(
                        key = label,
                        label = label
                    )
                }
            } else {
                emptyList()
            }
        }

        binding.rvAccessory.layoutManager = LinearLayoutManager(this)
        accessoryAdapter = AccessoryAdapter(mutableListOf())
        binding.rvAccessory.adapter = accessoryAdapter

        val adapterVehicleModel = object : ArrayAdapter<String>(
            this,
            R.layout.selected_item,
            vehicleName
        ) {
            override fun isEnabled(position: Int): Boolean = position != 0

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView

                if (position == 0) {
                    tv.setTextColor(ContextCompat.getColor(context, R.color.gray))
                } else {
                    val typedValue = TypedValue()
                    context.theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
                    tv.setTextColor(ContextCompat.getColor(context, typedValue.resourceId))
                }
                return view
            }
        }

        adapterVehicleModel.setDropDownViewResource(R.layout.item_dropdown)
        binding.vehicleModel.adapter = adapterVehicleModel

        binding.vehicleModel.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position != 0) {
                        showAccessory()
                    }

                    val selectedVehicle = vehicleName[position]
                    val accessories = vehicleData[selectedVehicle] ?: emptyList()

                    accessoryAdapter.clearSelection()
                    accessoryAdapter.updateData(accessories)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        binding.btnSubmit.setOnClickListener {

            if (accessoryAdapter.hasUnselected()) {
                showAlert("Silahkan pilih status aksesoris", AlertType.ERROR)
                return@setOnClickListener
            }

            val selectedVehicleModel = binding.vehicleModel.selectedItem
            val selectedAccessories = accessoryAdapter.getSelection()

            val request = AccessoryRequest(
                frameNumber.toString(),
                selectedVehicleModel.toString(),
                selectedAccessories
            )

            viewModel.sendData(request).observe(this) { data ->
                if (data != null) {
                    when (data) {
                        is Result.Loading -> {
                            showLoading(true)
                            setUiEnabled(false)
                        }
                        is Result.Success -> {
                            showLoading(false)
                            setUiEnabled(true)
                            showAlert(data.data.message, AlertType.SUCCESS)
                        }
                        is Result.Error -> {
                            showLoading(false)
                            setUiEnabled(true)
                            showAlert(data.error, AlertType.ERROR)
                        }
                    }
                }
            }
        }
    }

    private fun showAccessory() {
        binding.divider.visibility = View.VISIBLE
        binding.tvTitleAcc.visibility = View.VISIBLE
        binding.tvSubTitleAcc.visibility = View.VISIBLE
        binding.rvAccessory.visibility = View.VISIBLE
        binding.btnSubmit.visibility = View.VISIBLE
    }

    private fun showLoading(show: Boolean) {
        binding.btnSubmit.visibility = if (show) View.GONE else View.VISIBLE
        binding.pb.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setUiEnabled(enabled: Boolean) {
        binding.vehicleModel.isEnabled = enabled
        binding.btnSubmit.isEnabled = enabled
        accessoryAdapter.setEnabled(enabled)
    }

    private fun showAlert(message: String, type: AlertType) {
        MaterialAlertDialogBuilder(this)
            .setTitle(
                when (type) {
                    AlertType.SUCCESS -> "Berhasil"
                    AlertType.ERROR -> "Gagal"
                }
            )
            .setMessage(message)
            .setIcon(
                when (type) {
                    AlertType.SUCCESS -> R.drawable.ic_success
                    AlertType.ERROR -> R.drawable.ic_error
                }
            )
            .setPositiveButton("OK") { dialog , _ ->
                when (type) {
                    AlertType.SUCCESS -> finish()
                    AlertType.ERROR -> dialog.dismiss()
                }
            }
            .setCancelable(false)
            .show()
    }

    enum class AlertType {
        SUCCESS, ERROR
    }
}