package com.example.cti.musicfence.activity.utils

import android.Manifest
import android.app.Activity
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.example.cti.musicfence.musicPlayer.model.Music

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun getPermissionsAboveApi33(): Array<String> {
    return arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO
    )
}

fun getPermissionsUnderApi33(): Array<String> {
    return arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}

fun Activity.getAllMusics(): ArrayList<Music> {
    val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.DURATION
    )
    val cursor = contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,
        null,
        null
    )
    val songs = ArrayList<Music>()
    if (cursor != null) while (cursor.moveToNext()) {
        val music = Music(
            cursor.getInt(0), cursor.getString(1), cursor.getString(2),
            cursor.getString(3), cursor.getString(4), cursor.getInt(5)
        )
        songs.add(music)
    }
    cursor?.close()
    return songs
}