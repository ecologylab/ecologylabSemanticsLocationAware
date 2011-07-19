/**
 * 
 */
package ecologylab.semantics.sensing;

import java.util.Date;

import com.drew.metadata.MetadataException;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.metadata.scalar.MetadataDate;
import ecologylab.semantics.metadata.scalar.MetadataFloat;
import ecologylab.semantics.metadata.scalar.MetadataScalarBase;
import ecologylab.semantics.metadata.scalar.MetadataString;

/**
 * Maps an exif (metadata) tag with a S.IM.PL Metadata field in a S.IM.PL Metadata object.
 * 
 * @author andruid
 */
public class MetadataExifFeature
{
	String 	simplMetadataTag;
	
	int			exifTag;
	
	MetadataFieldDescriptor mfd;
	
	public MetadataExifFeature(String simplMetadataTag, int	exifTag)
	{
		this.simplMetadataTag	= simplMetadataTag;
		this.exifTag					= exifTag;
	}

	/**
	 * @return the metadataName
	 */
	public String getMetadataName()
	{
		return simplMetadataTag;
	}

	/**
	 * @return the exifTag
	 */
	public int getExifTag()
	{
		return exifTag;
	}
	
	/**
	 * This verision of extract does it the right way, using the ScalarType derived from the
	 * correct MetadataFieldDescriptor.
	 * <p/>
	 * Extract a value from the exif header. Set the matching S.IM.PL Metadata field.
	 * 
	 * @param metadata
	 * @param dir
	 * @return
	 */
	public Object extract(Metadata metadata, com.drew.metadata.Directory dir)
	{
		String value	= getStringValue(dir);
		Object result	= null;
		if (value != null)
		{
			MetadataFieldDescriptor mfd	= metadata.getFieldDescriptorByTagName(simplMetadataTag);
			mfd.set(metadata, value);
		}
		return result;
	}
	
	/**
	 * Get a String type value out of the exif dir.
	 * 
	 * @param dir
	 * @return
	 */
	public String getStringValue(com.drew.metadata.Directory dir)
	{
		String result	= null;
		if (dir.containsTag(exifTag))
			result			= dir.getString(exifTag);
		return result;
	}
	/**
	 * Probably never needed, as the extract method is more general.
	 * 
	 * @param metadata
	 * @param dir
	 * @return
	 */
	public Date extractDate(Metadata metadata, com.drew.metadata.Directory dir)
	{
		Date value	= getDateValue(dir);
		if (value != null)
		{
			MetadataDate metaDate				= new MetadataDate(value);
			setMetadataField(metadata, metaDate);
		}
		return value;
	}

	/**
	 * @param metadata
	 * @param metaDate
	 */
	public void setMetadataField(Metadata metadata, MetadataScalarBase metaDate)
	{
		MetadataFieldDescriptor mfd	= metadata.getFieldDescriptorByTagName(simplMetadataTag);
		if (mfd != null)
			mfd.setField(metadata, metaDate);
	}
	/**
	 * Get a Date type value out of the exif dir.
	 * 
	 * @param dir
	 * @return
	 */
	public Date getDateValue(com.drew.metadata.Directory dir)
	{
		Date result	= null;
		if (dir.containsTag(exifTag))
			try
			{
				result			= dir.getDate(exifTag);
			}
			catch (MetadataException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return result;
	}
}
