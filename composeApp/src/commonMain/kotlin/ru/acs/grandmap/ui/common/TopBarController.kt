import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import ru.acs.grandmap.ui.common.AppBarIconAction
import ru.acs.grandmap.ui.common.AppBarOverflowItem

data class TopBarSpec(
    val title: String? = null,
    val subtitle: String? = null,
    val loading: Boolean = false,
    val onBack: (() -> Unit)? = null,
    val primary: List<AppBarIconAction> = emptyList(),
    val overflow: List<AppBarOverflowItem> = emptyList(),
    val visible: Boolean = true,
    val overflowOpen: Boolean = false
)

class TopBarController {
    private val _spec = MutableStateFlow(TopBarSpec())
    val spec: StateFlow<TopBarSpec> = _spec
    fun update(spec: TopBarSpec) {
        _spec.value = spec
    }

    fun clear() {
        _spec.value = TopBarSpec()
    }

    fun setOverflowOpen(open: Boolean) {
        _spec.update { it.copy(overflowOpen = open) }
    }
}