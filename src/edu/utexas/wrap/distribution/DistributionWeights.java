package edu.utexas.wrap.distribution;

/**
 * Stores the production (A) and attraction (B) balancing weights used
 * in the doubly-constrained gravity model. These weights are iteratively
 * updated during distribution to ensure that row and column totals of the
 * trip matrix match the target productions and attractions.
 *
 * @author Will Alexander
 * @see BasicDistributionWeights
 * @see GravityDistributor
 */
public interface DistributionWeights {

	Double[] getProductionWeights();

	Double[] getAttractionWeights();

	void updateWeights(Double[] producerWeights, Double[] attractorWeights);

}
