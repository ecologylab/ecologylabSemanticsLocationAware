/**
 * 
 */
package ecologylab.semantics.sensing;

import ecologylab.generic.Debug;
import ecologylab.semantics.generated.library.gis.GisLocation;
import ecologylab.sensor.location.EarthData;

/**
 * Methods for computing on GisLocation and other location-based stuff.
 * 
 * @author andruid
 */
public class GisTools extends Debug implements EarthData
{

	/**
	 * 
	 */
	public GisTools()
	{
	}

	public GisLocation constructGisLocation(double lat, double lon, double alt)
	{
		GisLocation result	= new GisLocation();
		result.setLatitude(lat);
		result.setLongitude(lon);
		result.setAltitude(alt);
		
		return result;
	}


	/**
	 * Get the set of coordinates, serialized for use in KML / Google Earth.
	 * 
	 * @return
	 */
	public static String getKMLCommaDelimitedString(GisLocation GisLocation)
	{
		return GisLocation.getLongitude() + "," + GisLocation.getLatitude() + "," + GisLocation.getAltitude();
	}

	/**
	 * @param that
	 * @return positive if this is farther north than that, negative if that is more north; 0 if they
	 *         lie on exactly the same parallel.
	 */
	public static double compareNS(GisLocation GisLocation, GisLocation that)
	{
		return GisLocation.getLatitude() - that.getLatitude();
	}

	/**
	 * @param that
	 * @return compares two GPSDatum's based on the acute angle between their longitudes. Returns 1 if
	 *         this is farther east than that, -1 if this is farther west, 0 if the two points lie on
	 *         the same arc, 180/-180 if they are opposite.
	 */
	public static double compareEW(GisLocation GisLocation, GisLocation that)
	{
		double diff = GisLocation.getLongitude() - that.getLongitude();

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
	public static double distanceTo(GisLocation GisLocation, GisLocation other)
	{
		return distanceTo(GisLocation, other.getLatitude(), other.getLongitude());
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
	public static double distanceTo(GisLocation GisLocation, double otherLat, double otherLon)
	{
		double deltaLat = Math.toRadians(otherLat - GisLocation.getLatitude());
		double deltaLon = Math.toRadians(otherLon - GisLocation.getLongitude());

		double a = (Math.sin(deltaLat / 2.0) * Math.sin(deltaLat / 2.0))
				+ (Math.cos(Math.toRadians(GisLocation.getLatitude())) * Math.cos(Math.toRadians(otherLat))
						* Math.sin(deltaLon / 2.0) * Math.sin(deltaLon / 2.0));
		double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

		return c * RADIUS_EARTH_METERS;
	}
}
