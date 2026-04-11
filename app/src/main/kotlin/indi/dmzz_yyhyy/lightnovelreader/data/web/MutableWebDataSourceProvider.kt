package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.utils.AdvancedPrioritySemaphore
import io.nightfish.lightnovelreader.api.web.WebBookDataSource

class MutableWebDataSourceProvider: WebBookDataSourceProvider {
    private var _value: WebBookDataSource = EmptyWebDataSource
        set(value) {
            advancedPrioritySemaphore = AdvancedPrioritySemaphore(value.permits)
            field = value
        }
    private var highPriorityProcessedDataSource: WebBookDataSource = EmptyWebDataSource
    private var defaultProcessedDataSource: WebBookDataSource = EmptyWebDataSource
    private var lowPriorityProcessedDataSource: WebBookDataSource = EmptyWebDataSource

    private var advancedPrioritySemaphore = AdvancedPrioritySemaphore(_value.permits)
    fun processDataSource(permits: Int) =
        if (_value.cache != null)
            CacheWebBookDataSource(
                _value.cache!!,
                MergeWebBookDataSource(
                    AdvancedPrioritySemaphoreWebBookDataSource(
                        _value,
                        advancedPrioritySemaphore,
                        permits
                    )
                )
            )
        else
            MergeWebBookDataSource(
                AdvancedPrioritySemaphoreWebBookDataSource(
                    _value,
                    advancedPrioritySemaphore,
                    permits
                )
            )

    fun update(webBookDataSource: WebBookDataSource) {
                _value = webBookDataSource
    }
    val value get() = _value
    override val highPriority: WebBookDataSource
        get() =
            if (highPriorityProcessedDataSource.id == _value.id) highPriorityProcessedDataSource
            else processDataSource(3).also { highPriorityProcessedDataSource = it }
    override val default: WebBookDataSource
        get() =
            if (defaultProcessedDataSource.id == _value.id) defaultProcessedDataSource
            else processDataSource(2).also { defaultProcessedDataSource = it }
    override val lowPriority: WebBookDataSource
        get() =
            if (lowPriorityProcessedDataSource.id == _value.id) lowPriorityProcessedDataSource
            else processDataSource(1).also { lowPriorityProcessedDataSource = it }
}