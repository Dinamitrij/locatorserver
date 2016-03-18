package lv.div.locator.utils;

import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.Marker;
import java.util.ArrayList;
import java.util.List;

/**
 * Google map related utils
 */
public class GeoUtils {

    public static float distBetween(LatLng pos1, LatLng pos2) {
        return distBetween(pos1.getLat(), pos1.getLng(), pos2.getLat(),
                           pos2.getLng());
    }

    /**
     * Calculation distance between 2 MLS(GPS) points
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     *
     * @return
     */
    public static long mlsDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) +
                      Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        // To Meters
        dist = dist * 1.609344 * 1000;

        return (Math.round(dist));
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    /**
     * distance in meters *
     */
    public static float distBetween(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                   + Math.cos(Math.toRadians(lat1))
                     * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
                     * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        int meterConversion = 1609;

        return (float) (dist * meterConversion);
    }

    public static Marker getNearestMarker(List<Marker> markers,
                                          LatLng origin) {

        Marker nearestMarker = null;
        double lowestDistance = Double.MAX_VALUE;

        if (markers != null) {

            for (Marker marker : markers) {

                double dist = distBetween(origin, marker.getLatlng());

                if (dist < lowestDistance) {
                    nearestMarker = marker;
                    lowestDistance = dist;
                }
            }
        }

        return nearestMarker;
    }

    public static List<Marker> getSurroundingMarkers(List<Marker> markers,
                                                     LatLng origin, int maxDistanceMeters) {
        List<Marker> surroundingMarkers = null;

        if (markers != null) {
            surroundingMarkers = new ArrayList<Marker>();
            for (Marker marker : markers) {

                double dist = distBetween(origin, marker.getLatlng());

                if (dist < maxDistanceMeters) {
                    surroundingMarkers.add(marker);
                }
            }
        }

        return surroundingMarkers;
    }

}
