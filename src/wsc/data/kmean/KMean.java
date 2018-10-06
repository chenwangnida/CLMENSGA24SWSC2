package wsc.data.kmean;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import ec.Subpopulation;
import ec.multiobjective.nsga2.NSGA2MultiObjectiveFitness;
import wsc.problem.WSCInitializer;

public class KMean {

	public static List<CentroidCluster<LocationWrapper>> clusterResults;

	public void produceClusters(int numClusters, List<Location> locations, Subpopulation pop) {

		List<LocationWrapper> clusterInput = new ArrayList<LocationWrapper>(locations.size());
		for (Location location : locations)
			clusterInput.add(new LocationWrapper(location));

		// initialize a new clustering algorithm.
		// we use KMeans++ with 3 clusters and 10000 iterations maximum.
		KMeansPlusPlusClusterer<LocationWrapper> clusterer = new KMeansPlusPlusClusterer<LocationWrapper>(numClusters,
				10000, new EuclideanDistance(), WSCInitializer.randomGenerator);
		if (clusterResults != null) {
			clusterResults.clear();
		}

		clusterResults = clusterer.cluster(clusterInput);

		// output the clusters
		for (int i = 0; i < clusterResults.size(); i++) {
			System.out.println("Cluster " + i);

			// find best dominant solution
			LocationWrapper BestLocationWrapper = null;
			int bestRank = Integer.MAX_VALUE;

			for (LocationWrapper locationWrapper : clusterResults.get(i).getPoints()) {
				NSGA2MultiObjectiveFitness fit_i = (NSGA2MultiObjectiveFitness) pop.individuals[locationWrapper
						.getLocation().getIndi_index()].fitness;
				if (fit_i.rank < bestRank) {
					bestRank = fit_i.rank;
					BestLocationWrapper = locationWrapper;
					System.out.println("update rank:" + bestRank);
					if (bestRank == 0) {
						break;
					}
				}
			}

			// calculate distance to the best fit point

			for (LocationWrapper locationWrapper : clusterResults.get(i).getPoints()) {

				double euclidean_distance = calculateDistance(locationWrapper.getPoint(),
						BestLocationWrapper.getPoint());
				locationWrapper.setEuclidean_distance(euclidean_distance);
			}
		}

	}

	public static class LocationWrapper implements Clusterable {
		private double[] points;
		private Location location;
		private double diff;// difference between y and predicted y
		private double euclidean_distance;

		public LocationWrapper(Location location) {
			this.location = location;
			this.points = new double[] { location.getX(), location.getY() };
		}

		public Location getLocation() {
			return location;
		}

		public double[] getPoint() {
			return points;
		}

		public double getDiff() {
			return diff;
		}

		public void setDiff(double diff) {
			this.diff = diff;
		}

		public double getEuclidean_distance() {
			return euclidean_distance;
		}

		public void setEuclidean_distance(double euclidean_distance) {
			this.euclidean_distance = euclidean_distance;
		}

	}

	private static double calculateDistance(double[] vector1, double[] vector2) {
		double sum = 0;
		for (int i = 0; i < vector1.length; i++) {
			sum += Math.pow((vector1[i] - vector2[i]), 2);
		}
		return Math.sqrt(sum);
	}

}
