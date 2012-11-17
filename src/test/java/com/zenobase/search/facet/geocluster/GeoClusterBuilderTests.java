package com.zenobase.search.facet.geocluster;

import static com.zenobase.search.facet.geocluster.test.Places.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.elasticsearch.index.mapper.geo.GeoPoint;
import org.testng.annotations.Test;

public class GeoClusterBuilderTests {

	@Test
	public void testClusterNone() {

		GeoClusterBuilder builder = new GeoClusterBuilder(0.0);
		assertThat(builder.build().size(), equalTo(0));

		builder.add(DENVER);
		assertThat("Cluster after adding Denver", builder.build(), hasItems(
			new GeoCluster(1, DENVER, new GeoBoundingBox(DENVER))));

		builder.add(DENVER);
		assertThat("Cluster after adding Denver again", builder.build(), hasItems(
			new GeoCluster(2, DENVER, new GeoBoundingBox(DENVER))));

		builder.add(SAN_DIEGO);
		assertThat("Cluster after adding San Diego", builder.build(), hasItems(
			new GeoCluster(2, DENVER, new GeoBoundingBox(DENVER)),
			new GeoCluster(1, SAN_DIEGO, new GeoBoundingBox(SAN_DIEGO))));

		builder.add(LAS_VEGAS);
		assertThat("Cluster after adding Las Vegas", builder.build(), hasItems(
			new GeoCluster(2, DENVER, new GeoBoundingBox(DENVER)),
			new GeoCluster(1, SAN_DIEGO, new GeoBoundingBox(SAN_DIEGO)),
			new GeoCluster(1, LAS_VEGAS, new GeoBoundingBox(LAS_VEGAS))));
	}

	@Test
	public void testClusterSome() {

		GeoClusterBuilder builder = new GeoClusterBuilder(0.5);
		assertThat(builder.build().size(), equalTo(0));

		builder.add(DENVER);
		assertThat("Cluster after adding Denver", builder.build(), hasItems(
			new GeoCluster(1, DENVER, new GeoBoundingBox(DENVER))));

		builder.add(DENVER);
		assertThat("Cluster after adding Denver again", builder.build(), hasItems(
			new GeoCluster(2, DENVER, new GeoBoundingBox(DENVER))));

		builder.add(SAN_DIEGO);
		assertThat("Cluster after adding San Diego", builder.build(), hasItems(
			new GeoCluster(2, DENVER, new GeoBoundingBox(DENVER)),
			new GeoCluster(1, SAN_DIEGO, new GeoBoundingBox(SAN_DIEGO))));

		builder.add(LAS_VEGAS);
		assertThat("Cluster after adding Las Vegas", builder.build(), hasItems(
			new GeoCluster(2, DENVER, new GeoBoundingBox(DENVER)),
			new GeoCluster(2, new GeoPoint(34.4500, -116.1500), new GeoBoundingBox(SAN_DIEGO).extend(LAS_VEGAS))));
	}

	@Test
	public void testClusterAll() {

		GeoClusterBuilder builder = new GeoClusterBuilder(1.0);
		assertThat(builder.build().size(), equalTo(0));

		builder.add(DENVER);
		assertThat("Cluster after adding Denver", builder.build(), hasItems(
			new GeoCluster(1, DENVER, new GeoBoundingBox(DENVER))));

		builder.add(DENVER);
		assertThat("Cluster after adding Denver again", builder.build(), hasItems(
			new GeoCluster(2, DENVER, new GeoBoundingBox(DENVER))));

		builder.add(SAN_DIEGO);
		assertThat("Cluster after adding San Diego", builder.build(), hasItems(
			new GeoCluster(3, new GeoPoint(37.4400, -108.9567), new GeoBoundingBox(DENVER).extend(SAN_DIEGO))));

		builder.add(LAS_VEGAS);
		assertThat("Cluster after adding Las Vegas", builder.build(), hasItems(
			new GeoCluster(4, new GeoPoint(37.1000, -110.5100), new GeoBoundingBox(DENVER).extend(SAN_DIEGO).extend(LAS_VEGAS))));
	}
}
