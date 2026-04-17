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
import com.example.validasiaksesoris.data.model.accessory.AccessoryItem
import com.example.validasiaksesoris.data.model.accessory.AccessoryResponse
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
    private var dataList: List<AccessoryResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAccessoryBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val frameNumber = intent.getStringExtra("frameNumber")
        binding.tvFrameNumber.text = frameNumber

        viewModel.getAccessories("Master Data").observe(this) { result ->
            if (result != null) {
                when (result) {
                    is Result.Loading -> { showLoading(true) }
                    is Result.Success -> {
                        showLoading(false)

                        dataList = result.data

                        val models = mutableListOf("Pilih model kendaraan")
                        models.addAll(dataList.map { it.vehicleModel })

                        setupSpinner(models)
                    }
                    is Result.Error -> {
                        showLoading(false)
                        showAlert(result.error, AlertType.ERROR)
                    }
                }
            }
        }

        binding.rvAccessory.layoutManager = LinearLayoutManager(this)
        accessoryAdapter = AccessoryAdapter(mutableListOf())
        binding.rvAccessory.adapter = accessoryAdapter

        binding.btnSubmit.setOnClickListener {

            if (accessoryAdapter.hasUnselected()) {
                showAlert("Silahkan pilih status aksesoris", AlertType.ERROR)
                return@setOnClickListener
            }

            val selectedVehicleModel = binding.vehicleModel.selectedItem
            val selectedAccessories = accessoryAdapter.getSelectedAccessories()

            val request = AccessoryResponse(
                frameNumber.toString(),
                selectedVehicleModel.toString(),
                selectedAccessories
            )

            viewModel.sendData(request).observe(this) { result ->
                if (result != null) {
                    when (result) {
                        is Result.Loading -> {
                            showLoading(true)
                            setUiEnabled(false)
                        }
                        is Result.Success -> {
                            showLoading(false)
                            setUiEnabled(true)
                            showAlert(result.data.message, AlertType.SUCCESS)
                        }
                        is Result.Error -> {
                            showLoading(false)
                            setUiEnabled(true)
                            showAlert(result.error, AlertType.ERROR)
                        }
                    }
                }
            }
        }
    }

    private fun setupSpinner(models: List<String>) {
        val adapterVehicleModel = object : ArrayAdapter<String>(
            this,
            R.layout.selected_item,
            models
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
                    if (position === 0) return

                    showAccessory()

                    val selectedVehicle = dataList[position - 1]

                    val accessories = selectedVehicle.accessories.map {
                        AccessoryItem(it.name)
                    }

                    accessoryAdapter.clearSelection()
                    accessoryAdapter.updateData(accessories)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
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
        binding.tvVehicleModel.visibility = if (show) View.GONE else View.VISIBLE
        binding.vehicleModel.visibility = if (show) View.GONE else View.VISIBLE
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