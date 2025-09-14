package travel.vola.android.model.datastore

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import travel.vola.android.model.data.DataSourceType
import travel.vola.android.model.repository.UserPreferencesRepository
import travel.vola.android.proto.UserPreferences

private const val DATASOURCE_KEY_LOCAL = "LOCAL"
private const val DATASOURCE_KEY_FIREBASE = "FIREBASE"

class DataStoreUserPreferencesRepository(private val dataStore: DataStore<UserPreferences>) :
    UserPreferencesRepository {

    override val preferences: Flow<travel.vola.android.model.data.UserPreferences> =
        dataStore.data.map {
            travel.vola.android.model.data.UserPreferences(
                dataSourceType = when (it.dataSource) {
                    DATASOURCE_KEY_LOCAL -> DataSourceType.LOCAL
                    DATASOURCE_KEY_FIREBASE -> DataSourceType.FIREBASE
                    else -> DataSourceType.LOCAL
                }
            )
        }

    override suspend fun setDataSource(dataSourceType: DataSourceType) {
        dataStore.updateData { current ->
            current.toBuilder()
                .setDataSource(
                    when (dataSourceType) {
                        DataSourceType.LOCAL -> DATASOURCE_KEY_LOCAL
                        DataSourceType.FIREBASE -> DATASOURCE_KEY_FIREBASE
                    }
                )
                .build()
        }
    }
}