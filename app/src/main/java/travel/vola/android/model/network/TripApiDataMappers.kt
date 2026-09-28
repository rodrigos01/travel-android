package travel.vola.android.model.network

import travel.vola.android.extensions.asISO8601String
import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.AnsweredQuestion
import travel.vola.android.model.data.BasicInformation
import travel.vola.android.model.data.FlexibleDayCategory
import travel.vola.android.model.data.FlexibleDayItem
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.data.GroupType
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.TripParameters
import travel.vola.android.model.data.TripPreferences

fun Flight.toApiDataModel() = TripApiData.Flight(
    id = id,
    segments = segments.map { it.toApiDataModel() },
    price = price,
)

fun FlightSegment.toApiDataModel() = TripApiData.FlightSegment(
    airportFrom = airportFrom.toApiDataModel(),
    departure = departure.asISO8601String(),
    airportTo = airportTo.toApiDataModel(),
    arrival = arrival.asISO8601String(),
)

fun Airport.toApiDataModel() = TripApiData.Airport(
    iata = iata,
    name = name,
    timezone = timeZone.id,
    city = city.toApiDataModel(),
)

fun Lodging.toApiDataModel() = TripApiData.Lodging(
    id = id,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    city = city.toApiDataModel(),
    checkIn = checkIn.asISO8601String(),
    checkout = checkout.asISO8601String(),
)

fun Place.toApiDataModel() = TripApiData.Place(
    id = id,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    timeZone = timeZone.id,
    coverImage = coverImage,
    externalId = externalId,
    source = source,
)

fun TimedPlace.toApiDataModel() = TripApiData.TimedPlace(
    id = id,
    place = place.toApiDataModel(),
    time = startDateTime.asISO8601String(),
    hasTime = hasStartTime,
    endTime = endDateTime?.asISO8601String(),
    hasEndTime = hasEndTime,
    city = city.toApiDataModel(),
)

fun RestaurantReservation.toApiDataModel() = TripApiData.RestaurantReservation(
    id = id,
    time = dateTime.asISO8601String(),
    place = place.toApiDataModel(),
    city = city.toApiDataModel(),
)

fun TripPreferences.toApiDataModel() = TripApiData.TripPreferences(
    basicInformation = basicInformation.toApiDataModel(),
    initialParameters = initialParameters.toApiDataModel(),
    questionsAnswers = questionsAnswers.map { it.toApiDataModel() },
)

fun BasicInformation.toApiDataModel() = TripApiData.BasicInformation(
    groupType = groupType.toApiDataModel(),
    travelers = travelers,
)

fun GroupType.toApiDataModel() = when (this) {
    GroupType.SOLO -> TripApiData.GroupType.SOLO
    GroupType.FAMILY -> TripApiData.GroupType.FAMILY
    GroupType.FRIENDS -> TripApiData.GroupType.FRIENDS
    GroupType.COWORKERS -> TripApiData.GroupType.COWORKERS
    GroupType.COUPLE -> TripApiData.GroupType.COUPLE
}

fun TripParameters.toApiDataModel() = TripApiData.TripParameters(
    occasions = occasions,
    interests = interests,
    vibe = vibe,
    focus = focus,
    mustHave = mustHave,
    duration = duration,
    anythingElse = anythingElse,
)

fun AnsweredQuestion.toApiDataModel() = TripApiData.AnsweredQuestion(
    question = question,
    answer = answer,
)

fun FlexibleDaySection.toApiDataModel() = TripApiData.FlexibleDaySection(
    id = id,
    name = name,
    date = date.asISO8601String(),
    city = city.toApiDataModel(),
    categories = categories.map { it.toApiDataModel() },
)

fun FlexibleDayCategory.toApiDataModel() = TripApiData.FlexibleSectionCategory(
    name = name,
    items = items.map { it.toApiDataModel() },
)

fun FlexibleDayItem.toApiDataModel() = TripApiData.FlexibleSectionItem(
    id = id,
    place = place.toApiDataModel(),
    note = note,
)
