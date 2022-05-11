package com.wflin.indicator

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.ets100.secondary.R
import com.ets100.secondary.utils.ext.dp
import com.ets100.secondary.utils.ext.setHeight
import com.ets100.secondary.utils.ext.setWidth

/**
 * ViewPager指示器
 * @author wflin
 * @date 2022/4/26
 */
@SuppressLint("CustomViewStyleable")
class Indicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    interface Pager {
        val isEmpty: Boolean
        val isNotEmpty: Boolean
        val currentItem: Int
        val count: Int
        fun setCurrentItem(index: Int, smoothScroll: Boolean)
        fun removeOnPageChangeListener()
        fun addOnPageChangeListener(helper: PageChangeHelper)
    }

    private val dotList = mutableListOf<ImageView>()  // 指示器集合

    private val defaultColor = Color.parseColor("#DBE1E2")
    private val defaultSelectedColor = Color.parseColor("#00A0FA")
    private var dotColor = defaultColor // 指示器未选中颜色
    private var selectedDotColor = defaultSelectedColor // 指示器选中颜色
    private var dotWidth = 9.dp // 指示器宽度
    private var dotHeight = 6.dp // 指示器高度
    private var dotSpacing = 4.dp // 指示器间距
    private var dotRadius = 3.dp // 指示器圆角
    private var dotsWidthFactor = 0f // 变化因子
    private var progressMode: Boolean = false // 进度条模式

    private val argbEvaluator = ArgbEvaluator()
    private var currentIndex = 0 // 当前指示器角标

    private var pager: Pager? = null

    init {
        attrs?.let {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.indicator)

            dotColor = typeArray.getColor(R.styleable.indicator_dotColor, defaultColor)
            selectedDotColor =
                typeArray.getColor(R.styleable.indicator_selectedColor, defaultSelectedColor)
            dotWidth = typeArray.getDimension(R.styleable.indicator_dotWidth, 9.dp)
            dotHeight = typeArray.getDimension(R.styleable.indicator_dotHeight, 6.dp)
            dotSpacing = typeArray.getDimension(R.styleable.indicator_dotSpacing, 4.dp)
            dotRadius = typeArray.getDimension(R.styleable.indicator_dotRadius, 3.dp)
            dotsWidthFactor = typeArray.getFloat(R.styleable.indicator_dotWidthFactor, 0f)
            progressMode = typeArray.getBoolean(R.styleable.indicator_progressMode, false)

            typeArray.recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        refreshDots()
    }


    fun setViewPager(vp: ViewPager) {
        // 检查adapter
        val adapter: PagerAdapter = vp.adapter
            ?: throw IllegalStateException("You should set an ViewPager adapter first!")
        // 监听数据变化
        adapter.registerDataSetObserver(object : DataSetObserver() {
            override fun onChanged() {
                super.onChanged()
                refreshDots()
            }
        })
        // 初始化pager
        pager = object : Pager {
            var listener: ViewPager.OnPageChangeListener? = null

            override val isEmpty: Boolean
                get() = vp.adapter!!.count == 0
            override val isNotEmpty: Boolean
                get() = vp.adapter!!.count > 0
            override val currentItem: Int
                get() = vp.currentItem
            override val count: Int
                get() = vp.adapter!!.count

            override fun setCurrentItem(index: Int, smoothScroll: Boolean) {
                vp.setCurrentItem(index, smoothScroll)
            }

            override fun removeOnPageChangeListener() {
                listener?.let { vp.removeOnPageChangeListener(it) }
            }

            override fun addOnPageChangeListener(helper: PageChangeHelper) {
                listener = object : ViewPager.OnPageChangeListener {
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                        helper.onPageScrolled(position, positionOffset)
                    }

                    override fun onPageSelected(position: Int) {}

                    override fun onPageScrollStateChanged(state: Int) {}

                }
                vp.addOnPageChangeListener(listener!!)
            }

        }
        // 刷新
        refreshDots()
    }

    fun setViewPager2(vp: ViewPager2) {
        // 检查adapter
        val adapter = vp.adapter
            ?: throw IllegalStateException("You should set an ViewPager2 adapter first!")
        // 监听数据变化
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                refreshDots()
            }
        })
        // 初始化pager
        pager = object : Pager {
            var listener: ViewPager2.OnPageChangeCallback? = null

            override val isEmpty: Boolean
                get() = vp.adapter!!.itemCount == 0
            override val isNotEmpty: Boolean
                get() = vp.adapter!!.itemCount > 0
            override val currentItem: Int
                get() = vp.currentItem
            override val count: Int
                get() = vp.adapter!!.itemCount

            override fun setCurrentItem(index: Int, smoothScroll: Boolean) {
                vp.setCurrentItem(index, smoothScroll)
            }

            override fun removeOnPageChangeListener() {
                listener?.let { vp.unregisterOnPageChangeCallback(it) }
            }

            override fun addOnPageChangeListener(helper: PageChangeHelper) {
                listener = object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                        helper.onPageScrolled(position, positionOffset)
                    }

                }
                vp.registerOnPageChangeCallback(listener!!)
            }

        }
        // 刷新
        refreshDots()
    }

    private fun refreshDots() {
        post {
            refreshDotCount()
            refreshDotColors()
            refreshDotsWidth()
            refreshHelper()
        }
    }

    // 刷新指示器点的数量
    private fun refreshDotCount() {
        if (dotList.size < pager!!.count) {
            for (i in 0 until (pager!!.count - dotList.size)) {
                addDot(i)
            }
        } else if (dotList.size > pager!!.count) {
            for (i in 0 until (dotList.size - pager!!.count)) {
                removeViewAt(childCount - 1)
                dotList.removeAt(dotList.size - 1)
            }
        }
    }

    // 添加指示器的点
    private fun addDot(index: Int) {
        val ivDot = ImageView(context)
        addView(ivDot)
        val layoutParams = ivDot.layoutParams as LayoutParams
        layoutParams.width = dotWidth.toInt()
        layoutParams.height = dotHeight.toInt()
        layoutParams.setMargins(dotSpacing.toInt() / 2, 0, dotSpacing.toInt() / 2, 0)
        val bg = GradientDrawable()
        bg.cornerRadius = dotRadius
        bg.setColor(if (index == currentIndex) selectedDotColor else dotColor)
        ivDot.background = bg

        dotList.add(ivDot)
    }

    private fun refreshDotColors() {
        for (i in dotList.indices) {
            refreshDotColor(i)
        }
    }

    // 刷新点的颜色
    private fun refreshDotColor(index: Int) {
        val ivDot = dotList[index]
        val bg = ivDot.background as GradientDrawable?

        bg?.let {
            if (index == pager!!.currentItem || progressMode && index < pager!!.currentItem) {
                bg.setColor(selectedDotColor)
            } else {
                bg.setColor(dotColor)
            }
        }

        ivDot.background = bg
        ivDot.invalidate()
    }

    // 刷新点的宽
    private fun refreshDotsWidth() {
        for (i in 0 until pager!!.currentItem) {
            dotList[i].setWidth(dotWidth.toInt())
        }
    }

    // 重置 PageChangeHelper
    private fun refreshHelper() {
        pager?.let {
            if (it.isEmpty) return
            it.removeOnPageChangeListener()
            val helper = createPageChangeHelper()
            it.addOnPageChangeListener(helper)
            helper.onPageScrolled(it.currentItem, 0f)
        }
    }

    // new PageChangeHelper instance
    private fun createPageChangeHelper(): PageChangeHelper {
        return object : PageChangeHelper() {
            override val pageCount: Int
                get() = dotList.size

            override fun onPageScrolled(selectedPos: Int, nextPos: Int, posOffset: Float) {
                // 选中的点
                val selectedDot = dotList[selectedPos]
                // 选中点宽度变化
                println("width:$dotWidth         height:$dotHeight")
                val selectedDotWidth =
                    (dotWidth + dotWidth * (dotsWidthFactor - 1) * (1 - posOffset)).toInt()
                selectedDot.setWidth(selectedDotWidth)

                // 边界限制
                if (nextPos in 0 until dotList.size) {
                    // 下一个选中点
                    val nextDot = dotList[nextPos]
                    // 下一个选中点宽度变化
                    val nextDotWidth =
                        (dotWidth + dotWidth * (dotsWidthFactor - 1) * posOffset).toInt()
                    nextDot.setWidth(nextDotWidth)

                    val bgSelectedDot = selectedDot.background as GradientDrawable
                    val bgNextDot = nextDot.background as GradientDrawable

                    if (selectedDotColor != dotColor) {
                        val selectedColor =
                            argbEvaluator.evaluate(posOffset, selectedDotColor, dotColor) as Int
                        val nextColor =
                            argbEvaluator.evaluate(posOffset, dotColor, selectedDotColor) as Int

                        bgNextDot.setColor(nextColor)

                        if (progressMode && selectedPos <= pager!!.currentItem) {
                            bgSelectedDot.setColor(selectedDotColor)
                        } else {
                            bgSelectedDot.setColor(selectedColor)
                        }
                    }
                }

                invalidate()
            }

            override fun resetPos(pos: Int) {
                // 重置点
                dotList[pos].apply {
                    setWidth(dotWidth.toInt())
                    setHeight(dotHeight.toInt())
                    requestLayout()
                    refreshDotColor(pos)
                }
            }

        }
    }
}