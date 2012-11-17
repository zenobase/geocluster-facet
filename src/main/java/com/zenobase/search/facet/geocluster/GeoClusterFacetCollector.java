package com.zenobase.search.facet.geocluster;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.elasticsearch.index.cache.field.data.FieldDataCache;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.geo.GeoPoint;
import org.elasticsearch.index.mapper.geo.GeoPointFieldData;
import org.elasticsearch.index.mapper.geo.GeoPointFieldDataType;
import org.elasticsearch.search.facet.AbstractFacetCollector;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.FacetPhaseExecutionException;
import org.elasticsearch.search.internal.SearchContext;

public class GeoClusterFacetCollector extends AbstractFacetCollector {

	private final FieldDataCache fieldDataCache;
	private final double factor;
	private final GeoClusterBuilder builder;
    private final GeoPointFieldData.ValueInDocProc aggregator;
    private final String indexFieldName;
    private GeoPointFieldData fieldData;

	public GeoClusterFacetCollector(String facetName, String fieldName, double factor, SearchContext context) {

		super(facetName);
		this.fieldDataCache = context.fieldDataCache();
		this.factor = factor;
		this.builder = new GeoClusterBuilder(factor);
		this.aggregator = new Aggregator(builder);

		MapperService.SmartNameFieldMappers smartMappers = context.mapperService().smartName(fieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			throw new FacetPhaseExecutionException(facetName, "No mapping found for field [" + fieldName + "]");
		}
		if (smartMappers.mapper().fieldDataType() != GeoPointFieldDataType.TYPE) {
			throw new FacetPhaseExecutionException(facetName, "field [" + fieldName + "] is not a geo_point field");
		}
		if (factor < 0.0 || factor > 1.0) {
			throw new FacetPhaseExecutionException(facetName, "value [" + factor + "] is not in range [0.1, 1.0]");
		}

		// add type filter if there is exact doc mapper associated with it
		if (smartMappers.explicitTypeInNameWithDocMapper()) {
			setFilter(context.filterCache().cache(smartMappers.docMapper().typeFilter()));
		}

		this.indexFieldName = smartMappers.mapper().names().indexName();
	}

	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		 fieldData = (GeoPointFieldData) fieldDataCache.cache(GeoPointFieldDataType.TYPE, reader, indexFieldName);
	}

	@Override
	protected void doCollect(int doc) throws IOException {
		fieldData.forEachValueInDoc(doc, aggregator);
	}

	@Override
	public Facet facet() {
		return new InternalGeoClusterFacet(facetName, factor, builder.build().toArray(new GeoCluster[0])); // TODO: build array directly
	}

	private static class Aggregator implements GeoPointFieldData.ValueInDocProc {

		private final GeoClusterBuilder builder;

		public Aggregator(GeoClusterBuilder builder) {
			this.builder = builder;
		}

		@Override
		public void onValue(int docId, double lat, double lon) {
			builder.add(new GeoPoint(lat, lon));
		}
	}
}
