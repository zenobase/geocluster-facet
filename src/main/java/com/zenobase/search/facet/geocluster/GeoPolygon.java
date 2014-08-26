/**
 * 
 */
package com.zenobase.search.facet.geocluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

/**
 * @author knicholls
 *
 */
public class GeoPolygon {

	private List<GeoPoint> points;
	
	public GeoPolygon(GeoPoint point) {
		points = new ArrayList<GeoPoint>();
		points.add(point);
	}
	
	public GeoPolygon(GeoBoundingBox box) {
		points = new ArrayList<GeoPoint>();
		points.add(box.topLeft());
		if (!box.topLeft().equals(box.bottomRight())) {
			points.add(box.bottomRight());
			// top right
			points.add(new GeoPoint(box.topLeft().lat(), box.bottomRight().lon()));
			// bottom left
			points.add(new GeoPoint(box.bottomRight().lat(), box.topLeft().lon()));
		}
	}
	
	public GeoPolygon(List<GeoPoint> points) {
		this.points = points;
		if (this.points.size() >= 3)
			this.points = calculateConvexHull();
	}
	
	public GeoPolygon merge(GeoPolygon that) {
		this.points.addAll(that.getPoints());
		points = calculateConvexHull();
		return this;
	}
	
	/**
	 * @param point
	 */
	public void add(GeoPoint point) {
		// need at least three points to make a polygon
		if (points.size() < 3) {
			points.add(point);
			return;
		}
		for (GeoPoint p : points) {
			if (p.equals(point)) {
				return;
			}
		}
		// check if point is inside current polygon
		if (contains(point)) {
			// nothing to do
			return;
		}
		// if not, recalculate polygon
		points.add(point);
		points = calculateConvexHull();
	}
	
	/**
	 * @param point
	 * @return
	 */
	public boolean contains(GeoPoint point) {
		return windingNumber(point, points) != 0;
	}	
	
	public static GeoPolygon readFrom(StreamInput in) throws IOException {
		int edges = in.readVInt();
		List<GeoPoint> p = new ArrayList<GeoPoint>(edges);
		for (int i = 0; i < edges; i++) {
			p.add(GeoPoints.readFrom(in));
		}
		return new GeoPolygon(p);
	}

	public void writeTo(StreamOutput out) throws IOException {
		int edges = points.size();
		out.writeVInt(edges);
		for (int i = 0; i < edges; i++) {
			GeoPoints.writeTo(points.get(i), out);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(points.size()).append(" points: ");
		for (GeoPoint p : points) {
			buf.append("[").append(GeoPoints.toString(p)).append("] ");
		}
		return buf.toString();
	}
	
	/*
	 * Point in polygon winding number algorithm
	 * adapted from http://geomalgorithms.com/a03-_inclusion.html
	 * Copyright 2000 softSurfer, 2012 Dan Sunday
	 * This code may be freely used and modified for any purpose
	 * providing that this copyright notice is included with it.
	 * SoftSurfer makes no warranty for this code, and cannot be held
	 * liable for any real or imagined damage resulting from its use.
	 * Users of this code must verify correctness for their application.
	 */
	
	/**
	 * tests if a point is Left|On|Right of an infinite line.
	 * See: Algorithm 1 "Area of Triangles and Polygons"
	 * @param p0
	 * @param p1
	 * @param p2
	 * @return >0 for P2 left of the line through P0 and P1
          =0 for P2  on the line
          <0 for P2  right of the line
	 */
	private int isLeft(GeoPoint p0, GeoPoint p1, GeoPoint p2) {
		return (int)(( (p1.getLon() - p0.getLon()) * (p2.getLat() - p0.getLat())
          - (p2.getLon() -  p0.getLon()) * (p1.getLat() - p0.getLat())));
	}
	
	/**
	 * crossing number test for a point in a polygon
	 * This code is patterned after [Franklin, 2000]
	 * @param p a point
	 * @param v vertex points of a polygon V[n+1] with V[n]=V[0]
	 * @param n 0 = outside, 1 = inside
	 * @return
	 */
	private int crossingNumber(GeoPoint p, List<GeoPoint> v) {
		int cn = 0;    // the  crossing number counter

		// loop through all edges of the polygon
		int n = v.size()-1;
		for (int i=0; i<n; i++) {    // edge from V[i]  to V[i+1]
			if (((v.get(i).getLat() <= p.getLat()) && (v.get(i+1).getLat() > p.getLat()))     // an upward crossing
					|| ((v.get(i).getLat() > p.getLat()) && (v.get(i+1).getLat() <=  p.getLat()))) { // a downward crossing
				// compute  the actual edge-ray intersect x-coordinate
				double vt = (double)(p.getLat() - v.get(i).getLat()) / (v.get(i+1).getLat() - v.get(i).getLat());
				if (p.getLon() <  v.get(i).getLon() + vt * (v.get(i+1).getLon() - v.get(i).getLon())) // P.x < intersect
					++cn;   // a valid crossing of y=P.y right of P.x
			}
		}
		return (cn % 2);    // 0 if even (out), and 1 if  odd (in)
	}

	/**
	 * winding number test for a point in a polygon
	 * @param p a point
	 * @param v vertex points of a polygon V[n+1] with V[n]=V[0]
	 * @param n
	 * @return the winding number (=0 only when P is outside)
	 */
	private int windingNumber(GeoPoint p, List<GeoPoint> v) {
		int wn = 0;    // the  winding number counter

		// loop through all edges of the polygon
		int n = v.size()-1;
		for (int i=0; i<n; i++) {   // edge from V[i] to  V[i+1]
			if (v.get(i).getLat() <= p.getLat()) {          // start y <= P.y
				if (v.get(i+1).getLat()  > p.getLat())      // an upward crossing
					if (isLeft( v.get(i), v.get(i+1), p) > 0)  // P left of  edge
						++wn;            // have  a valid up intersect
			} else {                        // start y > P.y (no test needed)
				if (v.get(i+1).getLat()  <= p.getLat())     // a downward crossing
					if (isLeft( v.get(i), v.get(i+1), p) < 0)  // P right of  edge
						--wn;            // have  a valid down intersect
			}
		}
		return wn;
	}

	/*
	 *  QuickHull - calculate the convex hull of a list of points
	 *  adapted from https://code.google.com/p/convex-hull/source/browse/Convex+Hull/src/algorithms/FastConvexHull.java?r=4
	 *  http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
	 */

	protected List<GeoPoint> calculateConvexHull()  {
		Collections.sort(points, new XCompare());
		int n = points.size();

		GeoPoint[] lUpper = new GeoPoint[n];

		lUpper[0] = points.get(0);
		lUpper[1] = points.get(1);

		int lUpperSize = 2;

		for (int i = 2; i < n; i++) {
			lUpper[lUpperSize] = points.get(i);
			lUpperSize++;

			while (lUpperSize > 2 && !rightTurn(lUpper[lUpperSize - 3], lUpper[lUpperSize - 2], lUpper[lUpperSize - 1])) {
				// Remove the middle point of the three last
				lUpper[lUpperSize - 2] = lUpper[lUpperSize - 1];
				lUpperSize--;
			}
		}

		GeoPoint[] lLower = new GeoPoint[n];

		lLower[0] = points.get(n - 1);
		lLower[1] = points.get(n - 2);

		int lLowerSize = 2;

		for (int i = n - 3; i >= 0; i--) {
			lLower[lLowerSize] = points.get(i);
			lLowerSize++;

			while (lLowerSize > 2 && !rightTurn(lLower[lLowerSize - 3], lLower[lLowerSize - 2], lLower[lLowerSize - 1])) {
				// Remove the middle point of the three last
				lLower[lLowerSize - 2] = lLower[lLowerSize - 1];
				lLowerSize--;
			}
		}

		List<GeoPoint> result = new ArrayList<GeoPoint>();

		for (int i = 0; i < lUpperSize; i++) {
			result.add(lUpper[i]);
		}

		for (int i = 1; i < lLowerSize - 1; i++) {
			result.add(lLower[i]);
		}

		return result;
	}
    
    private boolean rightTurn(GeoPoint a, GeoPoint b, GeoPoint c) {
            return (b.getLon() - a.getLon())*(c.getLat() - a.getLat()) - (b.getLat() - a.getLat())*(c.getLon() - a.getLon()) > 0;
    }

    private class XCompare implements Comparator<GeoPoint> {
    	public int compare(GeoPoint o1, GeoPoint o2) {
    		return (new Double(o1.getLat())).compareTo(new Double(o2.getLat()));
    	}
    }

	public List<GeoPoint> getPoints() {
		return points;
	}
	
	public int getEdgeCount() {
		return points.size();
	}
}
