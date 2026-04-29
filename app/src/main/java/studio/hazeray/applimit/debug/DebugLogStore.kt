package studio.hazeray.applimit.debug

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class DebugLogStore @Inject constructor() {

    private val buffer = ArrayDeque<DebugTickRecord>(MAX_SIZE)
    private val _entries = MutableStateFlow<List<DebugTickRecord>>(emptyList())
    val entries: StateFlow<List<DebugTickRecord>> = _entries.asStateFlow()

    @Synchronized
    fun record(record: DebugTickRecord) {
        if (buffer.size >= MAX_SIZE) buffer.removeFirst()
        buffer.addLast(record)
        _entries.value = buffer.toList()
    }

    @Synchronized
    fun clear() {
        buffer.clear()
        _entries.value = emptyList()
    }

    companion object {
        private const val MAX_SIZE = 100
    }
}
