package TwitterGatherDataFollowers.userRyersonU;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import TwitterGatherDataFollowers.userRyersonU.Point;
 
public class Cluster {
	
	public List<Point> points;
	public Point centroid;
	public int id;
	public LinkedHashMap<String,Double> allTermsTfidf;
	
	//Creates a new Cluster
	public Cluster(int id) {
		this.id = id;
		this.points = new ArrayList<Point>();
		this.centroid = null;
	}
 
	public List getPoints() {
		return points;
	}
	
	public void addPoint(Point point) {
		points.add(point);
	}
 
	public void setPoints(List points) {
		this.points = points;
	}
 
	public Point getCentroid() {
		return centroid;
	}
 
	public void setCentroid(Point centroid) {
		this.centroid = centroid;
	}
 
	public int getId() {
		return id;
	}
	
	public void clear() {
		points.clear();
	}
	
	public void plotCluster() {
		System.out.println("[Cluster: " + id+"]");
		System.out.println("[Centroid: " + centroid + "]");
		System.out.println("[Points: "+points.size());
		for(Point p : points) {
			System.out.println(p);
		}
		System.out.println("]");
	}
 
}