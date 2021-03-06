import java.util.ArrayList;

class Cell {

	public int					index;
	public int					richness;
	public ArrayList<Integer>	neighs;

	public Cell() {
		neighs = new ArrayList<>();
	}

	public Cell(int index, int richness, ArrayList<Integer> neights) {
		super();
		this.index = index;
		this.richness = richness;
		this.neighs = neights;
	}

	public boolean existTree() {
		for (Tree tree : Player.trees) {
			if (tree.cell.index == this.index) {
				return true;
			}
		}
		return false;
	}

	public boolean existTree(int size) {
		for (Tree tree : Player.trees) {
			if (tree.cell.index == this.index) {
				if (tree.size == size) {
					return true;
				}
			}
		}
		return false;
	}

	public int getNbTreeInRangeToShadow() {
		int nbTreeInRangeToShadow = 0;

		for (Integer neigh : this.neighs) {
			Cell neighCell = Player.getCell(neigh);
			if (neighCell != null) {
				if (neighCell.existTree(1)) {
					nbTreeInRangeToShadow++;
				}

				for (Integer neigh2 : neighCell.neighs) {
					Cell neighCell2 = Player.getCell(neigh2);
					if (neighCell2 != null) {
						if (neighCell2.existTree(2)) {
							nbTreeInRangeToShadow++;
						}

						for (Integer neigh3 : neighCell2.neighs) {
							Cell neighCell3 = Player.getCell(neigh3);
							if (neighCell3 != null) {
								if (neighCell3.existTree(3)) {
									nbTreeInRangeToShadow++;
								}
							}
						}
					}
				}
			}
		}
		return nbTreeInRangeToShadow;
	}

	public Tree getBestTreeInRangeToSeed() {

		Tree bestTree = null;

		for (Tree tree : Player.treesMe) {
			if (tree.size > 0) {
				ArrayList<Cell> cellsTreeCanSeed = tree.getCellsTreeCanSeed();
				if (cellsTreeCanSeed.contains(this)) {
					if (bestTree == null) {
						bestTree = tree;
					} else {
						if (tree.size < bestTree.size) {
							bestTree = tree;
						}
					}
				}
			}
		}

		return bestTree;
	}
}