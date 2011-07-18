/**
 * 
 */
package ecologylab.semantics.sensing;

import ecologylab.generic.Debug;
import ecologylab.semantics.metadata.builtins.GeoLocation;
import ecologylab.sensor.location.EarthData;

/**
 * Methods for computing on GeoLocation and other location-based stuff.
 * 
 * @author andruid
 */
public class GeoTools extends Debug implements EarthData
{

	/**
	 * 
	 */
	public GeoTools()
	{
	}

	/**
	 * Get the set of coordinates, serialized for use in KML / Google Earth.
	 * 
	 * @return
	 */
	public static String getKMLCommaDelimitedString(GeoLocation geoLocation)
	{
		return geoLocation.getLongitude() + "," + geoLocation.getLatitude() + "," + geoLocation.getAltitude();
	}

	/**
	 * @param that
	 * @return positive if this is farther north than that, negative if that is more north; 0 if they
	 *         lie on exactly the same parallel.
	 */
	public static double compareNS(GeoLocation geoLocation, GeoLocation that)
	{
		return geoLocation.getLatitude() - that.getLatitude();
	}

	/**
	 * @param that
	 * @return compares two GPSDatum's based on the acute angle between their longitudes. Returns 1 if
	 *         this is farther east than that, -1 if this is farther west, 0 if the two points lie on
	 *         the same arc, 180/-180 if they are opposite.
	 */
	public static double compareEW(GeoLocation geoLocation, GeoLocation that)
	{
		double diff = geoLocation.getLongitude() - that.getLongitude();

		if (diff > 180)
		{
			return diff - 360;
		}
		else if (diff < -180)
		{
			return diff + 360;
		}
		else
		{
			return diff;
		}
	}

	/**
	 * Uses the haversine formula to compute the great-circle direct distance from this to the other
	 * point. Does not take into account altitude.
	 * 
	 * Result is given in meters.
	 * 
	 * Formula used from http://www.movable-type.co.uk/scripts/latlong.html.
	 * 
	 * @param other
	 * @return great-circle distance between this and other, in meters.
	 */
	public static double distanceTo(GeoLocation geoLocation, GeoLocation other)
	{
		return distanceTo(geoLocation, other.getLatitude(), other.getLongitude());
	}

	/**
	 * Uses the haversine formula to compute the great-circle direct distance from this to the other
	 * point. Does not take into account altitude.
	 * 
	 * Result is given in meters.
	 * 
	 * Formula used from http://www.movable-type.co.uk/scripts/latlong.html.
	 * 
	 * @param otherLat
	 * @param otherLon
	 * @return great-circle distance between this and other, in meters.
	 */
	public static double distanceTo(GeoLocation geoLocation, double otherLat, double otherLon)
	{
		double deltaLat = Math.toRadians(otherLat - geoLocation.getLatitude());
		double deltaLon = Math.toRadians(otherLon - geoLocation.getLongitude());

		double a = (Math.sin(deltaLat / 2.0) * Math.sin(deltaLat / 2.0))
				+ (Math.cos(Math.toRadians(geoLocation.getLatitude())) * Math.cos(Math.toRadians(otherLat))
						* Math.sin(deltaLon / 2.0) * Math.sin(deltaLon / 2.0));
		double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

		return c * RADIUS_EARTH_METERS;
	}
}
