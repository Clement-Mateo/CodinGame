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