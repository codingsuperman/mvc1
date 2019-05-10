package chapter3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

interface Solver {
	// void solve();
	List<Integer> getResult();
	List<String> getTrace();
}

class Edge {
	int v1, v2;

	public Edge(int v1, int v2) {
		if (v1 >= v2) {
			throw new IllegalArgumentException("v1 should be smaller than v2!");
		}
		this.v1 = v1;
		this.v2 = v2;
	}

	int otherNode(int value) {
		return (this.v1 == value ? this.v2 : this.v1);
	}
}

class Graph {
	int edgeNum;
	List<Edge>[] nodes;
	ArrayList<Integer> vertices;
	ArrayList<Edge> edges;

	public Graph(int nodeNum, int edgeNum) {
		nodes = new List[nodeNum];
		vertices = new ArrayList<Integer>();
		this.edgeNum = edgeNum;
		this.edges = new ArrayList<Edge>();
	}
}

class Bnb implements Solver {
	Graph graph;
	int bestResult;
	boolean[] presence;
	List<Integer> intermRes;
	List<Integer> result;
	List<String> trace;
	long startTime;
	double cutoff;

	public Bnb(Graph g, double cutoff) {
		this.graph = g;
		this.cutoff = cutoff;
		startTime = System.currentTimeMillis();
		trace = new ArrayList<String>();
		bestResult = Integer.MAX_VALUE; // MAX_VALUE as initial upper bound
		intermRes = new ArrayList<Integer>();
		presence = new boolean[graph.nodes.length]; // record whether node is used

		solve(0);
	}

	private void solve(int start) {
		if (start == graph.nodes.length) { // have checked all nodes

			if (intermRes.size() < bestResult) { // if this result is better than upper bound, update it
				result = new ArrayList<Integer>(intermRes);
				bestResult = intermRes.size();

				long endTime = System.currentTimeMillis();
				double duration = (double) (endTime - startTime) / 1000;
				trace.add(String.format("%.2f", duration) + "," + bestResult);
				System.out.println(trace.get(trace.size() - 1));
			}
			return;
		}
		for (int i = start; i < graph.nodes.length; i++) {
			if (intermRes.size() >= bestResult - 1) {
				break; // if the size of current solution plus lower bound (which is 1) is greater than
			} // upper bound, cut off

			int k = 0;
			List<Edge> neighbors = graph.nodes[i];
			while (k < neighbors.size() && presence[neighbors.get(k).otherNode(i)]) { // count how many neighbor covered
				k++;
			}
			if (k == neighbors.size()) {
				continue; // if all neighbor covered, don't need to add this one
			}
			presence[i] = true; // add this node in solution
			intermRes.add(i);
			if ((System.currentTimeMillis() - startTime) / 1000 > cutoff) {
				return;
			}
			solve(i + 1); // go check remaining graph
			intermRes.remove(intermRes.size() - 1);// remove it
			presence[i] = false;
			while (k < neighbors.size()
					&& (neighbors.get(k).otherNode(i) > i || presence[neighbors.get(k).otherNode(i)])) { // count how
																											// covered
				k++;
			}
			if (k < neighbors.size()) { // if any of the neighbors with smaller index are not covered, we can't skip
										// this node, otherwise will leave some edge never covered. So break
				break;
			}
		}

	}

	@Override
	public List<Integer> getResult() {
		return result;
	}

	@Override
	public List<String> getTrace() {
		return trace;
	}

}

public class RunExperiments_1 {
	public static void main(String[] args) throws Exception {

		if (args.length < 4) {
			System.err.println("Unexpected number of command line arguments");
			System.exit(1);
		}

		String inst = "", alg = "", time = "", seed = "";
		for (int i = 0; i < args.length; i = i + 2) {
			if (args[i].equals("-inst"))
				inst = args[i + 1];
			if (args[i].equals("-alg"))
				alg = args[i + 1];
			if (args[i].equals("-time"))
				time = args[i + 1];
			if (args[i].equals("-seed"))
				seed = args[i + 1];
		}
		if (alg.equals("") || inst.equals("") || time.equals("")) {
			System.out.println("Incorrect input");
			System.exit(0);
		}
		if ((alg.equals("LS1") || alg.equals("LS2")) && (seed.equals(""))) {
			System.out.println("Seed is required for local search to run");
			System.exit(0);
		}

		String output_sol = "", output_trace = "";
		int end = inst.indexOf(".graph");
		int start = inst.lastIndexOf("/");
		if (alg.equals("BnB") || alg.equals("Approx")) {
			output_sol = inst.substring(start + 1, end) + "_" + alg + "_" + time + ".sol";
			output_trace = inst.substring(start + 1, end) + "_" + alg + "_" + time + ".trace";
		} else {
			output_sol = inst.substring(start + 1, end) + "_" + alg + "_" + time + "_" + seed + ".sol";
			output_trace = inst.substring(start + 1, end) + "_" + alg + "_" + time + "_" + seed + ".trace";
		}

		PrintWriter sol_writer = new PrintWriter(output_sol, "UTF-8");
		PrintWriter trace_writer = new PrintWriter(output_trace, "UTF-8");
		Graph G = parseGraph(inst);
		Solver solver = null;

		if (alg.equals("BnB")) {
			solver = new Bnb(G, Double.parseDouble(time));
		}
		if (solver == null)
			System.exit(1);

		List<Integer> res = solver.getResult();
		List<String> traces = solver.getTrace();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < res.size(); i++) { // assemble sol file
			sb.append(Integer.toString(res.get(i) + 1));
			sb.append(", ");
		}

		System.out.println(sb.toString());
	
			for (int i = 0; i < traces.size(); i++) {
				trace_writer.println(traces.get(i));
				System.out.println(traces.get(i));

			}
		
		sb.setLength(sb.length() - 2);
		sol_writer.println(res.size());
		sol_writer.println(sb.toString());
		sol_writer.close();
		trace_writer.close();
	}

	static Graph parseGraph(String graph_file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(graph_file));
		String line = br.readLine();
		String[] split = line.split(" ");
		int nodeNum = Integer.parseInt(split[0]);
		int edgeNum = Integer.parseInt(split[1]);
		Graph G = new Graph(nodeNum, edgeNum);
		for (int i = 0; i < nodeNum; i++) {
			G.nodes[i] = new ArrayList<Edge>();
		}
		int lineIndex = 0;
		while ((line = br.readLine()) != null) {
			split = line.split(" ");
			for (int i = 0; i < split.length; i++) {
				if (split[i].equals(" ")) {
					continue;
				}
				int neighbor = Integer.parseInt(split[i]) - 1;

				if (neighbor > lineIndex) {
					Edge newEdge = new Edge(lineIndex, neighbor);
					G.nodes[lineIndex].add(newEdge);
					G.nodes[neighbor].add(newEdge);
					if (!G.edges.contains(newEdge)) {
						G.edges.add(newEdge);
					}
				}
				if (!G.vertices.contains(lineIndex)) {
					G.vertices.add(lineIndex);
				}
			}
			lineIndex++;
		}
		br.close();
		return G;
	}
}