package com.zenobase.search.facet.geocluster;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.common.collect.Iterables;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.FacetCollector;
import org.elasticsearch.search.facet.FacetProcessor;
import org.elasticsearch.search.internal.SearchContext;

public class GeoClusterFacetProcessor extends AbstractComponent implements FacetProcessor {

	@Inject
	public GeoClusterFacetProcessor(Settings settings) {
		super(settings);
		InternalGeoClusterFacet.registerStreams();
	}

	@Override
	public String[] types() {
		return new String[] { GeoClusterFacet.TYPE, InternalGeoClusterFacet.TYPE };
	}

	@Override
	public FacetCollector parse(String facetName, XContentParser parser, SearchContext context) throws IOException {

		String fieldName = null;
		double factor = 0.1;

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
			} else if (token.isValue()) {
				if ("field".equals(currentFieldName)) {
					fieldName = parser.text();
				} else if ("factor".equals(currentFieldName)) {
					factor = parser.doubleValue();
				}
			}
		}

		return new GeoClusterFacetCollector(facetName, fieldName, factor, context);
	}

	@Override
	public Facet reduce(String name, List<Facet> facets) {
		GeoClusterFacet facet = (GeoClusterFacet) Iterables.get(facets, 0);
		return reduce(name, facet.getFactor(), flatMap(facets));
	}

	private Iterable<GeoCluster> flatMap(Iterable<Facet> facets) {
		List<GeoCluster> entries = Lists.newArrayList();
		for (Facet facet : facets) {
			entries.addAll(((GeoClusterFacet) facet).entries());
		}
		return entries;
	}

	private Facet reduce(String name, double factor, Iterable<GeoCluster> entries) {
		GeoClusterReducer reducer = new GeoClusterReducer(factor);
		List<GeoCluster> reduced = reducer.reduce(entries); // TODO: return array?
		return new InternalGeoClusterFacet(name, factor, reduced.toArray(new GeoCluster[0]));
	}
}
