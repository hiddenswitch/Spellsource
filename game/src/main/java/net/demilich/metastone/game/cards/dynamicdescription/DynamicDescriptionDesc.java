package net.demilich.metastone.game.cards.dynamicdescription;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.EventTriggerDescDeserializer;

import java.util.Map;

@JsonDeserialize(using = DynamicDescriptionDeserializer.class)
public class DynamicDescriptionDesc extends Desc<DynamicDescriptionArg, DynamicDescription> {

    public DynamicDescriptionDesc() {
        super(DynamicDescriptionArg.class);
    }

    public DynamicDescriptionDesc(Class<? extends DynamicDescription> desctiptionClass) {
        super(desctiptionClass, DynamicDescriptionArg.class);
    }

    @Override
    protected Class<? extends Desc> getDescImplClass() {
        return DynamicDescriptionDesc.class;
    }

    @Override
    public DynamicDescriptionArg getClassArg() {
        return DynamicDescriptionArg.CLASS;
    }

    @Override
    public Desc<DynamicDescriptionArg, DynamicDescription> clone() {
        return (DynamicDescriptionDesc) copyTo(new DynamicDescriptionDesc(getDescClass()));
    }
}
