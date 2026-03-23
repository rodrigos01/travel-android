package travel.vola.android.common.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class SearchResult(val title: String, val subtitle: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBox(
    query: String,
    placeHolder: @Composable () -> Unit = {},
    searchResults: List<SearchResult> = emptyList(),
    onQueryChange: (String) -> Unit = {},
    onResultTapped: (Int) -> Unit = {},
    focusRequester: FocusRequester? = null,
) {
    val expanded =
        searchResults.isNotEmpty() && query.isNotBlank() && query.isNotEmpty()
    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                placeholder = placeHolder,
                onQueryChange = onQueryChange,
                expanded = expanded,
                onExpandedChange = {},
                onSearch = {},
                modifier = Modifier.then(
                    if (focusRequester != null) {
                        Modifier.focusRequester(focusRequester)
                    } else {
                        Modifier
                    },
                ),
            )
        },
        expanded = expanded,
        onExpandedChange = {},
        colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        searchResults.forEachIndexed { index, result ->
            Column(
                modifier = Modifier
                    .clickable { onResultTapped(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
            ) {
                Text(result.title, style = MaterialTheme.typography.labelMedium)
                Text(result.subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
@Preview
fun SearchBoxPreview() {
    SearchBox(
        query = "",
        placeHolder = { Text("Search") },
        searchResults = listOf(
            SearchResult("Paris", "France"),
            SearchResult("London", "England"),
        ),
        onQueryChange = {},
        onResultTapped = {},
    )
}
