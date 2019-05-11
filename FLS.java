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
 
 

 class l1 implements Solver{
    Graph graph;
    List<Integer> bestVC = null; 
    List<String> trace = null;  
    public l1(Graph graph, double cutoff, int seed){
        this.graph = graph;
        trace =new ArrayList<>();
        solve(cutoff, seed);
    }

    public void solve(double cutoff, int seed) {
     
        long startTime = System.currentTimeMillis();
        boolean[] currState = ConstructIVC();
        List<Integer> currVC = new ArrayList<>();
        
        for (int i = 0; i < currState.length; i++) {
            if (currState[i])
                currVC.add(i);
        }
        long elapsedmili;
        float elapsed = 0;

        
        while (elapsed < cutoff) {
            
            if (uncoveredEdge(currState).isEmpty()) {
                if(bestVC==null || currVC.size()< bestVC.size()){
                    bestVC = new ArrayList<>(currVC);
                    double duration = (System.currentTimeMillis() - startTime)/1000.000000;
                    trace.add("" + duration + "," + bestVC.size()); 
                }
                
                int randomInt = deleteRand(seed, currVC); 
                int randVertex = currVC.get(randomInt);
                currState[randVertex] = false;
                currVC.remove(randomInt);
            }
            
            int minLossVertex = findMinLoss(currState);
            currState[minLossVertex] = false;
            currVC.remove((Object) minLossVertex);
            
            Set<Edge> currUnEdge = uncoveredEdge(currState);
            
            int hVertex = addVertex(currUnEdge, currState);
            currState[hVertex] = true;
            currVC.add(hVertex);
            elapsedmili = System.currentTimeMillis() - startTime;
            elapsed = elapsedmili;
        }


    }
    @Override
    public List<Integer> getResult() {
            return bestVC;
    }
    @Override
    public List<String> getTrace() {
        return trace;
    }
    
    public int deleteRand(int seed, List<Integer> currVC){
        int l = currVC.size();
        Random rand = new Random(seed);
        int randomInt = rand.nextInt(l);
        return randomInt;
    }

    public int findMinLoss(boolean[] currState){
        int delete = 0;
        int minLoss = Integer.MAX_VALUE;

        for(int i = 0; i < currState.length; i++){
            int loss = 0;
            if(currState[i]){
                for(Edge edge : graph.nodes[i]){
                    if(!currState[edge.otherNode(i)]){
                        loss++;
                    }
                }
                if(loss <= minLoss){
                    minLoss = loss;
                    delete = i;
                }
            }
        }
        return delete;
    }


    public Set<Edge> uncoveredEdge(boolean[] currState){
        Set<Edge> uncoveredEdge = new HashSet<>();
        for(int i = 0; i < currState.length; i++) {

            if (!currState[i]) {
                for(Edge edge:graph.nodes[i]){
                    int v = edge.otherNode(i);
                    if(!currState[v]){
                        List<Edge> neighbors = graph.nodes[i];
                        int j = 0;
                        while(j < neighbors.size() && neighbors.get(j).otherNode(i) != v) {
                            j++;
                        }
                        Edge unEdge = neighbors.get(j);
                        uncoveredEdge.add(unEdge);
                    }
                }
            }
        }
        return uncoveredEdge;
    }

    public int addVertex(Set uncoveredEdge, boolean[] currState){
        int hDegree = Integer.MIN_VALUE;
        int addVertex = 0;
        for(int i = 0; i < currState.length; i++){
            int degree = 0;
            if(!currState[i]){
                for(Edge edge : graph.nodes[i]){
                    if(uncoveredEdge.contains(edge)){
                        degree++;
                    }
                }
                if(degree >= hDegree){
                    hDegree = degree;
                    addVertex = i;
                }
            }
        }
        return addVertex;
    }


    private boolean[] ConstructIVC (){
        
        boolean[] nodeState = new boolean[graph.nodes.length];
        for(int i=0; i < graph.nodes.length; i++){
            nodeState[i] = false;
        }
        
        Map<Integer, Integer> nodeDegrees = new HashMap<>(graph.nodes.length);
        for (int i = 0; i < graph.nodes.length; i++) {
            nodeDegrees.put(i, (graph.nodes[i].size()));
        }

        for(int i = 0; i < graph.nodes.length; i++){
            for(Edge edge : graph.nodes[i]){
                int v = edge.otherNode(i);
                if(!nodeState[i] && !nodeState[v]){
                    if(nodeDegrees.get(i) >= nodeDegrees.get(v)){
                        nodeState[i] = true;
                    }else{
                        nodeState[v] = true;
                    }
                }
            }
        }
        return nodeState;
    }
}


public class FLS {
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
		if ((alg.equals("FLS")) && (seed.equals(""))) {
			System.out.println("Seed is required for local search to run");
			System.exit(0);
		}

		String output_sol = "", output_trace = "";
		int end = inst.indexOf(".graph");
		int start = inst.lastIndexOf("/");
		if (alg.equals("FLS") )  {
			output_sol = inst.substring(start + 1, end) + "_" + alg + "_" + time + "_" + seed + ".sol";
			output_trace = inst.substring(start + 1, end) + "_" + alg + "_" + time + "_" + seed + ".trace";
		}

		PrintWriter sol_writer = new PrintWriter(output_sol, "UTF-8");
		PrintWriter trace_writer = new PrintWriter(output_trace, "UTF-8");
		Graph G = parseGraph(inst);
		Solver solver = null;

	   if (alg.equals("FLS")) {
			solver = new l1(G, Double.parseDouble(time) * 1000, Integer.parseInt(seed));
		} 
		if (solver == null) System.exit(1);

		List<Integer> res = solver.getResult();
		List<String> traces = solver.getTrace();

		StringBuilder sb = new StringBuilder();
		for (int num : res) { // assemble sol file
			sb.append(Integer.toString(num + 1));
			sb.append(", ");
		}

		System.out.println(sb.toString());
		 // print trace file
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
 