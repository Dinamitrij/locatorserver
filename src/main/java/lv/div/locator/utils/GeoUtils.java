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
