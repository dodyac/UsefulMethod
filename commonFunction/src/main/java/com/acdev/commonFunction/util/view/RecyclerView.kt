package com.acdev.commonFunction.util.view

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.acdev.commonFunction.util.Functionx.Companion.getCompatActivity
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import com.smarteist.autoimageslider.SliderViewAdapter

class RecyclerView {
    companion object{

        fun RecyclerView.adapter(adapter: RecyclerView.Adapter<*>?, spanCount: Int) {
            val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(this.context.getCompatActivity(), spanCount)
            this.layoutManager = layoutManager
            this.adapter = adapter
            adapter?.notifyDataSetChanged()
        }

        fun RecyclerView.adapterGrid(adapter: RecyclerView.Adapter<*>?, numOfColumns: Float) {
            val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(this.context.getCompatActivity()!!, this.context.getCompatActivity()!!.numOfColumns(numOfColumns))
            this.layoutManager = layoutManager
            this.adapter = adapter
            adapter?.notifyDataSetChanged()
        }

        fun SliderView.adapter(sliderViewAdapter: SliderViewAdapter<*>){
            this.setSliderAdapter(sliderViewAdapter)
            this.setIndicatorAnimation(IndicatorAnimationType.WORM)
            this.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
            this.startAutoCycle()
        }

        private fun Context.numOfColumns(columnWidthDp: Float): Int {
            val displayMetrics = this.resources.displayMetrics
            val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
            return (screenWidthDp / columnWidthDp + 0.5).toInt()
        }
    }
}