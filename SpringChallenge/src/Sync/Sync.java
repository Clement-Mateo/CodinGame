import java.util.ArrayList;
import java.util.Scanner;

class Player {

	public static Scanner			scanner;

	public static boolean			played	= false;

	public static int				numberOfCells;
	public static ArrayList<Cell>	cells;

	public static int				day;
	public static int				nutrients;

	public static PlayerSpring		playerMe;
	public static PlayerSpring		playerAdverse;

	public static int				numberOfTrees;
	public static ArrayList<Tree>	trees;
	public static ArrayList<Tree>	treesMe;
	public static ArrayList<Tree>	treesEnnemy;

	public static void main(String args[]) {

		scanner = new Scanner(System.in);

		cells = new ArrayList<>();
		numberOfCells = scanner.nextInt(); // 37
		for (int i = 0; i < numberOfCells; i++) {
			Cell cell = new Cell();
			cell.index = scanner.nextInt(); // 0 is the center cell, the next cells spiral outwards
			cell.richness = scanner.nextInt(); // 0 if the cell is unusable, 1-3 for usable cells
			cell.neighs.add(scanner.nextInt()); // the index of the neighbouring cell for each direction
			cell.neighs.add(scanner.nextInt());
			cell.neighs.add(scanner.nextInt());
			cell.neighs.add(scanner.nextInt());
			cell.neighs.add(scanner.nextInt());
			cell.neighs.add(scanner.nextInt());
			cells.add(cell);
		}

		while (true) { // game loop

			init();

			if (getNbRoundsLeft() > 4) { // s'il reste plus de 4 rounds

				if (treesMe.size() == 2) { // si le jeu viens de commencer
					if (day > 1) { // a partir du 3ème jour
						seedBestTreeICan(); // je plante le maximum d'arbres que je peux
					}

					growBestTreeICan(); // j'ameliore le meilleur arbre

				} else {

					if (getNbOfTreesMeForSize(3) >= 3) { // si j'ai au moins 3 arbres de taille 3

						if (getNbOfTreesMeForSize(3) > 2) { // si j'ai plus de 2 arbres de taille 3
							if (getScoreForCompleteBestTreeICan() > 12 || getNbOfTreesMeForSize(3) > 5) { // si c'est une bonne occasion (bon score) ou que j'ai vraiment beaucoup d'arbres de taille 3
								completeBestTreeICan(); // alors je complete des arbres jusqu'a n'en avoir que 3 de taille 3
							}

						}
					}

					growBestTreeICan(); // j'ameliore le meilleur arbre (en priorité les plus grand, sur la terre la plus riche)

					if (getNbRoundsLeft() > 12) {
						seedBestTreeICan(); // je plante le maximum d'arbres que je peux (en priorité sur les terres les plus riches)
					}
				}

			} else { // s'il reste moins de 4 rounds

				completeBestTreeICan(); // je complete tout mes arbes de taille 3 que je peux (en priorité ceux sur les terres les plus riches)

				quickGrowThrees3AndCompleteIt(); // je fait des arbres de taille 3 en priorité et je les termines (en priorité ceux sur les terres les plus riches)
			}

			action("WAIT J'attend");

		}
	}

	public static void init() {

		played = false;

		day = scanner.nextInt(); // the game lasts 24 days: 0-23
		nutrients = scanner.nextInt(); // the base score you gain from the next COMPLETE action

		playerMe = new PlayerSpring();
		playerMe.nbSuns = scanner.nextInt(); // your sun points
		playerMe.score = scanner.nextInt(); // your current score
		playerMe.isWaiting = false;

		playerAdverse = new PlayerSpring();
		playerAdverse.nbSuns = scanner.nextInt(); // opponent's sun points
		playerAdverse.score = scanner.nextInt(); // opponent's score
		playerAdverse.isWaiting = scanner.nextInt() == 1; // whether your opponent is asleep until the next day

		trees = new ArrayList<>();
		treesMe = new ArrayList<>();
		treesEnnemy = new ArrayList<>();

		numberOfTrees = scanner.nextInt(); // the current amount of trees
		for (int i = 0; i < numberOfTrees; i++) {
			Tree tree = new Tree();
			tree.cell = getCell(scanner.nextInt()); // location of this tree
			tree.size = scanner.nextInt(); // size of this tree: 0-3
			tree.isMine = scanner.nextInt() == 1; // 1 if this is your tree
			tree.isDormant = scanner.nextInt() == 1; // 1 if this tree is dormant
			trees.add(tree);

			if (tree.isMine) {
				treesMe.add(tree);
			} else {
				treesEnnemy.add(tree);
			}
		}

		// je m'en branle pas besoin d'aide
		int numberOfPossibleActions = scanner.nextInt(); // all legal actions
		if (scanner.hasNextLine()) {
			scanner.nextLine();
		}
		for (int i = 0; i < numberOfPossibleActions; i++) {
			scanner.nextLine();
		}
	}

	public static void seedBestTreeICan() {
		if (!played && playerMe.nbSuns >= getCostSeed() && (getNbOfTreesMeForSize(0) < 2 || playerMe.nbSuns > 10)) { // si je n'ai pas deja joue, que j'ai assez de soleils pour planter et que j'ai moins de 3 graines ou plus de 10 soleils

			Tree bestTree = null;
			Cell bestCell = null;

			for (Tree tree : treesMe) { // pour chacun de mes arbres
				if (tree.size > 0 && !tree.isDormant) {
					if (bestTree == null) {
						bestTree = tree;
					}
					ArrayList<Cell> cellsTreeCanSeed = tree.getCellsTreeCanSeed();
					for (Cell cell : cellsTreeCanSeed) {
						if (bestCell == null) {
							bestCell = cell;
						} else if (tree.getScoreForSeed(cell) > bestTree.getScoreForSeed(bestCell)) { // planter de cette arbre sur cette case a un meilleur score que le plant que j'avait choisi
							bestTree = tree;
							bestCell = cell;
						}
					}
				}
			}

			if (bestTree != null && bestCell != null) {
				action("SEED " + bestTree.cell.index + " " + bestCell.index + " seed score : " + bestTree.getScoreForSeed(bestCell));
				Tree treeSeeded = new Tree(bestCell, 0, true, true);
				trees.add(treeSeeded);
				treesMe.add(treeSeeded);

				playerMe.nbSuns -= getCostSeed();
			}
		}
	}

	public static void growBestTreeICan() {

		if (!played) { // si je n'ai pas deja joue
			Tree bestTree = null;
			for (Tree tree : treesMe) { // pour tout mes arbres
				if (playerMe.nbSuns >= getCostGrow(tree.size + 1) && !tree.isDormant) { // si j'ai assez d'energie pour améliorer cette arbre et qu'il n'est pas endormi
					if (bestTree == null) {
						bestTree = tree;
					} else {
						if (tree.getScoreForGrow() > bestTree.getScoreForGrow()) { // si améliorer cette arbre a un meilleur score qu'ameliorer l'arbre que j'avait choisi
							bestTree = tree;
						}
					}
				}
			}

			if (bestTree != null) {
				action("GROW " + bestTree.cell.index + " grow " + bestTree.cell.index + " score : " + bestTree.getScoreForGrow());

				bestTree.size++;
				bestTree.save();

				playerMe.nbSuns -= getCostGrow(bestTree.size);
			}
		}
	}

	public static void completeBestTreeICan() {
		if (playerMe.nbSuns >= 4 && !played) { // tant que j'ai assez d'energie

			Tree bestTree = null;
			for (Tree tree : treesMe) { // pour tout mes arbres

				if (tree.size == 3) {
					if (bestTree == null) {
						bestTree = tree;
					} else {
						if (tree.getScoreForComplete() > bestTree.getScoreForComplete()) { // si completer cette arbre a un meilleur score que completer l'abre que j'avais choisi
							bestTree = tree;
						}
					}
				}
			}

			if (bestTree != null) {
				action("COMPLETE " + bestTree.cell.index + " complete " + bestTree.cell.index + " score : " + bestTree.getScoreForComplete());

				trees.remove(bestTree);
				treesMe.remove(bestTree);

				playerMe.nbSuns -= 4;
			}
		}
	}

	public static int getScoreForCompleteBestTreeICan() {
		if (playerMe.nbSuns >= 4 && !played) { // tant que j'ai assez d'energie

			Tree bestTree = null;
			for (Tree tree : treesMe) { // pour tout mes arbres

				if (tree.size == 3) {
					if (bestTree == null) {
						bestTree = tree;
					} else {
						if (tree.getScoreForComplete() > bestTree.getScoreForComplete()) { // si completer cette arbre a un meilleur score que completer l'abre que j'avais choisi
							bestTree = tree;
						}
					}
				}
			}

			if (bestTree != null) {
				return bestTree.getScoreForComplete();
			}
		}
		return 0;
	}

	public static void quickGrowThrees3AndCompleteIt() {
		Tree bestTree = null;
		for (Tree tree : treesMe) { // pour chacun de mes arbres
			if (!tree.isDormant) { // si l'arbre n'est pas endormis
				if (bestTree == null) {
					bestTree = tree;
				} else {
					if (tree.size > bestTree.size) { // si l'arbre est plus grand
						bestTree = tree; // je le choisi
					} else if (tree.size == bestTree.size) { // si l'arbre est de même taille
						if (tree.cell.richness > bestTree.cell.richness) { // si l'arbre est sur une case plus riche
							bestTree = tree; // je le choisi
						}
					}
				}
			}
		}

		if (bestTree != null) {
			if (!played && playerMe.nbSuns >= bestTree.getCostForGrowAndCompleteTree()) {
				if (bestTree.getNbRoundsForGrowAndCompleteTree() <= getNbRoundsLeft()) { // si j'ai assez de rounds pour grow + complete l'arbre
					if (bestTree.size < 3) { // si l'arbre n'est pas encore de taille max
						action("GROW " + bestTree.cell.index + " quick grow " + bestTree.cell.index + " score : " + bestTree.getScoreForGrow());
						playerMe.nbSuns -= getCostGrow(bestTree.size);
					} else { // si l'arbre est de taille max
						action("COMPLETE " + bestTree.cell.index + " quick complete " + bestTree.cell.index + " score : "
								+ bestTree.getScoreForComplete());
						playerMe.nbSuns -= 4;

						trees.remove(bestTree);
						treesMe.remove(bestTree);
					}
				}
			}
		}
	}

	public static ArrayList<Cell> getCellsICanSeed() {
		ArrayList<Cell> cells = new ArrayList<>();

		for (Tree tree : treesMe) {
			ArrayList<Cell> cellsTreecanSeed = tree.getCellsTreeCanSeed();
			for (Cell cell : cellsTreecanSeed) {
				if (!cells.contains(cell)) {
					cells.add(cell);
				}
			}
		}

		return cells;
	}

	public static int getSunsPlannedForTrees(ArrayList<Tree> trees) {

		int sunsPlannedForTrees = 0;

		for (Tree tree : trees) { // pour chaque arbre
			if (tree.size > 0 && !tree.isDormant) { // si ce n'est pas une graine et que l'arbre n'est pas endormi
				sunsPlannedForTrees += tree.size;
			}
		}
		return sunsPlannedForTrees;
	}

	public static int getNbOfTreesMeForSize(int size) {
		int nb = 0;

		for (Tree tree : treesMe) {
			if (tree.size == size) {
				nb++;
			}
		}

		return nb;
	}

	public static int getNbRoundsLeft() {
		return 24 - day; // 0 a 23 jours, chaque jour dure 6 rounds
	}

	public static int getCostGrow(int size) {
		switch (size) {
		case 1:
			return 1 + getNbOfTreesMeForSize(1);
		case 2:
			return 3 + getNbOfTreesMeForSize(2);
		case 3:
			return 7 + getNbOfTreesMeForSize(3);
		default:
			return 999999;
		}
	}

	public static int getCostSeed() {
		return getNbOfTreesMeForSize(0);
	}

	public static int getSunDirection() {
		return day % 6;
	}

	public static Cell getCell(int index) {
		for (Cell cell : cells) {
			if (cell.index == index) {
				return cell;
			}
		}
		return null;
	}

	public static void action(String action) {
		if (!played) {
			System.out.println(action);
			played = true;
		}
	}

	public static void debug(String message) {
		System.err.println(message);
	}

	// public static int getNbRoundsForCompleteAllFinalTrees() {

	// int nbRoundsForCompleteAllFinalTrees = 0;

	// ArrayList<Tree> treesForSimulation = treesMe;
	// ArrayList<Tree> treesCompletedTmp = new ArrayList<>();

	// int sunsActual = playerMe.nbSuns;
	// int sunsPlanned = getSunsPlannedForTrees(treesMe);

	// boolean found = true;
	// while (found) {

	// found = false;
	// loop: for (Tree tree : treesForSimulation) { // pour tout mes arbres
	// if (tree.size == 3) { // si l'arbre est de taille 3
	// if (sunsActual >= 4) { // et que j'ai assez d'energie pour completer son cycle
	// sunsActual -= 4;

	// if (!tree.isDormant) {
	// sunsPlanned -= tree.size;
	// }

	// treesCompletedTmp.add(tree); // je le complete
	// } else {
	// if (sunsPlanned > 0) { // si j'ai encore des arbres qui me rapportent de l'energie alors j'attend d'avoir assez d'energie
	// sunsActual += sunsPlanned;
	// break loop;
	// } else {
	// return getNbOfTreesMeForSize(3); // sinon je renvoi le nombre de tours equivalent au nombre d'arbres de taille 3 que j'ai
	// }
	// }
	// found = true;
	// }
	// }

	// /* Suppression des arbres qu'on a completé */
	// for (Tree treeToRemove : treesCompletedTmp) {
	// Integer indexInTreesForSimulation = null;
	// for (int i = 0; i < treesForSimulation.size(); i++) {
	// Tree tree = treesForSimulation.get(i);

	// if (treeToRemove.cell.index == tree.cell.index) {
	// indexInTreesForSimulation = i;
	// }
	// }

	// if (indexInTreesForSimulation != null) {
	// treesForSimulation.remove(indexInTreesForSimulation);
	// }
	// }

	// nbRoundsForCompleteAllFinalTrees++;
	// }

	// return nbRoundsForCompleteAllFinalTrees;

	// }

}

class PlayerSpring {

	public int		nbSuns;
	public int		score;
	public boolean	isWaiting;

	public PlayerSpring() {

	}

	public PlayerSpring(int nbSuns, int score, boolean isWaiting) {
		super();
		this.nbSuns = nbSuns;
		this.score = score;
		this.isWaiting = isWaiting;
	}
}

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