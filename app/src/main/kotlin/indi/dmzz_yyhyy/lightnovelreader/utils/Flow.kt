package indi.dmzz_yyhyy.lightnovelreader.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    flow9: Flow<T9>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R,
): Flow<R> = combine(
    combine(flow1, flow2, flow3, flow4, flow5, ::CombinedValues),
    flow6,
    flow7,
    flow8,
    flow9,
) { values, value6, value7, value8, value9 ->
    transform(
        values.value1,
        values.value2,
        values.value3,
        values.value4,
        values.value5,
        value6,
        value7,
        value8,
        value9,
    )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    flow9: Flow<T9>,
    flow10: Flow<T10>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> R,
): Flow<R> = combine(
    combine(flow1, flow2, flow3, flow4, flow5, ::CombinedValues),
    combine(flow6, flow7, flow8, flow9, flow10, ::CombinedValues),
) { firstValues, secondValues ->
    transform(
        firstValues.value1,
        firstValues.value2,
        firstValues.value3,
        firstValues.value4,
        firstValues.value5,
        secondValues.value1,
        secondValues.value2,
        secondValues.value3,
        secondValues.value4,
        secondValues.value5,
    )
}

private data class CombinedValues<T1, T2, T3, T4, T5>(
    val value1: T1,
    val value2: T2,
    val value3: T3,
    val value4: T4,
    val value5: T5,
)
