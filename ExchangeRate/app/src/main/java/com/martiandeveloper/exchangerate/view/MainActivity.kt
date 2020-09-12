package com.martiandeveloper.exchangerate.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.martiandeveloper.exchangerate.R
import com.martiandeveloper.exchangerate.adapter.RecyclerViewExchangeAdapter
import com.martiandeveloper.exchangerate.databinding.ActivityMainBinding
import com.martiandeveloper.exchangerate.model.ExchangeRate
import com.martiandeveloper.exchangerate.service.MovieService
import com.martiandeveloper.exchangerate.utils.NetworkUtils
import com.martiandeveloper.exchangerate.utils.getColorRes
import com.martiandeveloper.exchangerate.utils.hide
import com.martiandeveloper.exchangerate.utils.show
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

    private lateinit var binding: ActivityMainBinding

    private val updateInterval: Long = 5000
    private val backgroundMillis: Long = 500
    private val animationDuration: Long = 2000

    private var hasConnection = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        window.setBackgroundDrawableResource(R.drawable.background)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        vm = getViewModel()
        vm.exchangeList.value = exchangeList
        getViewModel()
        setRecyclerView()
        setBase()
        setValue()
        startUpdates()
        observe()
        handleNetworkChanges()
    }

    private fun getViewModel(): MainViewModel {
        return ViewModelProvider(this).get(MainViewModel::class.java)
    }

    private fun setRecyclerView() {
        layoutManager = LinearLayoutManager(this)
        activity_main_mainRV.layoutManager = layoutManager
        adapter = RecyclerViewExchangeAdapter(
            vm.exchangeList.value!!,
            this@MainActivity,
            this@MainActivity
        )
        activity_main_mainRV.adapter = adapter
    }

    private fun setBase() {
        if (vm.base.value == null) {
            vm.base.value = getString(R.string.default_exchange)
        }
    }

    private fun setValue() {
        if (vm.value.value == null) {
            vm.value.value = getString(R.string.default_value).toDouble()
        }
    }

    private fun startUpdates() {
        scope.launch {
            while (true) {
                getData()
                delay(updateInterval)
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

                            if (vm.value.value != getString(R.string.default_value).toDouble()) {
                                for (i in 0 until list.size) {
                                    exchangeList.add(
                                        ExchangeRate(
                                            list[i].exchangeCode,
                                            (list[i].exchangeRate!! * vm.value.value!!)
                                        )
                                    )
                                }
                            } else {
                                exchangeList.addAll(list)
                            }

                            vm.exchangeList.value = exchangeList

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
        }, backgroundMillis)
    }

    private fun observe() {
        vm.base.observe(this, {
            getData()
        })

        vm.value.observe(this, {
            getData()
        })

        vm.exchangeList.observe(this, {
            if (!it.isNullOrEmpty()) {
                if (hasConnection) {
                    // We have a internet connection, so we don't have to change the list, we're gonna just notify the adapter.
                    adapter?.notifyDataSetChanged()
                } else {
                    adapter?.updateCountryList(it)
                }
            }
        })
    }

    private fun handleNetworkChanges() {
        NetworkUtils.getNetworkLiveData(applicationContext).observe(this, { isConnected ->
            if (!isConnected) {
                hasConnection = false
                binding.network = getString(R.string.no_connection)
                binding.activityMainNetworkFL.apply {
                    show()
                    setBackgroundColor(getColorRes(R.color.colorThree))
                }

                if (vm.exchangeList.value!!.size == 0) {
                    // The list is empty which means the user opens the app for the first time.
                    // So, we're getting data from the SQLite.
                    vm.getDataFromSQLite()
                } else {
                    // The list is not empty which means internet connection has gone while the user in the app.
                    // So, we're saving the latest data in the SQLite.
                    vm.storeInSQLite(vm.exchangeList.value!!)
                }

            } else {
                hasConnection = true
                binding.network = getString(R.string.back_online)
                binding.activityMainNetworkFL.apply {
                    setBackgroundColor(getColorRes(R.color.colorTwo))

                    animate()
                        .alpha(1f)
                        .setStartDelay(animationDuration)
                        .setDuration(animationDuration)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                hide()
                            }
                        })
                }
            }
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
