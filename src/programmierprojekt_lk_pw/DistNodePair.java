package programmierprojekt_lk_pw;

public class DistNodePair {
	public double distance;
	public int node;
	
	public DistNodePair(int node, double distance) {
		this.node = node;
		this.distance = distance;
	}
	
	public static DistNodePair getShorter(DistNodePair a, DistNodePair b) {
		if(a.distance <= b.distance) {
			return a;
		}
		return b;
	}
}
