package wsc.ecj.nsga2;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ObjectArrays;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.multiobjective.MultiObjectiveFitness;
import ec.multiobjective.nsga2.NSGA2Evaluator;
import ec.multiobjective.nsga2.NSGA2MultiObjectiveFitness;
import wsc.data.kmean.KMean;
import wsc.data.kmean.Location;
import wsc.ecj.eda.WSCSampling4Clusters;
import wsc.problem.WSCInitializer;

public class WSCNSGA2Evaluator extends NSGA2Evaluator {

	public static Individual[] sampledIndi1;

	@Override
	public void evaluatePopulation(EvolutionState state) {

		if (sampledIndi1 != null) {

			Individual[] sampledIndi2 = state.population.subpops[0].individuals;
			Individual[] combined = ObjectArrays.concat(sampledIndi1, sampledIndi2, Individual.class);

			state.population.subpops[0].individuals = combined;
		}

		super.evaluatePopulation(state);
		for (int x = 0; x < state.population.subpops.length; x++) {
			state.population.subpops[x].individuals = buildArchive(state, x);
		}
		// System.out.println("Finish Non-dominated Sorting");

		// Cluster individuals using K-Mean ++
		clusterPopulation(state);

		// Generate sampled individuals for next generation, but combine later on
		WSCSampling4Clusters cl = new WSCSampling4Clusters();
		sampledIndi1 = cl.sampling4Clusters(state);

	}

	private void clusterPopulation(EvolutionState state) {
		KMean cluster = new KMean();
		// we have a list of our locations we want to cluster. create a
		List<Location> locations = new ArrayList<Location>();

		// Get population
		Subpopulation pop = (Subpopulation) state.population.subpops[0];

		// Sort pop
		for (int i = 0; i < pop.individuals.length; i++) {
			NSGA2MultiObjectiveFitness fit_i = (NSGA2MultiObjectiveFitness) pop.individuals[i].fitness;
			double f1xi = fit_i.getObjective(0);
			double f2xi = fit_i.getObjective(1);
			locations.add(new Location(f1xi, f2xi, i));
		}
		cluster.produceClusters(WSCInitializer.numClusters, locations, pop);

	}

}
