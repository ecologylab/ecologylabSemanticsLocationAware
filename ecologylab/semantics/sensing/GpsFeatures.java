/**
 * 
 */
package ecologylab.semantics.sensing;

import com.drew.metadata.exif.GpsDirectory;

import ecologylab.generic.Debug;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.generated.library.gps.GeoLocation;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.serialization.types.scalar.DoubleType;

/**
 * @author andruid
 * 
 */
public class GpsFeatures extends Debug
{
	public static final MetadataExifFeature	GPS_LATITUDE_FEATURE			= new MetadataExifFeature(
																																				"latitude",
																																				GpsDirectory.TAG_GPS_LATITUDE);

	public static final MetadataExifFeature	GPS_ALTITUDE_FEATURE			= new MetadataExifFeature(
																																				"altitude",
																																				GpsDirectory.TAG_GPS_ALTITUDE);

	public static final MetadataExifFeature	GPS_LATITUDE_REF_FEATURE	= new MetadataExifFeature(
																																				"latitude_ref",
																																				GpsDirectory.TAG_GPS_LATITUDE_REF);

	public static final MetadataExifFeature	GPS_LONGITUDE_REF_FEATURE	= new MetadataExifFeature(
																																				"longitude_ref",
																																				GpsDirectory.TAG_GPS_LONGITUDE_REF);

	public static final MetadataExifFeature	GPS_LONGITUDE_FEATURE			= new MetadataExifFeature(
																																				"longitude",
																																				GpsDirectory.TAG_GPS_LONGITUDE);

	public static final MetadataExifFeature	GPS_METADATA_FEATURES[]		=
																																		{ GPS_LATITUDE_FEATURE,
			GPS_LONGITUDE_FEATURE, GPS_LONGITUDE_REF_FEATURE, GPS_LATITUDE_REF_FEATURE,
			GPS_ALTITUDE_FEATURE, new MetadataExifFeature("satellites", GpsDirectory.TAG_GPS_SATELLITES), };

	double																	latitude, longitude, altitude;



	GpsFeatures(com.drew.metadata.Directory gpsDir, String gpsLatitudeString)
	{
		double converted 					= convertToDegrees(gpsLatitudeString);
		
		String gpsLatitudeRef 		= GPS_LATITUDE_REF_FEATURE.getStringValue(gpsDir);
		latitude									= "S".equals(gpsLatitudeRef) ? -converted : converted;
		
		String gpsLongitudeString = GPS_LONGITUDE_FEATURE.getStringValue(gpsDir);
		converted 								= convertToDegrees(gpsLongitudeString);
	
		String gpsLongitudeRef 		= GPS_LONGITUDE_REF_FEATURE.getStringValue(gpsDir);
		longitude									= "W".equals(gpsLongitudeRef) ? -converted : converted;
		
		String altitudeString 		= GPS_ALTITUDE_FEATURE.getStringValue(gpsDir);
		if (altitudeString != null)
			altitude								= DoubleType.rationalToDouble(altitudeString);
	}

	private double convertToDegrees(String stringDMS)
	{
		String[] dms		= stringDMS.split(" ", 3);

		double degrees 	= rationalToDouble(dms, 0);

		double minutes 	= rationalToDouble(dms, 1);

		double seconds 	= rationalToDouble(dms, 2);

		return degrees + (minutes / 60) + (seconds / 3600);
	}

	public static double rationalToDouble(String[] dms, int whichRational)
	{
		String rationalString = dms[whichRational];
		return DoubleType.rationalToDouble(rationalString);
	}

	/**
	 * Construct one of these, and use it to get nice dms GPS data from the exif metadata --
	 * if the GPS stuff is there.
	 * 
	 * @param gpsDir
	 * @return		An instance of this, or null.
	 */
	public static GpsFeatures extract(com.drew.metadata.Directory gpsDir)
	{
		String gpsLatitudeString = GPS_LATITUDE_FEATURE.getStringValue(gpsDir);
		return (gpsLatitudeString != null) ? new GpsFeatures(gpsDir, gpsLatitudeString) : null;
	}
	
	Metadata extractMixin(SemanticsGlobalScope semanticsScope)
	{
		GeoLocation result					= new GeoLocation();
		result.setLongitude(longitude);
		result.setLatitude(latitude);
		result.setAltitude(altitude);

		return result;
	}
	public static Metadata extractMixin(com.drew.metadata.Directory gpsDir, SemanticsGlobalScope semanticsScope)
	{
		GpsFeatures gpsFeatures	= extract(gpsDir);
		Metadata result					= null;
		if (gpsFeatures != null)
		{
			result								= gpsFeatures.extractMixin(semanticsScope);	
			
			//TODO Reverse geo-code, to discover place!
			// http://developer.yahoo.com/geo/placefinder/
			// http://where.yahooapis.com/geocode?q=38.898717,-77.035974&gflags=R&appid=[yourappidhere]
		}
		return result;
	}
	
}
