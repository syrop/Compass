Demonstrates how to separate three stages of data acquiring and presentation:

1. Obtaining the data from either sensor or Google API client.
2. Wrapping it in LiveData.
3. Presenting on the screen as an animation. 

Two kinds of data are acquired: 

1. Location (using `FusedLocationProviderClient`).
2. Azimuth (using two sensors: magnetic field and accelerometer).

The data is then used to display a compass on the screen and (if selected is settings) an arrow pointing in the direction of one chosen location.
