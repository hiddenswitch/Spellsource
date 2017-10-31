package net.demilich.metastone.game.shared.threat.cuckoo;

import net.demilich.metastone.game.shared.threat.FeatureVector;

public interface IFitnessFunction {

	double evaluate(FeatureVector featureVector);

}
