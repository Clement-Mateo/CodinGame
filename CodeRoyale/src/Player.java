import java.util.*;
import java.io.*;
import java.math.*;

class Player {

	// inputs
	static Scanner in;

	static int nbSites;
	static ArrayList<Site> sites = new ArrayList<>();
	static ArrayList<Site> myCasernes = new ArrayList<>();
	static ArrayList<Site> ennemyCasernes = new ArrayList<>();

	static int gold;
	static int touchedSite;

	static int numUnits;
	static ArrayList<Unit> units = new ArrayList<>();

	/*** constantes ***/

	//camps
	static final int ALLY = 0;
	static final int ENNEMY = 1;

	//structure type
	static final int EMPTY_SITE = -1;
	static final int TOWER = 1;
	static final int CASERNE = 2;

	//units
	static final int QUEEN = -1;

	//unitTypes
	static final String KNIGHT = "KNIGHT";
	static final String ARCHER = "ARCHER";
	static final String GIANT = "GIANT";

	// for me
	static Unit initialEnnemyQueen;

	static Unit myQueen;
	static Unit ennemyQueen;

	static ArrayList<Site> toTrain = new ArrayList<>();

	private static boolean danger = false;


	public static void main(String args[]) {

		// game loop
		while (true) {

			initialise();

			// First line: A valid queen action

			boolean proche = false;
			if(!danger && touchedSite != -1 || proche) {
				Site _touchedSite = getSite(touchedSite);
				if(_touchedSite.getOwner() == ALLY) {
					for(int i=0; i < ennemyCasernes.size(); i++) {
						if(_touchedSite.getDistance(ennemyCasernes.get(i).getX(), ennemyCasernes.get(i).getY()) < 500) {
							proche = true;
						}
					}
					int nbKnightCasernes = 0;
					for(int i=0; i < myCasernes.size(); i++) {
						if(myCasernes.get(i).getUnitType() == KNIGHT) {
							nbKnightCasernes ++;
						}
					}
					Site site = new Site();
					if(proche) {
						site.setStructureType(TOWER);
					} else if(nbKnightCasernes > 3) {
						site.setStructureType(CASERNE);
						site.setUnitType(ARCHER);
					} else {
						site.setStructureType(CASERNE);
						site.setUnitType(KNIGHT);
					}
					build(_touchedSite, site);
				} else {
					bestMove();
				}

			} else {
				bestMove();
			}

			// Second line: A set of training instructions
			bestTrain();

		}
	}

	static void move (int x, int y) {
		System.out.println("MOVE " + x + " " + y);
	}

	static void build (Site oldSite, Site newSite) {
		switch(newSite.getStructureType()) {
		case TOWER:
			System.out.println("BUILD " + oldSite.getSiteId() + " TOWER");
			return;
		case CASERNE:
			System.out.println("BUILD " + oldSite.getSiteId() + " BARRACKS-" + newSite.getUnitType());
			return;
		}
	}

	static void train (ArrayList<Site> toTrain) {
		System.out.print("TRAIN");
		for(int i=0; i < toTrain.size(); i++) {
			System.out.print(" " + toTrain.get(i).getSiteId());
		}
		System.out.print("\n");
	}

	static Site getSite (int siteId) {
		for(int i = 0; i < nbSites; i++) {
			Site site = sites.get(i);
			if(siteId == site.getSiteId()) {
				return site;
			}
		}
		return null;
	}

	static ArrayList<Integer> getSafePosition() {
		ArrayList<Integer> xY = new ArrayList<>();
		if(initialEnnemyQueen.getX() < 960) {
			xY.add(1920);
			if(initialEnnemyQueen.getY() < 500) {
				xY.add(1000);
				return xY;
			} else {
				xY.add(0);
				return xY;
			}
		} else {
			xY.add(0);
			if(initialEnnemyQueen.getY() < 500) {
				xY.add(1000);
				return xY;
			} else {
				xY.add(0);
				return xY;
			}
		}
	}

	static Site getClosestSite() {
		int distance = 1000;
		Site closestSite = null;
		for(int i=0; i<nbSites; i++) {
			if(sites.get(i).getStructureType() == EMPTY_SITE) {
				if(sites.get(i).getDistance(myQueen.getX(), myQueen.getY()) < distance) {
					closestSite = sites.get(i);
					distance = sites.get(i).getDistance(myQueen.getX(), myQueen.getY());
				}
			}
		}
		return closestSite;
	}

	static void bestMove() {
		for(int i=0; i<ennemyCasernes.size(); i++) {
			if(ennemyCasernes.get(i).getDistance(myQueen.getX(), myQueen.getX()) < 500
					|| myQueen.getHealth() < 40) {
				danger = true;
			}
		}

		if(danger) {
			move(getSafePosition().get(0), getSafePosition().get(1));
		} else {
			Site closestSite = getClosestSite();
			if(closestSite != null) {
				move(closestSite.getX(), closestSite.getY());
			} else {
				System.out.println("WAIT");
			}
		}
	}

	static void bestTrain() {
		int cost = 0;
		for(int i=0; i<myCasernes.size(); i++) {
			if(myCasernes.get(i).getParam1() == 0 && cost + 80 <= gold) {
				toTrain.add(myCasernes.get(i));
				cost += 80;
			}
		}

		for(int i=0; i<myCasernes.size(); i++) {
			if(myCasernes.get(i).getParam1() == 0) {
				for(int y=0; y < toTrain.size(); y++) {
					if(myCasernes.get(i).getDistance(ennemyQueen.getX(), ennemyQueen.getY()) < toTrain.get(y).getDistance(ennemyQueen.getX(), ennemyQueen.getY())) {
						toTrain.set(y, myCasernes.get(i));
					}
				}
			}
		}

		if(toTrain.isEmpty()) {
			System.out.println("TRAIN");
		} else {
			train(toTrain);
		}
	}

	static void initialise() {

		// premiere initialisation
		if(in == null) {

			in = new Scanner(System.in);

			nbSites = in.nextInt();
			for (int i = 0; i < nbSites; i++) {
				Site site = new Site();
				site.setSiteId(in.nextInt());
				site.setX(in.nextInt());
				site.setY(in.nextInt());
				site.setRadius(in.nextInt());
				sites.add(site);
			}
		}

		// on réinitialise nos listes
		myCasernes = new ArrayList<>();
		toTrain = new ArrayList<>();
		units = new ArrayList<>();

		//inputs
		gold = in.nextInt();
		touchedSite = in.nextInt(); // -1 if none

		for (int i = 0; i < nbSites; i++) {
			Site site = getSite(in.nextInt());
			site.setIgnore1(in.nextInt()); // used in future leagues
			site.setIgnore2(in.nextInt()); // used in future leagues
			site.setStructureType(in.nextInt()); // -1 = No structure, 2 = Barracks
			site.setOwner(in.nextInt()); // -1 = No structure, 0 = Friendly, 1 = Enemy
			site.setParam1(in.nextInt());
			site.setParam2(in.nextInt());
		}

		for (int i = 0; i < nbSites; i++) {
			Site site = sites.get(i);
			if(site.getOwner() == ALLY) {
				if(site.getStructureType() == CASERNE) {
					myCasernes.add(site);
				}
			}

			if(site.getOwner() == ENNEMY) {
				if(site.getStructureType() == CASERNE) {
					ennemyCasernes.add(site);
				}
			}
		}

		numUnits = in.nextInt();
		for (int i = 0; i < numUnits; i++) {
			Unit unit = new Unit();
			unit.setX(in.nextInt());
			unit.setY(in.nextInt());
			unit.setOwner(in.nextInt());
			unit.setUnitType(in.nextInt()); // -1 = QUEEN, 0 = KNIGHT, 1 = ARCHER
			unit.setHealth(in.nextInt());
			units.add(unit);
		}

		for (int i = 0; i < numUnits; i++) {
			Unit unit = units.get(i);
			if(unit.getUnitType() == QUEEN) {
				if(unit.getOwner() == ALLY) {
					myQueen = unit;
				} else {
					if(initialEnnemyQueen == null) {
						initialEnnemyQueen = unit;
					}
					ennemyQueen = unit;
				} 
			}
		}

	}
}