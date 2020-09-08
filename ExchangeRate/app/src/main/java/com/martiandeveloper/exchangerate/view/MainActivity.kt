package com.martiandeveloper.exchangerate.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.martiandeveloper.exchangerate.R
import com.martiandeveloper.exchangerate.adapter.RecyclerViewExchangeAdapter
import com.martiandeveloper.exchangerate.model.ExchangeRate
import com.martiandeveloper.exchangerate.service.MovieService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), RecyclerViewExchangeAdapter.Listener {

    private var exchangeList: ArrayList<ExchangeRate>? = null

    private var layoutManager: LinearLayoutManager? = null
    private var adapter: RecyclerViewExchangeAdapter? = null

    private var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("Error: ${throwable.localizedMessage}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        window.setBackgroundDrawableResource(R.drawable.background)
        setContentView(R.layout.activity_main)
        setRecyclerView()
        getData()
    }

    private fun setRecyclerView() {
        layoutManager = LinearLayoutManager(this)
        activity_main_mainRV.layoutManager = layoutManager
    }

    private fun getData() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = MovieService.getClient().getExchanges("AZN")

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        exchangeList = ArrayList(it)
                        exchangeList?.let { it2 ->
                            adapter = RecyclerViewExchangeAdapter(it2, this@MainActivity)
                            activity_main_mainRV.adapter = adapter
                        }
                    }
                }
            }
        }
    }

    override fun onItemClick(cryptoModel: ExchangeRate) {
        Toast.makeText(applicationContext, "Clicked on: ${cryptoModel.code}", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}
