package io.ejekta.kambrik.gui.widgets

import io.ejekta.kambrik.gui.KGuiDsl
import io.ejekta.kambrik.gui.KWidget
import net.minecraft.client.gui.screen.Screen
import kotlin.math.max
import kotlin.math.min

open class KListWidget<T>(
    private val items: () -> List<T>,
    val itemWidth: Int,
    val itemHeight: Int,
    private val rows: Int,
    val onDrawItemFunc: KGuiDsl.(
        listWidget: KListWidget<T>,
        item: T,
        selected: Boolean
    ) -> Unit = { _, _, _ -> }
) : KWidget() {


    private var scrollBar: KScrollbar? = null

    fun attachScrollbar(bar: KScrollbar) {
        scrollBar = bar
    }

    // We use a map for lookup performance
    private var internalSelected = mutableMapOf<T, Boolean>()
    private var lastSelectedIndex: Int? = null

    val selected: List<T>
        get() = internalSelected.keys.toList()

    open fun canMultiSelect(): Boolean = true

    fun select(vararg items: T) {
        select(items.toList())
    }

    fun select(items: List<T>) {
        if (!Screen.hasControlDown() || !canMultiSelect()) {
            internalSelected.clear()
        }
        if (canMultiSelect()) {
            for (item in items) {
                internalSelected[item] = true
            }
        } else if (items.isNotEmpty()) {
            internalSelected.clear()
            internalSelected[items.last()] = true
        }
    }

    override val height: Int
        get() = items().size * itemHeight

    override val width: Int
        get() = itemWidth

    val shownRange: IntRange
        get() = (scrollBar?.getIndices(items().size, rows) ?: (0 until rows))

    override fun onDraw(dsl: KGuiDsl): KGuiDsl {
        return dsl {
            val toIterate = items()
            shownRange.forEachIndexed { index, rowNumToShow ->
                offset(0, index * itemHeight) {
                    onDrawItem(this, toIterate[rowNumToShow])
                }
            }
        }
    }

    override fun onClick(relX: Int, relY: Int, button: Int) {
        val itemRenderIndex = relY / itemHeight
        val itemListIndex = shownRange.toList().getOrNull(itemRenderIndex)

        val allItems = items()

        // If holding shift. we can select multiple
        if (Screen.hasShiftDown() && lastSelectedIndex != null && itemListIndex != null) {
            lastSelectedIndex?.let {
                val selectedRange = min(it, itemListIndex)..max(it, itemListIndex)
                val selectedItems = selectedRange.toList().mapNotNull { i -> allItems.getOrNull(i) }
                select(selectedItems)
            }
        } else {
            itemListIndex?.let {
                val item = allItems[it]
                select(item)
                lastSelectedIndex = itemListIndex
            }
        }

    }

    override fun onMouseScrolled(relX: Int, relY: Int, amount: Double) {
        scrollBar?.scroll(-amount / max(1, items().size))
    }

    open fun onDrawItem(dsl: KGuiDsl, item: T) {
        dsl.onDrawItemFunc(this, item, item in selected)
    }
}