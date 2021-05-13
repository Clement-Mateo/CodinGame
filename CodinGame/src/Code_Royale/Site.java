class Site {

	int	siteId;
	int	x;
	int	y;
	int	radius;
	int	ignore1;
	int	ignore2;
	int	structureType;
	int	owner;
	int	param1;
	int	param2;
	String unitType;

	public Site() {

	}

	public int getDistance(int xGiven, int yGiven) {
		return (int) Math.sqrt(Math.pow(xGiven-x, 2) + Math.pow(yGiven-y, 2));
	}

	public int getSiteId() {
		return siteId;
	}

	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public int getIgnore1() {
		return ignore1;
	}

	public void setIgnore1(int ignore1) {
		this.ignore1 = ignore1;
	}

	public int getIgnore2() {
		return ignore2;
	}

	public void setIgnore2(int ignore2) {
		this.ignore2 = ignore2;
	}

	public int getStructureType() {
		return structureType;
	}

	public void setStructureType(int structureType) {
		this.structureType = structureType;
	}

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	public int getParam1() {
		return param1;
	}

	public void setParam1(int param1) {
		this.param1 = param1;
	}

	public int getParam2() {
		return param2;
	}

	public void setParam2(int param2) {
		this.param2 = param2;
	}

	public String getUnitType() {
		return unitType;
	}

	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}

}