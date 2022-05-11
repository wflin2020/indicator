package com.wflin.indicator

/**
 *
 * @author wflin
 * @date 2022/4/27
 */
abstract class PageChangeHelper {

    private var lastSelectedIndex = -1 // 上一个选中点角标
    private var currentSelectedIndex = 0 // 当前选重点角标

    internal abstract val pageCount: Int // 页面数量
    internal abstract fun onPageScrolled(selectedPos: Int, nextPos: Int, posOffset: Float)
    internal abstract fun resetPos(pos: Int)

    fun onPageScrolled(pos: Int, posOffset: Float) {
        // （base选中点）滑动偏移量
        var offset = (pos + posOffset)
        // 最后一个点角标
        val lastPageIndex = (pageCount - 1).toFloat()
        if (offset == lastPageIndex) {
            offset = lastPageIndex - .0001f
        }

        // 选中点的角标
        val selectedIndex = offset.toInt()
        // 待选中点的角标
        val nextSelectedIndex = selectedIndex + 1
        // 下一个选中点到超出了边界
        if (nextSelectedIndex > lastPageIndex) {
            return
        }

        // 浮点数对1取余，最终保留了小数位
        onPageScrolled(selectedIndex, nextSelectedIndex, offset % 1)
        // 不是第一个点
        if (lastSelectedIndex != -1) {
            // 重置 [上移选中点, 当前点)，即：上一个选中点（含）到当前选中点（不含）范围内的点
            // 如果是点击滑动的，就会有多个点
            if (selectedIndex > lastSelectedIndex) {
                (lastSelectedIndex until selectedIndex).forEach { resetPos(it) }
            }

            // 以下代码只存在点击导致的滑动情况
            if (nextSelectedIndex < currentSelectedIndex) {
                resetPos(currentSelectedIndex)
                ((nextSelectedIndex + 1)..currentSelectedIndex).forEach { resetPos(it) }
            }
        }

        lastSelectedIndex = selectedIndex
        currentSelectedIndex = nextSelectedIndex
    }


}