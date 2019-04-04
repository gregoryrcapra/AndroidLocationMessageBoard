# Android Location Message Board

A location message app where you can access comment feed within 10 meters of specified UC Berkeley landmarks.

### Background

This app works by querying the user's location and asking for a username. It lists a number of Berkeley landmarks from a JSON file and shows the distance
between the landmark and the user. If the user is 10 meters or less away, it allows them to access a comment feed for that landmark and post comments with their username.
The comments are read from and written to a Firebase database called "GRC-location-message-board" (updates can be seen in real time via the Firebase console).

I used the below tutorials for help on some of the details:
- [Haversine Distance](https://rosettacode.org/wiki/Haversine_formula#Java)
- [Location Updates](https://github.com/codepath/android_guides/wiki/Retrieving-Location-with-LocationServices-API)

Make sure to have the latest Java installed, download Android Studio, and run the Gradle build before running the app.

Enjoy!
