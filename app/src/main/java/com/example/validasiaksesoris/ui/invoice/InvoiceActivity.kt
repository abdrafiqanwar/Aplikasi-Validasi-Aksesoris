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
import com.example.validasiaksesoris.data.model.invoice.DetailResponse
import com.example.validasiaksesoris.data.model.invoice.SummaryResponse
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
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.VerticalAlignment
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class InvoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInvoiceBinding
    private var adapter: InvoiceAdapter? = null
    private var currentList = listOf<FrameNumber>()
    private var detailData = listOf<DetailResponse>()
    private var summaryData = listOf<SummaryResponse>()
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
                            .reversed()
                            .sortedBy { it.createdAt.isNotEmpty() }

                        currentList = filtered

                        if (adapter == null) {
                            adapter = InvoiceAdapter { item ->
                                val selectedCount = currentList.count { it.isSelected }

                                if (selectedCount > 20) {
                                    currentList[item].isSelected = false
                                    adapter?.notifyItemChanged(item)
                                    Toast.makeText(this, "Maksimal 20 nomor rangka", Toast.LENGTH_SHORT).show()
                                    return@InvoiceAdapter
                                }

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

            val frameNumber = frameNumberSelected.joinToString(",") { it.frameNumber }

            val already = frameNumberSelected.any { it.createdAt.isNotEmpty() }
            val alreadyList = frameNumberSelected.filter { it.createdAt.isNotEmpty() }

            if (already) {
                val message = alreadyList.joinToString("\n") {
                    val formattedDate = formatDate(it.createdAt)
                    "Nomor rangka ${it.frameNumber} sudah dicetak pada tanggal $formattedDate"
                }
                showAlert(message, AlertType.ERROR)
                return@setOnClickListener
            } else {
                viewModel.getDetail(frameNumber).observe(this) { result ->
                    if (result != null) {
                        when (result) {
                            is Result.Loading -> { showLoading(true )}
                            is Result.Success -> {
                                showLoading(false)

                                detailData = result.data

                                if (detailData.isNotEmpty()) {
                                    viewModel.getSummary("Summary").observe(this) { result ->
                                        if (result != null) {
                                            when (result) {
                                                is Result.Loading -> { showLoading(true) }
                                                is Result.Success -> {
                                                    showLoading(false)

                                                    summaryData = result.data

                                                    if (summaryData.isNotEmpty()) {
                                                        val file = generateInvoicePdf(summaryData, detailData)
                                                        showAlert("Invoice berhasil dibuat", AlertType.SUCCESS)
                                                        openPdf(file)
                                                    }
                                                }
                                                is Result.Error -> { showLoading(false) }
                                            }
                                        }
                                    }
                                }
                            }
                            is Result.Error -> { showLoading(false) }
                        }
                    }
                }
            }
        }
    }

    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val date = inputFormat.parse(dateString)

            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateString // fallback kalau error
        }
    }

    private fun showLoading(show: Boolean) {
        binding.btnSubmit.visibility = if (show) View.GONE else View.VISIBLE
        binding.tvTitle.visibility = if (show) View.GONE else View.VISIBLE
        binding.rvFrameNumber.visibility = if (show) View.GONE else View.VISIBLE
        binding.etSearch.visibility = if (show) View.GONE else View.VISIBLE
        binding.pb.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun generateInvoicePdf(summaryData: List<SummaryResponse>, detailData: List<DetailResponse>): File {
        val sdf = SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH)
        val formattedDate = sdf.format(Date())
        val numberFormat = NumberFormat.getNumberInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "invoice ${formattedDate}.pdf"
        )

        val writer = PdfWriter(file)
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc, PageSize.A4)
        document.setMargins(50f, 80f, 50f, 80f)

        val inputStream = resources.openRawResource(R.drawable.logo)
        val bytes = inputStream.readBytes()

        val imageData = ImageDataFactory.create(bytes)
        val image = Image(imageData).scaleToFit(80f, 80f)

        val header = Table(floatArrayOf(1f, 1f)).useAllAvailableWidth()

        header.addCell(
            Cell()
                .add(image)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
        )

        header.addCell(
            Cell().apply {
                add(Paragraph("\n\n"))
                add(Paragraph("Citra Tower, Lv. 20. Unit A"))
                add(Paragraph("Kemayoran Jakarta 10630"))
                add(Paragraph("Indonesia"))
                add(Paragraph("+6221-39719888"))
                setTextAlignment(TextAlignment.RIGHT)
                setFontSize(8f)
                setBorder(Border.NO_BORDER)
            }
        )

        header.setBorderBottom(SolidBorder(1f))
        header.setMarginBottom(10f)

        document.add(header)

        document.add(
            Paragraph("INVOICE")
                .setBold()
                .setUnderline()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(8f)
        )

        document.add(
            Paragraph("INV.")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginLeft(-25f)
                .setMarginTop(-5f)
                .setFontSize(7f)
        )

        val subTable = Table(floatArrayOf(1f, 1f)).useAllAvailableWidth()

        subTable.setMarginTop(10f)

        subTable.addCell(
            Cell().apply {
                add(Paragraph("Kepada Yth."))
                add(Paragraph("PT.HADJI KALLA").setBold())
                add(Paragraph("Wisma Kalla Lt.12"))
                add(Paragraph("Makassar"))

                setBorder(Border.NO_BORDER)
                setFontSize(7f)
                setPaddingRight(150f)
            }
        )

        subTable.addCell(
            Cell().apply {
                add(Paragraph("Tanggal      : $formattedDate"))
                add(Paragraph("Mata Uang : IDR"))

                setBorder(Border.NO_BORDER)
                setFontSize(7f)
            }
        )

        document.add(subTable)

        val summaryTable = Table(5).useAllAvailableWidth()

        summaryTable.setMarginTop(20f)

        listOf("No", "Item", "QTY", "Harga Satuan", "Jumlah")
            .forEach {
                summaryTable.addHeaderCell(Cell().add(Paragraph(it)
                    .setFontSize(5f)
                    .setBold()))
                    .setTextAlignment(TextAlignment.CENTER)
            }

        var summaryNumber = 1

        summaryData.forEach { item ->
            summaryTable.addCell(Cell()
                .add(Paragraph(summaryNumber.toString())
                .setFontSize(5f)
            ))

            summaryTable.addCell(Cell()
                .add(Paragraph(item.item)
                .setFontSize(5f)
                .setTextAlignment(TextAlignment.LEFT)
            ))

            summaryTable.addCell(Cell()
                .add(Paragraph(item.qty.toString())
                .setFontSize(5f)
            ))

            summaryTable.addCell(Cell()
                .add(Paragraph(numberFormat.format(item.harga_satuan))
                .setFontSize(5f)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPaddingRight(5f)
            ))

            summaryTable.addCell(Cell()
                .add(Paragraph(numberFormat.format(item.jumlah))
                .setFontSize(5f)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPaddingRight(5f)
            ))

            summaryNumber++
        }

        val total = summaryData.sumOf { it.jumlah }
        val ppn = total * 0.11
        val grandTotal = total + ppn

        summaryTable.addCell(Cell(1, 3)
            .setBorder(Border.NO_BORDER)
        )

        summaryTable.addCell(Cell()
            .add(Paragraph("Total"))
            .setFontSize(5f)
            .setTextAlignment(TextAlignment.LEFT)
            .setPaddingLeft(5f)
        )

        summaryTable.addCell(Cell()
            .add(Paragraph(numberFormat.format(total)))
            .setFontSize(5f)
            .setTextAlignment(TextAlignment.RIGHT)
            .setPaddingRight(7f)
        )

        summaryTable.addCell(Cell(1, 3)
            .setBorder(Border.NO_BORDER)
        )

        summaryTable.addCell(Cell()
            .add(Paragraph("PPN 11%"))
            .setFontSize(5f)
            .setTextAlignment(TextAlignment.LEFT)
            .setPaddingLeft(5f)
        )

        summaryTable.addCell(Cell()
            .add(Paragraph(numberFormat.format(ppn)))
            .setFontSize(5f)
            .setTextAlignment(TextAlignment.RIGHT)
            .setPaddingRight(7f)
        )

        summaryTable.addCell(Cell(1, 3)
            .setBorder(Border.NO_BORDER)
        )

        summaryTable.addCell(Cell()
            .add(Paragraph("Grand Total"))
            .setFontSize(5f)
            .setTextAlignment(TextAlignment.LEFT)
            .setPaddingLeft(5f)
        )

        summaryTable.addCell(Cell()
            .add(Paragraph(numberFormat.format(grandTotal)))
            .setFontSize(5f)
            .setTextAlignment(TextAlignment.RIGHT)
            .setPaddingRight(7f)
        )

        document.add(summaryTable)

        document.add(AreaBreak())

        val detailTable = Table(6).useAllAvailableWidth()

        listOf("No", "Nomor Rangka", "Model", "Aksesoris", "Harga", "Total")
            .forEach {
                detailTable.addHeaderCell(Cell().add(Paragraph(it)
                    .setFontSize(5f)
                    .setBold()))
                    .setTextAlignment(TextAlignment.CENTER)
            }

        var detailNumber = 1

        detailData.forEach { item ->
            val size = item.accessories.size

            detailTable.addCell(Cell(size, 1)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(Paragraph(detailNumber.toString())
                .setFontSize(5f)
            ))
            detailTable.addCell(Cell(size, 1)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(Paragraph(item.frameNumber)
                .setFontSize(5f)
            ))
            detailTable.addCell(Cell(size, 1)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(Paragraph(item.vehicleModel)
                .setFontSize(5f)
            ))

            var detailFirst = true

            item.accessories.forEach { acc ->
                detailTable.addCell(Cell().add(Paragraph(acc.name)
                    .setFontSize(5f)
                    .setTextAlignment(TextAlignment.LEFT)
                ))
                detailTable.addCell(Cell().add(Paragraph(numberFormat.format(acc.price))
                    .setFontSize(5f)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPaddingRight(5f)
                ))

                if (detailFirst) {
                    detailTable.addCell(Cell(size, 1)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .add(Paragraph(numberFormat.format(item.total))
                            .setFontSize(5f)
                        ))
                    detailFirst = false
                }
            }

            detailNumber++
        }

        detailTable.addCell(Cell(1, 4)
            .setBorder(Border.NO_BORDER)
        )

        detailTable.addCell(Cell()
            .add(Paragraph("Jumlah"))
            .setFontSize(5f)
            .setTextAlignment(TextAlignment.LEFT)
            .setPaddingLeft(5f)
        )

        detailTable.addCell(Cell()
            .add(Paragraph(numberFormat.format(total)))
            .setFontSize(5f)
            .setTextAlignment(TextAlignment.RIGHT)
            .setPaddingRight(7f)
        )

        detailTable.addCell(Cell(1, 4)
            .setBorder(Border.NO_BORDER)
        )

        detailTable.addCell(Cell()
            .add(Paragraph("PPN 11%"))
            .setFontSize(5f)
            .setTextAlignment(TextAlignment.LEFT)
            .setPaddingLeft(5f)
        )

        detailTable.addCell(Cell()
            .add(Paragraph(numberFormat.format(ppn)))
            .setFontSize(5f)
            .setTextAlignment(TextAlignment.RIGHT)
            .setPaddingRight(7f)
        )

        detailTable.addCell(Cell(1, 4)
            .setBorder(Border.NO_BORDER)
        )

        detailTable.addCell(Cell()
            .add(Paragraph("Net Price"))
            .setFontSize(5f)
            .setTextAlignment(TextAlignment.LEFT)
            .setPaddingLeft(5f)
        )

        detailTable.addCell(Cell()
            .add(Paragraph(numberFormat.format(grandTotal)))
            .setFontSize(5f)
            .setTextAlignment(TextAlignment.RIGHT)
            .setPaddingRight(7f)
        )

        document.add(detailTable)

        document.close()

        return file
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
                    AlertType.SUCCESS -> {
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