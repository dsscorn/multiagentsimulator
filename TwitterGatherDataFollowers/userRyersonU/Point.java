package TwitterGatherDataFollowers.userRyersonU;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
 
public class Point {
 
    private double x = 0;
    private double y = 0;
    private int cluster_number = 0;
    private Long tweetId;
    private LinkedHashMap<String,Double> tfidf;
    
    public Point(long tweetId,LinkedHashMap<String,Double> tfidf)
    {
    	this.setTweetId(tweetId);
    	this.setTfidf(tfidf);
    }
    
    public void setTweetId(long tweetId)
    {
    	this.tweetId = tweetId;
    }
    
    public long getTweetId()
    {
    	return this.tweetId;
    }
    
    public void setTfidf(LinkedHashMap<String,Double> tfidf)
    {
    	this.tfidf = tfidf;
    }
    
    public LinkedHashMap<String,Double> getTfidf()
    {
    	return this.tfidf;
    }
        
    public void setCluster(int n) {
        this.cluster_number = n;
    }
    
    public int getCluster() {
        return this.cluster_number;
    }
  /*  
    //Calculates the distance between two points.
    protected static double distance(Point p, Point centroid) {
        return Math.sqrt(Math.pow((centroid.getY() - p.getY()), 2) + Math.pow((centroid.getX() - p.getX()), 2));
    }
    */
    //Calculates the cosine similarity between two points
    protected static double cosSimDistance(Point p, Point centroid) {
    	double cosSim = 0.0;
    	LinkedHashMap<String, Double> pointTfidf = p.getTfidf();
    	LinkedHashMap<String, Double> centroidTfidf = centroid.getTfidf();
    	for (String term : pointTfidf.keySet())
    	{
    		cosSim+= pointTfidf.get(term) * centroidTfidf.get(term);
    	}
    
    	return cosSim;
    }
    
    public String toString() {
    	return "("+tweetId+","+tfidf+")";
    }
}