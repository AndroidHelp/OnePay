package com.onecard.onepay.gpay

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onecard.onepay.R
import kotlinx.android.synthetic.main.layout_recent_transaction_item.view.*

class RecentTransAdapter : RecyclerView.Adapter<RecentTransAdapter.ItemViewHolder> {

    private val context: Context
    private val dataList: ArrayList<RecentTransactionItem>

    constructor(context: Context, dataList: ArrayList<RecentTransactionItem>) : super() {
        this.context = context
        this.dataList = dataList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_recent_transaction_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val dataItem = dataList[position]
        holder.bindItem(dataItem, position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItem(dataItem: RecentTransactionItem, position: Int) {
            itemView.apply {
                if (position==dataList.size-1) viewDivider.visibility = View.GONE
                else viewDivider.visibility = View.VISIBLE
                if (dataItem.status) {
                    tvAmount.text = context.getString(R.string.formatted_amount_add, dataItem.amount)
                    tvTransactionTitle.text = context.getString(R.string.message_success_transaction)
                    ivTransStatus.setImageResource(R.drawable.ic_success)
                } else {
                    tvAmount.text = context.getString(R.string.formatted_amount_failed, dataItem.amount)
                    tvTransactionTitle.text = context.getString(R.string.message_failed_transaction)
                    ivTransStatus.setImageResource(R.drawable.ic_failed)
                }
            }
        }
    }
}