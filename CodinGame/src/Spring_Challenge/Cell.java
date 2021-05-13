import java.util.ArrayList;

class Cell {
	
	public int index;
	public int richness;
	public ArrayList<Integer> neighs;
	
	public Cell() {
		neighs = new ArrayList<>();
	}

	public Cell(int index, int richness, ArrayList<Integer> neights) {
		super();
		this.index = index;
		this.richness = richness;
		this.neighs = neights;
	}
}
