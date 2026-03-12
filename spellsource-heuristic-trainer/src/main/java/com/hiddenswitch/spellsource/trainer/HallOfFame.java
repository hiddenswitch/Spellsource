package com.hiddenswitch.spellsource.trainer;

import net.demilich.metastone.game.behaviour.heuristic.FeatureVector;
import net.demilich.metastone.game.behaviour.heuristic.WeightedFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Hall of Fame archive for competitive coevolution.
 * <p>
 * Maintains a fixed-size archive of historically strong FeatureVectors.
 * Candidates are evaluated against all HoF members, so fitness measures
 * general strength rather than ability to exploit a single opponent.
 * <p>
 * Based on Rosin &amp; Belew (1997) "New Methods for Competitive Coevolution"
 * and Nogueira &amp; Cotta (2013) "An Analysis of Hall-of-Fame Strategies in
 * Competitive Coevolutionary Algorithms for Self-Learning in RTS Games".
 * <p>
 * Selection uses maximum diversity: when the archive is full, the new member
 * replaces the existing member most similar to another member, keeping the
 * archive as diverse as possible.
 */
public class HallOfFame {
	private static final Logger LOG = Logger.getLogger(HallOfFame.class.getName());

	private final int maxSize;
	private final List<FeatureVector> members;

	public HallOfFame(int maxSize) {
		this.maxSize = maxSize;
		this.members = new ArrayList<>();
	}

	/**
	 * Seeds the HoF with the initial baseline (e.g. getFittest()).
	 */
	public void seed(FeatureVector initial) {
		members.clear();
		members.add(initial.clone());
		LOG.info("HoF seeded with 1 member");
	}

	/**
	 * Attempts to add a new member to the HoF.
	 * If the archive is not full, the member is added directly.
	 * If full, uses diversity-based replacement: replaces the member
	 * that is most similar to its nearest neighbor (least diverse).
	 *
	 * @return true if the member was added
	 */
	public synchronized boolean add(FeatureVector candidate) {
		if (members.size() < maxSize) {
			members.add(candidate.clone());
			LOG.info(String.format("HoF: added member (%d/%d)", members.size(), maxSize));
			return true;
		}

		// Find the pair of existing members with smallest distance
		// Replace the one that contributes least to diversity
		double minDist = Double.MAX_VALUE;
		int replaceIdx = -1;

		for (int i = 0; i < members.size(); i++) {
			double nearestDist = Double.MAX_VALUE;
			for (int j = 0; j < members.size(); j++) {
				if (i == j) continue;
				double d = distance(members.get(i), members.get(j));
				if (d < nearestDist) {
					nearestDist = d;
				}
			}
			if (nearestDist < minDist) {
				minDist = nearestDist;
				replaceIdx = i;
			}
		}

		// Only replace if the candidate is sufficiently different from its nearest existing member
		double candidateMinDist = Double.MAX_VALUE;
		for (FeatureVector m : members) {
			double d = distance(candidate, m);
			if (d < candidateMinDist) {
				candidateMinDist = d;
			}
		}

		if (candidateMinDist > minDist && replaceIdx >= 0) {
			members.set(replaceIdx, candidate.clone());
			LOG.info(String.format("HoF: replaced member %d (diversity: %.1f > %.1f)", replaceIdx, candidateMinDist, minDist));
			return true;
		}

		return false;
	}

	/**
	 * Returns an unmodifiable view of the current HoF members.
	 */
	public synchronized List<FeatureVector> getMembers() {
		return Collections.unmodifiableList(new ArrayList<>(members));
	}

	public synchronized int size() {
		return members.size();
	}

	/**
	 * Euclidean distance between two FeatureVectors.
	 */
	private static double distance(FeatureVector a, FeatureVector b) {
		double sum = 0;
		for (WeightedFeature f : WeightedFeature.values()) {
			double diff = a.get(f) - b.get(f);
			sum += diff * diff;
		}
		return Math.sqrt(sum);
	}
}
