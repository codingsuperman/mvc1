
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

interface Solver {

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
	int numEdge;
	List<Edge>[] nodes;
	ArrayList<Integer> vertices;
	ArrayList<Edge> edges;

	public Graph(int nodeNum, int numEdge) {
		nodes = new List[nodeNum];
		vertices = new ArrayList<Integer>();
		this.numEdge = numEdge;
		this.edges = new ArrayList<Edge>();
	}
}

class b1 implements Solver {
	Graph graph;
	int MiniResult;
	boolean[] presence;
	List<Integer> currentRes;
	List<Integer> result;
	List<String> trace;
	long startTime;
	double cutoff;

	public b1(Graph g, double cutoff) {
		this.graph = g;
		this.cutoff = cutoff;
		startTime = System.currentTimeMillis();
		trace = new ArrayList<String>();
		MiniResult = Integer.MAX_VALUE; 
		currentRes = new ArrayList<Integer>();
		presence = new boolean[graph.nodes.length]; 

		solve(0);
	}

	private void solve(int start) {
		if (start == graph.nodes.length) { 

			if (currentRes.size() < MiniResult) { 
				result = new ArrayList<Integer>(currentRes);
				MiniResult = currentRes.size();

				long endTime = System.currentTimeMillis();
				double duration = (double) (endTime - startTime) / 1000;
				trace.add(String.format("%.2f", duration) + "," + MiniResult);
				System.out.println(trace.get(trace.size() - 1));
			}
			return;
		}
		for (int i = start; i < graph.nodes.length; i++) {
			if (currentRes.size() >= MiniResult - 1) {
				break; 
			} 

			int m = 0;
			List<Edge> neighbors = graph.nodes[i];
			while (m < neighbors.size() && presence[neighbors.get(m).otherNode(i)]) { 
				m++;
			}
			if (m == neighbors.size()) {
				continue; 
			}
			presence[i] = true; 
			currentRes.add(i);
			if ((System.currentTimeMillis() - startTime) / 1000 > cutoff) {
				return;
			}
			solve(i + 1); 
			currentRes.remove(currentRes.size() - 1);
			presence[i] = false;
			while (m < neighbors.size()
					&& (neighbors.get(m).otherNode(i) > i || presence[neighbors.get(m).otherNode(i)])) { 
																											
				m++;
			}
			if (m < neighbors.size()) { 
										
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

public class backtracking {
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
		

		String output_sol = "", output_trace = "";
		int end = inst.indexOf(".graph");
		int start = inst.lastIndexOf("/");
		if (alg.equals("backtracking") || alg.equals("Approx")) {
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

		if (alg.equals("backtracking")) {
			solver = new b1(G, Double.parseDouble(time));
		}
		if (solver == null)
			System.exit(1);

		List<Integer> res = solver.getResult();
		List<String> traces = solver.getTrace();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < res.size(); i++) { 
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
		int numEdge = Integer.parseInt(split[1]);
		Graph G = new Graph(nodeNum, numEdge);
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
