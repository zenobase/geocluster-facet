package com.zenobase.search.facet.geocluster;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.facet.FacetBuilder;

public class GeoClusterFacetBuilder extends FacetBuilder {

	private final String fieldName;
	private final double factor;
	private boolean calcPolygon;

	public GeoClusterFacetBuilder(String name, String fieldName, double factor) {
		this(name, fieldName, factor, false);
	}

	public GeoClusterFacetBuilder(String name, String fieldName, double factor, boolean calcPolygon) {
		super(name);
		this.fieldName = fieldName;
		this.factor = factor;
		this.calcPolygon = calcPolygon;
	}

	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(name);
		builder.startObject(GeoClusterFacet.TYPE);
		builder.field("field", fieldName);
		builder.field("factor", factor);
		builder.field("calc_polygon", calcPolygon);
		builder.endObject();
		addFilterFacetAndGlobal(builder, params);
		builder.endObject();
		return builder;
	}
}
