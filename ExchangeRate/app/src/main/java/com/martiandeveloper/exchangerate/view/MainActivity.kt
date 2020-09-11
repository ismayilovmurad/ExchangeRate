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

class MainActivity : AppCompatActivity(), RecyclerViewExchangeAdapter.ItemClickListener,
    RecyclerViewExchangeAdapter.CalculateClickListener {

    private var exchangeList = ArrayList<ExchangeRate>()

    private var layoutManager: LinearLayoutManager? = null
    private var adapter: RecyclerViewExchangeAdapter? = null

    private var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("Error: ${throwable.localizedMessage}")
    }

    private var scope = MainScope()

    private lateinit var vm: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        window.setBackgroundDrawableResource(R.drawable.background)
        setContentView(R.layout.activity_main)
        vm = getViewModel()
        vm.list.value = exchangeList
        getViewModel()
        setRecyclerView()
        setBase()
        setValue()
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
        adapter = RecyclerViewExchangeAdapter(vm.list.value!!, this@MainActivity, this@MainActivity)
        activity_main_mainRV.adapter = adapter
    }

    private fun setBase() {
        if (vm.base.value == null) {
            vm.base.value = "AZN"
        }
    }

    private fun setValue() {
        if (vm.value.value == null) {
            vm.value.value = 1.0
        }
    }

    private fun startUpdates() {
        scope.launch {
            while (true) {
                getData()
                delay(5000)
            }
        }
    }

    private fun getData() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            if (vm.base.value != null) {
                val response = MovieService.getClient().getExchanges(vm.base.value!!)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            val list = ArrayList(it)

                            exchangeList.clear()
                            exchangeList.add(ExchangeRate(vm.base.value!!, null))

                            if (vm.value.value != 1.0) {
                                for (i in 0 until list.size) {
                                    exchangeList.add(
                                        ExchangeRate(
                                            list[i].code,
                                            (list[i].rate!! * vm.value.value!!)
                                        )
                                    )
                                }
                            } else {
                                exchangeList.addAll(list)
                            }

                            vm.list.value = exchangeList

                            changeBackground()
                        }
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
            getData()
        })

        vm.value.observe(this, {
            getData()
        })

        vm.list.observe(this, {
            adapter?.notifyDataSetChanged()
        })
    }

    override fun onItemClick(code: String) {
        vm.base.value = code

        activity_main_mainRV.scrollToPosition(0)
    }

    override fun onCalculateClick(value: Double) {
        vm.value.value = value
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
