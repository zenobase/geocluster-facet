package com.zenobase.search.facet.geocluster;

import static com.zenobase.search.facet.geocluster.test.Places.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;

import org.elasticsearch.common.io.stream.BytesStreamInput;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.mapper.geo.GeoPoint;
import org.testng.annotations.Test;

public class GeoPointsTests {

	@Test
	public void testReadFromWriteTo() throws IOException {
		BytesStreamOutput out = new BytesStreamOutput();
		GeoPoints.writeTo(LAS_VEGAS, out);
		BytesStreamInput in = new BytesStreamInput(out.bytes());
		GeoPoint point = GeoPoints.readFrom(in);
		assertThat("Latitude", point.lat(), equalTo(LAS_VEGAS.lat()));
		assertThat("Longitude", point.lon(), equalTo(LAS_VEGAS.lon()));
	}

	@Test
	public void testDistance() throws IOException {
		assertThat("Distance (mi)", GeoPoints.distance(LAS_VEGAS, SAN_DIEGO, DistanceUnit.MILES), closeTo(250.0, 5.0));
	}
}
