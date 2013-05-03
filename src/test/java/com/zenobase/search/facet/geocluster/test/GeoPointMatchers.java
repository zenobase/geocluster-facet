package com.zenobase.search.facet.geocluster.test;

import org.elasticsearch.common.geo.GeoPoint;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import com.zenobase.search.facet.geocluster.GeoPoints;

public class GeoPointMatchers {

	private GeoPointMatchers() {

	}

	public static TypeSafeMatcher<GeoPoint> closeTo(final GeoPoint expected) {
		return closeTo(expected, 0.001);
	}

	public static TypeSafeMatcher<GeoPoint> closeTo(final GeoPoint expected, final double error) {
		return new CustomTypeSafeMatcher<GeoPoint>("close to \"" + GeoPoints.toString(expected) + "\"") {
			@Override
			protected void describeMismatchSafely(GeoPoint item, Description description) {
				description.appendText("was ").appendValue(GeoPoints.toString(item));
			}
			@Override
			protected boolean matchesSafely(GeoPoint point) {
				return Matchers.closeTo(point.getLat(), error).matches(expected.getLat()) &&
					Matchers.closeTo(point.getLon(), error).matches(expected.getLon());
			}
		};
	}
}
