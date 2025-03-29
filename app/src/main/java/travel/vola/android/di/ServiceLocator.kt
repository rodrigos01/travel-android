package travel.vola.android.di

import com.google.firebase.firestore.FirebaseFirestore
import com.vola.android.model.PlaceRepository
import com.vola.android.model.firebase.FirebaseTripRepository
import com.vola.android.model.repository.TripRepository

class ServiceLocator {
    val tripRepository: TripRepository by lazy {
        FirebaseTripRepository(FirebaseFirestore.getInstance())
    }
    val placeRepository: PlaceRepository by lazy {
        PlaceRepository()
    }
}