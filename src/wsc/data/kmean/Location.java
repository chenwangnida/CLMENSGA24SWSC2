package wsc.data.kmean;

public class Location {
	private double x;
	private double y;

	private int indi_index; // index mapped to pop

	public Location(double x, double y, int indi_index) {
		this.x = x;
		this.y = y;
		this.indi_index = indi_index;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public int getIndi_index() {
		return indi_index;
	}

	public void setIndi_index(int indi_index) {
		this.indi_index = indi_index;
	}

}
