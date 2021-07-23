package edu.utexas.wrap.distribution;

public interface DistributionWeights {

	Double[] getProductionWeights();

	Double[] getAttractionWeights();

	void updateWeights(Double[] producerWeights, Double[] attractorWeights);

}
