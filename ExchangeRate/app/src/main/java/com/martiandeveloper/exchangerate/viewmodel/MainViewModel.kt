package com.martiandeveloper.exchangerate.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.martiandeveloper.exchangerate.model.ExchangeRate

class MainViewModel : ViewModel() {
    val base = MutableLiveData<String>()
    val value = MutableLiveData<Double>()
    val list = MutableLiveData<ArrayList<ExchangeRate>>()
}
