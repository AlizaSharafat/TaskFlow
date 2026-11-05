package com.example.taskflow;

import android.content.Intent;
import android.os.Build;

import java.io.Serializable;

/**
 * getSerializableExtra(String) was deprecated in API 33 in favour of a version
 * that takes the expected class. This picks the right one for the device, so
 * the code compiles without warnings and runs from API 24 upwards.
 *
 * In the Kotlin version this was an extension function on Intent. Java has no
 * extension functions, so it becomes a static helper instead.
 */
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
