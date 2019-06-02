package pl.org.seva.compass.main.ui

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView

class RotationImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    private var lastRotation = 0f

    fun rotate(rotation: Float) {
        // Rotation code credit:
        // https://www.androidcode.ninja/android-compass-code-example/
        startAnimation(RotateAnimation(
                lastRotation,
                -rotation,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f).apply {
            duration = DURATION_MS
            fillAfter = true
        })
        lastRotation = -rotation
    }

    companion object {
        private const val DURATION_MS = 210L
    }

}