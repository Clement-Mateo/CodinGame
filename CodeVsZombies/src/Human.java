/**
 * Human.java
 * date de creation : 28 nov. 2020
 */

/**
 * 
 */
public class Human {

	private int	id;
	private int	x;
	private int	y;

	/**
	 * constructeur
	 * 
	 * @param x
	 * @param y
	 */
	public Human() {
		// unusued for now
	}

	/**
	 * constructeur
	 * 
	 * @param id
	 * @param x
	 * @param y
	 */
	public Human(int id, int x, int y) {
		super();
		this.id = id;
		this.x = x;
		this.y = y;
	}

	/**
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id le champ a modifier
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x le champ a modifier
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y le champ a modifier
	 */
	public void setY(int y) {
		this.y = y;
	}
}
