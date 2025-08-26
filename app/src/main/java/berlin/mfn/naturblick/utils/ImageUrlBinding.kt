/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.ThumbnailRequest
import berlin.mfn.naturblick.room.FullPortrait
import berlin.mfn.naturblick.room.ImageWithSizes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

@BindingAdapter("imageUrl")
fun imageUrlBinding(imageView: ImageView, imagePath: String?) {
    Glide.with(imageView.context).load(imagePath).into(imageView)
}

@BindingAdapter("imageUrl")
fun imageUrlBinding(imageView: ImageView, uri: Uri) {
    Glide.with(imageView.context).load(uri).into(imageView)
}

@BindingAdapter("imageUrlThumbnailCrop")
fun imageUrlThumbnailBinding(imageView: ImageView, imagePath: String?) {
    if (imagePath != null) {
        Glide.with(imageView.context)
            .load(imagePath)
            .placeholder(R.drawable.placeholder)
            .circleCrop()
            .into(imageView)
    } else {
        Glide.with(imageView.context)
            .load(R.drawable.placeholder)
            .into(imageView)
    }
}

@BindingAdapter("imageUrlThumbnailCrop")
fun glideImageUrlThumbnailBinding(imageView: ImageView, thumbnail: ThumbnailRequest?) {
    if (thumbnail != null) {
        Glide.with(imageView.context)
            .load(thumbnail)
            .placeholder(R.drawable.placeholder)
            .circleCrop()
            .into(imageView)
    } else {
        Glide.with(imageView.context)
            .load(R.drawable.placeholder)
            .into(imageView)
    }
}

@BindingAdapter("imageUrlThumbnail")
fun glideImageUrlThumbnailNoCropBinding(imageView: ImageView, thumbnail: ThumbnailRequest?) {
    if (thumbnail != null) {
        Glide.with(imageView.context)
            .load(thumbnail)
            .placeholder(R.drawable.placeholder)
            .into(imageView)
    } else {
        Glide.with(imageView.context)
            .load(R.drawable.placeholder)
            .into(imageView)
    }
}

@BindingAdapter("imageResourceThumbnailCrop")
fun imageResourceThumbnailBinding(imageView: ImageView, imageResource: Int) {
    Glide.with(imageView.context).load(imageResource)
        .circleCrop()
        .into(imageView)
}

@BindingAdapter("imageUrlHomeBackground")
fun imageUrlHomeBackgroundBinding(imageView: ImageView, imageResource: Drawable) {
    imageView.setImageDrawable(imageResource)
    imageView.scaleType = ImageView.ScaleType.MATRIX
    imageView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                imageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val width = imageView.measuredWidth.toFloat()

                // If width for some reason is 0, then the image is not scaled, but still shown
                // it has been observed, that this sometimes happens on some devices
                if (width > 0) {
                    val imageWidth = imageResource.intrinsicWidth.toFloat()
                    val factor = width / imageWidth
                    imageView.imageMatrix = Matrix().apply {
                        setScale(factor, factor)
                    }
                }
            }
        })
}

@BindingAdapter("imageWithSizesRoundCorners")
fun imageUrlBindingRoundCorners(imageView: ImageView, image: ImageWithSizes?) {
    if (image != null) {
        val params = imageView.layoutParams as ConstraintLayout.LayoutParams
        params.dimensionRatio = image.ratio
        imageView.layoutParams = params
        Glide.with(imageView.context)
            .load(image)
            .transform(CenterInside(), RoundedCorners(8))
            .thumbnail(
                Glide
                    .with(imageView.context)
                    .load(image)
                    .sizeMultiplier(0.25f)
                    .transform(CenterInside(), RoundedCorners(8))
            )
            .into(imageView)
    }
}

@BindingAdapter("imageUrlSpeciesHeader")
fun imageUrlSpeciesHeaderBinding(imageView: ImageView, portrait: FullPortrait?) {
    val image = portrait?.description
    if (image != null) {
        if (image.widerThanFocusPoint(portrait.portrait.landscape)) {
            val params = imageView.layoutParams as ConstraintLayout.LayoutParams
            params.dimensionRatio = image.ratio
            imageView.layoutParams = params
            Glide.with(imageView.context)
                .load(image)
                .transform(CenterInside())
                .thumbnail(Glide.with(imageView.context)
                    .load(image)
                    .sizeMultiplier(0.25f)
                    .transform(CenterInside())
                )
                .into(imageView)
        } else {
            val params = imageView.layoutParams as ConstraintLayout.LayoutParams
            params.dimensionRatio = if (portrait.portrait.landscape) "4:3" else "3:4"
            imageView.layoutParams = params
            Glide.with(imageView.context)
                .load(image)
                .transform(ObjectFit(portrait.portrait.focus))
                .thumbnail( Glide.with(imageView.context)
                    .load(image)
                    .sizeMultiplier(0.25f)
                    .transform(ObjectFit(portrait.portrait.focus))
                )
                .into(imageView)
        }
    }
}
