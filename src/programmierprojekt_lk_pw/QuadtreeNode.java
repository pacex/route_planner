package programmierprojekt_lk_pw;

public class QuadtreeNode {

	final private double minLat;

	final private double minLong;

	final private int depth;

	final private double latSize;
	final private double longSize;

	private final double thisLat;
	private final double thisLong;
	private final int node;

	private final QuadtreeNode[][] children;
	private boolean isLeaf;

	public QuadtreeNode(final double minLat, final double maxLat, final double minLong, final double maxLong,
			final double thisLat, final double thisLong, final int node, final int depth) {
		this.minLat = minLat;

		this.minLong = minLong;

		this.depth = depth;

		this.thisLat = thisLat;
		this.thisLong = thisLong;
		this.node = node;

		latSize = maxLat - minLat;
		longSize = maxLong - minLong;

		children = new QuadtreeNode[2][2];
		children[0][0] = null;
		children[0][1] = null;
		children[1][0] = null;
		children[1][1] = null;
		isLeaf = true;
	}

	public void insert(final double latitude, final double longitude, final int node) {
		//Relative coordinates inside the area of the node (0 - 1)
		//Quadrant borders at 0.5
		final double latFrac = (latitude - minLat) / (latSize);
		final double longFrac = (longitude - minLong) / (longSize);

		if (latFrac < 0 || latFrac > 1 || longFrac < 0 || longFrac > 1) {
			System.err.println("Wrong insertion at depth = " + depth + "!");
			return;
		}

		// Determining correct child according to node relative coordinates
		int x = 0;
		int y = 0;
		if (latFrac >= 0.5) {
			x = 1;
		}
		if (longFrac >= 0.5) {
			y = 1;
		}
		if (children[x][y] == null) { // Determined child does not exist
			//Create new child node with half the width/height and position corresponding to quadrant position
			children[x][y] = new QuadtreeNode(minLat + x * latSize / 2, minLat + (x + 1) * latSize / 2,
					minLong + y * longSize / 2, minLong + (y + 1) * longSize / 2, latitude, longitude, node, depth + 1);
		} else { // Child exists
			children[x][y].insert(latitude, longitude, node);
		}
		isLeaf = false;
	}

	public DistNodePair getNearestNode(final double latitude, final double longitude) {
		//Returns DistNodePair with closest node to given coordinate inside nodes quadrant
		
		//Default: own node
		final DistNodePair ownDistNode = new DistNodePair(node,
				Math.sqrt(Math.pow(latitude - thisLat, 2) + Math.pow(longitude - thisLong, 2)));
		if (isLeaf) {
			return ownDistNode;
		}
		
		//DistNodePair to be returned
		DistNodePair ret = null;
		
		//Relative coordinates inside the area of the node (0 - 1)
		//Quadrant borders at 0.5
		final double latFrac = (latitude - minLat) / (latSize);
		final double longFrac = (longitude - minLong) / (longSize);

		// Determining optimal child
		int x = 0;
		int y = 0;
		if (latFrac >= 0.5) {
			x = 1;
		}
		if (longFrac >= 0.5) {
			y = 1;
		}
		if (children[x][y] != null) { // Optimal child exists
			ret = DistNodePair.getShorter(ownDistNode, children[x][y].getNearestNode(latitude, longitude));
		}
		for (int i = 0; i < 2; i++) { //Iterate through remaining children to verify shortest found distance
			for (int j = 0; j < 2; j++) {
				if (children[i][j] == null) {
					continue;
				}
				if (ret == null) { // Optimal child did not exist
					ret = DistNodePair.getShorter(ownDistNode, children[i][j].getNearestNode(latitude, longitude));
				} else {
					// Check for better distance in remaining children
					ret = children[i][j].checkNearestNode(latitude, longitude, ret);
				}
			}
		}

		return ret;

	}

	public DistNodePair checkNearestNode(final double latitude, final double longitude, DistNodePair current) {
		
		//Checks if there is a closer node to a given coordinate inside the calling quadtreenodes quadrant
		
		if (Math.sqrt(Math.pow(latitude - Math.max(minLat, Math.min(minLat + latSize, latitude)), 2) + Math
				.pow(longitude - Math.max(minLong, Math.min(minLong + longSize, longitude)), 2)) > current.distance) {
			// If distance to closest point in quadrant is already longer than current
			// closest distance, no further checks must be made
			return current;
		}
		

		current = DistNodePair.getShorter(current,
				new DistNodePair(node, Math.sqrt(Math.pow(latitude - thisLat, 2) + Math.pow(longitude - thisLong, 2))));
		if (isLeaf) {
			return current;
		}
		for (int i = 0; i < 2; i++) { //Iterate through children to verify
			for (int j = 0; j < 2; j++) {
				if (children[i][j] == null) {
					continue;
				}
				current = children[i][j].checkNearestNode(latitude, longitude, current);
			}
		}

		return current;

	}
}
