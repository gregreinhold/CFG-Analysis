package control_flow;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import ui.UIFrame;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import graph.*;

public class ProgramExecutor {
	
	private UIFrame _frame;
	
	// default constructor
	public ProgramExecutor(){
	}
	
	// executes the functions required to create the Execution Time Equation and resulting flow graph
	public void execute(File cfgFile, File codeFile, File saveFolder, String flowGraphFileName, UIFrame frame){
		Graph gControlFlowGraph;
		_frame = frame;
		clearTextFromFrame();
		// File paths might have to be changed for linux
		if (codeFile.getName().endsWith(".txt"))
			gControlFlowGraph = new GraphParser().Parse(codeFile);
		else 
			gControlFlowGraph = readGraphMLFile(cfgFile, codeFile);
		reduceGraph(gControlFlowGraph);
		analyzeGraph(gControlFlowGraph);
		gControlFlowGraph.curveEdges();
		new DrawingApp(gControlFlowGraph);
		generateDot(gControlFlowGraph, saveFolder, flowGraphFileName);
		gControlFlowGraph.PrintGraph(false, true);		// DEBUG PRINT
	}
	

	// Read XML and parse it, generate a graph
	public Graph readGraphMLFile(File cfgFile, File codeFile)
	{
		// Determines which type of statement/loop caused a split in flow and 
		// saves it alongside its corresponding line number.
		Map<String,String> lineloop = new HashMap<String,String>();
        Map<String,String> lineif = new HashMap<String,String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(codeFile));
			String line=null;
			int lineNumber=1;
			while((line=br.readLine()) !=null){
				if(line.contains("for")) lineloop.put(lineNumber+"", "for");
				if(line.contains("while")) lineloop.put(lineNumber+"", "while");
				if(line.contains("if")) lineif.put(lineNumber+"", "if");
				lineNumber++;
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		// Uses DocumentBuilder to parse GraphML file and create an internal Graph representation
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		Document doc = null;
		Node objNode;
		Node objChildNode;
		NodeList lstNode;
		NodeList lstChildNode;
		
		int intX = 0, intY = 0;
		String strName = "", strLabel = "", strType = "";
		Graph gGraph = new Graph();
		
		try 
		{
			dBuilder = dbFactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			System.out.println(e.getMessage());
			return null;
		}
		try 
		{ 
			doc = dBuilder.parse(cfgFile);
		} 
		catch (Exception e) 
		{
			System.out.println(e.getMessage());
			return null;
		}
		doc.getDocumentElement().normalize();

		lstNode = doc.getElementsByTagName("node");
		for (int i = 0; i < lstNode.getLength(); i++) {
			objNode = lstNode.item(i);
			strName = objNode.getAttributes().item(0).getTextContent();
			
			lstChildNode = objNode.getChildNodes();
			for (int j = 0; j < lstChildNode.getLength(); j++)
			{
				objChildNode = lstChildNode.item(j);
				if (objChildNode.getNodeType() == Node.ELEMENT_NODE) {
					switch (objChildNode.getAttributes().item(0).getTextContent())
					{
						case "a_x":
							intX = Integer.parseInt(objChildNode.getTextContent());
							break;
						case "a_y":
							intY = Integer.parseInt(objChildNode.getTextContent());
							break;
						case "a_label":
							strLabel = objChildNode.getTextContent();
							break;
						case "a_type":
							strType = objChildNode.getTextContent();
							break;
					}
				}				
			}
			Vertex newv = new Vertex(strName, strLabel, strType, intX, intY);
			if(lineloop.containsKey(newv.getLabel())) newv.setLooptype(lineloop.get(newv.getLabel()));
			if(lineif.containsKey(newv.getLabel())) newv.setType(lineif.get(newv.getLabel()));
			gGraph.addVertex(newv);
		}
		
		lstNode = doc.getElementsByTagName("edge");
		for (int i = 0; i < lstNode.getLength(); i++) {
			objNode = lstNode.item(i);
			
			if (objNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) objNode;
				gGraph.addEdge(new Edge(gGraph.getVertexByName(eElement.getAttribute("source")), gGraph.getVertexByName(eElement.getAttribute("target")), "e" + i));
			}
		}
		
		return gGraph;
	}
	
	// Reduces input Graph g by consolidating nodes along one flow into a single edge.
	// i.e. each Vertex will correspond to a fork/merge of flows in the graph,
	// and each Edge corresponds to a single flow with cost = cost of the consolidated operations
	// Note: It should be merging vertex of type = instruction, but this is more or less equivalent
	public void reduceGraph(Graph g){
		ArrayList<Vertex> lstVertices = new ArrayList<Vertex>();
		for(Vertex v : g.getVerticesList()) {
			lstVertices.add(v);
		}
		for(Vertex v : lstVertices)
		{
			if(v.getInEdgeList().size()==1 && v.getOutEdgeList().size()==1 
					&& !v.getLabel().equals("START") && !v.getLabel().equals("EXIT"))
			{
				Edge newEdge = new Edge(v.getInEdgeList().get(0).getSource(),v.getOutEdgeList().get(0).getTarget(),
						v.getInEdgeList().get(0).getLabel());
				if(newEdge.getTimecost().equals("")){
					newEdge.setTimecost("C"+v.getLabel());
				} else {
					newEdge.setTimecost("C"+v.getLabel()+" + "+newEdge.getTimecost());
				}
				newEdge.setFlowType(v.getInEdgeList().get(0).getFlowType());
				g.addEdge(newEdge);
				g.deleteVertex(v);
			}
		}
		//Put Start node in first
		Vertex start = g.getVertexByLabel("START");
		g.getVerticesList().remove(start);

		g.getVerticesList().add(0, start);
		//Reassign the edge label
		int k=1;
		for(Vertex v : g.getVerticesList()){
			if(!v.getVisited() && v.getOutEdgeList().size()>0) {
				for(Edge e : v.getOutEdgeList()) 
				{
					e.setLabel("e"+k++);
					if(e.getTimecost().equals("")) e.setTimecost("0");
				}
			}
		}
		Collections.sort(g.getEdgeList(), new Comparator<Edge>(){
			public int compare(Edge a, Edge b){
				return Integer.parseInt(a.getLabel().substring(1)) 
						> Integer.parseInt(b.getLabel().substring(1)) ? 1 : -1;
			}
		});
		g.resetGraph(false);
	}
	
	// generate the DOT file containing the flow graph and save it to the desired location
	public void generateDot(Graph g, File saveFolder, String flowGraphFileName){
		File outDot = new File(saveFolder.getAbsolutePath()+ "\\" + flowGraphFileName + ".dot");
		String graphname = flowGraphFileName;
		String nodeshape="";
		try{
			BufferedWriter bfw = new BufferedWriter(new FileWriter(outDot));
			bfw.write(" digraph \""+graphname +"\" {");bfw.newLine();
			bfw.write("graph [label=\""+graphname+ "\"];");bfw.newLine();
			for(Vertex v : g.getVerticesList()){
				if(v.getLabel().equals("START")||v.getLabel().equals("EXIT")) {
					nodeshape="box"; 
				} else {
					nodeshape="circle";
				}
				bfw.write(v.getLabel()+ " "+"[label=\""+v.getLabel()+"\",shape="+nodeshape
						+" style=filled, fillcolor=\"#CECEFF\", fixedsize=true, fontsize=12, width=0.78, height=0.36 ]");
				bfw.newLine();
			}
			String style=null;
			String edgecolor="";
			for(Edge e : g.getEdgeList()){
				if(e.getIndependent()){
					edgecolor="blue"; 
				} else {
					edgecolor="black";
				}
				if(e.getSource().getLabel().equals("EXIT")) {
					style="dashed"; 
				} else {
					style="solid"; 
				}
				bfw.write(" "+e.getSource().getLabel()+" -> "+e.getTarget().getLabel()
						+" [label=\""+e.getLabel()+"\", style="+style+" color="+edgecolor+ "]");
				bfw.newLine();
			}
			bfw.write("}");
			bfw.close();

			Process pr = new ProcessBuilder("dot", "-Tsvg", saveFolder + "\\" + flowGraphFileName + ".dot", "-o", saveFolder + "\\" + flowGraphFileName + ".svg").start();
			pr.waitFor();
			Desktop.getDesktop().open(new File(saveFolder + "\\" + flowGraphFileName + ".svg"));
		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// Analyze the graph to derive the Execution Time Equation
	public void analyzeGraph(Graph pGraph)
	{
		Edge eFlow;
		Vertex vSrc = null, vTgt = null;
		Vertex vStart = pGraph.getVertexByLabel("START");									// Start vertex
		Vertex vEnd = pGraph.getVertexByLabel("EXIT");										// End vertex
		ArrayList<Vertex> lstVertex = null;													// Temp list of vertex cycle
		ArrayList<ArrayList<Vertex>> lstCycleVertex = new ArrayList<ArrayList<Vertex>>();	// List all lists of cycles
        ArrayList<Edge> independentEdges = new ArrayList<Edge>(); // list of independent flows
 		// Add virtual edge from start to end, so we can assign equation of all edges outside any loops to 1
		eFlow = new Edge(vEnd, vStart, "e0");
		eFlow.setValue("1");
		eFlow.setVisible(false);
		pGraph.addEdge(eFlow);
		
		// Find 1 cycle per loop, set 1 edge in the cycle found as independent flow
		do
		{
			if (lstVertex != null)
			{
				lstCycleVertex.add(lstVertex);
				eFlow = lstVertex.get(0).findEdge(lstVertex.get(1), false);
				if (eFlow != null)
				{
					eFlow.setIndependent(true);
					independentEdges.add(eFlow);
				}
			}
			lstVertex = new ArrayList<Vertex>();
			pGraph.resetGraph(false);
			pGraph.findCycle(vEnd, lstVertex);
		}
		while (lstVertex.size() != 0);
		
		// *Debug print
		String temp;
		for (ArrayList<Vertex> l : lstCycleVertex)
		{
			temp = "Cycle: ";
			for (Vertex v : l)
			{
				temp += v.getLabel() + ", ";
			}
			appendLineToFrame(temp.substring(0, temp.length() - 2));
		}
		appendLineToFrame("");
		
		// Generate dependent flow equations
		String strEquation;
		String strIndFlowLabel = "";
		boolean blnForward = false;
		
		for (ArrayList<Vertex> lstVertices : lstCycleVertex)
		{
			// Get direction of independent flow (first edge)
			vSrc = lstVertices.get(0);
			vTgt = lstVertices.get(1);
			eFlow = vSrc.findDiffIndepEdge(vTgt);
			if (eFlow != null)
			{
				blnForward = (eFlow.getTarget() == vTgt);		// Direction of independent flow, A -> B = Forward, A <- B = Backward
				strIndFlowLabel = eFlow.getValue();				// If the flow has an assigned cost, use that instead of the flow label
				if (strIndFlowLabel.length() == 0){
					strIndFlowLabel = eFlow.getLabel();
				}
			}
			else
			{
				appendLineToFrame("ERROR: Independent flow not found");
				System.out.println("ERROR: Independent flow not found");
				return;
			}

			// Get direction of dependent flow and determine +/- independent flow
			// eFlow.GetTarget() == vTgt --> Forward edge
			if(!blnForward){
				// need to reverse the order of the vertices being visited if the edge is backwards
				Collections.reverse(lstVertices);
			}
			for (int intVertex = 0; intVertex < lstVertices.size(); intVertex++)
			{
				vSrc = lstVertices.get(intVertex);
				vTgt = lstVertices.get((intVertex + 1) % lstVertices.size());

				eFlow = vSrc.findOutEdge(vTgt, false);
				if (eFlow != null)
				{
					strEquation = eFlow.getEquation();
					strEquation += ((blnForward == (eFlow.getTarget() == vTgt)) ? ((strEquation.length() == 0) ? "" : " + ") : " - ") + strIndFlowLabel;				
					eFlow.setEquation(strEquation);
				}
			}
		}
		pGraph.resetGraph(false);
		// Print equation of all flows
		for (Edge e : pGraph.getEdgeList())
		{
			if (!e.getIndependent())
				appendLineToFrame(e.getLabel() + " = " + e.getEquation());
		}
		appendLineToFrame("");
		String totalCost = "";
		String subCost = "";
		//Print Edge Time cost
		for (Edge e : pGraph.getEdgeList())
			if(e.getVisible())
			{
				if(subCost == "")
					subCost = "(" + e.getTimecost()+")*"+e.getLabel();
				else
					subCost = "+(" + e.getTimecost()+")*"+e.getLabel();
				appendLineToFrame("C"+e.getLabel() + " = (" + e.getTimecost()+")*"+e.getLabel());
			    totalCost = totalCost+subCost;
			}	
		appendLineToFrame(" ");
		appendLineToFrame("C="+totalCost);
		appendLineToFrame(" ");
		String[] tokens = totalCost.split("e");
		int n=tokens.length;
		int cValues[] = new int[n];
		for (int i=0;i< n;i++)
		{
			int c =0; 
			for(int j=0;j<tokens[i].length();j++)
			{
				String token = tokens[i];
				if(token.charAt(j)=='C')
					c++;
			}
			cValues[i] = c;
		}
		int i =0;
		totalCost ="";
		for (Edge e : pGraph.getEdgeList())
		{	
			if(e.getVisible())
			{
			    if(totalCost == "")
			    {
			    	if(e.getEquation() == "")
			    		totalCost = totalCost+"("+cValues[i]+")*"+e.getLabel();
			    	else
			    		totalCost = totalCost+"("+cValues[i]+")*"+"("+e.getEquation()+")";
			    }
			    else
			    {
			    	if(e.getEquation() == "")
			    		totalCost = totalCost+"+("+cValues[i]+")*"+e.getLabel();
			    	else
			    		totalCost = totalCost+"+("+cValues[i]+")*"+"("+e.getEquation()+")";
			    }
			    i++;
			}
		}	
		appendLineToFrame("C="+totalCost);
		String finalCost = "";
		//Print final cost equation
		GraphParser gp = new GraphParser();
		try {
			finalCost = gp.Simplify(gp.EvalExprAsString(totalCost));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		appendLineToFrame(" ");
		appendLineToFrame("C="+finalCost);
		//find the vertices that begin each loop (if any)
		Map<String,ArrayList<Vertex>> loopVertices = new HashMap<String,ArrayList<Vertex>>();

		for(ArrayList<Vertex> cycle : lstCycleVertex){
			if(!cycle.get(0).equals("EXIT")){
	            if(cycle.get(0).getLooptype()!=null) {
	            	if(!loopVertices.containsKey(cycle.get(0).getLabel())){
	            		loopVertices.put(cycle.get(0).getLabel(), cycle);
	            	}
	            	else if(loopVertices.get(cycle.get(0).getLabel()).size()<cycle.size()){
	            		loopVertices.remove(cycle.get(0).getLabel());
	            		loopVertices.put(cycle.get(0).getLabel(), cycle);
	            	}
	            }
			}

		}
		// save strings containing order of vertices in each loop with key of starting vertex
		Map<String,String> loopstr=new HashMap<String,String>();
		for(String key : loopVertices.keySet()){ 
			StringBuffer s = new StringBuffer();
			for(Vertex v : loopVertices.get(key)){
				s.append(v.getLabel()+" ");
			}
			loopstr.put(key, s.toString());
		}
		ArrayList<String> cycleStr = new ArrayList<String>();
		for(ArrayList<Vertex> cycle : lstCycleVertex){
			StringBuffer s = new StringBuffer();
			for(Vertex v : cycle) s.append(v.getLabel()+" ");
			cycleStr.add(s.toString());
		}
		int index=0;
		for(ArrayList<Vertex> cycle : lstCycleVertex){
			if(!cycle.get(0).equals("END")){
				 Edge flow = independentEdges.get(index);
		         Vertex c=null; // condition predecessors vertex of e, 
                                // if->A, e=if->A, c=if
		         if(flow.getSource().getLabel().equals(cycle.get(0).getLabel())){
		        	 c=cycle.get(0);
		         }
		         else {
		        	 c=cycle.get(1);
		         }
		         
		         if(c.getLooptype()!=null){
		        	 for(Vertex vertex: cycle){
		        		 vertex.setLooptype(c.getLooptype());
		        	 }
		         }
		         
//		         for(String key : loopstr.keySet()){
//		        	 if(loopstr.get(key).contains(cycleStr.get(index))) {
//		        		 flow.setLooptype(loopVertices.get(key).get(0).GetLooptype());
//		        		 break;
//		        	 }
//		         }
		         if(c.getLooptype()!=null){
		        	 flow.setLooptype(c.getLooptype());
		         }
		         if(c.getType() !=null && !c.getType().equals("decision")){
		        	 flow.setCondition(c.getType());
		         }
			} 


			index++;
		}
		appendLineToFrame("");
		for(Edge e : independentEdges) 
			if(e.getVisible()){
				if(e.getCondition()!=null && e.getLoopType()!=null){

					appendLineToFrame(e.getLabel()+ ": " + e.getCondition()+" statement in "+e.getLoopType() + " loop");
				}
				else if(e.getLoopType()!=null){
					appendLineToFrame(e.getLabel()+ ": " + e.getLoopType() + " loop");
				}
				else {
					appendLineToFrame(e.getLabel()+ ": " + e.getCondition() + " statement");
				}
			}
	}
	
	// adds a new line to the output display of the frame
	private void appendLineToFrame(String string){
		if(_frame!=null){
			_frame.appendLineToOutputDisplay(string);
		}
	}
	
	// clears all of the text in the output display of the frame
	private void clearTextFromFrame(){
		if(_frame!=null){
			_frame.resetOutputDisplay();
		}
	}
	
	public static void main(String[] args)
	{
		new UIFrame();
	}
}
