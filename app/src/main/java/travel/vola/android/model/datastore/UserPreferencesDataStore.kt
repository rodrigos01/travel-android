package travel.vola.android.model.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import travel.vola.android.proto.UserPreferences


val Context.userPreferencesDataStore: DataStore<UserPreferences> by dataStore(
    fileName = "UserPreferences.proto",
    serializer = UserPreferencesSerializer
)