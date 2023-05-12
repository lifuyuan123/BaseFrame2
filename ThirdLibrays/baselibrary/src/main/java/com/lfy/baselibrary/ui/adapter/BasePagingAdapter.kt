package com.lfy.baselibrary.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.lfy.baselibrary.ActivityManager
import com.lfy.baselibrary.Tags
import com.lfy.baselibrary.singleClick
import me.jessyan.autosize.AutoSize

/**
 * @Author admin
 * @Date 2021/8/5-14:12
 * @describe:  pagingadapter基类  封装点击事件
 */
abstract class BasePagingDataAdapter<T : Any> :
    PagingDataAdapter<T, RecyclerView.ViewHolder> {
    constructor() : super(itemCallback())

    constructor(differCallback: DiffUtil.ItemCallback<T>) : super(differCallback)

    abstract fun getLayout(): Int


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        AutoSize.autoConvertDensity(ActivityManager.instance.mCurrentActivity, Tags.with, true)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            getLayout(), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? BasePagingDataAdapter<*>.ViewHolder)?.onBindViewHolder(position)
    }

    companion object {

        fun <T> itemCallback(): DiffUtil.ItemCallback<T> {
            return object : DiffUtil.ItemCallback<T>() {
                override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
                    return oldItem == newItem
                }

                @SuppressLint("DiffUtilEquals")
                override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
                    return newItem == oldItem
                }
            }
        }
    }


    inner class ViewHolder internal constructor(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener,
        View.OnLongClickListener {
        private val itemHelper = ItemHelper(this)
        private val mPosition get() = bindingAdapterPosition

        init {
            if (!childIds.isNullOrEmpty()) {
                itemHelper.addChildClickOfId(childIds)
            }
            if (!childLongIds.isNullOrEmpty()) {
                itemHelper.addChildLongClickOfId(childLongIds)
            }
            itemHelper.setOnItemChildClickListener(mOnItemChildClickListenerProxy)
            itemHelper.setOnItemChildLongClickListener(mOnItemChildLongClickListenerProxy)
            itemHelper.setRVAdapter(this@BasePagingDataAdapter)
            itemView.singleClick(this)
            itemView.setOnLongClickListener(this)
        }

        fun onBindViewHolder(position: Int) {
            getItem(position)?.let { bindData(binding, it, position) }
        }

        override fun onClick(v: View) {
            if (::mOnItemClickListener.isInitialized) {
                mOnItemClickListener(this@BasePagingDataAdapter, v, mPosition)
            }
        }

        override fun onLongClick(v: View): Boolean {
            if (::mOnItemLongClickListener.isInitialized) {
                mOnItemLongClickListener(this@BasePagingDataAdapter, v, mPosition)
                return true
            }
            return false
        }
    }

    /**
     * 给条目绑定数据
     * @param helper  条目帮助类
     * @param data    对应数据
     */
    protected abstract fun bindData(binding: ViewDataBinding, bean: T, postion: Int)


    class ItemHelper(private val viewHolder: BasePagingDataAdapter<*>.ViewHolder) :
        View.OnClickListener, View.OnLongClickListener {

        private val position get() = viewHolder.bindingAdapterPosition
        private lateinit var adapter: BasePagingDataAdapter<out Any>

        private lateinit var mOnItemChildClickListener:
                    (adapter: BasePagingDataAdapter<out Any>, v: View, position: Int) -> Unit

        private lateinit var mOnItemChildLongClickListener:
                    (adapter: BasePagingDataAdapter<out Any>, v: View, position: Int) -> Unit

        fun addChildClickOfId(ids: MutableList<Int>) {
            for (id in ids) {
                viewHolder.itemView.findViewById<View>(id)?.singleClick(this)
            }
        }

        fun addChildLongClickOfId(ids: MutableList<Int>) {
            for (id in ids) {
                viewHolder.itemView.findViewById<View>(id)?.setOnLongClickListener(this)
            }
        }

        fun setRVAdapter(PagedListAdapter: BasePagingDataAdapter<out Any>) {
            adapter = PagedListAdapter
        }

        fun setOnItemChildClickListener(
            onItemChildClickListener:
                (adapter: BasePagingDataAdapter<out Any>, v: View, position: Int) -> Unit
        ) {
            mOnItemChildClickListener = onItemChildClickListener
        }

        fun setOnItemChildLongClickListener(
            onItemChildLongClickListener:
                (adapter: BasePagingDataAdapter<out Any>, v: View, position: Int) -> Unit
        ) {
            mOnItemChildLongClickListener = onItemChildLongClickListener
        }

        override fun onClick(v: View) {
            if (::mOnItemChildClickListener.isInitialized) {
                mOnItemChildClickListener(adapter, v, position)
            }
        }

        override fun onLongClick(v: View): Boolean {
            if (::mOnItemChildLongClickListener.isInitialized) {
                mOnItemChildLongClickListener(adapter, v, position)
                return true
            }
            return false
        }

    }

    //通过外部添加控件id，实现内部itemChild点击事件
    private val childIds by lazy { mutableListOf<Int>() }
    private val childLongIds by lazy { mutableListOf<Int>() }

    fun addChildClickOfId(vararg id: Int) {
        childIds.addAll(id.toMutableList())
    }

    fun addChildLongClickOfId(vararg id: Int) {
        childLongIds.addAll(id.toMutableList())
    }


    //adapter暴露给外部各种点击事件
    private lateinit var mOnItemClickListener:
                (adapter: BasePagingDataAdapter<out Any>, v: View, position: Int) -> Unit
    private lateinit var mOnItemLongClickListener:
                (adapter: BasePagingDataAdapter<out Any>, v: View, position: Int) -> Unit
    private lateinit var mOnItemChildClickListener:
                (adapter: BasePagingDataAdapter<out Any>, v: View, position: Int) -> Unit
    private lateinit var mOnItemChildLongClickListener:
                (adapter: BasePagingDataAdapter<out Any>, v: View, position: Int) -> Unit

    private val mOnItemChildClickListenerProxy:
                (adapter: BasePagingDataAdapter<out Any>, v: View, position: Int) -> Unit =
        { adapter, v, position ->
            if (::mOnItemChildClickListener.isInitialized) {
                mOnItemChildClickListener(adapter, v, position)
            }
        }
    private val mOnItemChildLongClickListenerProxy:
                (adapter: BasePagingDataAdapter<out Any>, v: View, position: Int) -> Unit =
        { adapter, v, position ->
            if (::mOnItemChildLongClickListener.isInitialized) {
                mOnItemChildLongClickListener(adapter, v, position)
            }
        }

    fun setOnItemClickListener(
        onItemClickListener:
            (adapter: BasePagingDataAdapter<out Any>, v: View, position: Int) -> Unit
    ) {
        mOnItemClickListener = onItemClickListener
    }

    fun setOnItemLongClickListener(
        onItemLongClickListener:
            (adapter: BasePagingDataAdapter<out Any>, v: View, position: Int) -> Unit
    ) {
        mOnItemLongClickListener = onItemLongClickListener
    }

    fun setOnItemChildClickListener(
        OnItemChildClickListenerProxy:
            (adapter: BasePagingDataAdapter<out Any>, v: View, position: Int) -> Unit
    ) {
        mOnItemChildClickListener = OnItemChildClickListenerProxy
    }

    fun setOnItemChildLongClickListener(
        mOnItemChildLongClickListenerProxy:
            (adapter: BasePagingDataAdapter<out Any>, v: View, position: Int) -> Unit
    ) {
        mOnItemChildLongClickListener = mOnItemChildLongClickListenerProxy
    }


    fun getData(position: Int): T? {
        return getItem(position)
    }
}