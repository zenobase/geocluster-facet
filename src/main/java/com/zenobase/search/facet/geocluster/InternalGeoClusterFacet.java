package com.zenobase.search.facet.geocluster;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.bytes.HashedBytesArray;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.InternalFacet;

public class InternalGeoClusterFacet extends InternalFacet implements GeoClusterFacet {

	private static final BytesReference STREAM_TYPE = new HashedBytesArray(Strings.toUTF8Bytes("geoCluster"));

	private static InternalFacet.Stream STREAM = new Stream() {

		@Override
		public Facet readFacet(StreamInput in) throws IOException {
			return readGeoClusterFacet(in);
		}
	};

	public static void registerStreams() {
		Streams.registerStream(STREAM, STREAM_TYPE);
	}

	private double factor;
	private boolean calcPolygon;
	private List<GeoCluster> entries;

	InternalGeoClusterFacet() {

	}

	public InternalGeoClusterFacet(String name, double factor, boolean calcPolygon, List<GeoCluster> entries) {
		super(name);
		this.factor = factor;
		this.calcPolygon = calcPolygon;
		this.entries = entries;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public BytesReference streamType() {
		return STREAM_TYPE;
	}

	@Override
	public List<GeoCluster> getEntries() {
		return ImmutableList.copyOf(entries);
	}

	@Override
	public Iterator<GeoCluster> iterator() {
		return getEntries().iterator();
	}

	@Override
	public Facet reduce(ReduceContext context) {
		GeoClusterReducer reducer = new GeoClusterReducer(factor);
		List<GeoCluster> reduced = reducer.reduce(flatMap(context.facets()));
		return new InternalGeoClusterFacet(getName(), factor, calcPolygon, reduced);
	}

	private List<GeoCluster> flatMap(Iterable<Facet> facets) {
		List<GeoCluster> entries = Lists.newArrayList();
		for (Facet facet : facets) {
			entries.addAll(((GeoClusterFacet) facet).getEntries());
		}
		return entries;
	}

	public static InternalGeoClusterFacet readGeoClusterFacet(StreamInput in) throws IOException {
		InternalGeoClusterFacet facet = new InternalGeoClusterFacet();
		facet.readFrom(in);
		return facet;
	}

	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		factor = in.readDouble();
		calcPolygon = in.readBoolean();
		entries = Lists.newArrayList();
		for (int i = 0, max = in.readVInt(); i < max; ++i) {
			entries.add(GeoCluster.readFrom(in));
		}
	}

	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeDouble(factor);
		out.writeBoolean(calcPolygon);
		out.writeVInt(entries.size());
		for (GeoCluster entry : entries) {
			entry.writeTo(out);
		}
	}

	private interface Fields {

		final XContentBuilderString _TYPE = new XContentBuilderString("_type");
		final XContentBuilderString FACTOR = new XContentBuilderString("factor");
		final XContentBuilderString CALC_POLYGON = new XContentBuilderString("calc_polygon");
		final XContentBuilderString VERTICES = new XContentBuilderString("vertices");
		final XContentBuilderString CLUSTERS = new XContentBuilderString("clusters");
		final XContentBuilderString TOTAL = new XContentBuilderString("total");
		final XContentBuilderString CENTER = new XContentBuilderString("center");
		final XContentBuilderString TOP_LEFT = new XContentBuilderString("top_left");
		final XContentBuilderString BOTTOM_RIGHT = new XContentBuilderString("bottom_right");
		final XContentBuilderString LAT = new XContentBuilderString("lat");
		final XContentBuilderString LON = new XContentBuilderString("lon");
		final XContentBuilderString VERTEX = new XContentBuilderString("vertex");
	}

	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(getName());
		builder.field(Fields._TYPE, GeoClusterFacet.TYPE);
		builder.field(Fields.FACTOR, factor);
		builder.field(Fields.CALC_POLYGON, calcPolygon);
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
			if (entry.isCalcPolygon()) {
				List<GeoPoint> vertices = entry.getPolygon().getPoints();
				if (vertices.size() >= 3) {
					builder.startArray(Fields.VERTICES);
					for (GeoPoint p : vertices) {
						builder.startObject();
						toXContent(p, Fields.VERTEX, builder);
						builder.endObject();
					}
					builder.endArray();
				}
			}
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
