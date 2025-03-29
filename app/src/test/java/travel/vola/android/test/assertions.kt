package travel.vola.android.test

import org.assertj.core.api.Assertions
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object Assertions {
    @ExperimentalContracts
    inline fun <reified T> assertType(obj: Any?) {
        contract { returns() implies (obj is T) }
        Assertions.assertThat(obj).isInstanceOf(T::class.java)
    }
}
