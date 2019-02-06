package net.demilich.metastone.game.cards.desc;

public interface HasDesc<T extends Desc<?, ?>> {
	T getDesc();

	void setDesc(Desc<?, ?> desc);
}
