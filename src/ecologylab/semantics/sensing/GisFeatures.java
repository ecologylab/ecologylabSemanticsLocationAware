/**
 * 
 */
package ecologylab.semantics.sensing;

import com.drew.metadata.exif.GpsDirectory;

import ecologylab.generic.Continuation;
import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.generated.library.gis.GisLocation;
import ecologylab.semantics.generated.library.gis.PostalAddress;
import ecologylab.semantics.generated.library.search.YahooGeoCodeResult;
import ecologylab.semantics.generated.library.search.YahooResult;
import ecologylab.semantics.generated.library.search.YahooResultSet;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.serialization.types.scalar.DoubleType;

/**
 * Tools for extracting GIS data from EXIF+ headrs from image files, 
 * and using these to populate appropriate S.IM.PL Metadata.
 * 
 * @author andruid
 */
public class GisFeatures extends Debug
{
	public static final MetadataExifFeature	GPS_LATITUDE_FEATURE			= new MetadataExifFeature( "latitude", GpsDirectory.TAG_GPS_LATITUDE);
	
 public static final MetadataExifFeature	GPS_ALTITUDE_FEATURE			= new MetadataExifFeature( "altitude", GpsDirectory.TAG_GPS_ALTITUDE);

	public static final MetadataExifFeature	GPS_LATITUDE_REF_FEATURE	= new MetadataExifFeature( "latitude_ref", GpsDirectory.TAG_GPS_LATITUDE_REF);

	public static final MetadataExifFeature	GPS_LONGITUDE_REF_FEATURE	= new MetadataExifFeature( "longitude_ref", GpsDirectory.TAG_GPS_LONGITUDE_REF);

	public static final MetadataExifFeature	GPS_LONGITUDE_FEATURE			= new MetadataExifFeature( "longitude", GpsDirectory.TAG_GPS_LONGITUDE);
	
	public static final MetadataExifFeature	GPS_IMG_DIRECTION_REF_FEATURE		= new MetadataExifFeature( "direction_ref", GpsDirectory.TAG_GPS_IMG_DIRECTION_REF);
	
	public static final MetadataExifFeature	GPS_IMG_DIRECTION_FEATURE		= new MetadataExifFeature( "direction", GpsDirectory.TAG_GPS_IMG_DIRECTION);

	public static final MetadataExifFeature	GPS_METADATA_FEATURES[]		= {
																																		GPS_LATITUDE_FEATURE,
																																		GPS_LONGITUDE_FEATURE,
																																		GPS_LONGITUDE_REF_FEATURE,
																																		GPS_LATITUDE_REF_FEATURE,
																																		GPS_ALTITUDE_FEATURE,
																																		GPS_IMG_DIRECTION_FEATURE,
																																		GPS_IMG_DIRECTION_REF_FEATURE,
																																		new MetadataExifFeature("satellites", GpsDirectory.TAG_GPS_SATELLITES),
																																		};

	double																	latitude, longitude, altitude, direction;



	GisFeatures(com.drew.metadata.Directory gpsDir, String gpsLatitudeString)
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
		
		String directionString		= GPS_IMG_DIRECTION_FEATURE.getStringValue(gpsDir);
		if (directionString != null)
			direction								= DoubleType.rationalToDouble(directionString);
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
	public static GisFeatures extract(com.drew.metadata.Directory gpsDir)
	{
		String gpsLatitudeString = GPS_LATITUDE_FEATURE.getStringValue(gpsDir);
		return (gpsLatitudeString != null) ? new GisFeatures(gpsDir, gpsLatitudeString) : null;
	}
	
	public static boolean containsGisMixin(final Metadata parent)
	{
		return parent.containsMixin(GisLocation.class);
	}

	GisLocation extractMixin(Metadata parentMetadata)
	{
		GisLocation result					= containsGisMixin(parentMetadata) ? parentMetadata.getMixin(GisLocation.class) : new GisLocation();
		setMixinFeatures(result);

		return result;
	}

	private void setMixinFeatures(GisLocation result)
	{
		result.setLongitude(longitude);
		result.setLatitude(latitude);
		result.setAltitude(altitude);
		result.setDirection(direction);
	}
	
	//TODO Reverse geo-code, to discover place!
	// http://developer.yahoo.com/geo/placefinder/
	// http://where.yahooapis.com/geocode?q=38.898717,-77.035974&gflags=R&appid=[yourappidhere]
	public static final String	YAHOO_REVERSE_GEO	= "http://where.yahooapis.com/geocode?gflags=R&q=";
	
	public static Metadata extractMixin(com.drew.metadata.Directory gpsDir, final SemanticsGlobalScope semanticsScope, final Metadata parentMetadata)
	{
		final GisFeatures gpsFeatures				= extract(gpsDir);
		if (gpsFeatures == null)
			return null;
		
		final GisLocation result		= gpsFeatures.extractMixin(parentMetadata);	
		parentMetadata.addMixin(result);
		
		ParsedURL reverseGeo	= ParsedURL.getAbsolute(YAHOO_REVERSE_GEO + result.getLongitude()+","+result.getLatitude());

		Document reverseGeoDoc			= semanticsScope.getOrConstructDocument(reverseGeo);
		DocumentClosure geoClosure	= reverseGeoDoc.getOrConstructClosure();
		geoClosure.addContinuation(new Continuation<DocumentClosure>()
		{
			@Override
			public void callback(DocumentClosure o)
			{
				// TODO Auto-generated method stub
				YahooResultSet resultSet		= (YahooResultSet) o.getDocument();
				YahooResult yahooResult = resultSet.getResults().get(0);
				if (yahooResult instanceof YahooGeoCodeResult)
				{
					YahooGeoCodeResult yahooGeoResult = (YahooGeoCodeResult) yahooResult;
					println(yahooGeoResult.getCountry() + " > " + yahooGeoResult.getCity());
					PostalAddress postalAddress	= new PostalAddress(semanticsScope.getMetaMetadataRepository().getMMByClass(PostalAddress.class));
					postalAddress.setStreetAddress(yahooGeoResult.getLine1());
					postalAddress.setLocality(yahooGeoResult.getCity());
					postalAddress.setRegion(yahooGeoResult.getState());
					postalAddress.setPostalCode(yahooGeoResult.getPostal());
					postalAddress.setCounty(yahooGeoResult.getCounty());
					postalAddress.setCountry(yahooGeoResult.getCountry());
					parentMetadata.addMixin(postalAddress);
				}
				else
				{
					gpsFeatures.warning("type of results is not YahooGeoResult! " + yahooResult);
				}
			}
		});
		geoClosure.queueDownload();
		return result;
	}
	
}
