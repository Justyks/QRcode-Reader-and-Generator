package com.example.qrscanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Size
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

@ExperimentalGetImage class MainActivity : AppCompatActivity() {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))

        val generateQR: Button = findViewById(R.id.generateQRBtn)
        generateQR.setOnClickListener(){
            val qrCodeInput: EditText = findViewById(R.id.qrcodeInput)
            val text = qrCodeInput.text.toString()
            val encoder = BarcodeEncoder()
            val bitmap = encoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 300, 300)
            val qrCode: ImageView = findViewById(R.id.QrCode)
            qrCode.setImageBitmap(bitmap)
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder()
            .build()
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val previewView: PreviewView = findViewById(R.id.previewView);
        preview.setSurfaceProvider(previewView.createSurfaceProvider(null))
        val analyzer = QRAnalyzer(supportFragmentManager)
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer)

        cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            imageAnalysis,
            preview
        )
    }
}