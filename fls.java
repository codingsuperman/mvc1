import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
public class Edge {
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



public interface Solver {
    //void solve();
    List<Integer> getResult();
    List<String> getTrace();
}


public class Graph {

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
public class LS1 implements Solver{
    Graph graph;
    List<Integer> bestVC = null; //store the solution found by LS1
    List<String> trace = null;  //store trace for trace file
    public LS1(Graph graph, double cutoff, int seed){
        this.graph = graph;
        trace =new ArrayList<>();
        solve(cutoff, seed);
    }

    public void solve(double cutoff, int seed) {
     //  start to construct IVC and count time
        long startTime = System.currentTimeMillis();
        boolean[] currState = ConstructIVC();
        List<Integer> currVC = new ArrayList<>();
        //store the IVC results into currVC as current vertex cover
        for (int i = 0; i < currState.length; i++) {
            if (currState[i])
                currVC.add(i);
        }
        long elapsedmili;
        float elapsed = 0;

        //before cutoff time, start local search procedure
        while (elapsed < cutoff) {
            //check the current solution is a vertex cover
            if (uncoveredEdge(currState).isEmpty()) {
                if(bestVC==null || currVC.size()< bestVC.size()){
                    bestVC = new ArrayList<>(currVC);
                    double duration = (System.currentTimeMillis() - startTime)/1000.000000;
                    trace.add("" + duration + "," + bestVC.size()); //record into trace every time a better solution is found
                }
                //delete a vertex using random seed
                int randomInt = deleteRand(seed, currVC); 
                int randVertex = currVC.get(randomInt);
                currState[randVertex] = false;
                currVC.remove(randomInt);
            }
            //delete the vertex with minimum loss
            int minLossVertex = findMinLoss(currState);
            currState[minLossVertex] = false;
            currVC.remove((Object) minLossVertex);
            //update uncovered edges
            Set<Edge> currUnEdge = uncoveredEdge(currState);
            //add a vertex back to the current solution which covers the most edges that are uncovered yet
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
    //generate a random number with the given seed, to decide one vertex in the current VC to delete
    public int deleteRand(int seed, List<Integer> currVC){
        int l = currVC.size();
        Random rand = new Random(seed);
        int randomInt = rand.nextInt(l);
        return randomInt;
    }

    //loss function as one of the scoring functions, which is the number of covered edges that would become uncovered by removing this vertex
    //return the vertex in the current VC with minimum loss
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

//the edges not in the current vertex cover
    //also used to check if the curr_VC is VC, uncoveredEdge.isEmpty()
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
//find the vertex with highest degree in the uncovered edges, which means choosing it will cover the most uncovered edges
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
        //initialize with all vertices not in the IVC
        boolean[] nodeState = new boolean[graph.nodes.length];
        for(int i=0; i < graph.nodes.length; i++){
            nodeState[i] = false;
        }
        //compute the derees for all vertices
        Map<Integer, Integer> nodeDegrees = new HashMap<>(graph.nodes.length);
        for (int i = 0; i < graph.nodes.length; i++) {
            nodeDegrees.put(i, (graph.nodes[i].size()));
        }
//foreach e in E do
        //if e is uncovered then add the endpoint with higher degree into ivc
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