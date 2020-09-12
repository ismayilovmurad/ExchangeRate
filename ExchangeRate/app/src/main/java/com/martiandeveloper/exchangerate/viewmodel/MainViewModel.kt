package com.martiandeveloper.exchangerate.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.martiandeveloper.exchangerate.database.ExchangeDatabase
import com.martiandeveloper.exchangerate.model.ExchangeRate
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : BaseViewModel(application) {
    val base = MutableLiveData<String>()
    val value = MutableLiveData<Double>()
    val exchangeList = MutableLiveData<ArrayList<ExchangeRate>>()

    fun getDataFromSQLite() {
        launch {
            val countries = ExchangeDatabase(getApplication()).exchangeDao().getAll()
            showExchanges(countries)
        }
    }

    fun storeInSQLite(list: List<ExchangeRate>) {
        launch {
            val dao = ExchangeDatabase(getApplication()).exchangeDao()
            dao.deleteAll()
            val listLong = dao.insertAll(*list.toTypedArray())
            var i = 0
            while (i < list.size) {
                list[i].id = listLong[i].toInt()
                i += 1
            }
        }
    }

    private fun showExchanges(list: List<ExchangeRate>) {
        exchangeList.value = ArrayList(list)
    }
}
