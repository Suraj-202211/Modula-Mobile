package com.modulamobile.viewmodel

import androidx.lifecycle.ViewModel
import com.modulamobile.network.SkinFetcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SkinViewModel @Inject constructor(
    val skinFetcher: SkinFetcher
) : ViewModel()
