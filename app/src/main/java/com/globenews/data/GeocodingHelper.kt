package com.globenews.data

object GeocodingHelper {
    data class Location(val name: String, val lat: Double, val lng: Double)

    private val knownLocations = listOf(
        Location("New York", 40.7128, -74.0060),
        Location("London", 51.5074, -0.1278),
        Location("Tokyo", 35.6762, 139.6503),
        Location("Paris", 48.8566, 2.3522),
        Location("Sydney", -33.8688, 151.2093),
        Location("Beijing", 39.9042, 116.4074),
        Location("Moscow", 55.7558, 37.6173),
        Location("Cairo", 30.0444, 31.2357),
        Location("Rio de Janeiro", -22.9068, -43.1729),
        Location("Mumbai", 19.0760, 72.8777),
        Location("Lagos", 6.5244, 3.3792),
        Location("Berlin", 52.5200, 13.4050),
        Location("Mexico City", 19.4326, -99.1332),
        Location("Toronto", 43.6532, -79.3832),
        Location("Seoul", 37.5665, 126.9780),
        Location("Buenos Aires", -34.6037, -58.3816),
        Location("Istanbul", 41.0082, 28.9784),
        Location("Nairobi", -1.2921, 36.8219),
        Location("Bangkok", 13.7563, 100.5018),
        Location("San Francisco", 37.7749, -122.4194),
        Location("Los Angeles", 34.0522, -118.2437),
        Location("Chicago", 41.8781, -87.6298),
        Location("Washington", 38.9072, -77.0369),
        Location("Dubai", 25.2048, 55.2708),
        Location("Singapore", 1.3521, 103.8198),
        Location("Hong Kong", 22.3193, 114.1694),
        Location("Rome", 41.9028, 12.4964),
        Location("Madrid", 40.4168, -3.7038),
        Location("Johannesburg", -26.2041, 28.0473),
        Location("Lima", -12.0464, -77.0428),
        Location("Jakarta", -6.2088, 106.8456),
        Location("Baghdad", 33.3152, 44.3661),
        Location("Kyiv", 50.4501, 30.5234),
        Location("Taipei", 25.0330, 121.5654),
        Location("Tehran", 35.6892, 51.3890),
        Location("Brasilia", -15.7975, -47.8919),
        Location("Canberra", -35.2809, 149.1300),
        Location("Ottawa", 45.4215, -75.6972),
        Location("Stockholm", 59.3293, 18.0686),
        Location("Zurich", 47.3769, 8.5417),
        // Regions / continents
        Location("Europe", 50.0, 10.0),
        Location("Africa", 0.0, 25.0),
        Location("Asia", 35.0, 105.0),
        Location("North America", 45.0, -100.0),
        Location("South America", -15.0, -60.0),
        Location("Australia", -25.0, 135.0),
        Location("Middle East", 29.0, 47.0),
        Location("Antarctica", -82.0, 0.0),
    )

    fun search(query: String): Location? {
        if (query.isBlank()) return null
        val lower = query.lowercase().trim()
        return knownLocations.firstOrNull { it.name.lowercase().contains(lower) }
    }
}
