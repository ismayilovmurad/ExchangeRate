package com.martiandeveloper.exchangerate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.martiandeveloper.exchangerate.R
import com.martiandeveloper.exchangerate.databinding.RecyclerviewRateItem0Binding
import com.martiandeveloper.exchangerate.databinding.RecyclerviewRateItem1Binding
import com.martiandeveloper.exchangerate.model.ExchangeRate
import kotlinx.android.synthetic.main.recyclerview_rate_item_0.view.*

class RecyclerViewExchangeAdapter(
    private val exchangeList: ArrayList<ExchangeRate>,
    private val itemClickListener: ItemClickListener,
    private val calculateClickListener: CalculateClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class RecyclerViewExchangeViewHolder0(private val binding: RecyclerviewRateItem0Binding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(exchangeRate: ExchangeRate, calculateClickListener: CalculateClickListener) {

            binding.code = exchangeRate.code

            binding.executePendingBindings()

            itemView.recyclerview_rate_item_0_calculateIBTN.setOnClickListener {
                if (!binding.value.isNullOrEmpty()) {
                    calculateClickListener.onCalculateClick(
                        binding.value!!.toDouble()
                    )
                }
            }
        }
    }

    class RecyclerViewExchangeViewHolder1(private val binding: RecyclerviewRateItem1Binding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(exchangeRate: ExchangeRate, itemClickListener: ItemClickListener) {

            binding.code = exchangeRate.code
            binding.rate = "${exchangeRate.rate}"

            binding.executePendingBindings()

            itemView.setOnClickListener {
                itemClickListener.onItemClick(exchangeRate.code)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            0
        } else {
            1
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val binding0: RecyclerviewRateItem0Binding = DataBindingUtil
            .inflate(
                LayoutInflater.from(parent.context),
                R.layout.recyclerview_rate_item_0,
                parent,
                false
            )

        val binding1: RecyclerviewRateItem1Binding = DataBindingUtil
            .inflate(
                LayoutInflater.from(parent.context),
                R.layout.recyclerview_rate_item_1,
                parent,
                false
            )

        return when (viewType) {
            0 -> RecyclerViewExchangeViewHolder0(binding0)
            else -> RecyclerViewExchangeViewHolder1(binding1)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                (holder as RecyclerViewExchangeViewHolder0).bind(
                    exchangeList[position], calculateClickListener
                )
            }
            1 -> {
                (holder as RecyclerViewExchangeViewHolder1).bind(
                    exchangeList[position],
                    itemClickListener
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return exchangeList.count()
    }

    interface ItemClickListener {
        fun onItemClick(code: String)
    }

    interface CalculateClickListener {
        fun onCalculateClick(value: Double)
    }
}
