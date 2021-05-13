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

		// game loop
		while (true) {

			init();

			// debug(String.valueOf(getNbRoundsForCompleteAllFinalTrees()));

			if (getNbRoundsLeft() <= 5) { // s'il me reste juste assez de tours pour completer mes arbres deja de taille 3
				completeAllTreesICan(); // alors je les complete tout mes arbes de taille 3 que je peux (en priorité ceux sur les terres les plus riches)
			}

			growAllTreesICan(); // ensuite j'ameliore le maximum d'arbres que je peux (en priorité les plus grands, sur les terres les plus riches)

			seedAllTreesICan(); // ensuite je plante le maximum d'arbres que je peux (en priorité sur les terres les plus riches)

			if (!played) {
				action("WAIT");
			}

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

	public static void seedAllTreesICan() {
		loop: while (playerMe.nbSuns >= getNbOfTreesMeForSize(0) && !played) { // tant que j'ai assez d'energie pour planter un arbre

			ArrayList<Cell> cells = getCellsICanSeed();

			Cell bestCell = null;
			for (Cell cell : cells) { // pour tout les cases dans lesquels je peux planter un arbre
				if (bestCell == null) {
					bestCell = cell;
				} else {
					if (cell.richness > bestCell.richness) { // si la case est plus riche que celle que j'ai choisi
						bestCell = cell; // je la choisie }
					}
				}
			}

			if (bestCell != null) {
				action("SEED " + getBestTreeInRangeToSeed(bestCell).cell.index + " " + bestCell.index + " Je plante");
				Tree treeSeeded = new Tree(bestCell, 0, true, true);
				trees.add(treeSeeded);
				treesMe.add(treeSeeded);

				playerMe.nbSuns -= getNbOfTreesMeForSize(0);
			} else {
				break loop;
			}
		}
	}

	public static void growAllTreesICan() {
		loop: while (playerMe.nbSuns >= 7 + getNbOfTreesMeForSize(3) && !played) { // tant que j'ai assez d'energie pour level up de 2 a 3
			Tree bestTree = null;
			for (Tree tree : treesMe) { // pour tout mes arbres
				if (tree.size == 2) {
					if (bestTree == null) {
						bestTree = tree;
					} else {
						if (tree.cell.richness > bestTree.cell.richness && !tree.isDormant) { // si l'arbre est sur une terre plus riche et qu'il n'est pas endormi
							bestTree = tree; // je le choisit
						}
					}
				}
			}

			if (bestTree != null) {
				action("GROW " + bestTree.cell.index + " Je fait grandir de 2 a 3");

				bestTree.size++;
				saveTree(bestTree);

				playerMe.nbSuns -= 7 + getNbOfTreesMeForSize(3);
			} else {
				break loop;
			}
		}

		loop: while (playerMe.nbSuns >= 3 + getNbOfTreesMeForSize(2) && !played) { // tant que j'ai assez d'energie pour level up de 1 a 2
			Tree bestTree = null;
			for (Tree tree : treesMe) { // pour tout mes arbres
				if (tree.size == 1) {
					if (bestTree == null) {
						bestTree = tree;
					} else {
						if (tree.cell.richness > bestTree.cell.richness && !tree.isDormant) { // si l'arbre est sur une terre plus riche et qu'il n'est pas endormi
							bestTree = tree; // je le choisit
						}
					}
				}
			}

			if (bestTree != null) {
				action("GROW " + bestTree.cell.index + " Je fait grandir de 1 a 2");

				bestTree.size++;
				saveTree(bestTree);

				playerMe.nbSuns -= 3 + getNbOfTreesMeForSize(2);
			} else {
				break loop;
			}
		}

		loop: while (playerMe.nbSuns >= 3 + getNbOfTreesMeForSize(1) && !played) { // tant que j'ai assez d'energie pour level up de 0 a 1
			Tree bestTree = null;
			for (Tree tree : treesMe) { // pour tout mes arbres
				if (tree.size == 0) {
					if (bestTree == null) {
						bestTree = tree;
					} else {
						if (tree.cell.richness > bestTree.cell.richness && !tree.isDormant) { // si l'arbre est sur une terre plus riche et qu'il n'est pas endormi
							bestTree = tree; // je le choisit
						}
					}
				}
			}

			if (bestTree != null) {
				action("GROW " + bestTree.cell.index + " je fait grandir de 0 à 1");

				bestTree.size++;
				saveTree(bestTree);

				playerMe.nbSuns -= 1 + getNbOfTreesMeForSize(1);
			} else {
				break loop;
			}
		}
	}

	public static void completeAllTreesICan() {
		loop: while (playerMe.nbSuns >= 4 && !played) { // tant que j'ai assez d'energie

			Tree bestTree = null;
			for (Tree tree : treesMe) { // pour tout mes arbres

				if (tree.size == 3) {
					if (bestTree == null) {
						bestTree = tree;
					} else {
						if (tree.cell.richness > bestTree.cell.richness) { // si l'arbre est sur une terre plus riche
							bestTree = tree; // je le choisit
						}
					}
				}
			}

			if (bestTree != null) {
				action("COMPLETE " + bestTree.cell.index + " je complete un arbre");

				trees.remove(bestTree);
				treesMe.remove(bestTree);

				playerMe.nbSuns -= 4;
			} else {
				break loop;
			}
		}
	}

	public static int getNbRoundsForCompleteAllFinalTrees() {

		int nbRoundsForCompleteAllFinalTrees = 0;

		ArrayList<Tree> treesForSimulation = treesMe;
		ArrayList<Tree> treesCompletedTmp = new ArrayList<>();

		int sunsActual = playerMe.nbSuns;
		int sunsPlanned = getSunsPlannedForTrees(treesMe);

		boolean found = true;
		while (found) {

			found = false;
			loop: for (Tree tree : treesForSimulation) { // pour tout mes arbres
				if (tree.size == 3) { // si l'arbre est de taille 3
					if (sunsActual >= 4) { // et que j'ai assez d'energie pour completer son cycle
						sunsActual -= 4;

						if (!tree.isDormant) {
							sunsPlanned -= tree.size;
						}

						treesCompletedTmp.add(tree); // je le complete
					} else {
						if (sunsPlanned > 0) { // si j'ai encore des arbres qui me rapportent de l'energie alors j'attend d'avoir assez d'energie
							sunsActual += sunsPlanned;
							break loop;
						} else {
							return getNbOfTreesMeForSize(3); // sinon je renvoi le nombre de tours equivalent au nombre d'arbres de taille 3 que j'ai
						}
					}
					found = true;
				}
			}

			/* Suppression des arbres qu'on a completé */
			for (Tree treeToRemove : treesCompletedTmp) {
				Integer indexInTreesForSimulation = null;
				for (int i = 0; i < treesForSimulation.size(); i++) {
					Tree tree = treesForSimulation.get(i);

					if (treeToRemove.cell.index == tree.cell.index) {
						indexInTreesForSimulation = i;
					}
				}

				if (indexInTreesForSimulation != null) {
					treesForSimulation.remove(indexInTreesForSimulation);
				}
			}

			nbRoundsForCompleteAllFinalTrees++;
		}

		return nbRoundsForCompleteAllFinalTrees;

	}

	public static Tree getBestTreeInRangeToSeed(Cell cellSeeded) {

		Tree bestTree = null;

		for (Tree tree : treesMe) {
			if (tree.size > 0) {
				ArrayList<Cell> cellsTreeCanSeed = getCellsTreeCanSeed(tree);
				if (cellsTreeCanSeed.contains(cellSeeded)) {
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

	public static ArrayList<Cell> getCellsICanSeed() {
		ArrayList<Cell> cells = new ArrayList<>();

		for (Tree tree : treesMe) {
			ArrayList<Cell> cellsTreecanSeed = getCellsTreeCanSeed(tree);
			for (Cell cell : cellsTreecanSeed) {
				if (!cells.contains(cell)) {
					cells.add(cell);
				}
			}
		}

		return cells;
	}

	public static ArrayList<Cell> getCellsTreeCanSeed(Tree tree) {
		ArrayList<Cell> cells = new ArrayList<>();

		if (tree.size > 0 && !tree.isDormant) {
			for (int neigh : tree.cell.neighs) {
				Cell cell = getCell(neigh);
				if (cell != null) {
					if (!cells.contains(cell) && !existTree(cell) && cell.richness > 0) {// si la case n'est pas inutilisable
						cells.add(cell);
					}

					if (tree.size > 1) {
						for (int neigh2 : cell.neighs) {
							Cell cell2 = getCell(neigh2);
							if (cell2 != null) {
								if (!cells.contains(cell2) && !existTree(cell2) && cell2.richness > 0) {
									cells.add(cell2);
								}

								if (tree.size > 2) {
									for (int neigh3 : cell2.neighs) {
										Cell cell3 = getCell(neigh3);
										if (cell3 != null) {
											if (!cells.contains(cell3) && !existTree(cell3) && cell2.richness > 0) {
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
		// 0 a 23 jours, chaque jour dure 6 rounds
		return 23 - day;
	}

	public static boolean existTree(Cell cell) {
		for (Tree tree : trees) {
			if (tree.cell.index == cell.index) {
				return true;
			}
		}
		return false;
	}

	public static void saveTree(Tree treeToSave) {
		for (int i = 0; i < trees.size(); i++) {
			Tree tree = trees.get(i);

			if (tree.cell.index == treeToSave.cell.index) {
				trees.set(i, treeToSave);
			}
		}

		for (int i = 0; i < treesMe.size(); i++) {
			Tree tree = treesMe.get(i);

			if (tree.cell.index == treeToSave.cell.index) {
				treesMe.set(i, treeToSave);
			}
		}
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
		System.out.println(action);
		played = true;
	}

	public static void debug(String message) {
		System.err.println(message);
	}

}