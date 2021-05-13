class Tree {
	
	public Cell cell;
	public int size;
	public boolean isMine;
	public boolean isDormant;
	
	public Tree() {
		
	}

	public Tree(Cell cell, int size, boolean isMine, boolean isDormant) {
		super();
		this.cell = cell;
		this.size = size;
		this.isMine = isMine;
		this.isDormant = isDormant;
	}
}