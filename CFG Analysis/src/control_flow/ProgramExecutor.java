package control_flow;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

<<<<<<< HEAD:CFG Analysis/src/control_flow/Test.java
import java.awt.Desktop;
=======
import ui.UIFrame;

>>>>>>> refs/remotes/origin/master:CFG Analysis/src/control_flow/ProgramExecutor.java
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import graph.*;

public class ProgramExecutor {
	
<<<<<<< HEAD:CFG Analysis/src/control_flow/Test.java
	//private Graph graph = new Graph();

	public Test()
	{
		Graph gGraph = null;
		String sGraphPath = ""; //src/control_flow/TestAlgorithms.java";
		//String sCodePath = "TestAlgorithms.TestFunction3.src.graphml";
		String sCodePath = "code.txt";
		
		try
		{
			//System.out.println(new GraphParser().EvalExpr("(C(5*2,2) - 1)^3 + C(10,5)"));
			if (sCodePath.length() > 0)
			{
				if (sGraphPath.length() > 0)
					gGraph = ReadXML(sGraphPath, sCodePath);
				else 
					gGraph = new GraphParser().Parse(sCodePath);
			}
			if (gGraph != null)
			{
				ReducedGraph(gGraph);
				AnalysisGraph(gGraph);
				gGraph.PrintGraph(true, true);
				gGraph.CurveEdges();
				new DrawingApp(gGraph);
				generateDot(gGraph);
			}
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
=======
	private UIFrame _frame;
	
	// default constructor
	public ProgramExecutor(){
	}
	
	// executes the functions required to create the Execution Time Equation and resulting flow graph
	public void execute(File cfgFile, File codeFile, File saveFolder, String flowGraphFileName, UIFrame frame){
		Graph gControlFlowGraph;
		_frame = frame;
		clearTextFromFrame();
		gControlFlowGraph = readGraphMLFile(cfgFile, codeFile);
		reduceGraph(gControlFlowGraph);
		analyzeGraph(gControlFlowGraph);
		gControlFlowGraph.CurveEdges();
		new DrawingApp(gControlFlowGraph);
		generateDot(gControlFlowGraph, saveFolder, flowGraphFileName);
>>>>>>> refs/remotes/origin/master:CFG Analysis/src/control_flow/ProgramExecutor.java
	}
	

	// Read XML and parse it, generate a graph
<<<<<<< HEAD:CFG Analysis/src/control_flow/Test.java
	public Graph ReadXML(String sXMLPath, String sCodePath)
	{
		File codes = new File(sCodePath);
=======
	public Graph readGraphMLFile(File cfgFile, File codeFile)
	{
		// Determines which type of statement/loop caused a split in flow and 
		// saves it alongside its corresponding line number.
>>>>>>> refs/remotes/origin/master:CFG Analysis/src/control_flow/ProgramExecutor.java
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
<<<<<<< HEAD:CFG Analysis/src/control_flow/Test.java
		File fXml = new File(sXMLPath);
=======

		// Uses DocumentBuilder to parse GraphML file and create an internal Graph representation
>>>>>>> refs/remotes/origin/master:CFG Analysis/src/control_flow/ProgramExecutor.java
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
		for (int i = 0; i < lstNode.getLength(); i++)
		{
			objNode = lstNode.item(i);
			strName = objNode.getAttributes().item(0).getTextContent();
			
			lstChildNode = objNode.getChildNodes();
			for (int j = 0; j < lstChildNode.getLength(); j++)
			{
				objChildNode = lstChildNode.item(j);
				if (objChildNode.getNodeType() == Node.ELEMENT_NODE)
				{
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
			if(lineloop.containsKey(newv.GetLabel())) newv.SetLooptype(lineloop.get(newv.GetLabel()));
			if(lineif.containsKey(newv.GetLabel())) newv.SetType(lineif.get(newv.GetLabel()));
			gGraph.AddVertex(newv);
		}
		
		lstNode = doc.getElementsByTagName("edge");
		for (int i = 0; i < lstNode.getLength(); i++) {
			objNode = lstNode.item(i);
			
			if (objNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) objNode;
				gGraph.AddEdge(new Edge(gGraph.GetVertexByName(eElement.getAttribute("source")), gGraph.GetVertexByName(eElement.getAttribute("target")), "e" + i));
			}
		}
		
		return gGraph;
	}
	
<<<<<<< HEAD:CFG Analysis/src/control_flow/Test.java
	public void ReducedGraph(Graph g){
		ArrayList<Vertex> gvs = new ArrayList<Vertex>();
		for(Vertex v : g.GetVertex()) gvs.add(v);
		for(Vertex v : gvs)
=======
	// Reduces input Graph g by consolidating nodes along one flow into a single edge.
	// i.e. each Vertex will correspond to a fork/merge of flows in the graph,
	// and each Edge corresponds to a single flow with cost = cost of the consolidated operations
	public void reduceGraph(Graph g){
		ArrayList<Vertex> lstVertices = new ArrayList<Vertex>();
		for(Vertex v : g.getVerticesList()) {
			lstVertices.add(v);
		}
		for(Vertex v : lstVertices)
>>>>>>> refs/remotes/origin/master:CFG Analysis/src/control_flow/ProgramExecutor.java
		{
			if(v.GetInEdge().size()==1 && v.GetOutEdge().size()==1 
					&& !v.GetLabel().equals("START") && !v.GetLabel().equals("EXIT"))
			{
				Edge newEdge = new Edge(v.GetInEdge().get(0).getSource(),v.GetOutEdge().get(0).getTarget(),
						v.GetInEdge().get(0).getLabel());
				if(newEdge.getTimecost().equals("")){
					newEdge.setTimecost("C"+v.GetLabel());
				} else {
					newEdge.setTimecost("C"+v.GetLabel()+" + "+newEdge.getTimecost());
				}
				g.AddEdge(newEdge);
				g.DeleteVertex(v);
<<<<<<< HEAD:CFG Analysis/src/control_flow/Test.java
				}
=======
				g.DeleteEdge(v.GetInEdge().get(0));
				g.DeleteEdge(v.GetOutEdge().get(0));
			}
>>>>>>> refs/remotes/origin/master:CFG Analysis/src/control_flow/ProgramExecutor.java
		}
		//Put Start node in first
		Vertex start = g.GetVertexByLabel("START");
<<<<<<< HEAD:CFG Analysis/src/control_flow/Test.java
		g.GetVertex().remove(start);
		g.GetVertex().add(0, start);
		
=======
		g.DeleteVertex(g.GetVertexByLabel("START"));
		g.getVerticesList().add(0, start);
>>>>>>> refs/remotes/origin/master:CFG Analysis/src/control_flow/ProgramExecutor.java
		//Reassign the edge label
		int k=1;
		for(Vertex v : g.getVerticesList()){
			if(!v.GetVisited() && v.GetOutEdge().size()>0) {
				for(Edge e : v.GetOutEdge()) 
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
		File outDot = new File(saveFolder.getAbsolutePath()+ "\\" + flowGraphFileName);
		String graphname = flowGraphFileName;
		String nodeshape="";
		try{
			BufferedWriter bfw = new BufferedWriter(new FileWriter(outDot));
			bfw.write(" digraph \""+graphname +"\" {");bfw.newLine();
			bfw.write("graph [label=\""+graphname+ "\"];");bfw.newLine();
			for(Vertex v : g.getVerticesList()){
				if(v.GetLabel().equals("START")||v.GetLabel().equals("EXIT")) {
					nodeshape="box"; 
				} else {
					nodeshape="circle";
				}
				bfw.write(v.GetLabel()+ " "+"[label=\""+v.GetLabel()+"\",shape="+nodeshape
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
				if(e.getSource().GetLabel().equals("EXIT")) {
					style="dashed"; 
				} else {
					style="solid"; 
				}
				bfw.write(" "+e.getSource().GetLabel()+" -> "+e.getTarget().GetLabel()
						+" [label=\""+e.getLabel()+"\", style="+style+" color="+edgecolor+ "]");
				bfw.newLine();
			}
			bfw.write("}");
			bfw.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		try
		{
			new ProcessBuilder("graphviz-2.38\\bin\\dot", "-Tpng", "test.dot", "-o", "test.png").start();
			Desktop.getDesktop().open(new File("test.png"));
		} 
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
	}

	// Analyze the graph to derive the Execution Time Equation
	public void analyzeGraph(Graph pGraph)
	{
		Edge eFlow;
		Vertex vSrc = null, vTgt = null;
		Vertex vStart = pGraph.GetVertexByLabel("START");									// Start vertex
		Vertex vEnd = pGraph.GetVertexByLabel("EXIT");										// End vertex
		ArrayList<Vertex> lstVertex = null;													// Temp list of vertex cycle
		ArrayList<ArrayList<Vertex>> lstCycleVertex = new ArrayList<ArrayList<Vertex>>();	// List all lists of cycles
        ArrayList<Edge> independentEdges = new ArrayList<Edge>(); // list of independent flows
 		// Add virtual edge from start to end, so we can assign equation of all edges outside any loops to 1
		eFlow = new Edge(vEnd, vStart, "e0");
<<<<<<< HEAD:CFG Analysis/src/control_flow/Test.java
		eFlow.SetValue("1");
		eFlow.SetVisible(false);
=======
		eFlow.setCost("1");
		eFlow.setVisible(false);
>>>>>>> refs/remotes/origin/master:CFG Analysis/src/control_flow/ProgramExecutor.java
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
<<<<<<< HEAD:CFG Analysis/src/control_flow/Test.java
					eFlow.SetIndependent(true);
=======
					//System.out.println("Independent Flow " + eFlow.GetLabel() + " : " + eFlow.GetSource().GetLabel() + " -> " + eFlow.GetTarget().GetLabel());
					eFlow.setIndependent(true);
>>>>>>> refs/remotes/origin/master:CFG Analysis/src/control_flow/ProgramExecutor.java
					independentEdges.add(eFlow);
				}
			}
			lstVertex = new ArrayList<Vertex>();
			pGraph.ResetGraph(false);
			pGraph.FindCycle(vStart, lstVertex);
		}
		while (lstVertex.size() != 0);
		
		// *Debug print
		/*String temp;
		for (ArrayList<Vertex> l : lstCycleVertex)
		{
			temp = "Cycle: ";
			for (Vertex v : l)
			{
				temp += v.GetLabel() + ", ";
			}
			appendLineToFrame(temp.substring(0, temp.length() - 2));
		}
<<<<<<< HEAD:CFG Analysis/src/control_flow/Test.java
		System.out.println();*/
=======
		appendLineToFrame("");
>>>>>>> refs/remotes/origin/master:CFG Analysis/src/control_flow/ProgramExecutor.java
		
		// Generate dependent flow equations
		String strEquation;
		String strIndFlowLabel = "";
		boolean blnForward = false;
		
		for (ArrayList<Vertex> l : lstCycleVertex)
		{
			// Get direction of independent flow (last edge)
			vSrc = l.get(l.size() - 1);
			vTgt = l.get(0);
			eFlow = vSrc.FindDiffIndepEdge(vTgt);
			if (eFlow != null)
			{
<<<<<<< HEAD:CFG Analysis/src/control_flow/Test.java
				blnForward = (eFlow.GetTarget() == vTgt);		// Direction of independent flow, A -> B = Forward, A <- B = Backward
				strIndFlowLabel = eFlow.GetValue();				// If the flow has an assigned value, use that instead of the flow label
				if (strIndFlowLabel.length() == 0)
					strIndFlowLabel = eFlow.GetLabel();
=======
				blnForward = (eFlow.getTarget() == vTgt);		// Direction of independent flow, A -> B = Forward, A <- B = Backward
				strIndFlowLabel = eFlow.getCost();				// If the flow has an assigned cost, use that instead of the flow label
				if (strIndFlowLabel.length() == 0){
					strIndFlowLabel = eFlow.getLabel();
				}
>>>>>>> refs/remotes/origin/master:CFG Analysis/src/control_flow/ProgramExecutor.java
			}
			else
			{
				System.out.println("ERROR: Independent flow not found");
				return;
			}

			// Get direction of dependent flow and determine +/- independent flow
			// eFlow.GetTarget() == vTgt --> Forward edge
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
            if(cycle.get(0).GetLooptype()!=null) 
            	if(!loopVertices.containsKey(cycle.get(0).GetLabel()))
            		loopVertices.put(cycle.get(0).GetLabel(), cycle);
            	else if(loopVertices.get(cycle.get(0).GetLabel()).size()<cycle.size()){
            		loopVertices.remove(cycle.get(0).GetLabel());
            		loopVertices.put(cycle.get(0).GetLabel(), cycle);
            	}
		}
		Map<String,String> loopstr=new HashMap<String,String>();
		for(String key : loopVertices.keySet()){ 
			StringBuffer s = new StringBuffer();
			for(Vertex v : loopVertices.get(key)) s.append(v.GetLabel()+" ");
			loopstr.put(key, s.toString());
		}
		ArrayList<String> cycleStr = new ArrayList<String>();
		for(ArrayList<Vertex> cycle : lstCycleVertex){
			StringBuffer s = new StringBuffer();
			for(Vertex v : cycle) s.append(v.GetLabel()+" ");
			cycleStr.add(s.toString());
		}
		int index=0;
		for(ArrayList<Vertex> cycle : lstCycleVertex){
			if(!cycle.get(0).equals("START")){
				 Edge flow = independentEdges.get(index);
		         Vertex c=null; // condition predecessors vertex of e, 
                                // if->A, e=if->A, c=if
		         if(flow.getSource().GetLabel().equals(cycle.get(0).GetLabel()))
		        	 c=cycle.get(0);
		         else c=cycle.get(cycle.size()-1);
		         for(String key : loopstr.keySet()){
		        	 if(loopstr.get(key).contains(cycleStr.get(index))) 
		        	 {flow.setLooptype(loopVertices.get(key).get(0).GetLooptype());break;}
		         }
		         if(c.GetLooptype()!=null) flow.setCondition(c.GetLooptype());
		         else if(c.GetType() !=null) flow.setCondition(c.GetType());
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
//		new Test();
		
//		new UIFrame();
		
		new UIFrame();
	}
}
