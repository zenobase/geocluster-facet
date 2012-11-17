package com.zenobase.search.facet.geocluster;

import org.elasticsearch.common.inject.Module;
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

	@Override
	public void processModule(Module module) {
		if (module instanceof FacetModule) {
			((FacetModule) module).addFacetProcessor(GeoClusterFacetProcessor.class);
			InternalGeoClusterFacet.registerStreams();
		}
	}
}
