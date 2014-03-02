package com.zenobase.search.facet.geocluster;

import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.unit.DistanceUnit;

public class GeoClusterReducer {

	private final DistanceUnit unit = DistanceUnit.KILOMETERS;
	private final double factor;

	public GeoClusterReducer(double factor) {
		this.factor = factor;
	}

	public List<GeoCluster> reduce(Iterable<GeoCluster> clusters) {
		GeoBoundingBox bounds = getBounds(clusters);
		double maxDistance = bounds != null ? factor * bounds.size(unit) : 0.0;
		List<GeoCluster> reduced = Lists.newLinkedList(clusters);
		REDUCE: while (true) {
			for (int i = 0; i < reduced.size(); ++i) {
				for (int j = i + 1; j < reduced.size(); ++j) {
					GeoCluster a = reduced.get(i);
					GeoCluster b = reduced.get(j);
					if (a.center().equals(b.center()) || maxDistance > 0.0 && GeoPoints.distance(a.center(), b.center(), unit) <= maxDistance) {
						reduced.remove(a);
						reduced.remove(b);
						reduced.add(a.merge(b));
						continue REDUCE;
					}
				}
			}
			break;
		}
		return reduced;
	}

	private static GeoBoundingBox getBounds(Iterable<GeoCluster> clusters) {
		GeoBoundingBox bounds = null;
		for (GeoCluster cluster : clusters) {
			if (bounds != null) {
				bounds = bounds.extend(cluster.bounds());
			} else {
				bounds = cluster.bounds();
			}
		}
		return bounds;
	}
}
