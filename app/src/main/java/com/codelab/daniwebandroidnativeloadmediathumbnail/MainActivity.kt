package com.codelab.daniwebandroidnativeloadmediathumbnail

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button_loadMusic)
        val imageView = findViewById<ImageView>(R.id.imageView_displayArt)

        val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getAlbumArtAfterQ()
            } else {
                getAlbumArtBeforeQ()
            }

            imageView.setImageBitmap(thumbnail)
        }

        button.setOnClickListener {
            permissionResultLauncher.launch(READ_EXTERNAL_STORAGE)
        }
    }

    private fun getAlbumArtBeforeQ(): Bitmap? {
        val collection = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

        //The columns that you want. We need the ID to build the content uri
        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM_ART
        )

        //filter by title here
        val selection = "${MediaStore.Audio.Albums._ID} = ?"

        val albumId = getAlbumId()

        //We already know the song title in advance
        val selectionArgs = arrayOf(
            "$albumId"
        )
        val sortOrder = null //sorting order is not needed

        var thumbnail: Bitmap? = null

        applicationContext.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val albumArtColIndex = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)

            while (cursor.moveToNext()) {
                val albumArtPath = cursor.getString(albumArtColIndex)

                thumbnail = BitmapFactory.decodeFile(albumArtPath)
                if (thumbnail === null){
                    TODO("Load alternative thumbnail here")
                }
            }
        }

        return thumbnail
    }

    private fun getAlbumId(): Long? {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        //The columns that you want. We need the ID to build the content uri
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM_ID
        )

        //filter by title here
        val selection = "${MediaStore.Audio.Media.TITLE} = ?"

        //We already know the song title in advance
        val selectionArgs = arrayOf(
            "I Move On (Sintel's Song)"
        )

        val sortOrder = null //sorting order is not needed

        var id: Long? = null

        applicationContext.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val albumIdColIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                id = cursor.getLong(albumIdColIndex)
            }
        }

        return id
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getAlbumArtAfterQ(): Bitmap? {
        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

        //The columns that you want. We need the ID to build the content uri
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
        )

        //filter by title here
        val selection = "${MediaStore.Audio.Media.TITLE} = ?"

        //We already know the song title in advance
        val selectionArgs = arrayOf(
            "I Move On (Sintel's Song)"
        )
        val sortOrder = null //sorting order is not needed

        var thumbnail: Bitmap? = null

        applicationContext.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColIndex)

                //Builds the content uri here
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                try {
                    thumbnail = contentResolver.loadThumbnail(
                        uri,
                        Size(300, 300),
                        null
                    )
                } catch (e: IOException) {
                    TODO("Load alternative thumbnail here")
                }
            }
        }
        return thumbnail
    }
}