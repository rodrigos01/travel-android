package travel.vola.android.model.repository

import kotlinx.coroutines.flow.Flow
import travel.vola.android.model.data.DataSourceType
import travel.vola.android.model.data.UserPreferences

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun setDataSource(dataSourceType: DataSourceType)
}
