package com.zenobase.search.facet.geocluster;

import java.util.List;

import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;

public class GeoClusterBuilder {

	private final DistanceUnit unit = DistanceUnit.KILOMETERS;
	private final double factor;
	private final List<GeoCluster> clusters = Lists.newArrayList();
	private GeoBoundingBox bounds;
	private double maxDistance = 0.0;

	public GeoClusterBuilder(double factor) {
		this.factor = factor;
	}

	public GeoClusterBuilder add(GeoPoint point) {
		if (bounds == null) {
			bounds = new GeoBoundingBox(point);
		} else if (!bounds.contains(point)) {
			bounds = bounds.extend(point);
			maxDistance = factor * bounds.size(unit);
		}
		GeoCluster nearest = null;
		for (GeoCluster cluster : clusters) {
			if (cluster.center().equals(point)) {
				nearest = cluster;
				break;
			}
			if (maxDistance > 0.0) {
				double distance = GeoPoints.distance(cluster.center(), point, unit);
				if (distance <= maxDistance && cluster.bounds().extend(point).size(unit) <= maxDistance) {
					nearest = cluster;
				}
			}
		}
		if (nearest == null) {
			nearest = new GeoCluster(point);
			clusters.add(nearest);
		} else {
			nearest.add(point);
		}
		return this;
	}

	public ImmutableList<GeoCluster> build() {
		return ImmutableList.copyOf(clusters);
	}
}
