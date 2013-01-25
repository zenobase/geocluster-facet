package com.zenobase.search.facet.geocluster;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.index.mapper.geo.GeoPoint;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.InternalFacet;

public class InternalGeoClusterFacet implements GeoClusterFacet, InternalFacet {

	private static final String STREAM_TYPE = "geoCluster";

	public static void registerStreams() {
		Streams.registerStream(STREAM, STREAM_TYPE);
	}

	static InternalFacet.Stream STREAM = new InternalFacet.Stream() {
		@Override
		public Facet readFacet(String type, StreamInput in) throws IOException {
			return readGeoClusterFacet(in);
		}
	};

	private String name;
	private double factor;
	private GeoCluster[] entries;

	InternalGeoClusterFacet() {

	}

	public InternalGeoClusterFacet(String name, double factor, GeoCluster[] entries) {
		this.name = name;
		this.factor = factor;
		this.entries = entries;
	}

	@Override
	public String name() {
		return this.name;
	}

	@Override
	public String getName() {
		return name();
	}

	@Override
	public double getFactor() {
		return factor;
	}

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public String getType() {
		return type();
	}

	@Override
	public List<GeoCluster> entries() {
		return ImmutableList.copyOf(entries);
	}

	@Override
	public List<GeoCluster> getEntries() {
		return entries();
	}

	@Override
	public Iterator<GeoCluster> iterator() {
		return entries().iterator();
	}

	@Override
	public String streamType() {
		return STREAM_TYPE;
	}

	public static InternalGeoClusterFacet readGeoClusterFacet(StreamInput in) throws IOException {
		InternalGeoClusterFacet facet = new InternalGeoClusterFacet();
		facet.readFrom(in);
		return facet;
	}

	@Override
	public void readFrom(StreamInput in) throws IOException {
		name = in.readString();
		factor = in.readDouble();
		entries = new GeoCluster[in.readVInt()];
		for (int i = 0; i < entries.length; ++i) {
			entries[i] = GeoCluster.readFrom(in);
		}
	}

	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeString(name);
		out.writeDouble(factor);
		out.writeVInt(entries.length);
		for (GeoCluster entry : entries) {
			entry.writeTo(out);
		}
	}

	private interface Fields {

		final XContentBuilderString _TYPE = new XContentBuilderString("_type");
		final XContentBuilderString FACTOR = new XContentBuilderString("factor");
		final XContentBuilderString CLUSTERS = new XContentBuilderString("clusters");
		final XContentBuilderString TOTAL = new XContentBuilderString("total");
		final XContentBuilderString CENTER = new XContentBuilderString("center");
		final XContentBuilderString TOP_LEFT = new XContentBuilderString("top_left");
		final XContentBuilderString BOTTOM_RIGHT = new XContentBuilderString("bottom_right");
		final XContentBuilderString LAT = new XContentBuilderString("lat");
		final XContentBuilderString LON = new XContentBuilderString("lon");
	}

	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(name);
		builder.field(Fields._TYPE, GeoClusterFacet.TYPE);
		builder.field(Fields.FACTOR, factor);
		builder.startArray(Fields.CLUSTERS);
		for (GeoCluster entry : entries) {
			toXContent(entry, builder);
		}
		builder.endArray();
		builder.endObject();
		return builder;
	}

	private static void toXContent(GeoCluster entry, XContentBuilder builder) throws IOException {
		builder.startObject();
		builder.field(Fields.TOTAL, entry.size());
		toXContent(entry.center(), Fields.CENTER, builder);
		if (entry.size() > 1) {
			toXContent(entry.bounds().topLeft(), Fields.TOP_LEFT, builder);
			toXContent(entry.bounds().bottomRight(), Fields.BOTTOM_RIGHT, builder);
		}
		builder.endObject();
	}

	private static void toXContent(GeoPoint point, XContentBuilderString field, XContentBuilder builder) throws IOException {
		builder.startObject(field);
		builder.field(Fields.LAT, point.getLat());
		builder.field(Fields.LON, point.getLon());
		builder.endObject();
	}
}
