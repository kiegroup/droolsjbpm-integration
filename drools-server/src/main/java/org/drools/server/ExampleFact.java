package org.drools.server;

public class ExampleFact {
	private String carType;
	private int carPrice;
	public String getCarType() {
		return carType;
	}
	public void setCarType(String carType) {
		this.carType = carType;
	}
	public int getCarPrice() {
		return carPrice;
	}
	public void setCarPrice(int carPrice) {
		this.carPrice = carPrice;
	}
	public ExampleFact(String carType, int carPrice) {
		super();
		this.carType = carType;
		this.carPrice = carPrice;
	}

	public ExampleFact() {}
}
