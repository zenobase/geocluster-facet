package com.zenobase.search.facet.geocluster;

import java.io.IOException;
import java.util.Arrays;

import org.elasticsearch.common.Preconditions;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.unit.DistanceUnit;

public class GeoBoundingBox {

	private final GeoPoint topLeft, bottomRight;

	public GeoBoundingBox(GeoPoint point) {
		this(point, point);
	}

	public GeoBoundingBox(GeoPoint topLeft, GeoPoint bottomRight) {
		Preconditions.checkArgument(topLeft.getLat() >= bottomRight.getLat());
		Preconditions.checkArgument(topLeft.getLon() <= bottomRight.getLon());
		this.topLeft = topLeft;
		this.bottomRight = bottomRight;
	}

	public GeoPoint topLeft() {
		return topLeft;
	}

	public GeoPoint bottomRight() {
		return bottomRight;
	}

	public boolean contains(GeoPoint point) {
		return point.getLat() <= topLeft.getLat() && point.getLat() >= bottomRight.getLat() &&
			point.getLon() >= topLeft.getLon() && point.getLon() <= bottomRight.getLon();
	}

	public GeoBoundingBox extend(GeoPoint point) {
		return extend(point, point);
	}

	public GeoBoundingBox extend(GeoBoundingBox bounds) {
		return extend(bounds.topLeft(), bounds.bottomRight());
	}

	private GeoBoundingBox extend(GeoPoint topLeft, GeoPoint bottomRight) {
		return contains(topLeft) && contains(bottomRight) ? this : new GeoBoundingBox(
			new GeoPoint(Math.max(topLeft().getLat(), topLeft.getLat()), Math.min(topLeft().getLon(), topLeft.getLon())),
			new GeoPoint(Math.min(bottomRight().getLat(), bottomRight.getLat()), Math.max(bottomRight().getLon(), bottomRight.getLon())));
	}

	public double size(DistanceUnit unit) {
		return GeoPoints.distance(topLeft, bottomRight, unit);
	}

	public static GeoBoundingBox readFrom(StreamInput in) throws IOException {
		return new GeoBoundingBox(GeoPoints.readFrom(in), GeoPoints.readFrom(in));
	}

	public void writeTo(StreamOutput out) throws IOException {
		GeoPoints.writeTo(topLeft, out);
		GeoPoints.writeTo(bottomRight, out);
	}

	@Override
	public boolean equals(Object that) {
		return that instanceof GeoBoundingBox &&
			equals((GeoBoundingBox) that);
	}

	private boolean equals(GeoBoundingBox that) {
		return GeoPoints.equals(topLeft, that.topLeft()) &&
			GeoPoints.equals(bottomRight, that.bottomRight());
	}

	@Override
	public int hashCode() {
		return hashCode(topLeft.toString(), bottomRight.toString());
	}

	private static int hashCode(Object... objects) {
		return Arrays.hashCode(objects);
	}

	@Override
	public String toString() {
		return GeoPoints.toString(topLeft) + ".." + GeoPoints.toString(bottomRight);
	}
}
