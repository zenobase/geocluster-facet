package com.zenobase.search.facet.geocluster;

import static com.zenobase.search.facet.geocluster.test.Places.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.elasticsearch.common.unit.DistanceUnit;
import org.testng.annotations.Test;

public class GeoBoundingBoxTests {

	@Test
	public void testSize() {
		assertThat("Size of Denver", new GeoBoundingBox(DENVER, DENVER).size(DistanceUnit.KILOMETERS), equalTo(0.0));
		assertThat("Size of Colorado", COLORADO.size(DistanceUnit.KILOMETERS), greaterThan(750.0));
	}

	@Test
	public void testContains() {
		assertThat("Top left corner is in bounds", COLORADO.contains(COLORADO.topLeft()), is(true));
		assertThat("Bottom right corner is in bounds", COLORADO.contains(COLORADO.bottomRight()), is(true));
		assertThat("Denver is in Colorado", COLORADO.contains(DENVER), is(true));
		assertThat("Las Vegas is in Colorado", COLORADO.contains(LAS_VEGAS), is(false));
	}

	@Test
	public void testExtend() {
		GeoBoundingBox southwest = COLORADO.extend(SAN_DIEGO);
		assertThat("Denver is in the SW", southwest.contains(DENVER), is(true));
		assertThat("Las Vegas is in the SW", southwest.contains(LAS_VEGAS), is(true));
	}
}
