import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Scanner;

class Player {

	//imputs
	static ArrayList<Integer>	playerHealth			= new ArrayList<>();
	static ArrayList<Integer>	playerMana				= new ArrayList<>();
	static ArrayList<Integer>	playerDeck				= new ArrayList<>();
	static ArrayList<Integer>	playerRune				= new ArrayList<>();
	static ArrayList<Integer>	playerDraw				= new ArrayList<>();

	static int					opponentHand;
	static int					opponentActions;
	static int					cardCount;

	static ArrayList<Card>		cards					= new ArrayList<>();

	//FOR ME
	static int					manaRestant;
	final  static int			TARGET_PLAYER			= -1; // cibler l'adversaire

	static ArrayList<Card>		myHand; // liste des cartes dans ma main
	static ArrayList<Card>		monstersHand; // liste des monstres dans ma main
	static ArrayList<Card>		canInvoque; // liste des monstres que l'on peut invoquer

	static ArrayList<Card>		greenObjects; // liste des objets verts (pour boost un monstre)
	static ArrayList<Card>		redObjects; // liste des objets rouges (pour tuer un monstre)
	static ArrayList<Card>		blueObjects; // liste des objets bleus (pour taper l'adversaire)

	static ArrayList<Card>		myBoard; // liste des monstres dans mon board
	static int					canAttackSum		= 0;

	static ArrayList<Card>		ennemyBoard; // liste des monstres dans le board ennemie
	static int					ennemyBoardAttackSum	= 0;

	static ArrayList<Card>		monstersPick; // liste des monstres dans la phase de pick
	static ArrayList<Integer>	monstersPickID; // liste des picks correspondant a ces monstres

	static ArrayList<Integer>	greenObjectsPickID;
	static ArrayList<Integer>	redObjectsPickID;
	static ArrayList<Integer>	blueObjectsPickID;

	static String				sentActions				= ""; // les actions que l'on envoi a la  fin du tour

	static boolean 				perfect = false;
	static boolean 				ok = false;

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);

		// game loop
		while (true) {

			Initialise(in);

			//DEBUG
			System.err.println("mana :  " + manaRestant);

			//  System.err.println("playerHand.size() :  " + playerHand.size());
			//  System.err.println("monstersHand.size() :  " + monstersHand.size());
			//  System.err.println("canInvoque.size() :  " + canInvoque.size());

			// for (int i=0; i < playerHand.size(); i++) {
			//     System.err.println("carte " + i + " dans la main :"
			//     + "\nId instance : " + playerHand.get(i).getInstanceId()
			//     + "\ncost : " + playerHand.get(i).getCost()
			//     + "\nscore : " + playerHand.get(i).getBattleScore());
			// }

			for (int i=0; i < myBoard.size(); i++) {
				System.err.println("carte " + i + " dans mon board :\nId instance : "
						+ myBoard.get(i).getInstanceId());
			}

			for (int i=0; i < ennemyBoard.size(); i++) {
				System.err.println("carte " + i + " dans le board ennemi :\nId instance : "
						+ ennemyBoard.get(i).getInstanceId());
			}

			/*****************************************
			 **************** ACTION *****************
			 ****************************************/

			//PICK PHASE
			if (playerMana.get(0) == 0) {
				bestPick();
			} else {

				/******************************
				 ******** BATTLE PHASE ********
				 ******************************/

				attackWithAll(); // pour si on invoque des charges apres (tmp)
				// #TODO ne jouer les monstres a 0 attaque que lorsqu'on a un objet vert pour boost leur attaque

				ennemyBoardAttackSum = 0;
				for (int j = 0; j < ennemyBoard.size(); j++) { // pour chaque monstre du board ennemi
					ennemyBoardAttackSum += ennemyBoard.get(j).getAttack(); // on calcule la somme de l'attaque de tous ses monstres
				}
				if(ennemyBoardAttackSum >= playerHealth.get(0)-10) { // si l'adversaire peut me tuer ou s'il en est proche

					summonBestCombinaisonAndSpellsWhenCritick();

				} else { //sinon j'invoque la meilleure combinaison de monstres et d'objets au mieux possible
					summonBestCombinaisonAndSpells();
				}

				attackWithAll(); //je fait attaquer tous mes monstres
				pass(); // puis je termine mon tour
			}

			System.out.println(sentActions);

		}
	}

	public static Card getCard(int id) {
		for (int i = 0; i < cards.size(); i++) {
			if (cards.get(i).getCardNumber() == id) {    //si l'id correspond
				return cards.get(i);    //on renvoie la carte correspondante
			}
		}
		return null;    //si l'id ne correspond à aucune carte, on renvoie null
	}

	public static void pick(int pick) {
		sentActions += "PICK " + pick;
	}

	public static void pass() {
		sentActions += "PASS";
	}

	public static void summon(Card monstre) {
		for (int i=0; i<myHand.size(); i++) {
			if(monstre.getCardNumber() == myHand.get(i).getCardNumber()) {
				myHand.remove(i); // on supprime ce monstre de notre main
			}
		}
		myBoard.add(monstre);
		if(monstre.getAbilities().contains("C")) {
			myBoard.get(myBoard.size()-1).setCanAttack(true);
		} else {
			myBoard.get(myBoard.size()-1).setCanAttack(false);
		}
		sentActions += "SUMMON " + monstre.getInstanceId() + "; ";
	}

	public static void attack(Card attacker, Card target) {

		for (int i=0; i<myBoard.size(); i++) {
			if(attacker.getCardNumber() == myBoard.get(i).getCardNumber()) {
				myBoard.get(i).setCanAttack(false);
				// if(target.getAttack() >= attacker.getDefense()) { // si le monstre meurt en l'attaquant
				//     myBoard.remove(i);
				// }
			}
		}
		if(attacker.getAttack() >= target.getDefense()) { // si on tue le monstre
			for (int i=0; i<ennemyBoard.size(); i++) {
				if(attacker.getCardNumber() == ennemyBoard.get(i).getCardNumber()) {
					ennemyBoard.remove(i); // on supprime ce monstre du board ennemi
				}
			}
		}
		sentActions += "ATTACK " + attacker.getInstanceId() + " " + target.getInstanceId() + "; ";
	}

	public static void attackPlayer(Card attacker) {
		for (int i=0; i<myBoard.size(); i++) {
			if(attacker.getCardNumber() == myBoard.get(i).getCardNumber()) {
				myBoard.get(i).setCanAttack(false); // on bloque l'attaque pour ce tour
			}
		}
		sentActions += "ATTACK " + attacker.getInstanceId() + " " + TARGET_PLAYER + "; ";
	}

	public static void attackPlayerWithAll() {
		if (!myBoard.isEmpty()) {
			for (int i = 0; i < myBoard.size(); i++) {
				if(myBoard.get(i).getCanAttack() == true) {
					attackPlayer(myBoard.get(i));
				}
			}
		}
	}

	public static void use(Card objet, Card target) {
		for (int i=0; i<myHand.size(); i++) {
			if(objet.getCardNumber() == myHand.get(i).getCardNumber()) {
				myHand.remove(i); // on supprime ce monstre de notre main
			}
		}
		sentActions += "USE " + objet.getInstanceId() + " " + target.getInstanceId() + "; ";
	}

	public static void useOnPlayer(Card objet) {
		for (int i=0; i<myHand.size(); i++) {
			if(objet.getCardNumber() == myHand.get(i).getCardNumber()) {
				myHand.remove(i); // on supprime ce monstre de notre main
			}
		}
		sentActions += "USE " + objet.getInstanceId() + " " + TARGET_PLAYER + "; ";
	}

	public static void bestPick() {
		int pick = 0;

		if (monstersPick.isEmpty()) { // s'il n'y a aucun monstre
			if (greenObjects.isEmpty()) { // s'il n'y a pas d'objets verts
				if (redObjects.isEmpty()) { // s'il n'y a pas d'objets rouges
					if (!blueObjects.isEmpty()) { // s'il y a des objets bleus
						pick = blueObjectsPickID.get(0); // on recupere le premier objet bleu
						for (int i = 0; i < blueObjects.size(); i++) { // pour chaque objet bleu
							if ((blueObjects.get(i)).getPickScore() > blueObjects.get(pick).getPickScore()) { // si son score est plus grand que l'objet choisi
								pick = blueObjectsPickID.get(i); // on renvoi sa clé
							}
						}
					}
				}
				else { // s'il y a des objets rouges
					if (blueObjects.isEmpty()) { // s'il n'y a pas d'objets bleus
						pick = redObjectsPickID.get(0); // on recupere le premier objet rouge
						for (int i = 0; i < redObjects.size(); i++) { // pour chaque objets rouge
							if ((redObjects.get(i)).getPickScore() > redObjects.get(pick).getPickScore()) { // si son score est plus grand que l'objet choisi
								pick = redObjectsPickID.get(i); // on renvoi sa clé
							}
						}
					}
					else { // s'il y a des d'objets bleus
						Card objetChoisi = redObjects.get(0);
						pick = redObjectsPickID.get(0); // on recupere le premier objet rouge
						for (int i = 0; i < redObjects.size(); i++) { // pour chaque objets rouge
							if ((redObjects.get(i)).getPickScore() > objetChoisi.getPickScore()) { // si son score est plus grand que l'objet choisi
								pick = redObjectsPickID.get(i); // on renvoi sa clé
								objetChoisi = redObjects.get(i); // on choisi cette objet
							}
						}

						for (int i = 0; i < blueObjects.size(); i++) { // pour chaque objet bleu
							if ((blueObjects.get(i)).getPickScore() > objetChoisi.getPickScore()) { // si son score est plus grand que l'objet choisi
								pick = blueObjectsPickID.get(i); // on renvoi sa clé
								objetChoisi = blueObjects.get(i); // on choisi cette objet
							}
						}

					}
				}
			}
			else { // s'il y a des objets verts
				Card objetChoisi = greenObjects.get(0);
				pick = greenObjectsPickID.get(0); // on recupere le premier objet vert
				for (int i = 0; i < greenObjects.size(); i++) { // pour chaque objets vert
					if ((greenObjects.get(i)).getPickScore() > objetChoisi.getPickScore()) { // si son score est plus grand
						pick = greenObjectsPickID.get(i); // on renvoi sa clé
						objetChoisi = greenObjects.get(i);
					}
				}
			}
		}
		else if (monstersPick.size() == 1) { // s'il n'y a qu'un monstre
			pick = monstersPickID.get(0); // on renvoi sa clé
		}
		else if (monstersPick.size() > 1) { // si il y a plus d'un monstre
			pick = monstersPickID.get(0); // on recupere le premier monstre
			for (int i = 1; i < monstersPick.size(); i++) { // pour chaque autre monstre
				if ((monstersPick.get(i)).getPickScore() > monstersPick.get(pick).getPickScore()) { // si son score est plus grand
					pick = monstersPickID.get(i); // on renvoi sa clé
				}
			}
		}

		pick(pick);
	}

	public static void attackWithAll() {
		if (!myBoard.isEmpty()) { // si mon board n'est pas vide

			canAttackSum = 0;
			for (int i = 0; i < myBoard.size(); i++) { // pour chaque monstre de mon board
				if(myBoard.get(i).getCanAttack() == true) { // si il peut attacker
					canAttackSum += myBoard.get(i).getAttack(); // on calcule la somme de l'attaque de tous mes monstres

				}
			}

			if (ennemyBoard.isEmpty()) { // si le board de l'adversaire est vide
				attackPlayerWithAll(); //on attaque l'adversaire
			} else {
				ennemyBoardAttackSum = 0;
				for (int j = 0; j < ennemyBoard.size(); j++) { // pour chaque monstre du board ennemi
					ennemyBoardAttackSum += ennemyBoard.get(j).getAttack(); // on calcule la somme de l'attaque de tous ses monstres
					if(ennemyBoard.get(j).getAbilities().contains("G")) {
						canAttackSum -= ennemyBoard.get(j).getDefense();
					}
				}

				ListIterator<Card> myBoardIterator = myBoard.listIterator();
				while (myBoardIterator.hasNext()) {
					Card myBoardCurrentCard = myBoardIterator.next();

					if (myBoardCurrentCard.getAttack() > 0 // si le monstre a une attaque > 0
							|| myBoardCurrentCard.getCanAttack() == false) { // ou s'il ne peut pas attacker
						//#TODO gérer léthal et ward

						if (!ennemyBoard.isEmpty()) {

							Card provoc = null;
							for (int y = 0; y < ennemyBoard.size() && provoc == null; y++) { // pour chaque monstre du board ennemi tant qu'on a pas trouvé de provocs
								if (ennemyBoard.get(y).getAbilities().contains("G")) { // si il y a une provoc
									provoc = ennemyBoard.get(y); // on la récupere pour l'attaquer
								}
							}

							if (provoc != null) { // si il y a une provoc

								if(provoc.getAbilities().contains("W")) {
									Card monstreUsed = myBoardCurrentCard;

									for (int j = 0; j < myBoard.size(); j++) { // pour chaque monstre de mon board
										if (myBoard.get(j).getAttack() < monstreUsed.getAttack()) { // si l'attaque du monstre est plus petite que celle du monstre choisi
											monstreUsed = myBoard.get(j);
										}
									}
									attack(monstreUsed, provoc);
								} else {
									perfect = false;
									ok = false;
									Card monstreUsed = myBoardCurrentCard;

									for (int j = 0; j < myBoard.size(); j++) { // pour chaque monstre de mon board
										if (myBoard.get(j).getAttack() == provoc.getDefense()) { // on tue la provoc avec juste l'attaque necessaire si possible  
											monstreUsed = myBoard.get(j);
											perfect = true;
										}
										if (!perfect && myBoard.get(j).getDefense() > myBoardCurrentCard.getAttack()) { // on tue la provoc avec plus d'attaque que necessaire si possible 
											monstreUsed = myBoard.get(j);
											ok = true;
										}
										if (!perfect && !ok) { // sinon on attaque simplement un monstre
											monstreUsed = myBoard.get(j);
										}
									}
									attack(monstreUsed, provoc);
								}
							} else if (canAttackSum >= playerHealth.get(1)) { // si la somme de l'attaque de tous mes monstres me fait gagner
								attackPlayer(myBoardCurrentCard); // on attaque l'adversaire
							} else if ((!myBoardCurrentCard.getAbilities().contains("G") // si le monstre n'est pas un garde
									|| myBoardCurrentCard.getAbilities().contains("D")) // ou qu'il a drain de vie
									&& (ennemyBoardAttackSum >= playerHealth.get(0) - 10 // si la somme de l'attaque de tous ses monstres est proche de me tuer ou me tue
									|| myBoard.size() == 6)) { // ou si mon board est plein

								perfect = false;
								ok = false;
								Card monstreCible = null;
								for (int j = 0; j < ennemyBoard.size() && !perfect; j++) { // pour chaque monstre du board ennemi
									if(ennemyBoard.get(j).getAbilities().contains("W")) {
										if(myBoardCurrentCard.getAttack() == 1) {
											monstreCible = ennemyBoard.get(j);
											perfect = true;
										}
									} else {
										if (myBoardCurrentCard.getAttack() == ennemyBoard.get(j).getDefense()) { // on tue un monstre avec juste l'attaque necessaire si possible  
											monstreCible = ennemyBoard.get(j);
											perfect = true;
										}
										if (!perfect && myBoardCurrentCard.getAttack() > ennemyBoard.get(j).getDefense()) { // on tue un monstre avec plus d'attaque que necessaire si possible 
											monstreCible = ennemyBoard.get(j);
											ok = true;
										}
										if (!perfect && !ok) { // sinon on attaque simplement un monstre
											monstreCible = ennemyBoard.get(j);
										}
									}
								}
								if(monstreCible != null) {
									attack(myBoardCurrentCard, monstreCible);
								} else {
									attackPlayer(myBoardCurrentCard);
								}

							} else {
								perfect = false;
								Card monstreCible = null;
								if (myBoardCurrentCard.getAbilities().contains("B")) { // ou si le monstre inflige des degats au heros lors de l'attaque d'un monstre
									for (int j = 0; j < ennemyBoard.size(); j++) { // pour chaque monstre du board ennemi
										if (myBoardCurrentCard.getAttack() > ennemyBoard.get(j).getDefense()) { // on tue un monstre avec plus d'attaque que necessaire si possible 
											monstreCible = ennemyBoard.get(j);
											perfect = true;
										}
									}
								}
								if(perfect) {
									attack(myBoardCurrentCard, monstreCible);
								} else {
									attackPlayer(myBoardCurrentCard); // on attaque l'adversaire
								}
							}							
						} else {
							attackPlayer(myBoardCurrentCard); // on attaque l'adversaire
						}
					}
				}
			}
		}
	}

	private static void useObjectsAfterCombinaison(int combinaisonCost) {
		if (combinaisonCost < manaRestant) { // si il me reste du mana en plus je jou des objets si j'en j'ai

			ArrayList<Card> selectedObjects = new ArrayList<>();
			ArrayList<Card> targets = new ArrayList<>();
			int nbObjets = 0;

			if (!myBoard.isEmpty()) { // si mon board n'est pas vide
				if (!greenObjects.isEmpty()) {
					Card monster0Attack = null;
					Card monsterLethal = null;
					Card monsterGarde = null;
					for (int j = 0; j < myBoard.size(); j++) { // pour chaque monstre de mon board
						if (myBoard.get(j).getAttack() == 0) { // il y a un monstre a 0 attaque ?
							monster0Attack = myBoard.get(j);
						}
						if (myBoard.get(j).getAbilities().contains("L")) { // il y a un lethal ?
							monsterLethal = myBoard.get(j);
						}
						if (myBoard.get(j).getAbilities().contains("G")) { // il y a un lethal ?
							monsterGarde = myBoard.get(j);
						}
					}
					for (int j = 0; j < greenObjects.size(); j++) { // pour chaque objet vert dans ma main
						if (combinaisonCost + greenObjects.get(j).getCost() < manaRestant) { // si j'ai asser de mana pour l'utiliser
							manaRestant -= greenObjects.get(j).getCost(); // on met a jour le mana restant
							selectedObjects.add(greenObjects.get(j)); // on jou cette objet
							if (monster0Attack != null && greenObjects.get(j).getAttack() > 0) { // il y a un monstre a 0 attaque et si lo'objet donne de l'attaque
								targets.add(monster0Attack); // on définit le monstre a 0 attaque comme cible
							} else if (monsterLethal != null && greenObjects.get(j).getDefense() > 0) { // il y a un lethal et si l'objet donne de la defense
								targets.add(monsterLethal); // on définit le monstre lethal comme cible
							} else {
								if (monsterGarde != null) {
									targets.add(monsterGarde); // on définit la cible sur le monstre garde
								} else {
									targets.add(myBoard.get(0)); // on définit le premier monstre de mon board comme cible
								}
							}
							nbObjets++;
						}
					}
				}
			}

			if (!ennemyBoard.isEmpty()) { // si le board ennemi n'est pas vide
				if (!redObjects.isEmpty()) {
					for (int j = 0; j < redObjects.size(); j++) { // pour chaque objet rouge dans ma main
						if (combinaisonCost + redObjects.get(j).getCost() < manaRestant) { // si j'ai asser de mana pour l'utiliser
							manaRestant -= redObjects.get(j).getCost(); // on met a jour le mana restant
							selectedObjects.add(redObjects.get(j)); // on jou cette objet
							perfect = false;
							ok = false;
							for (int i = 0; i < ennemyBoard.size(); i++) { // pour chaque monstre du board ennemi
								if (-redObjects.get(j).getDefense() == ennemyBoard.get(i).getDefense()) { // si l'attaque tue exactement le monstre
									perfect = true;
									targets.add(ennemyBoard.get(i)); // on définit le monstre ennemi en question comme cible
								}
								if (!perfect // si on a pas encore trouvé de perfect
										&& -redObjects.get(j).getDefense() == ennemyBoard.get(i).getDefense()) { // si l'attaque tue le monstre avec du surplus
									ok = true;
									targets.add(ennemyBoard.get(i)); // on définit le monstre ennemi en question comme cible
								}
								if (!perfect && !ok) { // si on a pas de trouver de perfect ou de ok
									targets.add(ennemyBoard.get(i)); // on définit le monstre ennemi en question comme cible
								}
							}
							nbObjets++;
						}
					}
				}
			}

			if (!blueObjects.isEmpty()) {
				for (int j = 0; j < blueObjects.size(); j++) { // pour chaque objet bleu dans ma main
					if (combinaisonCost + blueObjects.get(j).getCost() < manaRestant) { // si j'ai asser de mana pour l'utiliser
						manaRestant -= blueObjects.get(j).getCost(); // on met a jour le mana restant
						useOnPlayer(blueObjects.get(j)); // on jou cet objet sur l'adversaire
						nbObjets++;
					}
				}
			}

			for (int i = 0; i < selectedObjects.size(); i++) { // pour chaque objet choisi
				use(selectedObjects.get(i), targets.get(i)); // je l'utilise sur la cible associée
			}
		}
	}

	/**
	 * Invocation lorsque l'etat n'est pas critique
	 */
	private static void summonBestCombinaisonAndSpells() {
		ArrayList<Integer> bestScore = new ArrayList<>(); // chaque élément i correspond au meilleur score de la combinaison de taille i
		ArrayList<ArrayList<Card>> cardCombinaisons = new ArrayList<ArrayList<Card>>(); // chaque élément i correspond a la combinaison de taille i au meilleur score

		boolean haveCombinaison = true;
		for (int i = 0; i < canInvoque.size() //pour les combinaisons de i cartes
				&& haveCombinaison; i++) { //tant qu'il y a des combinaisons
			bestScore.add(-1000); //on créer un nouveau meilleur score pour les combinaisons de cette taille
			cardCombinaisons.add(new ArrayList<Card>()); //on créer la liste qui contiendra la combinaison de cette taille de meilleure score
			int combinaisonCost = 0;
			int combinaisonScore = 0;
			for (int y = 0; y < canInvoque.size(); y++) {
				if (cardCombinaisons.get(i).size() == 0) { // si la combinaison est vide
					combinaisonCost = 0; // on reinitialise le cout de la combinaison
					combinaisonScore = 0; // on reinitialise le score de la combinaison
				}
				Card monster = canInvoque.get(y);
				if (cardCombinaisons.get(i).size() < i + 1) { //si la combinaison ne fait pas encore la bonne taille
					if ((combinaisonCost + monster.getCost()) <= manaRestant) { // et qu'on a assez de mana pour y ajouter cette carte
						cardCombinaisons.get(i).add(monster); //on ajoute la carte a la combinaison
						combinaisonCost += monster.getCost(); //on met a jour le cout de la combinaison
						combinaisonScore += monster.getBattleScore(); //on met a jour le score de la combinaison
					}
				} else { // si la combinaison fait deja la bonne taille
					for (int j = 0; j < cardCombinaisons.get(i).size(); j++) { // pour chaque carte de la combinaison
						if (combinaisonCost + monster.getCost() - cardCombinaisons.get(i).get(j).getCost() <= manaRestant) { // si le cout total de la carte plus le reste de la combinaison ne depasse pas mon mana
							if (monster.getBattleScore() >= cardCombinaisons.get(i).get(j).getBattleScore()) { // et que son score est egal ou plus grand
								cardCombinaisons.get(i).set(j, monster); // on modifie la combinaison
								combinaisonScore += monster.getBattleScore() - cardCombinaisons.get(i).get(j).getBattleScore(); // on modifie le score de la combinaison
								combinaisonCost += monster.getCost() - cardCombinaisons.get(i).get(j).getCost(); // on modifie le cout de la combinaison
							}
						}
					}
					if (bestScore.get(i) < combinaisonScore) { // si la combinaison a un meilleur score
						bestScore.set(i, combinaisonScore); // on conserve le score de cette combinaison
					}
				}
				if (y == canInvoque.size() - 1 && cardCombinaisons.get(i).size() < i) { //si on arrive a la fin de la main et que la combinaison n'atteind pas la bonne taille
					cardCombinaisons.remove(i); // on supprime cette combinaison
					bestScore.remove(i); // ainsi que son score
					haveCombinaison = false; // et on arrete de chercher des combinaisons
				}
			}
		}

		if (!bestScore.isEmpty()) { // s'il y a au moins une combinaison possible

			int maxScore = -1000;
			int combinaisonNum = 0;

			for (int i = 0; i < bestScore.size(); i++) { // pour chaque combinaison
				if (bestScore.get(i) > maxScore) { // si son score est plus grand que le meilleur score actuel
					maxScore = bestScore.get(i); // on met a jour le meilleure score
					combinaisonNum = i; // on choisi d'invoquer cette combinaison
				}
			}

			int combinaisonCost = 0;
			for (int i = 0; i < cardCombinaisons.get(combinaisonNum).size(); i++) { // pour chaque monstre de la meilleure combinaison
				combinaisonCost += cardCombinaisons.get(combinaisonNum).get(i).getCost(); // j'ajoute son cout pour calculer le cout total de ma combinaison
				summon(cardCombinaisons.get(combinaisonNum).get(i)); // je l'invoque
			}

			useObjectsAfterCombinaison(combinaisonCost);

		} else { // s'il n'y a pas de combinaisons possibles

			ArrayList<Card> selectedObjects = new ArrayList<>();
			ArrayList<Card> targets = new ArrayList<>();
			int nbObjets = 0;

			if (!myBoard.isEmpty()) { // si mon board n'est pas vide
				if (!greenObjects.isEmpty()) {
					Card monster0Attack = null;
					Card monsterLethal = null;
					Card monsterGarde = null;
					for (int j = 0; j < myBoard.size(); j++) { // pour chaque monstre de mon board
						if (myBoard.get(j).getAttack() == 0) { // il y a un monstre a 0 attaque ?
							monster0Attack = myBoard.get(j);
						}
						if (myBoard.get(j).getAbilities().contains("L")) { // il y a un lethal ?
							monsterLethal = myBoard.get(j);
						}
						if (myBoard.get(j).getAbilities().contains("G")) { // il y a un lethal ?
							monsterGarde = myBoard.get(j);
						}
					}
					for (int j = 0; j < greenObjects.size(); j++) { // pour chaque objet vert dans ma main
						if (greenObjects.get(j).getCost() < manaRestant) { // si j'ai asser de mana pour l'utiliser
							manaRestant -= greenObjects.get(j).getCost(); // on met a jour le mana restant
							selectedObjects.add(greenObjects.get(j)); // on jou cette objet
							if (monster0Attack != null && greenObjects.get(j).getAttack() > 0) { // il y a un monstre a 0 attaque et si lo'objet donne de l'attaque
								targets.add(monster0Attack); // on définit le monstre a 0 attaque comme cible
							} else if (monsterLethal != null && greenObjects.get(j).getDefense() > 0) { // il y a un lethal et si l'objet donne de la defense
								targets.add(monsterLethal); // on définit le monstre lethal comme cible
							} else {
								if (monsterGarde != null) {
									targets.add(monsterGarde); // on définit la cible sur le monstre garde
								} else {
									targets.add(myBoard.get(0)); // on définit le premier monstre de mon board comme cible
								}
							}
							nbObjets++;
						}
					}
				}
			}

			if (!ennemyBoard.isEmpty()) { // si le board ennemi n'est pas vide
				if (!redObjects.isEmpty()) {
					for (int j = 0; j < redObjects.size(); j++) { // pour chaque objet rouge dans ma main
						if (redObjects.get(j).getCost() < manaRestant) { // si j'ai asser de mana pour l'utiliser
							manaRestant -= redObjects.get(j).getCost(); // on met a jour le mana restant
							selectedObjects.add(redObjects.get(j)); // on jou cette objet
							perfect = false;
							ok = false;
							for (int i = 0; i < ennemyBoard.size(); i++) { // pour chaque monstre du board ennemi
								if (-redObjects.get(j).getDefense() == ennemyBoard.get(i).getDefense()) { // si l'attaque tue exactement le monstre
									perfect = true;
									targets.add(ennemyBoard.get(i)); // on définit le monstre ennemi en question comme cible
								}
								if (!perfect // si on a pas encore trouvé de perfect
										&& -redObjects.get(j).getDefense() == ennemyBoard.get(i).getDefense()) { // si l'attaque tue le monstre avec du surplus
									ok = true;
									targets.add(ennemyBoard.get(i)); // on définit le monstre ennemi en question comme cible
								}
								if (!perfect && !ok) { // si on a pas de trouver de perfect ou de ok
									targets.add(ennemyBoard.get(i)); // on définit le monstre ennemi en question comme cible
								}
							}
							nbObjets++;
						}
					}
				}
			}

			if (!blueObjects.isEmpty()) {
				for (int j = 0; j < blueObjects.size(); j++) { // pour chaque objet bleu dans ma main
					if (blueObjects.get(j).getCost() < manaRestant) { // si j'ai asser de mana pour l'utiliser
						manaRestant -= blueObjects.get(j).getCost(); // on met a jour le mana restant
						useOnPlayer(blueObjects.get(j)); // on jou cet objet sur l'adversaire
					}
				}
			}

			for (int i = 0; i < selectedObjects.size(); i++) { // pour chaque objet choisi
				use(selectedObjects.get(i), targets.get(i)); // je l'utilise sur la cible associée
			}
		}
	}

	/**
	 * Invocation lorsque l'etat est critique
	 */
	private static void summonBestCombinaisonAndSpellsWhenCritick() {
		ArrayList<Card> combinaison = new ArrayList<Card>();
		int combinaisonCost = 0;
		boolean garde = false;

		/* 1ere vague pour les gardes */
		for (int i=0; i < canInvoque.size(); i++) { // pour chaque monstre de ma main que je peux invoquer
			Card monster = canInvoque.get(i);
			if(monster.getAbilities().contains("G")) { // si c'est un garde
				if(monster.getCost() + combinaisonCost <= manaRestant) { // s'il me reste asser de mana
					combinaisonCost += monster.getCost(); // je met a jour le coup de ma combinaison
					combinaison.add(monster); // je choisi ce monstre
					garde = true;
				} else {
					ok = false;
					for(int y=0; y < combinaison.size() && !ok; y++) { // pour chaque monstre de ma combinaison
						if(monster.getCost() <= combinaison.get(y).getCost()) { // si le monstre de ma main coute autant ou moins cher que le monstre de ma combinaison
							if(monster.getBattleScore() > combinaison.get(y).getBattleScore()) { // et que son score est plus grand
								combinaisonCost += monster.getCost() - combinaison.get(y).getCost(); // je met a jour le coup de ma combinaison
								combinaison.set(y, monster); // je choisi ce monstre
								ok = true;
							}
						}
					}
				}
			}
		}
		if(garde) {
			/* 2ème vague pour ajouter les non gardes */
			for (int i=0; i < canInvoque.size(); i++) { // pour chaque monstre de ma main que je peux invoquer
				Card monster = canInvoque.get(i);
				if(monster.getCost() + combinaisonCost <= manaRestant) { // s'il me reste asser de mana
					combinaisonCost += monster.getCost(); // je met a jour le coup de ma combinaison
					combinaison.add(monster); // je choisi ce monstre
				} else {
					ok = false;
					for(int y=0; y < combinaison.size() && !ok; y++) { // pour chaque monstre de ma combinaison
						if(!combinaison.get(y).getAbilities().contains("G")) { // si ce n'est pas un garde
							if(monster.getCost() <= combinaison.get(y).getCost()) { // si le monstre de ma main coute autant ou moins cher que le monstre de ma combinaison
								if(monster.getBattleScore() > combinaison.get(y).getBattleScore()) { // et que son score est plus grand
									combinaisonCost += monster.getCost() - combinaison.get(y).getCost(); // je met a jour le coup de ma combinaison
									combinaison.set(y, monster); // je choisi ce monstre
									ok = true;
								}
							}
						}
					}
				}
			}

			for (int i = 0; i < combinaison.size(); i++) { // pour chaque monstre de la combinaison
				summon(combinaison.get(i)); // je l'invoque
			}

			/* on ajoute des objets si possible */
			useObjectsAfterCombinaison(combinaisonCost);
		} else { // si on a pas de garde alors on jou de manière habituelle
			summonBestCombinaisonAndSpells();
		}
	}

	public static void Initialise(Scanner in) {

		for (int i = 0; i < 2; i++) {
			if (playerHealth.size() < 2) {
				playerHealth.add(in.nextInt());
				playerMana.add(in.nextInt());
				playerDeck.add(in.nextInt());
				playerRune.add(in.nextInt());
				playerDraw.add(in.nextInt());
			}
			else {
				playerHealth.set(i, in.nextInt());
				playerMana.set(i, in.nextInt());
				playerDeck.set(i, in.nextInt());
				playerRune.set(i, in.nextInt());
				playerDraw.set(i, in.nextInt());
			}
		}

		opponentHand = in.nextInt();
		opponentActions = in.nextInt();
		if (in.hasNextLine()) {
			in.nextLine();
		}
		for (int i = 0; i < opponentActions; i++) {
			String cardNumberAndAction = in.nextLine();
		}

		cardCount = in.nextInt();

		cards = new ArrayList<>();

		//PICK PHASE
		monstersPick = new ArrayList<Card>();
		monstersPickID = new ArrayList<Integer>();

		greenObjectsPickID = new ArrayList<Integer>();
		redObjectsPickID = new ArrayList<Integer>();
		blueObjectsPickID = new ArrayList<Integer>();

		//BATTLE PHASE
		myHand = new ArrayList<Card>();

		monstersHand = new ArrayList<Card>();
		canInvoque = new ArrayList<Card>();

		//OBJETS
		greenObjects = new ArrayList<Card>();
		redObjects = new ArrayList<Card>();
		blueObjects = new ArrayList<Card>();

		//ON BOARD
		myBoard = new ArrayList<Card>();
		ennemyBoard = new ArrayList<Card>();

		for (int i = 0; i < cardCount; i++) {

			cards.add(new Card());
			Card card = cards.get(i);

			card.setCardNumber(in.nextInt());
			card.setInstanceId(in.nextInt());
			card.setLocation(in.nextInt());
			card.setCardType(in.nextInt());
			card.setCost(in.nextInt());
			card.setAttack(in.nextInt());
			card.setDefense(in.nextInt());
			card.setAbilities(in.next());
			card.setMyHealthChange(in.nextInt());
			card.setOpponentHealthChange(in.nextInt());
			card.setCardDraw(in.nextInt());
			card.setPickScore(card.calculPickScore());
			card.setBattleScore(card.calculBattleScore());

			//INITIALISATION
			manaRestant = playerMana.get(0);
			sentActions = ""; // actions que l'on renvoi

			/* PICK PHASE */
			if (card.getCardType() == 0) { // si c'est un monstre
				monstersPick.add(card);
				monstersPickID.add(i);
			}

			if (card.getCardType() == 1) { // si c'est un objet vert
				greenObjects.add(card);
				greenObjectsPickID.add(i);
			}

			if (card.getCardType() == 2) { // si c'est un objet rouge
				redObjects.add(card);
				redObjectsPickID.add(i);
			}

			if (card.getCardType() == 3) { // si c'est un objet bleu
				blueObjects.add(card);
				blueObjectsPickID.add(i);
			}

			/* BATTLE PHASE */
			if (card.getLocation() == 0) { // si la carte est dans ma main
				myHand.add(card); // je l'ajoute a la liste des cartes dans ma main
				if (card.getCardType() == 0) { // si c'est un monstre
					monstersHand.add(card); // je l'ajoute a la liste des monstres dans ma main
					if (manaRestant >= card.getCost()) { // si j'ai asser de mana pour l'invoquer
						canInvoque.add(card); // je l'ajoute a ma liste des monstres que je peux invoquer
					}
				}
			} else if (card.getLocation() == 1) {
				myBoard.add(card);
				card.setCanAttack(true); // je debloque son attaque
			} else if (card.getLocation() == -1) {
				ennemyBoard.add(card);
			}
		}
	}
}

/**
 * Card.java
 * @author mateo
 * Tout ce qui concerne les cartes
 */
class Card {
	private int			cardNumber;
	private int			instanceId;
	private int			location;
	private int			cardType;
	private int			cost;
	private int			attack;
	private int			defense;
	private String		abilities;
	private boolean		canAttack; // true ssi le monstre peut attacker
	private int			myHealthChange;
	private int			opponentHealthChange;
	private int			cardDraw;
	private int			pickScore;
	private int			battleScore;

	public Card() {
		super();
		this.canAttack = false;
		this.pickScore = 0;
		this.battleScore = 0;
	}

	public Card(int cardNumber, int instanceId, int location, int cardType, int cost, int attack, int defense,
			String abilities, int myHealthChange, int opponentHealthChange, int cardDraw) {
		super();
		this.cardNumber = cardNumber;
		this.instanceId = instanceId;
		this.location = location;
		this.cardType = cardType;
		this.cost = cost;
		this.attack = attack;
		this.defense = defense;
		this.abilities = abilities;
		this.canAttack = false;
		this.myHealthChange = myHealthChange;
		this.opponentHealthChange = opponentHealthChange;
		this.cardDraw = cardDraw;
		this.pickScore = 0;
		this.battleScore = 0;
	}

	public int calculPickScore() {
		if (cardType == 0) { // si c'est un monstre
			pickScore = (attack + defense - cost * 2) * 4 + myHealthChange + opponentHealthChange + cardDraw * 2;
			if (abilities.contains("B")) {
				pickScore += attack * 0.5;
			}
			if (abilities.contains("C")) {
				pickScore += attack * 0.4;
			}
			if (abilities.contains("G")) {
				pickScore += attack * 0.4 + defense * 0.6;
			}
			if (abilities.contains("D")) {
				pickScore += attack * 0.6;
			}
			if (abilities.contains("L")) {
				pickScore += defense * 0.6;
			}
			if (abilities.contains("W")) {
				pickScore += attack * 0.5;
			}
			// TMP
			if (attack == 0) {
				pickScore -= 10;
			}
		} else { // si c'est un objet
			pickScore = myHealthChange + opponentHealthChange + cardDraw * 2;
			if (cardType == 1) { // si c'est un objet vert
				pickScore += (attack + defense - cost * 2) * 2 - 100; // on prend le meilleur mais on prefere toujours les monstres
			} else if (cardType == 2) { // si c'est un objet rouge
				pickScore += (defense - cost) * 2 - 500; // on prend le meilleur mais on prefere toujours les objets verts
			} else { // si c'est un objet bleu
				pickScore += (defense - cost) * 2 - 499; // alors on prend le meilleur (a peine mieux que les objets rouges)
			}
		}
		return pickScore;
	}

	public int calculBattleScore() {
		if (cardType == 0) { // si c'est un monstre
			battleScore = (attack + defense) * 3 + myHealthChange + opponentHealthChange;
			if (abilities.contains("B")) {
				battleScore += attack * 0.5;
			}
			if (abilities.contains("C")) {
				battleScore += attack * 0.4;
			}
			if (abilities.contains("G")) {
				battleScore += attack * 0.3 + defense * 0.6;
			}
			if (abilities.contains("D")) {
				battleScore += attack * 0.6;
			}
			if (abilities.contains("L")) {
				battleScore += defense * 0.6;
			}
			if (abilities.contains("W")) {
				battleScore += attack * 0.5;
			}
			// TMP
			if (attack == 0) {
				battleScore -= 10;
			}
		} else { // si c'est un objet
			battleScore = myHealthChange + opponentHealthChange;
			if (cardType == 1) { // si c'est un objet vert
				battleScore += (attack + defense - cost * 2) * 2 - 100; // on prend le meilleur mais on prefere toujours les monstres
			} else if (cardType == 2) { // si c'est un objet rouge
				battleScore += (defense - cost) * 2 - 500; // on prend le meilleur mais on prefere toujours les objets verts
			} else { // si c'est un objet bleu
				battleScore += (defense - cost) * 2 - 499; // alors on prend le meilleur (a peine mieux que les objets rouges)
			}
		}
		return battleScore;
	}

	public boolean getCanAttack() {
		return canAttack;
	}

	public void setCanAttack(boolean canAttack) {
		this.canAttack = canAttack;
	}

	public int getPickScore() {
		return pickScore;
	}

	public void setPickScore(int pickScore) {
		this.pickScore = pickScore;
	}

	public int getBattleScore() {
		return battleScore;
	}

	public void setBattleScore(int battleScore) {
		this.battleScore = battleScore;
	}

	public int getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(int cardNumber) {
		this.cardNumber = cardNumber;
	}

	public int getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(int instanceId) {
		this.instanceId = instanceId;
	}

	public int getLocation() {
		return location;
	}

	public void setLocation(int location) {
		this.location = location;
	}

	public int getCardType() {
		return cardType;
	}

	public void setCardType(int cardType) {
		this.cardType = cardType;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public int getAttack() {
		return attack;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}

	public int getDefense() {
		return defense;
	}

	public void setDefense(int defense) {
		this.defense = defense;
	}

	public String getAbilities() {
		return abilities;
	}

	public void setAbilities(String abilities) {
		this.abilities = abilities;
	}

	public int getMyHealthChange() {
		return myHealthChange;
	}

	public void setMyHealthChange(int myHealthChange) {
		this.myHealthChange = myHealthChange;
	}

	public int getOpponentHealthChange() {
		return opponentHealthChange;
	}

	public void setOpponentHealthChange(int opponentHealthChange) {
		this.opponentHealthChange = opponentHealthChange;
	}

	public int getCardDraw() {
		return cardDraw;
	}

	public void setCardDraw(int cardDraw) {
		this.cardDraw = cardDraw;
	}

}