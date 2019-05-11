package p2;

import java.util.*;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
class Edge {
    int v1, v2;
    public Edge(int v1, int v2) {
        if (v1 >= v2) throw new IllegalArgumentException("v1 should be smaller than v2!");
        this.v1 = v1;
        this.v2 = v2;
    }
    int otherNode(int value) {
        return (this.v1 == value ? this.v2 : this.v1);
    }
}



 interface Solver {
    List<Integer> getResult();
    List<String> getTrace();
}


 class Graph {

    int edgeNum;
    List<Edge>[] nodes;
    ArrayList<Integer> vertices;
    ArrayList<Edge> edges;
    public Graph (int nodeNum, int edgeNum) {
        nodes = new List[nodeNum];
        vertices = new ArrayList<>();
        this.edgeNum = edgeNum;
        this.edges = new ArrayList<>();
    }
   
}
 
 
 class h1 implements Solver{

	    Graph graph;
	    List<Integer> res;
	    List<String> trace;
	    double cutoff;
	    long startTime;

	    public h1(Graph g, double cutoff) {
	        this.graph = g;
	        this.cutoff = cutoff;
	        trace = new ArrayList<>();
	        startTime = System.currentTimeMillis();
	        solve();
	        long endTime = System.currentTimeMillis();
	        double duration = (double)(endTime - startTime) / 1000;
	        trace.add(String.format("%.2f", duration) + "," + res.size());
	        System.out.println(trace);
	    }

	    private void solve() {

	        List<Integer> vertexCover = new ArrayList<>();
	        int edgeNum = graph.edgeNum;
	        Map<Integer, Integer> nodeDegrees = new HashMap<>(graph.nodes.length);
	        for (int i = 0; i < graph.nodes.length; i++) {
	            nodeDegrees.put(i, (graph.nodes[i].size()));
	        }
	        while(edgeNum > 0) {
	            int maxDegree = Integer.MIN_VALUE;
	            int nodeToDelete = -1;
	            for (Map.Entry<Integer, Integer> entry : nodeDegrees.entrySet()) {
	                int degree = entry.getValue();
	                if (degree > maxDegree) {
	                    maxDegree = degree;
	                    nodeToDelete = entry.getKey();
	                }
	            }
	            System.out.println("nodeToDelete: " + nodeToDelete);
	            int deletedEdgeNum = 0;
	            for (Edge edge : graph.nodes[nodeToDelete]) {
	                int neighbor = edge.otherNode(nodeToDelete);
	                if (nodeDegrees.containsKey(neighbor)) {
	                    nodeDegrees.put(neighbor, nodeDegrees.get(neighbor) - 1);
	                    deletedEdgeNum++;
	                }
	            }
	            System.out.println("deletedEdgeNum: " + deletedEdgeNum);
	            nodeDegrees.remove(nodeToDelete);
	            edgeNum -= deletedEdgeNum;
	            vertexCover.add(nodeToDelete);
	            System.out.println("edgeNum: " + edgeNum);
	        }
	        this.res = vertexCover;
	    }

	    @Override
	    public List<Integer> getResult() {
	        return res;
	    }
	    @Override
	    public List<String> getTrace() {
	        return trace;
	    }
	}

 
 
 

public class MDG {
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
		if ( alg.equals("Approx")) {
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

		if (alg.equals("MDG")) {
			solver = new h1(G, Double.parseDouble(time));
		} 
		if (solver == null) System.exit(1);

		List<Integer> res = solver.getResult();
		List<String> traces = solver.getTrace();

		StringBuilder sb = new StringBuilder();
		for (int num : res) { 
			sb.append(Integer.toString(num + 1));
			sb.append(", ");
		}

		System.out.println(sb.toString());
			for (String trace : traces) {
				trace_writer.println(trace);
				System.out.println(trace);
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
			G.nodes[i] = new ArrayList<>();
		}
		int lineIndex = 0;
		while ((line = br.readLine()) != null) {
			split = line.split(" ");
			for (String neighborS : split) {
				if (neighborS.equals("")) continue;
				int neighbor = Integer.parseInt(neighborS) - 1;
				if (neighbor > lineIndex) {
					Edge newEdge = new Edge(lineIndex, neighbor);
					G.nodes[lineIndex].add(newEdge);
					G.nodes[neighbor].add(newEdge);
					if (!G.edges.contains(newEdge))
						G.edges.add(newEdge);
				}
				if (!G.vertices.contains(lineIndex))
					G.vertices.add(lineIndex);
			}
			lineIndex++;
		}
		br.close();
		return G;
	}
}

