package com.example.validasiaksesoris.ui.invoice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.validasiaksesoris.R
import com.example.validasiaksesoris.data.model.invoice.FrameNumber
import com.example.validasiaksesoris.data.model.invoice.InvoiceResponse
import com.example.validasiaksesoris.databinding.ActivityInvoiceBinding
import com.example.validasiaksesoris.di.Result
import com.example.validasiaksesoris.ui.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.VerticalAlignment
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInvoiceBinding
    private var adapter: InvoiceAdapter? = null
    private var currentList = listOf<FrameNumber>()
    private var detailData = listOf<DetailResponse>()
    private val viewModel by viewModels<InvoiceViewModel> {
        ViewModelFactory.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityInvoiceBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.rvFrameNumber.layoutManager = LinearLayoutManager(this)

        viewModel.getData().observe(this) { result ->
            if (result != null) {
                when (result) {
                    is Result.Loading -> { showLoading(true) }
                    is Result.Success -> {
                        showLoading(false)

                        val filtered = result.data
                            .filter { it.frameNumber.isNotEmpty() }

                        currentList = filtered

                        if (adapter == null) {
                            adapter = InvoiceAdapter { item ->
                                val selectedCount = currentList.count { it.isSelected }

                                if (selectedCount >= 21) {
                                    currentList[item].isSelected = false
                                    adapter?.notifyItemChanged(item)
                                    Toast.makeText(this, "Maksimal 20 nomor rangka", Toast.LENGTH_SHORT).show()
                                    return@InvoiceAdapter
                                }

                                adapter?.submitList(currentList)

                                binding.btnSubmit.isEnabled = selectedCount > 0
                            }

                            binding.rvFrameNumber.adapter = adapter
                        }

                        adapter?.submitList(currentList)
                    }
                    is Result.Error -> { showLoading(false) }
                }
            }
        }

        binding.etSearch.addTextChangedListener { text ->
            val query = text.toString()

            val filteredList = if (query.isEmpty()) {
                currentList
            } else {
                currentList.filter {
                    it.frameNumber.contains(query, ignoreCase = true)
                }
            }

            adapter?.submitList(filteredList)
        }

        binding.btnSubmit.setOnClickListener {
            val frameNumberSelected = currentList
                .filter { it.isSelected }
                .map { it.frameNumber }

            val frameNumber = frameNumberSelected.joinToString(",")

            viewModel.getDetail(frameNumber).observe(this) { result ->
                if (result != null) {
                    when (result) {
                        is Result.Loading -> { showLoading(true )}
                        is Result.Success -> {
                            showLoading(false)

                            detailData = result.data

                            createPdf(data)
                        }
                        is Result.Error -> { showLoading(false) }
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.btnSubmit.visibility = if (show) View.GONE else View.VISIBLE
        binding.tvTitle.visibility = if (show) View.GONE else View.VISIBLE
        binding.rvFrameNumber.visibility = if (show) View.GONE else View.VISIBLE
        binding.etSearch.visibility = if (show) View.GONE else View.VISIBLE
        binding.pb.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun createPdf(invoices: List<InvoiceResponse>) {
        val sdf = SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH)
        val formattedDate = sdf.format(Date())
        val numberFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "invoice ${formattedDate}.pdf"
        )

        val writer = PdfWriter(file)
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc, PageSize.A4)
        document.setMargins(50f, 80f, 50f, 80f)

        val table = Table(floatArrayOf(1f, 4f, 4f, 6f, 3f, 3f)).useAllAvailableWidth()

        listOf("No", "Nomor Rangka", "Model", "Aksesoris", "Harga", "Total")
            .forEach {
                table.addHeaderCell(Cell().add(Paragraph(it)
                    .setFontSize(10f)
                    .setBold()))
                    .setTextAlignment(TextAlignment.CENTER)
            }

        var no = 1

        invoices.forEach { item ->
            val size = item.accessories.size

            table.addCell(Cell(size, 1)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(Paragraph(no.toString())
                .setFontSize(8f)
            ))
            table.addCell(Cell(size, 1)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(Paragraph(item.frameNumber)
                .setFontSize(8f)
            ))
            table.addCell(Cell(size, 1)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(Paragraph(item.vehicleModel)
                .setFontSize(8f)
            ))

            var first = true

            item.accessories.forEach { acc ->
                table.addCell(Cell().add(Paragraph(acc.name)
                    .setFontSize(8f)
                ))
                table.addCell(Cell().add(Paragraph(numberFormat.format(acc.price))
                    .setFontSize(8f)
                ))

                if (first) {
                    table.addCell(Cell(size, 1)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .add(Paragraph(numberFormat.format(item.total))
                            .setFontSize(8f)))
                    first = false
                }
            }

            no++
        }

        document.add(table)

        document.close()

        showAlert("Invoice berhasil dibuat", AlertType.SUCCESS, file)
    }

    private fun showAlert(message: String, type: AlertType, file: File) {
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
                    AlertType.SUCCESS -> {
                        openPdf(file)
                        dialog.dismiss()
                        finish()
                    }
                    AlertType.ERROR -> dialog.dismiss()
                }
            }
            .setCancelable(false)
            .show()
    }

    enum class AlertType {
        SUCCESS, ERROR
    }

    fun openPdf(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(intent)
    }
}