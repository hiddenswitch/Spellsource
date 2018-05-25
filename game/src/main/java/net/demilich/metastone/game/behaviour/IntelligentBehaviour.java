package net.demilich.metastone.game.behaviour;

public abstract class IntelligentBehaviour extends AbstractBehaviour {
	@Override
	public boolean isHuman() {
		return false;
	}
}
