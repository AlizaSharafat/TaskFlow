package com.example.taskflow;

import android.content.Intent;
import android.os.Build;

import java.io.Serializable;


public final class IntentCompat {

    private IntentCompat() {
        // Utility class, never instantiated.
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    public static <T extends Serializable> T serializableExtra(
            Intent intent, String key, Class<T> clazz) {
        if (intent == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getSerializableExtra(key, clazz);
        }
        Object value = intent.getSerializableExtra(key);
        if (clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
}
