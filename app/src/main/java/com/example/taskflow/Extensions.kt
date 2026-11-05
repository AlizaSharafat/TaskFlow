package com.example.taskflow

import android.content.Intent
import android.os.Build
import java.io.Serializable


@Suppress("DEPRECATION", "UNCHECKED_CAST")
fun <T : Serializable> Intent.serializableExtra(key: String, clazz: Class<T>): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(key, clazz)
    } else {
        getSerializableExtra(key) as? T
    }
