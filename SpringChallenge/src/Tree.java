import java.util.ArrayList;

class Tree {

	public Cell		cell;
	public int		size;
	public boolean	isMine;
	public boolean	isDormant;

	public Tree() {

	}

	public Tree(Cell cell, int size, boolean isMine, boolean isDormant) {
		super();
		this.cell = cell;
		this.size = size;
		this.isMine = isMine;
		this.isDormant = isDormant;
	}

	public int getScoreForSeed(Cell cell) {
		int score = 0;

		score -= Player.getCostSeed();
		score += cell.richness;
		score -= this.size;

		return score;
	}

	public int getScoreForGrow() {
		int score = 0;

		switch (this.size) {
		case 0:
			score += 4 - Player.getCostGrow(1);
			break;
		case 1:
			score += 9 - Player.getCostGrow(2);
			break;
		case 2:
			score += 12 - Player.getCostGrow(3);
			break;
		}

		score += this.cell.richness;
		score += this.getBestCellRichnessTreeCanSeedAfterGrowUp() - this.getBestCellRichnessTreeCanSeed();
		if (this.isUnderShadowNextRound()) {
			score -= size;
		}
		score += getNbSunsLostForEnnemyNextRoundAfterGrowUp() * 3;
		score -= getNbSunsLostForMeNextRoundAfterGrowUp() * 3;

		return score;
	}

	public int getScoreForComplete() {
		int score = 0;

		if (this.size < 3 || Player.playerMe.nbSuns < 4) { // si l'arbre n'est pas de taille 3 ou que j'ai pas asser d'energie alors je peux pas le complete
			return 0;
		}
		score += this.cell.richness; // plus la terre de l'arbre est riche, mieux c'est
		if (this.isUnderShadowNextRound()) { // si l'arbre est ombragé au prochain tour
			score += 10; // c'est super
		}
		score -= this.getNbSunsLostForEnnemyNextRound();
		score += this.getNbSunsLostForMeNextRound();

		return score;
	}

	public int getNbSunsLostForMeNextRoundAfterGrowUp() {
		int nbSunsLostForEnnemyNextRound = 0;

		ArrayList<Tree> treesMeUnderShadowNextRoundByTree = this.getTreesMeUnderShadowNextRoundByTreeAfterGrowUp();

		for (Tree tree : treesMeUnderShadowNextRoundByTree) {
			nbSunsLostForEnnemyNextRound += tree.size;
		}

		return nbSunsLostForEnnemyNextRound;
	}

	public int getNbSunsLostForMeNextRound() {
		int nbSunsLostForEnnemyNextRound = 0;

		ArrayList<Tree> treesMeUnderShadowNextRoundByTree = this.getTreesMeUnderShadowNextRoundByTree();

		for (Tree tree : treesMeUnderShadowNextRoundByTree) {
			nbSunsLostForEnnemyNextRound += tree.size;
		}

		return nbSunsLostForEnnemyNextRound;
	}

	public int getNbSunsLostForEnnemyNextRoundAfterGrowUp() {
		int nbSunsLostForEnnemyNextRound = 0;

		ArrayList<Tree> treesEnnemyUnderShadowNextRoundByTree = this.getTreesEnnemyUnderShadowNextRoundByTreeAfterGrowUp();

		for (Tree tree : treesEnnemyUnderShadowNextRoundByTree) {
			nbSunsLostForEnnemyNextRound += tree.size;
		}

		return nbSunsLostForEnnemyNextRound;
	}

	public int getNbSunsLostForEnnemyNextRound() {
		int nbSunsLostForEnnemyNextRound = 0;

		ArrayList<Tree> treesEnnemyUnderShadowNextRoundByTree = this.getTreesEnnemyUnderShadowNextRoundByTree();

		for (Tree tree : treesEnnemyUnderShadowNextRoundByTree) {
			nbSunsLostForEnnemyNextRound += tree.size;
		}

		return nbSunsLostForEnnemyNextRound;
	}

	public ArrayList<Tree> getTreesEnnemyUnderShadowNextRoundByTreeAfterGrowUp() {
		ArrayList<Tree> ennemyTreesUnderShadowNextRoundByTree = new ArrayList<Tree>();
		ArrayList<Cell> cellsUnderShadowByTree = this.getCellsUnderShadowNextRoundAfterGrowUp();

		for (Tree tree : Player.treesEnnemy) { // pour chacun des arbres de l'ennemie
			if (tree.size <= this.size) { // si l'arbre est de meme taille ou plus petit
				if (cellsUnderShadowByTree.contains(tree.cell)) { // et qu'il est ombragé par cette arbre
					ennemyTreesUnderShadowNextRoundByTree.add(tree); // je l'ajoute a la liste
				}
			}
		}

		return ennemyTreesUnderShadowNextRoundByTree;
	}

	public ArrayList<Tree> getTreesEnnemyUnderShadowNextRoundByTree() {
		ArrayList<Tree> ennemyTreesUnderShadowNextRoundByTree = new ArrayList<Tree>();
		ArrayList<Cell> cellsUnderShadowByTree = this.getCellsUnderShadowNextRound();

		for (Tree tree : Player.treesEnnemy) { // pour chacun des arbres de l'ennemie
			if (tree.size <= this.size) { // si l'arbre est de meme taille ou plus petit
				if (cellsUnderShadowByTree.contains(tree.cell)) { // et qu'il est ombragé par cette arbre
					ennemyTreesUnderShadowNextRoundByTree.add(tree); // je l'ajoute a la liste
				}
			}
		}

		return ennemyTreesUnderShadowNextRoundByTree;
	}

	public ArrayList<Tree> getTreesMeUnderShadowNextRoundByTreeAfterGrowUp() {
		ArrayList<Tree> treesMeUnderShadowByTreeNextRound = new ArrayList<Tree>();
		ArrayList<Cell> cellsUnderShadowByTree = this.getCellsUnderShadowNextRoundAfterGrowUp();

		for (Tree tree : Player.treesMe) { // pour chacun de mes arbres
			if (tree.size <= this.size) { // si l'arbre est de meme taille ou plus petit
				if (cellsUnderShadowByTree.contains(tree.cell)) { // et qu'il est ombragé par cette arbre
					treesMeUnderShadowByTreeNextRound.add(tree); // je l'ajoute a la liste
				}
			}
		}

		return treesMeUnderShadowByTreeNextRound;
	}

	public ArrayList<Tree> getTreesMeUnderShadowNextRoundByTree() {
		ArrayList<Tree> treesMeUnderShadowByTreeNextRound = new ArrayList<Tree>();
		ArrayList<Cell> cellsUnderShadowByTree = this.getCellsUnderShadowNextRound();

		for (Tree tree : Player.treesMe) { // pour chacun de mes arbres
			if (tree.size <= this.size) { // si l'arbre est de meme taille ou plus petit
				if (cellsUnderShadowByTree.contains(tree.cell)) { // et qu'il est ombragé par cette arbre
					treesMeUnderShadowByTreeNextRound.add(tree); // je l'ajoute a la liste
				}
			}
		}

		return treesMeUnderShadowByTreeNextRound;
	}

	public ArrayList<Cell> getCellsUnderShadowNextRoundAfterGrowUp() {
		int direction = Player.getSunDirection();
		ArrayList<Cell> cellsUnderShadow = new ArrayList<Cell>();

		Cell neigh1 = Player.getCell(this.cell.neighs.get(direction));
		cellsUnderShadow.add(neigh1); // on ajoute la cellule

		int size = this.size + 1;

		if (size > 1) {
			Cell neigh2;
			try {
				neigh2 = Player.getCell(neigh1.neighs.get(direction));
				cellsUnderShadow.add(neigh2); // on ajoute la cellule
			} catch (Exception e) {
				return cellsUnderShadow;
			}

			try {
				if (size > 2) {
					Cell neigh3 = Player.getCell(neigh2.neighs.get(direction));
					cellsUnderShadow.add(neigh3); // on ajoute la cellule
				}
			} catch (Exception e) {
				return cellsUnderShadow;
			}
		}

		return cellsUnderShadow;
	}

	public ArrayList<Cell> getCellsUnderShadowNextRound() {
		int direction = Player.getSunDirection();
		ArrayList<Cell> cellsUnderShadow = new ArrayList<Cell>();

		Cell neigh1 = Player.getCell(this.cell.neighs.get(direction));
		if (neigh1 != null) {
			cellsUnderShadow.add(neigh1); // on ajoute la cellule

			if (this.size > 1) {
				Cell neigh2 = Player.getCell(neigh1.neighs.get(direction));
				if (neigh2 != null) {
					cellsUnderShadow.add(neigh2); // on ajoute la cellule

					if (this.size > 2) {
						Cell neigh3 = Player.getCell(neigh2.neighs.get(direction));
						if (neigh3 != null) {
							cellsUnderShadow.add(neigh3); // on ajoute la cellule
						}
					}
				}
			}
		}

		return cellsUnderShadow;
	}

	public boolean isUnderShadowNextRound() {
		int direction = Player.getSunDirection();

		for (Tree tree : Player.trees) { // pour chaque arbre de la carte
			if (tree.size >= this.size) { // si l'arbre est plus grand ou de même taille
				ArrayList<Cell> cellsUnderShadowNextRound = tree.getCellsUnderShadowNextRound();
				for (Cell cell : cellsUnderShadowNextRound) {
					if (cell.index == this.cell.index) { // si l'arbre fait de l'ombre a cette arbre
						return true;
					}
				}
			}
		}
		return false;
	}

	public void save() {
		for (int i = 0; i < Player.trees.size(); i++) {
			Tree tree = Player.trees.get(i);

			if (tree.cell.index == this.cell.index) {
				Player.trees.set(i, this);
			}
		}

		for (int i = 0; i < Player.treesMe.size(); i++) {
			Tree tree = Player.treesMe.get(i);

			if (tree.cell.index == this.cell.index) {
				Player.treesMe.set(i, this);
			}
		}
	}

	public int getNbRoundsForGrowAndCompleteTree() {
		int nbRounds = 0;
		if (this.isDormant) {
			nbRounds++;
		}
		nbRounds += 4 - this.size;
		return nbRounds;
	}

	public ArrayList<Cell> getCellsTreeCanSeedAfterGrowUp() {
		ArrayList<Cell> cells = new ArrayList<>();

		int size = this.size + 1;

		if (this.size > 0 && !this.isDormant) {
			for (int neigh : this.cell.neighs) {
				Cell cell = Player.getCell(neigh);
				if (cell != null) {
					if (!cells.contains(cell) && !cell.existTree() && cell.richness > 0) {// si la case n'est pas inutilisable
						cells.add(cell);
					}

					if (this.size > 1) {
						for (int neigh2 : cell.neighs) {
							Cell cell2 = Player.getCell(neigh2);
							if (cell2 != null) {
								if (!cells.contains(cell2) && !cell2.existTree() && cell2.richness > 0) {
									cells.add(cell2);
								}

								if (this.size > 2) {
									for (int neigh3 : cell2.neighs) {
										Cell cell3 = Player.getCell(neigh3);
										if (cell3 != null) {
											if (!cells.contains(cell3) && !cell3.existTree() && cell2.richness > 0) {
												cells.add(cell3);
											}
										}
									}
								}
							}
						}
					}

				}
			}
		}
		return cells;
	}

	public ArrayList<Cell> getCellsTreeCanSeed() {
		ArrayList<Cell> cells = new ArrayList<>();

		if (this.size > 0 && !this.isDormant) {
			for (int neigh : this.cell.neighs) {
				Cell cell = Player.getCell(neigh);
				if (cell != null) {
					if (!cells.contains(cell) && !cell.existTree() && cell.richness > 0) {// si la case n'est pas inutilisable
						cells.add(cell);
					}

					if (this.size > 1) {
						for (int neigh2 : cell.neighs) {
							Cell cell2 = Player.getCell(neigh2);
							if (cell2 != null) {
								if (!cells.contains(cell2) && !cell2.existTree() && cell2.richness > 0) {
									cells.add(cell2);
								}

								if (this.size > 2) {
									for (int neigh3 : cell2.neighs) {
										Cell cell3 = Player.getCell(neigh3);
										if (cell3 != null) {
											if (!cells.contains(cell3) && !cell3.existTree() && cell2.richness > 0) {
												cells.add(cell3);
											}
										}
									}
								}
							}
						}
					}

				}
			}
		}
		return cells;
	}

	public int getCostForGrowAndCompleteTree() {
		int cost = 0;
		int size = this.size;

		while (size < 3) { // tant que l'arbre n'est pas de taille 3
			cost += Player.getCostGrow(size); // on ajoute le cout de l'amelioration de l'arbre au cout total
			size++; // on ameliore l'arbre
		}
		cost += 4; // enfin on ajoute le cout de completion de l'arbre

		return cost;
	}

	public Integer getBestCellRichnessTreeCanSeedAfterGrowUp() {
		ArrayList<Cell> cellsTreeCanSeed = this.getCellsTreeCanSeedAfterGrowUp();
		Integer bestCellRichnessTreeCanSeed = null;

		for (Cell cell : cellsTreeCanSeed) {
			if (bestCellRichnessTreeCanSeed == null) {
				bestCellRichnessTreeCanSeed = cell.richness;
			} else {
				if (cell.richness > bestCellRichnessTreeCanSeed) {
					bestCellRichnessTreeCanSeed = cell.richness;
				}
			}
		}

		if (bestCellRichnessTreeCanSeed == null) {
			return 0;
		}

		return bestCellRichnessTreeCanSeed;
	}

	public Integer getBestCellRichnessTreeCanSeed() {
		ArrayList<Cell> cellsTreeCanSeed = this.getCellsTreeCanSeed();
		Integer bestCellRichnessTreeCanSeed = null;

		for (Cell cell : cellsTreeCanSeed) {
			if (bestCellRichnessTreeCanSeed == null) {
				bestCellRichnessTreeCanSeed = cell.richness;
			} else {
				if (cell.richness > bestCellRichnessTreeCanSeed) {
					bestCellRichnessTreeCanSeed = cell.richness;
				}
			}
		}

		if (bestCellRichnessTreeCanSeed == null) {
			return 0;
		}

		return bestCellRichnessTreeCanSeed;
	}
}