package com.zenobase.search.facet.geocluster;

import static com.zenobase.search.facet.geocluster.test.GeoPointMatchers.closeTo;
import static com.zenobase.search.facet.geocluster.test.Places.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.io.stream.BytesStreamInput;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.testng.annotations.Test;

public class GeoClusterTests {

	@Test
	public void testReadFromWriteToSingleton() throws IOException {
		GeoCluster expected = new GeoCluster(DENVER);
		BytesStreamOutput out = new BytesStreamOutput();
		expected.writeTo(out);
		BytesStreamInput in = new BytesStreamInput(out.bytes());
		GeoCluster actual = GeoCluster.readFrom(in);
		assertThat("Center", actual.center(), closeTo(DENVER));
		assertThat("Bounds", actual.bounds(), equalTo(new GeoBoundingBox(DENVER)));
	}

	@Test
	public void testReadFromWriteTo() throws IOException {
		GeoCluster expected = new GeoCluster(42, DENVER, COLORADO);
		BytesStreamOutput out = new BytesStreamOutput();
		expected.writeTo(out);
		BytesStreamInput in = new BytesStreamInput(out.bytes());
		GeoCluster actual = GeoCluster.readFrom(in);
		assertThat("Center", actual.center(), closeTo(DENVER));
		assertThat("Bounds", actual.bounds(), equalTo(COLORADO));
	}

	@Test
	public void testGrow() {

		GeoCluster cluster = new GeoCluster(DENVER);
		assertThat("Cluster size after adding the first point", cluster.size(), equalTo(1));
		assertThat("Center after adding the first point", cluster.center(), closeTo(DENVER));
		assertThat("Top left corner after adding the first point", cluster.bounds().topLeft(), closeTo(DENVER));
		assertThat("Bottom right corner after adding the first point", cluster.bounds().bottomRight(), closeTo(DENVER));

		cluster.add(LAS_VEGAS);
		assertThat("Cluster size after adding a second point", cluster.size(), equalTo(2));
		assertThat("Center after adding a second point", cluster.center(), closeTo(new GeoPoint(37.915, -110.02)));
		assertThat("Top left corner after adding a second point", cluster.bounds().topLeft(), closeTo(new GeoPoint(DENVER.getLat(), LAS_VEGAS.getLon())));
		assertThat("Bottom right corner after adding a second point", cluster.bounds().bottomRight(), closeTo(new GeoPoint(LAS_VEGAS.getLat(), DENVER.getLon())));

		cluster.add(SAN_DIEGO);
		assertThat("Cluster size after adding a third point", cluster.size(), equalTo(3));
		assertThat("Center after adding a third point", cluster.center(), closeTo(new GeoPoint(36.217, -112.39)));
		assertThat("Top left corner after adding a third point", cluster.bounds().topLeft(), closeTo(new GeoPoint(DENVER.getLat(), SAN_DIEGO.getLon())));
		assertThat("Bottom right corner after adding a third point", cluster.bounds().bottomRight(), closeTo(new GeoPoint(SAN_DIEGO.getLat(), DENVER.getLon())));
	}

	@Test
	public void testMerge() {
		GeoCluster cluster = new GeoCluster(1, LAS_VEGAS, new GeoBoundingBox(LAS_VEGAS));
		GeoCluster merged = cluster.merge(new GeoCluster(2, SAN_DIEGO, new GeoBoundingBox(SAN_DIEGO)));
		assertThat(merged.size(), equalTo(3));
		assertThat(merged.center(), closeTo(new GeoPoint(33.9067, -116.4767)));
		assertThat(merged.bounds(), equalTo(new GeoBoundingBox(LAS_VEGAS).extend(SAN_DIEGO)));
	}

	@Test
	public void testCalcPolygon() {
		GeoCluster cluster = new GeoCluster(DENVER, true);
		cluster.add(LAS_VEGAS);
		cluster.add(SAN_DIEGO);
		assertThat(cluster.polygon(), notNullValue());
		assertThat(cluster.polygon().getEdgeCount(), equalTo(3));
	}

}
