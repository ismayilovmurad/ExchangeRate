package com.martiandeveloper.exchangerate.view

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.martiandeveloper.exchangerate.R
import com.martiandeveloper.exchangerate.adapter.RecyclerViewExchangeAdapter
import com.martiandeveloper.exchangerate.model.ExchangeRate
import com.martiandeveloper.exchangerate.service.MovieService
import com.martiandeveloper.exchangerate.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), RecyclerViewExchangeAdapter.Listener {

    private var exchangeList = ArrayList<ExchangeRate>()

    private var layoutManager: LinearLayoutManager? = null
    private var adapter: RecyclerViewExchangeAdapter? = null

    private var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("Error: ${throwable.localizedMessage}")
    }

    private var scope = MainScope()

    // private val tag = "MartianDeveloper"

    private lateinit var vm: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        window.setBackgroundDrawableResource(R.drawable.background)
        setContentView(R.layout.activity_main)
        vm = getViewModel()
        getViewModel()
        setRecyclerView()
        startUpdates()
        observe()
    }

    private fun getViewModel(): MainViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel() as T
            }
        })[MainViewModel::class.java]
    }

    private fun setRecyclerView() {
        layoutManager = LinearLayoutManager(this)
        activity_main_mainRV.layoutManager = layoutManager
        adapter = RecyclerViewExchangeAdapter(exchangeList, this@MainActivity)
        activity_main_mainRV.adapter = adapter
    }

    private fun startUpdates() {
        scope.launch {
            while (true) {
                val base = vm.base.value

                if (base != null) {
                    // Log.d(tag, "Base is not null we got $base")
                    getData(base)
                } else {
                    // Log.d(tag, "Base is null we got AZN")
                    getData("AZN")
                }

                delay(5000)
            }
        }
    }

    private fun getData(base: String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = MovieService.getClient().getExchanges(base)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val list = ArrayList(it)

                        exchangeList.clear()
                        exchangeList.add(ExchangeRate(base, null))
                        exchangeList.addAll(list)

                        adapter?.notifyDataSetChanged()

                        // Log.d(tag, "The data has updated")

                        changeBackground()
                    }
                }
            }
        }
    }

    private fun changeBackground() {
        window.setBackgroundDrawableResource(R.drawable.background2)
        Handler().postDelayed({
            window.setBackgroundDrawableResource(R.drawable.background)
        }, 500)
    }

    private fun observe() {
        vm.base.observe(this, {
            getData(it)
        })
    }

    override fun onItemClick(exchangeRate: ExchangeRate) {
        vm.base.value = exchangeRate.code

        activity_main_mainRV.scrollToPosition(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelGettingData()
    }

    private fun cancelGettingData() {
        job?.cancel()
        scope.cancel()
    }
}
