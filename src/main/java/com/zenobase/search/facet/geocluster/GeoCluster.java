package com.zenobase.search.facet.geocluster;

import java.io.IOException;
import java.util.Arrays;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

public class GeoCluster {

	private int size;
	private GeoPoint center;
	private GeoBoundingBox bounds;
	private boolean calcPolygon = false;
	private GeoPolygon polygon = null;

	public GeoCluster(GeoPoint point) {
		this(1, point, new GeoBoundingBox(point), false);
	}

	public GeoCluster(GeoPoint point, boolean calcPolygon) {
		this(1, point, new GeoBoundingBox(point), calcPolygon);
	}

	public GeoCluster(int size, GeoPoint center, GeoBoundingBox bounds) {
		this(size, center, bounds, false);
	}
	
	public GeoCluster(int size, GeoPoint center, GeoBoundingBox bounds, boolean calcPolygon) {
		this(size, center, bounds, null);
		this.calcPolygon = calcPolygon;
		if (this.calcPolygon) {
			this.polygon = new GeoPolygon(bounds);
		}
	}
	
	public GeoCluster(int size, GeoPoint center, GeoBoundingBox bounds, GeoPolygon polygon) {
		this.size = size;
		this.center = center;
		this.bounds = bounds;
		this.polygon = polygon;
		this.calcPolygon = polygon != null;
	}

	public void add(GeoPoint point) {
		++size;
		center = mean(center, size - 1, point, 1);
		bounds = bounds.extend(point);
		if (calcPolygon) {
			polygon.add(point);
		}
	}

	public GeoCluster merge(GeoCluster that) {
		int size = this.size + that.size();
		GeoPoint center = mean(this.center, size - that.size(), that.center(), that.size());
		GeoBoundingBox bounds = this.bounds.extend(that.bounds());
		if (calcPolygon) {
			return new GeoCluster(size, center, bounds, polygon.merge(that.getPolygon()));
		}
		return new GeoCluster(size, center, bounds, false);
	}

	private static GeoPoint mean(GeoPoint left, int leftWeight, GeoPoint right, int rightWeight) {
		double lat = (left.getLat() * leftWeight + right.getLat() * rightWeight) / (leftWeight + rightWeight);
		double lon = (left.getLon() * leftWeight + right.getLon() * rightWeight) / (leftWeight + rightWeight);
		return new GeoPoint(lat, lon);
	}

	public int size() {
		return size;
	}

	public GeoPoint center() {
		return center;
	}

	public GeoBoundingBox bounds() {
		return bounds;
	}

	public GeoPolygon polygon() {
		return polygon;
	}

	public static GeoCluster readFrom(StreamInput in) throws IOException {
		int size = in.readVInt();
		GeoPoint center = GeoPoints.readFrom(in);
		GeoBoundingBox bounds = size > 1
			? GeoBoundingBox.readFrom(in)
			: new GeoBoundingBox(center, center);
		if (in.readBoolean()) {	
			GeoPolygon p = GeoPolygon.readFrom(in);
			return new GeoCluster(size, center, bounds, p);
		} else {
			return new GeoCluster(size, center, bounds);
		}
	}

	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(size);
		GeoPoints.writeTo(center, out);
		if (size > 1) {
			bounds.writeTo(out);
		}
		if (calcPolygon && polygon != null) {
			out.writeBoolean(true);
			polygon.writeTo(out);
		} else {
			out.writeBoolean(false);
		}
	}

	@Override
	public boolean equals(Object that) {
		return that instanceof GeoCluster &&
			equals((GeoCluster) that);
	}

	private boolean equals(GeoCluster that) {
		return size == that.size() &&
			GeoPoints.equals(center, that.center()) &&
			bounds.equals(that.bounds());
	}

	@Override
	public int hashCode() {
		return hashCode(size, center.toString(), bounds);
	}

	private static int hashCode(Object... objects) {
		return Arrays.hashCode(objects);
	}

	@Override
	public String toString() {
		return String.format("%s (%d)", GeoPoints.toString(center), size);
	}
	
	public boolean isCalcPolygon() {
		return calcPolygon;
	}

	public GeoPolygon getPolygon() {
		return polygon;
	}

}
