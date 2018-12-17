package net.demilich.metastone.game.behaviour;

public abstract class IntelligentBehaviour extends AbstractBehaviour {
	private static final long serialVersionUID = 1918440329086472977L;

	@Override
	public boolean isHuman() {
		return false;
	}
}
