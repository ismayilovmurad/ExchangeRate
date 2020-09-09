package com.martiandeveloper.exchangerate.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val base = MutableLiveData<String>()
}
