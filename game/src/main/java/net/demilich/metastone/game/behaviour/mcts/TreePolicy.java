package net.demilich.metastone.game.behaviour.mcts;

/**
 * A function that determines which node to select from a parent.
 */
@FunctionalInterface
interface TreePolicy {

	Node select(Node parent);
}
