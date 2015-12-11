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
import java.util.LinkedList;
import java.util.Map;

import graph.*;
import graph.GraphConstants.FlowType;
import graph.GraphConstants.Keyword;
import graph.GraphConstants.NodeType;

public class ProgramExecutor {
	
	private UIFrame _frame;
	private GraphParser gp;
	private boolean bCustomParser = false;
	
	// default constructor
	public ProgramExecutor(){
	}
	
	// executes the functions required to create the Execution Time Equation and resulting flow graph
	public void execute(File cfgFile, File codeFile, File saveFolder, String flowGraphFileName, UIFrame frame){
		Graph gControlFlowGraph;
		gp = new GraphParser();
		_frame = frame;
		
		clearTextFromFrame();
		// File paths might have to be changed for linux
		if (codeFile.getName().endsWith(".txt"))
		{
			gControlFlowGraph = gp.Parse(codeFile);
			bCustomParser = true;
		}
		else
		{
			gControlFlowGraph = readGraphMLFile(cfgFile, codeFile);
			bCustomParser = false;
		}
		reduceGraph(gControlFlowGraph);
		analyzeGraph(gControlFlowGraph);
		gControlFlowGraph.curveEdges();
		new DrawingApp(gControlFlowGraph);
		generateDot(gControlFlowGraph, saveFolder, flowGraphFileName);
	}
	

	// Read XML and parse it, generate a graph
	public Graph readGraphMLFile(File cfgFile, File codeFile)
	{
		// Determines which type of statement/loop caused a split in flow and 
		// saves it alongside its corresponding line number.
		Map<String,String> lineloop = new HashMap<String,String>();
        Map<String,String> lineif = new HashMap<String,String>();
        ArrayList<String> aLines = new ArrayList<String>();
        aLines.add("");	// index starts at 1
		try{
			BufferedReader br = new BufferedReader(new FileReader(codeFile));
			String line=null;
			int lineNumber=1;
			while((line=br.readLine()) !=null){
				if(line.contains("for")) lineloop.put(lineNumber+"", "for");
				if(line.contains("while")) lineloop.put(lineNumber+"", "while");
				if(line.contains("if")) lineif.put(lineNumber+"", "if");
				aLines.add(line);
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
			if (gp.IsNumeric(strLabel))
				newv.setExpr(aLines.get(Integer.parseInt(strLabel)));
			gGraph.addVertex(newv);
			if(lineloop.containsKey(newv.getLabel())) newv.setLooptype(lineloop.get(newv.getLabel()));
			if(lineif.containsKey(newv.getLabel())) newv.setType(lineif.get(newv.getLabel()));
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
		HashMap<String, String> mVariable = gp.getVariables();
		ArrayList<Vertex> lstVertices = new ArrayList<Vertex>();
		
		for(Vertex v : g.getVerticesList()) {
			lstVertices.add(v);
			if (!mVariable.containsKey("C" + v.getLabel()))
				mVariable.put("C" + v.getLabel(), "1");	// All time costs are assumed to be 1, but can be changed before graph reduction
		}
		
		Edge newEdge = null;
		Edge eIn, eOut;
		for(Vertex v : lstVertices)
		{
			if(v.getInEdgeList().size() == 1 && v.getOutEdgeList().size() == 1 && !v.getLabel().equals("START") && !v.getLabel().equals("EXIT"))
			{
				if (v.getInEdgeList().get(0).getSource().getLabel().equals(v.getOutEdgeList().get(0).getTarget().getLabel()))
				{
					v.getInEdgeList().get(0).setTimecost(trim('+', v.getInEdgeList().get(0).getTimecost() + "+" + "C" + v.getLabel()));
				}
				else
				{
				    // not the only node in the loop
					eIn = v.getInEdgeList().get(0);
					eOut = v.getOutEdgeList().get(0);
					newEdge = new Edge(eIn.getSource(), eOut.getTarget(), "");	// label gets reassigned later
					if (!v.getType().equals("return"))
						newEdge.setTimecost(trim('+', trim('+', eIn.getTimecost() + "+" + eOut.getTimecost()) + "+" + "C" + v.getLabel()));
					else 
						newEdge.setTimecost(trim('+', trim('+', eIn.getTimecost() + "+" + eOut.getTimecost())));
					g.addEdge(newEdge);
					g.deleteVertex(v);
				}
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
		for (Edge e : g.getEdgeList())
		{
			System.out.println(e.getLabel() + " : " + e.getTimecost());
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
		
		// From here it is difficult to reconcile the difference between GraphParser and GraphML parser without some major rewriting
		// I'll keep them separate
		if (!bCustomParser)
		{
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
			{
				if(e.getVisible()){
					if(e.getCondition()!=null && e.getLoopType()!=null){
	
						appendLineToFrame(e.getLabel()+ ": " + e.getCondition()+" statement in "+e.getLoopType() + " loop");
						if(e.getCondition().equals("if") && e.getLoopType().equals("for"))
							appendLineToFrame("      Binomial Distribution.");
						else if(e.getCondition().equals("if") && e.getLoopType().equals("while"))
							appendLineToFrame("      Poisson Distribution.");
					}
					else if(e.getLoopType()!=null){
						appendLineToFrame(e.getLabel()+ ": " + e.getLoopType() + " loop");
						if(e.getLoopType().equals("for"))
							appendLineToFrame("      Deterministic.");
						if(e.getLoopType().equals("while"))
							appendLineToFrame("      Modified Geometric Distribution.");
					}
					else {
						appendLineToFrame(e.getLabel()+ ": " + e.getCondition() + " statement");
						if(e.getCondition().equals("if"))
							appendLineToFrame("      Bernoulli Distribution.");
					}
				}
			}
		}
		else
		{
			FlowType ft;
			for (Edge e : independentEdges)
			{
				ft = e.getFlowType();
				if (ft != null)
				{
					appendLineToFrame(e.getLabel() + ": " + ft.toString());
					if (ft == FlowType.FOR)
					{
						appendLineToFrame("      Deterministic.");	// should use a fixed size font...
					}
					else if (ft == FlowType.WHILE)
					{
						appendLineToFrame("      Modified geometric distribution.");
					}
					else if (ft == FlowType.IF || ft == FlowType.ELSE)
					{
						appendLineToFrame("      Bernoulli distribution.");
					}
					else if (ft == FlowType.IF_INSIDE_FOR_TRUE ||  ft == FlowType.IF_INSIDE_FOR_FALSE)
					{
						appendLineToFrame("      Binomial distribution.");
					}
					else if (ft == FlowType.IF_INSIDE_WHILE_TRUE ||  ft == FlowType.IF_INSIDE_WHILE_FALSE)
					{
						appendLineToFrame("      Poisson distribution.");
					}
				}
			}
		}

		// If an edge is a loop and source vertex is decision/branch, add 2 to the time cost to account for increment/decrement and condition test
		// Note that this assumes that for/while loop always increment/decrement, which they don't have to.
		// The getExpr returns the line of code for the vertex.
		for (Vertex v : pGraph.getVerticesList())
		{
			if ((v.getExpr() != null && (v.getExpr().contains("for") || v.getExpr().contains("while"))) ||
				(v.getType() != null && (v.getType().equals(Keyword.FOR.toString()) || v.getType().equals(Keyword.WHILE.toString()))))
			{
				for (Edge e : v.getOutEdgeList())
				{
					// there should really be EXACTLY 1 edge going into the loop block from the loop branch vertex
					// and EXACTLY 1 edge going out of the loop branch vertex
					// This code effectively tries to add a cost 2 vertex into the loop block
					// the CFG factory sometimes reduces the graph too much so I don't know there is any guarantee the above will always happen
					// *** with this implementation of storing whether an edge is in a loop, it is difficult to do anything
					if (e.getLoopType() != null)
					{	// this edge goes into the loop block, this gets multiplied by e
						e.setTimecost(trim('+', e.getTimecost() + "+2"));
					}
					else
					{	// this edge exits the loop and IS NOT part of the loop, so the +1 should not get multiplied by e of the for loop
						e.setTimecost(trim('+', e.getTimecost() + "+1"));
					}
				}
			}
		}
		
		String totalCost = "";
		String subCost = "";
		//Print Edge Time cost
		for (Edge e : pGraph.getEdgeList())
		{
			if(e.getVisible())
			{
				if(subCost == "")
					subCost = "(" + e.getTimecost()+")*"+e.getLabel();
				else
					subCost = "+(" + e.getTimecost()+")*"+e.getLabel();
				appendLineToFrame("C"+e.getLabel() + " = (" + e.getTimecost()+")*"+e.getLabel());
			    totalCost = totalCost+subCost;
			}
		}
		appendLineToFrame("\nC="+totalCost+"\n");

		// set known value of e
		for (Edge e : pGraph.getEdgeList())
		{	
			if (e.getEquation() != "")
				gp.getVariables().put(e.getLabel(), e.getEquation());
		}

		//Print final cost equation
		try {
			appendLineToFrame("\nC=" + gp.Simplify(gp.EvalExprAsString(totalCost)));
		} catch (Exception e1) {
			System.out.println("Cost equation parsing: " + e1.getMessage());
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
	
	private String trim(char cTrim, String s)
	{
		int idx = 0;
		if (s.length() != 0)
		{
			for (int i = 0; i < s.length(); i++)
			{
				if (s.charAt(i) != cTrim)
				{
					idx = i;
					break;
				}
			}
			s = s.substring(idx, s.length());
			for (int i = s.length() - 1; i > -1; i--)
			{
				if (s.charAt(i) != cTrim)
				{
					idx = i + 1;
					break;
				}
			}
			s = s.substring(0, idx);
		}
		return s;
	}
	
	public static void main(String[] args)
	{
		new UIFrame();
	}
}
