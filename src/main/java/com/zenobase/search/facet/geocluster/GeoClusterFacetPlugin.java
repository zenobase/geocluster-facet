package com.zenobase.search.facet.geocluster;

import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.search.facet.FacetModule;

public class GeoClusterFacetPlugin extends AbstractPlugin {

	@Override
	public String name() {
		return "geocluster-facet";
	}

	@Override
	public String description() {
		return "Facet for clustering geo points";
	}

	public void onModule(FacetModule module) {
		module.addFacetProcessor(GeoClusterFacetParser.class);
		InternalGeoClusterFacet.registerStreams();
	}
}
