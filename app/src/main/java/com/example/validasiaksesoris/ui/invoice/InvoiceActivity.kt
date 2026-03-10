package com.example.validasiaksesoris.ui.invoice

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.validasiaksesoris.data.model.invoice.FrameNumber
import com.example.validasiaksesoris.data.retrofit.ApiConfig
import com.example.validasiaksesoris.databinding.ActivityInvoiceBinding
import com.example.validasiaksesoris.di.Result
import com.example.validasiaksesoris.ui.ViewModelFactory
import kotlinx.coroutines.launch

class InvoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInvoiceBinding
    private val viewModel by viewModels<InvoiceViewModel> {
        ViewModelFactory.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityInvoiceBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.rvFrameNumber.layoutManager = LinearLayoutManager(this)
        val data = mutableListOf<FrameNumber>()

        viewModel.getData().observe(this) {
            if (it != null) {
                when (it) {
                    is Result.Loading -> {}
                    is Result.Success -> {
                        data.clear()
                        data.addAll(it.data)
                        val adapter = InvoiceAdapter(data)
                        binding.rvFrameNumber.adapter = adapter
                    }
                    is Result.Error -> {}
                }
            }
        }
    }
}