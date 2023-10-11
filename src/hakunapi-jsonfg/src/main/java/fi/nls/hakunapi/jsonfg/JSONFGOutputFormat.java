package fi.nls.hakunapi.jsonfg;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;
import fi.nls.hakunapi.geojson.hakuna.JSONFGFeatureCollectionWriter;
import fi.nls.hakunapi.geojson.hakuna.JSONFGSingleFeatureWriter;

public class JSONFGOutputFormat implements OutputFormat, JSONFG {

	public static final OutputFormat INSTANCE = new JSONFGOutputFormat();

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMediaMainType() {
		return MEDIA_MAIN_TYPE;
	}

	@Override
	public String getMediaSubType() {
		return MEDIA_SUB_TYPE;
	}

	@Override
	public String getMimeType() {
		return MIME_TYPE;
	}

	@Override
	public FeatureCollectionWriter getFeatureCollectionWriter() {
		return new JSONFGFeatureCollectionWriter();
	}

	@Override
	public SingleFeatureWriter getSingleFeatureWriter() {
		return new JSONFGSingleFeatureWriter();
	}

}
