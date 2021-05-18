package programmierprojekt_lk_pw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Graph {

	private final int[][] adjacencyArray;

	private int currentOffset;
	private int currentStartNode;
	private int currentInputNode;

	private final int[] offset;
	private final double[][] nodeCoordinates; // [0][i]: lat; [1][i]: long
	private Quadtree distanceTree;

	private double minLat;
	private double maxLat;
	private double minLong;
	private double maxLong;

	private final int[][] nodeData; // [0][i]: dist; [1][i]: pred; [2][i] 1 = unknown, 0 = known or edge
	private int currentDijkstraStartNode; // Node that all distances are currently relative to
	private boolean oneToAll;

	private final List<Integer> path;;
	private int temp;

	private final int nodeCount;

	public Graph(final int nodeCount, final int edgeCount) {
		adjacencyArray = new int[3][edgeCount];
		// nodeArray = new Node[nodeCount];

		nodeCoordinates = new double[2][nodeCount];
		distanceTree = null;

		minLat = Double.MAX_VALUE;
		maxLat = Double.MIN_VALUE;
		minLong = Double.MAX_VALUE;
		maxLong = Double.MIN_VALUE;

		nodeData = new int[3][nodeCount];
		path = new ArrayList<Integer>(nodeCount);
		offset = new int[nodeCount];
		for (int i = 0; i < nodeCount; i++) {
			offset[i] = -1;
		}
		offset[0] = 0;
		currentOffset = 0;
		currentStartNode = 0;
		currentInputNode = 0;
		this.nodeCount = nodeCount;
		currentDijkstraStartNode = -1;
	}

	public void addEdge(final int start, final int end, final int weight) throws IOException { // Add an edge to
																								// adjacencyArray
		if (start > currentStartNode) {
			// This is the first edge from the next node
			currentStartNode = start;
			offset[start] = currentOffset;

		}
		if (start < currentStartNode) {
			System.err.println("Edges have to be ordered by starting node!");
			throw new IOException();
		}
		adjacencyArray[0][currentOffset] = start;
		adjacencyArray[1][currentOffset] = end;
		adjacencyArray[2][currentOffset] = weight;
		currentOffset++;
	}

	public void addNode(final double latitude, final double longitude) {
		// nodeArray[currentInputNode] = new Node(latitude, longitude,
		// currentInputNode);
		nodeCoordinates[0][currentInputNode] = latitude;
		minLat = Math.min(latitude, minLat);
		maxLat = Math.max(latitude, maxLat);
		nodeCoordinates[1][currentInputNode] = longitude;
		minLong = Math.min(longitude, minLong);
		maxLong = Math.max(longitude, maxLong);
		nodeData[0][currentInputNode] = -1;
		nodeData[1][currentInputNode] = -1;
		currentInputNode++;
		if (currentInputNode == nodeCount) {

			buildTree();

		}
	}

	private void buildTree() {
		final long startTime = System.currentTimeMillis();
		distanceTree = new Quadtree(minLat, maxLat, minLong, maxLong);
		for (int i = 0; i < nodeCount; i++) {
			distanceTree.insert(nodeCoordinates[0][i], nodeCoordinates[1][i], i);

		}
		final long finalTime = (System.currentTimeMillis() - startTime);
		System.out.println("Building the Quadtree took " + finalTime + "ms.");
	}

	public double getMinLat() {
		return minLat;
	}

	public double getMaxLat() {
		return maxLat;
	}

	public double getMinLong() {
		return minLong;
	}

	public double getMaxLong() {
		return maxLong;
	}
	
	public double[] getNodeCoordinates(int node) {
		if (node < 0 || node >= nodeCount) {
			return null;
		}
		double[] ret = new double[2];
		ret[0] = nodeCoordinates[0][node];
		ret[1] = nodeCoordinates[1][node];
		return ret;
	}

	// Calculates and prints the nearest distance calculated by iterative and
	// quadtree method
	public int getNearestNeighborIter(final double x, final double y) {
		final long startTime = System.currentTimeMillis();
		double nearestDistance = Double.MAX_VALUE;
		int nearestNode = -1;
		double d = 0;

		for (int i = 0; i < nodeCount; i++) {
			d = Math.sqrt(Math.pow(x - nodeCoordinates[0][i], 2) + Math.pow(y - nodeCoordinates[1][i], 2));
			if (d < nearestDistance) {
				nearestDistance = d;
				nearestNode = i;
			}
		}
		final long finalTime = (System.currentTimeMillis() - startTime);
		System.out.println("This iterative request took " + finalTime + "ms.");
		System.out.println("ITER | Nearest Node: " + nearestNode + " | Distance: " + nearestDistance);
		return nearestNode;

	}

	// Used in the main method
	public int getNearestNeighborTree(final double x, final double y) {
		final long startTime = System.currentTimeMillis();
		final DistNodePair distanceNodePair = distanceTree.getNearestNeighbor(x, y);
		final double nearestDistanceTree = distanceNodePair.distance;
		final int nearestNode = distanceNodePair.node;
		final long finalTime = (System.currentTimeMillis() - startTime);
		System.out.println("This QuadTree request took " + finalTime + "ms.");
		System.out.println("TREE | Nearest Node: " + nearestNode + " | Distance: " + nearestDistanceTree);
		return nearestNode;

	}

	// Checks whether iterative method and quadtree method return the same nearest
	// distance. Used in QuadtreeCorrectnessTest.
	public int nearestNeighborCalculationTest(final double x, final double y) {
		long startTime = System.currentTimeMillis();
		double nearestDistance = Double.MAX_VALUE;
		
		//Iter
		double d = 0;
		int nearestNodeIter = -1;
		for (int i = 0; i < nodeCount; i++) {
			d = Math.sqrt(Math.pow(x - nodeCoordinates[0][i], 2) + Math.pow(y - nodeCoordinates[1][i], 2));
			if (d < nearestDistance) {
				nearestDistance = d;
				nearestNodeIter = i;
			}
		}
		
		long finalTime = (System.currentTimeMillis() - startTime);
		System.out.println("This iterative request took " + finalTime + "ms.");
		
		//Quadtree
		startTime = System.currentTimeMillis();
		final int nearestNodeTree = distanceTree.getNearestNeighbor(x, y).node;
		finalTime = (System.currentTimeMillis() - startTime);
		System.out.println("This QuadTree request took " + finalTime + "ms.");
		
		//Compare
		System.out.println("Iter: " + nearestNodeIter + " | Tree: " + nearestNodeTree);
		if (nearestNodeIter == nearestNodeTree) {
			return (int)finalTime;
		} else {
			return -1;
		}

	}

	// Calls the Quadtree method once. Used in the QuadTreeSpeedTest
	public void nearestNeighborTree(final double x, final double y) {
		distanceTree.getNearestNeighbor(x, y);
	}

	public void resetNodeDistance() {
		for (int i = 0; i < nodeCount; i++) {
			nodeData[0][i] = -1;
			nodeData[1][i] = -1;
			nodeData[2][i] = 1;
		}
	}

	public int getNodeDistance(final int n) {
		return nodeData[0][n];
	}

	public int getNodeCount() {
		return nodeCount;
	}

	/*
	 * ===LEGACY=== private void setNodeDistAndPredIfLower(final int node, final int
	 * distance, final int predecessor) { if (nodeData[0][node] == -1 ||
	 * nodeData[0][node] < distance) { nodeData[0][node] = distance;
	 * nodeData[1][node] = predecessor; } }
	 */

	public void dijkstra(final int start, final int destination) {
		// INIT
		resetNodeDistance();
		oneToAll = (destination == -1);
		final BitSet knownNodes = new BitSet();// init B

		nodeData[0][start] = 0;
		nodeData[1][start] = -1;
		nodeData[2][start] = 0;
		knownNodes.set(start);

		int currentNode = -1;

		// init R
		@SuppressWarnings("deprecation")
		final PriorityQueue<Integer> edgeNodes = new PriorityQueue<Integer>(
				(a, b) -> new Integer(nodeData[0][a]).compareTo(new Integer(nodeData[0][b])));

		// Iterate through edges from start node
		for (int i = offset[start]; i >= 0 && i < adjacencyArray[0].length && adjacencyArray[0][i] == start; i++) {
			if (nodeData[2][adjacencyArray[1][i]] != 1) {
				continue;
			}
			nodeData[0][adjacencyArray[1][i]] = adjacencyArray[2][i];
			nodeData[1][adjacencyArray[1][i]] = start;
			edgeNodes.add(adjacencyArray[1][i]);
			nodeData[2][adjacencyArray[1][i]] = 0;
		}

		while (edgeNodes.size() > 0) { // Exit loop if all reachable nodes are known

			currentNode = edgeNodes.poll();
			knownNodes.set(currentNode);

			if (destination == currentNode) {
				return;
			}

			if (offset[currentNode] != -1) {
				for (int i = offset[currentNode]; i < adjacencyArray[0].length
						&& adjacencyArray[0][i] == currentNode; i++) {
					final int n = adjacencyArray[1][i];

					if (nodeData[2][n] == 1) { // Update edge
						nodeData[0][n] = nodeData[0][currentNode] + adjacencyArray[2][i];
						nodeData[1][n] = currentNode;
						edgeNodes.add(n);
						nodeData[2][n] = 0;

					} else if (!knownNodes.get(n) && nodeData[0][currentNode] + adjacencyArray[2][i] < nodeData[0][n]) {
						// Shorter path via currentnode
						nodeData[0][n] = nodeData[0][currentNode] + adjacencyArray[2][i];
						nodeData[1][n] = currentNode;
					}
				}
			}
		}

		currentDijkstraStartNode = start;
	}

	public List<Integer> findPath(final int start, final int destination, final boolean withPath,
			final boolean oneToAll) {
		// INIT PATH
		path.clear();
		int dest = -1;
		if (start == destination) { // trivial
			path.add(0);
			path.add(start);
			return path;
		}
		if (start != currentDijkstraStartNode || this.oneToAll != oneToAll) { // Only perform Dijsktra if distances
																				// weren't already calculated
			// System.out.println("New oneToAll");
			if (!oneToAll) {
				dest = destination;
			}

			dijkstra(start, dest);
		}

		if (nodeData[1][destination] == -1) {
			return null;
		}

		if (withPath) {
			// List of the indices of the nodes making up the shortest path
			path.add(destination);
			int currentNode = destination;
			while (nodeData[1][currentNode] != start) {

				temp = nodeData[1][currentNode];
				path.add(temp);
				currentNode = temp;
			}
			path.add(start);
			path.add(nodeData[0][destination]); // First element in the list is the shortest distance and needs to be
												// ignored
												// by the receiving class

			Collections.reverse(path);
		} else {
			path.add(nodeData[0][destination]);
			return path;
		}

		// Return distance of destination node
		return path;
	}
}
