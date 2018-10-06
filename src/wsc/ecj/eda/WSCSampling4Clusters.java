package wsc.ecj.eda;

import java.util.List;

import com.google.common.primitives.Doubles;

import ec.EvolutionState;
import ec.Individual;
import wsc.data.kmean.KMean;
import wsc.data.kmean.KMean.LocationWrapper;
import wsc.data.pool.Service;
import wsc.ecj.nsga2.SequenceVectorIndividual;
import wsc.nhbsa.NHBSA;
import wsc.problem.WSCInitializer;

import ec.Subpopulation;
import ec.multiobjective.nsga2.NSGA2MultiObjectiveFitness;

public class WSCSampling4Clusters {

	public SequenceVectorIndividual[] sampling4Clusters(EvolutionState state) {

		WSCInitializer init = (WSCInitializer) state.initializer;

		int numCluster4Sampling = 0;
		for (int i = 0; i < KMean.clusterResults.size(); i++) {
			if (KMean.clusterResults.get(i).getPoints().size() > 1) {
				numCluster4Sampling++;
			}
		}

		SequenceVectorIndividual[] pop_sampled = new SequenceVectorIndividual[WSCInitializer.numLocalSearchTries
				* numCluster4Sampling];

		int j = 0;

		for (int i = 0; i < KMean.clusterResults.size(); i++) {
			if (KMean.clusterResults.get(i).getPoints().size() > 1) {

				List<int[]> pop_updated = sampleNeighbors(init, state, KMean.clusterResults.get(i).getPoints());

				for (int[] indi_genome : pop_updated) {
					SequenceVectorIndividual t1 = (SequenceVectorIndividual) (state.population.subpops[0].individuals[0]
							.clone());
					updatedIndi(t1.genome, indi_genome);
					t1.evaluated = false;
					pop_sampled[j] = t1;
					j++;
				}
			}

		}
		return pop_sampled;
	}

	private List<int[]> sampleNeighbors(WSCInitializer init, EvolutionState state,
			List<LocationWrapper> locationWrapperList) {
		// Get population

		Subpopulation pop = state.population.subpops[0];

		int numIndi4Cluser = locationWrapperList.size();
		// System.out.println("learn a NHM from a pop size: " + pop.individuals.length);
		NHBSA nhbsa = new NHBSA(numIndi4Cluser, WSCInitializer.dimension_size);

		int[][] m_generation = new int[numIndi4Cluser][WSCInitializer.dimension_size];

		double euclideanDistances[] = new double[numIndi4Cluser];
		double normalizedPenalization[] = new double[numIndi4Cluser];

		for (int m = 0; m < numIndi4Cluser; m++) {

			euclideanDistances[m] = locationWrapperList.get(m).getEuclidean_distance();
			int index = locationWrapperList.get(m).getLocation().getIndi_index();

			for (int n = 0; n < WSCInitializer.dimension_size; n++) {
				m_generation[m][n] = ((SequenceVectorIndividual) (pop.individuals[index])).serQueue.get(n);
			}
		}

		normalizeEuclideanDistances4Subproblem(euclideanDistances, normalizedPenalization);

		nhbsa.setM_pop(m_generation);
		nhbsa.setM_L(WSCInitializer.dimension_size);
		nhbsa.setM_N(numIndi4Cluser);
		nhbsa.setNormalizedConsineSIM(normalizedPenalization);

		// Sample numLocalSearchTries number of neighbors
		return nhbsa.sampling4NHBSA(WSCInitializer.numLocalSearchTries, WSCInitializer.random);
	}

	private void normalizeEuclideanDistances4Subproblem(double[] euclideanDistances, double[] normalizedPenalization) {
		for (int i = 0; i < euclideanDistances.length; i++) {
			double min = 0.0;
			double max = 1.0;
			// lower score has higher influence on frequency
			normalizedPenalization[i] = (max - euclideanDistances[i]) / (max - min);
		}

	}

	public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		for (int i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);
		}
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}

	private void updatedIndi(Service[] genome, int[] updateIndi) {
		for (int n = 0; n < updateIndi.length; n++) {
			genome[n] = WSCInitializer.Index2ServiceMap.get(updateIndi[n]);
		}
	}

}
