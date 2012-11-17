package com.zenobase.search.facet.geocluster;

import java.io.IOException;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.mapper.geo.GeoPoint;
import org.elasticsearch.index.search.geo.GeoDistance;

public class GeoPoints {

	private GeoPoints() {

	}

	public static double distance(GeoPoint from, GeoPoint to, DistanceUnit unit) {
		return GeoDistance.ARC.calculate(from.getLat(), from.getLon(),
			to.getLat(), to.getLon(), unit);
	}

	public static GeoPoint readFrom(StreamInput in) throws IOException {
		return new GeoPoint(in.readDouble(), in.readDouble());
	}

	public static void writeTo(GeoPoint point, StreamOutput out) throws IOException {
		out.writeDouble(point.getLat());
		out.writeDouble(point.getLon());
	}

	public static boolean equals(GeoPoint left, GeoPoint right) {
		return toString(left).equals(toString(right));
	}

	public static String toString(GeoPoint point) {
		return String.format("%.4f,%.4f", point.getLat(), point.getLon());
	}
}
