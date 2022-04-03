package uz.context.firebasecloud

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import uz.context.firebasecloud.adapter.ImageAdapter
import uz.context.firebasecloud.databinding.ActivityMainBinding
import uz.context.firebasecloud.util.toast

private const val REQUEST_CODE = 0

class MainActivity : AppCompatActivity() {

    var currentFile: Uri? = null
    val imageRef = Firebase.storage.reference

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

    }

    private fun initViews() {
        listFiles()
        binding.ivImage.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it,REQUEST_CODE)
            }
        }
        binding.btnUploadImage.setOnClickListener {
            uploadImageToStorage("myImage")
        }
        binding.btnDownloadImage.setOnClickListener {
            downloadImage("myImage")
        }
        binding.btnDeleteImage.setOnClickListener {
            deleteImage("myImage")
        }
    }

    private fun listFiles() = CoroutineScope(Dispatchers.IO).launch {
        try {
           val images = imageRef.child("images/").listAll().await()
            val imageUrls = mutableListOf<String>()
            for (image in images.items) {
                val url = image.downloadUrl.await()
                imageUrls.add(url.toString())
            }
            withContext(Dispatchers.Main) {
                val imageAdapter = ImageAdapter(imageUrls)
                binding.recyclerView.apply {
                    adapter = imageAdapter
                    layoutManager = LinearLayoutManager(this@MainActivity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                toast(e.message.toString())
            }
        }
    }

    private fun deleteImage(fileName: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            imageRef.child("images/$fileName").delete().await()
            withContext(Dispatchers.Main) {
                toast("Successfully deleted image")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                toast(e.message.toString())
            }
        }
    }

    private fun downloadImage(fileName: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val maxDownloadSize = 5L * 1024 * 1024
            val bytes = imageRef.child("images/$fileName").getBytes(maxDownloadSize).await()
            val bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.size)
            withContext(Dispatchers.Main) {
                binding.ivImage.setImageBitmap(bmp)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                toast(e.message.toString())
            }
        }
    }

    private fun uploadImageToStorage(fileName: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            currentFile?.let {
                imageRef.child("images/$fileName").putFile(it).await()
                withContext(Dispatchers.Main) {
                    toast("Successfully uploaded image")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                toast(e.message.toString())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            data?.data?.let {
                currentFile = it
                binding.ivImage.setImageURI(it)
            }
        }
    }
}