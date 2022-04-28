package com.sunnyweather.android.ui.place

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunnyweather.android.databinding.FragmentPlaceBinding

class PlaceFragment : Fragment() {
    // 使用lazy函数这种懒加载技术来获取PlaceViewModel的实例
    val viewModel by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }

    private lateinit var binding: FragmentPlaceBinding

    private lateinit var adapter: PlaceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * 注意这里和书本的代码不同，书本这部分的逻辑是写在onActivityCreated()中
     * 但是onActivityCreated()被废弃了，通过Attach的lifecycle来监听到Created
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)

        // requireActivity() 返回的是宿主Activity
        requireActivity().lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                // 替代onActivityCreated()，因为onActivityCreated()被废弃了
                if (event.targetState == Lifecycle.State.CREATED) {
                    // 这里就是原来替代onActivityCreated里的逻辑
                    val layoutManager = LinearLayoutManager(activity)
                    binding.recyclerView.layoutManager = layoutManager
                    adapter = PlaceAdapter(this@PlaceFragment, viewModel.placeList)
                    binding.recyclerView.adapter = adapter

                    binding.searchPlaceEdit.addTextChangedListener { editable ->
                        val content = editable.toString()
                        if (content.isNotEmpty()) {
                            viewModel.searchPlaces(content)
                        } else {
                            binding.recyclerView.visibility = View.GONE
                            binding.bgImageView.visibility = View.VISIBLE
                            viewModel.placeList.clear()
                            adapter.notifyDataSetChanged()
                        }
                    }

                    // 对PlaceViewModel中的placeLiveData对象进行观察，当有任何数据变化时，就会回调到传入的Observer接口实现中
                    viewModel.placeLiveData.observe(this@PlaceFragment, Observer { result ->
                        val places = result.getOrNull()
                        if (places != null) {
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.bgImageView.visibility = View.GONE
                            viewModel.placeList.clear()
                            viewModel.placeList.addAll(places)
                            adapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(activity, "未能查询到任何地点", Toast.LENGTH_LONG).show()
                            result.exceptionOrNull()?.printStackTrace()
                        }
                    })

                    // 监听完成后，记得remove
                    lifecycle.removeObserver(this)
                }
            }

        })
    }
}