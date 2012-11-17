package com.zenobase.search.facet.geocluster;

import java.util.List;

import org.elasticsearch.search.facet.Facet;

public interface GeoClusterFacet extends Facet, Iterable<GeoCluster> {

	/**
	 * The type of the filter facet.
	 */
	public String TYPE = "geo_cluster";

	/**
	 * A value that is used to control the granularity of the clustering.
	 */
	double getFactor();

	/**
	 * A list of geo clusters.
	 */
	List<GeoCluster> entries();

	/**
	 * A list of geo clusters.
	 */
	List<GeoCluster> getEntries();
}
