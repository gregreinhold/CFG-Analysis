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
import java.util.*;

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
		gControlFlowGraph.CurveEdges();
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
			String line = null;
			int lineNumber = 1;
			while((line=br.readLine()) !=null){
				if(line.contains("for")) lineloop.put(lineNumber + "", "for");
				if(line.contains("while")) lineloop.put(lineNumber + "", "while");
				if(line.contains("if")) lineif.put(lineNumber + "", "if");
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
			gGraph.AddVertex(newv);
		}
		
		lstNode = doc.getElementsByTagName("edge");
		for (int i = 0; i < lstNode.getLength(); i++) {
			objNode = lstNode.item(i);
			
			if (objNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) objNode;
				gGraph.AddEdge(new Edge(gGraph.getVertexByName(eElement.getAttribute("source")), gGraph.getVertexByName(eElement.getAttribute("target")), "e" + i));
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
			if(v.getInEdge().size()==1 && v.getOutEdge().size()==1 
					&& !v.getLabel().equals("START") && !v.getLabel().equals("EXIT"))
			{
				Edge newEdge = new Edge(v.getInEdge().get(0).getSource(),v.getOutEdge().get(0).getTarget(),
						v.getInEdge().get(0).getLabel());
				if(newEdge.getTimecost().equals("")){
					newEdge.setTimecost("C"+v.getLabel());
				} else {
					newEdge.setTimecost("C"+v.getLabel()+" + "+newEdge.getTimecost());
				}
				newEdge.setFlowType(v.getInEdge().get(0).getFlowType());
				g.AddEdge(newEdge);
				g.DeleteVertex(v);
			}
		}
		//Put Start node in first
		Vertex start = g.getVertexByLabel("START");
		g.getVerticesList().remove(start);
		g.getVerticesList().add(0, start);
		//Reassign the edge label
		int k=1;
		for(Vertex v : g.getVerticesList()){
			if(!v.getVisited() && v.getOutEdge().size()>0) {
				for(Edge e : v.getOutEdge()) 
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
		g.ResetGraph(false);
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
			
			new ProcessBuilder("dot", "-Tsvg", saveFolder + "\\" + flowGraphFileName + ".dot", "-o", saveFolder + "\\" + flowGraphFileName + ".svg").start();
			Thread.sleep(1000);	// just in case it wasn't generated in time
			Desktop.getDesktop().open(new File(saveFolder + "\\" + flowGraphFileName + ".svg"));
		} catch(IOException e){
			e.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		pGraph.AddEdge(eFlow);
		
		// Find 1 cycle per loop, set 1 edge in the cycle found as independent flow
		do
		{
			if (lstVertex != null)
			{
				lstCycleVertex.add(lstVertex);
				eFlow = lstVertex.get(lstVertex.size() - 1).FindEdge(lstVertex.get(0), false);
				if (eFlow != null)
				{
					eFlow.setIndependent(true);
					independentEdges.add(eFlow);
				}
			}
			lstVertex = new ArrayList<Vertex>();
			pGraph.ResetGraph(false);
			pGraph.FindCycle(vStart, lstVertex);
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
		
		for (ArrayList<Vertex> l : lstCycleVertex)
		{
			// get direction of independent flow (last edge)
			vSrc = l.get(l.size() - 1);
			vTgt = l.get(0);
			eFlow = vSrc.FindDiffIndepEdge(vTgt);
			if (eFlow != null)
			{
				blnForward = (eFlow.getTarget() == vTgt);		// Direction of independent flow, A -> B = Forward, A <- B = Backward
				strIndFlowLabel = eFlow.getValue();				// If the flow has an assigned value, use that instead of the flow label
				if (strIndFlowLabel.length() == 0){
					strIndFlowLabel = eFlow.getLabel();
				}
			}
			else
			{
				System.out.println("ERROR: Independent flow not found");
				return;
			}

			// get direction of dependent flow and determine +/- independent flow
			// eFlow.getTarget() == vTgt --> Forward edge
			for (int intVertex = 0; intVertex < l.size() - 1; intVertex++)
			{
				vSrc = l.get(intVertex);
				vTgt = l.get(intVertex + 1);
				
				eFlow = vSrc.FindEdge(vTgt, false);
				if (eFlow != null)
				{
					strEquation = eFlow.getEquation();
					strEquation += ((blnForward == (eFlow.getTarget() == vTgt)) ? ((strEquation.length() == 0) ? "" : " + ") : " - ") + strIndFlowLabel;				
					eFlow.setEquation(strEquation);
				}
				
			}
		}
		pGraph.ResetGraph(false);
		// Print equation of all flows
		for (Edge e : pGraph.getEdgeList())
		{
			if (!e.getIndependent())
				appendLineToFrame(e.getLabel() + " = " + e.getEquation());
		}
		appendLineToFrame("");
		// Print Edge time cost
		for (Edge e : pGraph.getEdgeList())
			if(e.getVisible()){
				appendLineToFrame("C"+e.getLabel() + " = " + e.getTimecost());
			}
		//find the vertices of loop
		Map<String,ArrayList<Vertex>> loopVertices = new HashMap<String,ArrayList<Vertex>>();

		for(ArrayList<Vertex> cycle : lstCycleVertex)
			if(!cycle.get(0).equals("START")){
            if(cycle.get(0).getLooptype()!=null) 
            	if(!loopVertices.containsKey(cycle.get(0).getLabel()))
            		loopVertices.put(cycle.get(0).getLabel(), cycle);
            	else if(loopVertices.get(cycle.get(0).getLabel()).size()<cycle.size()){
            		loopVertices.remove(cycle.get(0).getLabel());
            		loopVertices.put(cycle.get(0).getLabel(), cycle);
            	}
		}
		Map<String,String> loopstr=new HashMap<String,String>();
		for(String key : loopVertices.keySet()){ 
			StringBuffer s = new StringBuffer();
			for(Vertex v : loopVertices.get(key)) s.append(v.getLabel()+" ");
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
			if(!cycle.get(0).equals("START")){
				 Edge flow = independentEdges.get(index);
		         Vertex c=null; // condition predecessors vertex of e, 
                                // if->A, e=if->A, c=if
		         if(flow.getSource().getLabel().equals(cycle.get(0).getLabel()))
		        	 c=cycle.get(0);
		         else c=cycle.get(cycle.size()-1);
		         for(String key : loopstr.keySet()){
		        	 if(loopstr.get(key).contains(cycleStr.get(index))) 
		        	 {flow.setLooptype(loopVertices.get(key).get(0).getLooptype());break;}
		         }
		         if(c.getLooptype()!=null) flow.setCondition(c.getLooptype());
		         else if(c.getType() !=null) flow.setCondition(c.getType());
			} index++;
		}
		appendLineToFrame("");
		for(Edge e : independentEdges) 
			if(e.getVisible()){
				if(e.getLooptype()!=null){
					appendLineToFrame(e.getLabel()+ ": " + e.getCondition()+" in "+e.getLooptype());
				}
				else {
					appendLineToFrame(e.getLabel()+ ": " + e.getCondition());
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
