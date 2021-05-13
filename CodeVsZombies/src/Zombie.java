/**
 * Zombie.java
 * date de creation : 29 nov. 2020
 */

/**
 * 
 */
public class Zombie extends Human {
	private int	XNext;
	private int	YNext;

	/**
	 * constructeur
	 */
	public Zombie() {
		super();
	}

	/**
	 * @return XNext
	 */
	public int getXNext() {
		return XNext;
	}

	/**
	 * @param xNext le champ a modifier
	 */
	public void setXNext(int xNext) {
		XNext = xNext;
	}

	/**
	 * @return YNext
	 */
	public int getYNext() {
		return YNext;
	}

	/**
	 * @param yNext le champ a modifier
	 */
	public void setYNext(int yNext) {
		YNext = yNext;
	}

}
