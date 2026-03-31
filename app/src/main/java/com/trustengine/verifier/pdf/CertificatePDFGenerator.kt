package com.trustengine.verifier.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import com.trustengine.verifier.R
import com.trustengine.verifier.domain.model.CertificateData
import com.trustengine.verifier.domain.model.EKTPData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificatePDFGenerator @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private val TRUSTENGINE_DARK_BLUE = DeviceRgb(10, 37, 64)
        private val TRUSTENGINE_ACCENT = DeviceRgb(0, 212, 170)
        private val TRUSTENGINE_SECONDARY = DeviceRgb(99, 91, 255)
        private val TEXT_PRIMARY = DeviceRgb(10, 37, 64)
        private val TEXT_SECONDARY = DeviceRgb(107, 123, 143)
        private val SUCCESS_GREEN = DeviceRgb(0, 212, 170)
    }
    
    suspend fun generateCertificate(certificateData: CertificateData): Result<File> = 
        withContext(Dispatchers.IO) {
            try {
                val fileName = "TrustEngine_Certificate_${certificateData.certificateId}.pdf"
                val certificatesDir = File(context.getExternalFilesDir(null), "Certificates")
                if (!certificatesDir.exists()) {
                    certificatesDir.mkdirs()
                }
                
                val pdfFile = File(certificatesDir, fileName)
                val pdfWriter = PdfWriter(FileOutputStream(pdfFile))
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument, PageSize.A4)
                
                // Add header with logo
                addHeader(document)
                
                // Add certificate title
                addCertificateTitle(document)
                
                // Add verification status
                addVerificationStatus(document, certificateData.verificationResult.isVerified)
                
                // Add identity information
                addIdentitySection(document, certificateData.ektpData)
                
                // Add photos section
                addPhotosSection(document, certificateData.ktpPhotoUri, certificateData.selfieUri)
                
                // Add verification details
                addVerificationDetails(document, certificateData)
                
                // Add footer
                addFooter(document, certificateData)
                
                document.close()
                
                Result.success(pdfFile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    private fun addHeader(document: Document) {
        // Create header table with logo
        val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 3f)))
            .setWidth(UnitValue.createPercentValue(100f))
            .setMarginBottom(20f)
        
        // Add logo
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.trustengine_logo)
            val logoBytes = bitmapToBytes(logoBitmap)
            val logoImage = Image(ImageDataFactory.create(logoBytes))
                .setWidth(80f)
                .setHeight(40f)
            
            val logoCell = Cell()
                .add(logoImage)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
            
            headerTable.addCell(logoCell)
        } catch (e: Exception) {
            headerTable.addCell(Cell().setBorder(Border.NO_BORDER))
        }
        
        // Add title text
        val titleCell = Cell()
            .add(
                Paragraph("TRUSTENGINE")
                    .setFontSize(24f)
                    .setBold()
                    .setFontColor(TRUSTENGINE_DARK_BLUE)
            )
            .add(
                Paragraph("e-Certificate Verifier")
                    .setFontSize(14f)
                    .setFontColor(TEXT_SECONDARY)
            )
            .setBorder(Border.NO_BORDER)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
        
        headerTable.addCell(titleCell)
        document.add(headerTable)
        
        // Add separator line
        val line = LineSeparator(SolidLine(2f))
        line.setStrokeColor(TRUSTENGINE_ACCENT)
        document.add(line)
        document.add(Paragraph("").setMarginBottom(20f))
    }
    
    private fun addCertificateTitle(document: Document) {
        document.add(
            Paragraph("IDENTITY VERIFICATION CERTIFICATE")
                .setFontSize(20f)
                .setBold()
                .setFontColor(TRUSTENGINE_DARK_BLUE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10f)
        )
        
        document.add(
            Paragraph("This certificate confirms that the identity verification process has been completed successfully.")
                .setFontSize(11f)
                .setFontColor(TEXT_SECONDARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20f)
        )
    }
    
    private fun addVerificationStatus(document: Document, isVerified: Boolean) {
        val statusColor = if (isVerified) SUCCESS_GREEN else ColorConstants.RED
        val statusText = if (isVerified) "VERIFIED" else "NOT VERIFIED"
        
        val statusTable = Table(1)
            .setWidth(UnitValue.createPercentValue(60f))
            .setHorizontalAlignment(HorizontalAlignment.CENTER)
            .setMarginBottom(20f)
        
        val statusCell = Cell()
            .add(
                Paragraph(statusText)
                    .setFontSize(18f)
                    .setBold()
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER)
            )
            .setBackgroundColor(statusColor)
            .setBorder(Border.NO_BORDER)
            .setPadding(10f)
            .setBorderRadius(BorderRadius(8f))
        
        statusTable.addCell(statusCell)
        document.add(statusTable)
    }
    
    private fun addIdentitySection(document: Document, ektpData: EKTPData) {
        document.add(
            Paragraph("Identity Information")
                .setFontSize(14f)
                .setBold()
                .setFontColor(TRUSTENGINE_DARK_BLUE)
                .setMarginBottom(10f)
        )
        
        val infoTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 2f)))
            .setWidth(UnitValue.createPercentValue(100f))
            .setMarginBottom(20f)
        
        addInfoRow(infoTable, "NIK", ektpData.nik)
        addInfoRow(infoTable, "Full Name", ektpData.name)
        addInfoRow(infoTable, "Place of Birth", ektpData.placeOfBirth)
        addInfoRow(infoTable, "Date of Birth", ektpData.dateOfBirth)
        addInfoRow(infoTable, "Gender", ektpData.gender)
        addInfoRow(infoTable, "Address", ektpData.address)
        
        document.add(infoTable)
    }
    
    private fun addInfoRow(table: Table, label: String, value: String) {
        val labelCell = Cell()
            .add(
                Paragraph(label)
                    .setFontSize(10f)
                    .setFontColor(TEXT_SECONDARY)
            )
            .setBorder(Border.NO_BORDER)
            .setPadding(5f)
        
        val valueCell = Cell()
            .add(
                Paragraph(value)
                    .setFontSize(11f)
                    .setFontColor(TEXT_PRIMARY)
                    .setBold()
            )
            .setBorder(Border.NO_BORDER)
            .setPadding(5f)
        
        table.addCell(labelCell)
        table.addCell(valueCell)
    }
    
    private fun addPhotosSection(document: Document, ktpUri: Uri?, selfieUri: Uri?) {
        document.add(
            Paragraph("Verification Photos")
                .setFontSize(14f)
                .setBold()
                .setFontColor(TRUSTENGINE_DARK_BLUE)
                .setMarginBottom(10f)
        )
        
        val photoTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f)))
            .setWidth(UnitValue.createPercentValue(100f))
            .setMarginBottom(20f)
        
        // KTP Photo
        val ktpCell = Cell()
            .add(Paragraph("KTP Photo").setFontSize(10f).setFontColor(TEXT_SECONDARY))
            .setBorder(Border.NO_BORDER)
            .setTextAlignment(TextAlignment.CENTER)
        
        ktpUri?.let { uri ->
            try {
                val bitmap = uriToBitmap(uri)
                val bytes = bitmapToBytes(bitmap)
                val image = Image(ImageDataFactory.create(bytes))
                    .setWidth(150f)
                    .setHeight(100f)
                ktpCell.add(image)
            } catch (e: Exception) {
                ktpCell.add(Paragraph("[Image not available]"))
            }
        }
        
        // Selfie Photo
        val selfieCell = Cell()
            .add(Paragraph("Selfie Photo").setFontSize(10f).setFontColor(TEXT_SECONDARY))
            .setBorder(Border.NO_BORDER)
            .setTextAlignment(TextAlignment.CENTER)
        
        selfieUri?.let { uri ->
            try {
                val bitmap = uriToBitmap(uri)
                val bytes = bitmapToBytes(bitmap)
                val image = Image(ImageDataFactory.create(bytes))
                    .setWidth(100f)
                    .setHeight(100f)
                selfieCell.add(image)
            } catch (e: Exception) {
                selfieCell.add(Paragraph("[Image not available]"))
            }
        }
        
        photoTable.addCell(ktpCell)
        photoTable.addCell(selfieCell)
        document.add(photoTable)
    }
    
    private fun addVerificationDetails(document: Document, certificateData: CertificateData) {
        document.add(
            Paragraph("Verification Details")
                .setFontSize(14f)
                .setBold()
                .setFontColor(TRUSTENGINE_DARK_BLUE)
                .setMarginBottom(10f)
        )
        
        val detailsTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 2f)))
            .setWidth(UnitValue.createPercentValue(100f))
            .setMarginBottom(20f)
        
        val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault())
        
        addInfoRow(detailsTable, "Certificate ID", certificateData.certificateId)
        addInfoRow(detailsTable, "Verification ID", certificateData.verificationResult.verificationId)
        addInfoRow(detailsTable, "Generated Date", dateFormat.format(Date(certificateData.generatedAt)))
        addInfoRow(detailsTable, "Confidence Score", "${(certificateData.verificationResult.confidenceScore * 100).toInt()}%")
        
        document.add(detailsTable)
    }
    
    private fun addFooter(document: Document, certificateData: CertificateData) {
        // Add separator line
        val line = LineSeparator(SolidLine(1f))
        line.setStrokeColor(TEXT_SECONDARY)
        document.add(line)
        
        document.add(
            Paragraph("Verified by TrustEngine")
                .setFontSize(10f)
                .setFontColor(TEXT_SECONDARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10f)
        )
        
        document.add(
            Paragraph("This certificate is generated electronically and is valid without signature.")
                .setFontSize(9f)
                .setFontColor(TEXT_SECONDARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5f)
        )
        
        document.add(
            Paragraph("Certificate ID: ${certificateData.certificateId}")
                .setFontSize(8f)
                .setFontColor(TEXT_SECONDARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10f)
        )
    }
    
    private fun uriToBitmap(uri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }
    
    private fun bitmapToBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}

// Extension for border radius (simplified)
private fun Cell.setBorderRadius(radius: BorderRadius): Cell {
    // iText 7 doesn't have direct border radius support
    // This is a placeholder for custom implementation if needed
    return this
}

data class BorderRadius(val radius: Float)