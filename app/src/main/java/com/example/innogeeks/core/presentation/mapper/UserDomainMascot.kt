package com.example.innogeeks.core.presentation.mapper

import androidx.annotation.DrawableRes
import com.example.innogeeks.core.domain.model.UserDomain
import edu.kiet.innogeeks.R

@DrawableRes
fun UserDomain.mascotRes(): Int = when (this) {
    UserDomain.ANDROID -> R.drawable.ic_domain_appd
    UserDomain.WEB -> R.drawable.ic_domain_webd
    UserDomain.ML -> R.drawable.ic_domain_ml
    UserDomain.IOT -> R.drawable.ic_domain_iot
    UserDomain.AR_VR -> R.drawable.ic_domain_arvr
}
