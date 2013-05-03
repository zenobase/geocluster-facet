package com.zenobase.search.facet.geocluster;

import java.util.List;

import org.elasticsearch.search.facet.Facet;

public interface GeoClusterFacet extends Facet, Iterable<GeoCluster> {

	/**
	 * The type of the filter facet.
	 */
	public String TYPE = "geo_cluster";

	/**
	 * A list of geo clusters.
	 */
	List<GeoCluster> getEntries();
}
