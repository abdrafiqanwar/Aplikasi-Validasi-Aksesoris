package com.example.validasiaksesoris.ui.invoice

import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.validasiaksesoris.R
import com.example.validasiaksesoris.data.model.invoice.FrameNumber
import com.example.validasiaksesoris.data.model.invoice.InvoiceResponse
import com.example.validasiaksesoris.databinding.ActivityInvoiceBinding
import com.example.validasiaksesoris.di.Result
import com.example.validasiaksesoris.ui.ViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInvoiceBinding
    private lateinit var adapter: InvoiceAdapter
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
                    is Result.Loading -> { showLoading(true) }
                    is Result.Success -> {
                        showLoading(false)
                        data.clear()
                        data.addAll(it.data.filter { !it.frameNumber.isNullOrEmpty() })
                        adapter = InvoiceAdapter(data) {
                            val selectedCount = data.count{ it.isSelected }

                            if (selectedCount > 20) {
                                data[it].isSelected = false
                                adapter.notifyItemChanged(it)

                                Toast.makeText(this, "Maksimal 20 nomor rangka", Toast.LENGTH_SHORT).show()
                                return@InvoiceAdapter
                            }

                            binding.btnSubmit.isEnabled = selectedCount > 0
                        }
                        binding.rvFrameNumber.adapter = adapter
                    }
                    is Result.Error -> { showLoading(false) }
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            val frameNumberSelected = data
                .filter { it.isSelected }
                .map { it.frameNumber }

            val frameNumber = frameNumberSelected.joinToString(",")

            viewModel.getInvoice(frameNumber).observe(this) {
                if (it != null) {
                    when (it) {
                        is Result.Loading -> { showLoading(true )}
                        is Result.Success -> {
                            showLoading(false)

                            val data = it.data

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
        binding.pb.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun createPdf(invoices: List<InvoiceResponse>) {
        val sdf = SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH)
        val formattedDate = sdf.format(Date())

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // width, height, page number
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val options = BitmapFactory.Options().apply {
            inSampleSize = 4
        }
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo, options)
        val src = Rect(0, 0, bitmap.width, bitmap.height)
        val dst = Rect(100, 40, 185, 125)
        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }
        canvas.drawBitmap(bitmap, src, dst, paint)

        val textAddress = Paint().apply {
            textSize = 8f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText("Citra Tower, Lv. 20. Unit A", 495f, 70f, textAddress)
        canvas.drawText("Kemayoran Jakarta 10630", 495f, 80f, textAddress)
        canvas.drawText("Indonesia", 495f, 90f, textAddress)
        canvas.drawText("+6221-39719888", 495f, 100f, textAddress)

        val linePaint = Paint().apply {
            color = ContextCompat.getColor(this@InvoiceActivity, R.color.line)
            strokeWidth = 4f
        }

        canvas.drawLine(190f, 110f, 495f, 110f, linePaint)

        val invoice = Paint().apply {
            textSize = 12f
            textAlign = Paint.Align.CENTER
            isUnderlineText = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText("INVOICE", 300f, 140f, invoice)

        val invoiceNum = Paint().apply {
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText("INV.", 280f, 155f, invoiceNum)

        val normalText = Paint().apply {
            textSize = 10f
        }

        val boldText = Paint().apply {
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText("Kepada Yth.", 100f, 180f, normalText)
        canvas.drawText("PT.HADJI KALLA", 100f, 195f, boldText)
        canvas.drawText("Wisma Kalla Lt.12", 100f, 210f, normalText)
        canvas.drawText("Makassar", 100f, 225f, normalText)

        canvas.drawText("Tanggal", 380f, 180f, normalText)
        canvas.drawText(":", 430f, 180f, normalText)
        canvas.drawText(formattedDate, 440f, 180f, normalText)
        canvas.drawText("Mata Uang", 380f, 195f, normalText)
        canvas.drawText(":", 430f, 195f, normalText)
        canvas.drawText("IDR", 440f, 195f, normalText)

        val tablePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val headerTable = Paint().apply {
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val colNo = 100f
        val colFrame = 130f
        val colItem = 250f
        val colPrice = 420f
        var y = 260f

        canvas.drawText("No.", colNo, y, headerTable)
        canvas.drawText("Nomor Rangka", colFrame, y, headerTable)
        canvas.drawText("Uraian", colItem, y, headerTable)
        canvas.drawText("Harga Satuan", colPrice, y, headerTable)

        y += 10f
        canvas.drawLine(100f, y, 495f, y, tablePaint)

        y += 15f

        val tableText = Paint().apply {
            textSize = 8f
        }

        var no = 1
        var grandTotal = 0

        invoices.forEach { frame ->
            var totalFrame = 0
            var firstItem = true

            frame.accessories.forEach { item ->
                totalFrame += item.price

                canvas.drawText(if (firstItem) no.toString() else "", colNo, y, tableText)
                canvas.drawText(if (firstItem) frame.frameNumber else "", colFrame, y, tableText)

                canvas.drawText(item.name, colItem, y, tableText)
                canvas.drawText(item.price.toString(), colPrice, y, tableText)

                y += 15f
                firstItem = false
            }

            grandTotal += totalFrame

            val subtotal = Paint().apply {
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            }
            canvas.drawText("Subtotal", colItem, y, subtotal)
            canvas.drawText(totalFrame.toString(), colPrice, y, subtotal)

            y += 20f
            no++
        }

        y -= 10f

        canvas.drawLine(100f, y, 495f, y, tablePaint)

        y += 15f

        val totalPaint = Paint().apply {
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText("Total", colItem, y, totalPaint)
        canvas.drawText(grandTotal.toString(), colPrice, y, totalPaint)

        pdfDocument.finishPage(page)
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "invoice.pdf"
        )
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            // Show success message
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error
        }
        pdfDocument.close()
    }
}