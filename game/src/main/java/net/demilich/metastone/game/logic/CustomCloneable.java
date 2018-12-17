package net.demilich.metastone.game.logic;

import java.io.Serializable;

public class CustomCloneable implements Cloneable, Serializable {

	private static final long serialVersionUID = -385083408761766369L;

	@Override
	public CustomCloneable clone() {
		try {
			return (CustomCloneable) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
