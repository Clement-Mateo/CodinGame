import java.awt.Point;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Save humans, destroy zombies!
 **/
class Player {

	static Scanner				in;

	static Point				player;

	static ArrayList<Human>		humans;
	static ArrayList<Zombie>	zombies;

	public static void main(String args[]) {

		in = new Scanner(System.in);

		while (true) {

			initialize();

			// <=

			// choisir d'aller vers l'humain qui a un zombie le plus proche de lui
			Human humanSelected = null;
			Double shortestDistance = (double) 99999;
			Double shortestDistanceFromKiller = (double) 99999;
			for (Human human : humans) { // pour chaque humain
				for (Zombie zombie : zombies) { // on check tout les zombies
					if (distance(human.getX(), human.getY(), zombie.getX(), zombie.getY()) < shortestDistance) { // si l'un d'eux est plus proche que la distance enregistrée
						humanSelected = human;
						shortestDistance = (double) distance(human.getX(), human.getY(), zombie.getX(), zombie.getY());
						shortestDistanceFromKiller = (double) distance(human.getX(), human.getY(), player.x, player.y);
					} else if (distance(human.getX(), human.getY(), zombie.getX(), zombie.getY()) == shortestDistance) { // si l'un d'eux est aussi proche que la distance enregistrée
						if (distance(human.getX(), human.getY(), player.x, player.y) < shortestDistanceFromKiller) { // et qu'il est plus proche du killer
							humanSelected = human;
							shortestDistance = (double) distance(human.getX(), human.getY(), zombie.getX(), zombie.getY());
							shortestDistanceFromKiller = (double) distance(human.getX(), human.getY(), player.x, player.y);
						}
					}
				}
			}

			if (humanSelected != null) {
				System.err.println("Je protege l'humain :" + humanSelected.getId());
				System.out.println(humanSelected.getX() + " " + humanSelected.getY());
			} else {
				Zombie zombieSelected = null;
				Double distance = (double) 99999;
				for (Zombie zombie : zombies) {
					if (distance(player.x, player.y, zombie.getX(), zombie.getY()) < distance) {
						zombieSelected = zombie;
					}
				}
				System.err.println("Je vise le zombie :" + zombieSelected.getId());
				System.out.println(zombieSelected.getX() + " " + zombieSelected.getY());
			}
		}
	}

	public static boolean canSaveHuman(Human human) {
		for (Zombie zombie : zombies) {
			if (nbToursForKillHuman(zombie, human) < nbToursForKillZombie(zombie)) { // si le zombie va tuer l'humain avant que j'ai le temps de le tuer
				return false;
			}
		}
		return true;
	}

	// À chaque tour, Ash se déplace de 1000 unités en direction de la coordonnée cible, ou sur la cible s'il se situe à moins de 1000 unités.
	// Si à la fin d'un tour, un zombie se trouve dans un rayon de 2000 unités de Ash, il tire sur le zombie pour le détruire.
	// calculer nbTours pour que le killer ait la range pour tirer sur un zombie (en fonction de ses coordonées)
	public static int nbToursForKillZombie(Zombie zombie) {
		Double distance = distance(player.x, player.y, zombie.getX(), zombie.getY()); // on calcule la distance entre le joueur et le zombie
		if (distance > 2000) { // si le zombie n'est pas dans la range du joueur
			distance -= 2000; // on retranche a la distance la range de tir du joueur
			int nbTours = (int) Math.floor(distance / 1000); // on calcule le nombre de tours necessaires
			return nbTours;
		} else {
			return 0;
		}
	}

	// À chaque tour, chaque zombie se déplace en direction de l'humain le plus proche, incluant Ash, et par pas de 400 unités.
	// Si le zombie se trouve à moins de 400 unités, le zombie se déplace sur la coordonnée de l'humain et le tue.
	// calculer nbTours pour que le zombie mange l'humain
	public static int nbToursForKillHuman(Zombie zombie, Human human) {
		Double distance = distance(human.getX(), human.getY(), zombie.getX(), zombie.getY());
		if (distance > 400) {
			distance -= 400;
			int nbTours = (int) Math.floor(distance / 400);
			return nbTours;
		} else {
			return 0;
		}
	}

	/**
	 * Initialise chaque tour en recupérant les inputs
	 */
	public static void initialize() {

		int x = in.nextInt();
		int y = in.nextInt();
		player = new Point(x, y);

		humans = new ArrayList<Human>();
		zombies = new ArrayList<Zombie>();

		int nbHuman = in.nextInt();
		for (int i = 0; i < nbHuman; i++) {
			Human human = new Human();
			human.setId(in.nextInt());
			human.setX(in.nextInt());
			human.setY(in.nextInt());
			humans.add(human);
		}

		int nbZombies = in.nextInt();
		for (int i = 0; i < nbZombies; i++) {
			Zombie zombie = new Zombie();
			zombie.setId(in.nextInt());
			zombie.setX(in.nextInt());
			zombie.setY(in.nextInt());
			zombie.setXNext(in.nextInt());
			zombie.setYNext(in.nextInt());
			zombies.add(zombie);
		}

		ArrayList<Human> humansWhichCantBeSave = new ArrayList<>();
		for (Human human : humans) { // pour chaque humain
			if (!canSaveHuman(human)) { // s'il ne peut pas être sauvé
				humansWhichCantBeSave.add(human); // alors on ajoute cet humain a la liste des humains qu'on ne doit pas protéger
			}
		}
		humans.removeAll(humansWhichCantBeSave); // on enleve les humains que l'on ne peux pas sauver de la liste des humains a proteger
	}

	public static double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}
}