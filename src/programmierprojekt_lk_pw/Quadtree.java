package programmierprojekt_lk_pw;

public class Quadtree {
	final private double minLat;
	final private double maxLat;
	final private double minLong;
	final private double maxLong;
	
	private QuadtreeNode root;
	
	public Quadtree(double minLat, double maxLat, double minLong, double maxLong) {
		this.minLat = minLat;
		this.maxLat = maxLat;
		this.minLong = minLong;
		this.maxLong = maxLong;
		
		root = null;
	}
	
	public void insert(final double latitude, final double longitude, int node) {
		if (root == null) {
			root = new QuadtreeNode(minLat, maxLat, minLong, maxLong, latitude, longitude, node, 1);
		}else {
			root.insert(latitude, longitude, node);
		}
		return;
	}
	
	public DistNodePair getNearestNeighbor(final double latitude, final double longitude) {
		return root.getNearestNode(latitude, longitude);
	}
}
