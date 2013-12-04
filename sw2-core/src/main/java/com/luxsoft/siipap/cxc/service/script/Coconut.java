package com.luxsoft.siipap.cxc.service.script;

public class Coconut {

	public Coconut() {
	}

	public void drinkThemBothUp() {
		System.out.println("Probando scripting....");
		lime.drink();

	}

	private Lime lime;

	public void setLime(Lime lime) {
		this.lime = lime;
	}

}
