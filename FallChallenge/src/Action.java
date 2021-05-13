import java.util.ArrayList;

class Action {
	private int					actionId;								// the unique ID of this spell or recipe
	private String				actionType;								// in the first league: BREW; later: CAST, OPPONENT_CAST, LEARN, BREW
	private ArrayList<Integer>	ressources	= new ArrayList<Integer>();
	private int					price;									// the price in rupees if this is a potion
	private int					tomeIndex;								// in the first two leagues: always 0; later: the index in the tome if this is a
																		// tome spell, equal to the read-ahead tax; For brews, this is the value of the
																		// current urgency bonus
	private int					taxCount;								// in the first two leagues: always 0; later: the amount of taxed tier-0
																		// ingredients you gain from learning this spell; For brews, this is how many
																		// times you can still gain an urgency bonus
	private boolean				castable;								// in the first league: always 0; later: 1 if this is a castable player spell
	private boolean				repeatable;								// for the first two leagues: always 0; later: 1 if this is a repeatable player
																		// spell

	public Action() {
		this.price = 0;
	}

	public Action(int actionId, String actionType, ArrayList<Integer> ressources, int price, int tomeIndex, int taxCount, boolean castable,
			boolean repeatable) {
		super();
		this.actionId = actionId;
		this.actionType = actionType;
		this.ressources = ressources;
		this.price = price;
		this.tomeIndex = tomeIndex;
		this.taxCount = taxCount;
		this.castable = castable;
		this.repeatable = repeatable;
	}

	/**
	 * @return le prix de la potion avec le bonus d'urgence appliqué
	 */
	public int getRealPrice() {
		return price + tomeIndex;
	}

	public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
		this.actionId = actionId;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public ArrayList<Integer> getRessources() {
		return ressources;
	}

	public void setRessources(ArrayList<Integer> ressources) {
		this.ressources = ressources;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getTomeIndex() {
		return tomeIndex;
	}

	public void setTomeIndex(int tomeIndex) {
		this.tomeIndex = tomeIndex;
	}

	public int getTaxCount() {
		return taxCount;
	}

	public void setTaxCount(int taxCount) {
		this.taxCount = taxCount;
	}

	public boolean isCastable() {
		return castable;
	}

	public void setCastable(boolean castable) {
		this.castable = castable;
	}

	public boolean isRepeatable() {
		return repeatable;
	}

	public void setRepeatable(boolean repeatable) {
		this.repeatable = repeatable;
	}
}