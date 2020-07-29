package net.demilich.metastone.game.cards.dynamicdescription;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.HasDesc;
import net.demilich.metastone.game.cards.desc.HasDescSerializer;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.CustomCloneable;

/**
 * The base class for dynamic descriptions.
 * <p>
 * Dynamic descriptions are simple concatenated strings, typically implemented with a {@link MetaDescription}, like so:
 *
 * <pre>
 *   "dynamicDescription": [
 *     {
 *       "class": "MetaDescription",
 *       "descriptions": [
 *         {
 *           "class": "ValueDescription",
 *           "value": {
 *             "class": "AttributeValueProvider",
 *             "target": "FRIENDLY_PLAYER",
 *             "attribute": "JADE_BUFF",
 *             "offset": 1
 *           }
 *         },
 *         "/",
 *         {
 *           "class": "ValueDescription",
 *           "value": {
 *             "class": "AttributeValueProvider",
 *             "target": "FRIENDLY_PLAYER",
 *             "attribute": "JADE_BUFF",
 *             "offset": 1
 *           }
 *         },
 *         " "
 *       ]
 *     }
 *   ]
 * </pre>
 */
@JsonSerialize(using = HasDescSerializer.class)
public abstract class DynamicDescription extends CustomCloneable implements HasDesc<DynamicDescriptionDesc> {

	private DynamicDescriptionDesc desc;

	public DynamicDescription(DynamicDescriptionDesc desc) {
		this.desc = desc;
	}

	public abstract String resolveFinalString(GameContext context, Player player, Entity entity);

	@Override
	public DynamicDescriptionDesc getDesc() {
		return desc;
	}

	@Override
	public void setDesc(Desc<?, ?> desc) {
		this.desc = (DynamicDescriptionDesc) desc;
	}
}
