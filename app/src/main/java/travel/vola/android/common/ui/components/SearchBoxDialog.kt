package travel.vola.android.common.ui.components

import travel.vola.android.R
import androidx.compose.ui.res.stringResource

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.window.Dialog

@Composable
fun SearchBoxDialog(
    onDismiss: () -> Unit,
    searchResults: List<SearchResult>,
    onLocationSearchTextChanged: (CharSequence) -> Unit,
    onLocationSearchResultSelected: (Int) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        var query by remember { mutableStateOf("") }

        val focusRequester = remember { FocusRequester() }
        SearchBox(
            query = query,
            placeHolder = { Text(stringResource(R.string.prompt_enter_location)) },
            searchResults = searchResults,
            onQueryChange = {
                query = it
                onLocationSearchTextChanged(it)
            },
            onResultTapped = {
                onLocationSearchResultSelected(it)
                onDismiss()
            },
            focusRequester = focusRequester,
        )
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}