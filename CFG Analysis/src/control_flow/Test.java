package control_flow;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import graph.*;
import static graph.Constant.*;

public class Test {
	
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
	}
	

	// Read XML and parse it, generate a graph
	public Graph ReadXML(String sXMLPath, String sCodePath)
	{
		File codes = new File(sCodePath);
		Map<String,String> lineloop = new HashMap<String,String>();
        Map<String,String> lineif = new HashMap<String,String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(codes));
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
		File fXml = new File(sXMLPath);
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
			doc = dBuilder.parse(fXml);
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
	
	public void ReducedGraph(Graph g){
		ArrayList<Vertex> gvs = new ArrayList<Vertex>();
		for(Vertex v : g.GetVertex()) gvs.add(v);
		for(Vertex v : gvs)
		{
			if(v.GetInEdge().size()==1 && v.GetOutEdge().size()==1 
					&& !v.GetLabel().equals("START") && !v.GetLabel().equals("EXIT"))
				{
				Edge addedge=new Edge(v.GetInEdge().get(0).GetSource(),v.GetOutEdge().get(0).GetTarget()
						,v.GetInEdge().get(0).GetLabel());
				if(addedge.GetTimecost().equals(""))
				addedge.SetTimecost("C"+v.GetLabel());
				else addedge.SetTimecost("C"+v.GetLabel()+" + "+addedge.GetTimecost());
				g.AddEdge(addedge);
				g.DeleteVertex(v);
				}
		}
		//Put Start node in the first
		Vertex start = g.GetVertexByLabel("START");
		g.GetVertex().remove(start);
		g.GetVertex().add(0, start);
		
		//Reassign the edge label
		int k=1;
		for(Vertex v : g.GetVertex()){
			if(!v.GetVisited() && v.GetOutEdge().size()>0) {
				for(Edge e : v.GetOutEdge()) 
				{e.SetLabel("e"+k++);
				if(e.GetTimecost().equals("")) e.SetTimecost("0");
				}
			}
		}
		Collections.sort(g.GetEdge(), new Comparator<Edge>(){
			public int compare(Edge a, Edge b){
				return Integer.parseInt(a.GetLabel().substring(1)) 
						> Integer.parseInt(b.GetLabel().substring(1)) ? 1 : -1;
			}
		});
		g.ResetGraph(false);
	}
	public void generateDot(Graph g){
		File outdot= new File("test.dot");
		String graphname = "TEST";
		String nodeshape="";
		try{
		BufferedWriter bfw = new BufferedWriter(new FileWriter(outdot));
		bfw.write(" digraph \""+graphname +"\" {");bfw.newLine();
		bfw.write("graph [label=\""+graphname+ "\"];");bfw.newLine();
		for(Vertex v : g.GetVertex()){
		if(v.GetLabel().equals("START")||v.GetLabel().equals("EXIT")) nodeshape="box"; else nodeshape="circle";
		bfw.write(v.GetLabel()+ " "+"[label=\""+v.GetLabel()+"\",shape="+nodeshape
				+" style=filled, fillcolor=\"#CECEFF\", fixedsize=true, fontsize=12, width=0.78, height=0.36 ]");
		bfw.newLine();
		}
		String style=null;
		String edgecolor="";
		for(Edge e : g.GetEdge()){
			if(e.GetIndependent()) edgecolor="blue"; else edgecolor="black";
			if(e.GetSource().GetLabel().equals("EXIT")) style="dashed"; else style="solid"; 
			bfw.write(" "+e.GetSource().GetLabel()+" -> "+e.GetTarget().GetLabel()
					+" [label=\""+e.GetLabel()+"\", style="+style+" color="+edgecolor+ "]");
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

	// This analyze the graph, obviously
	public void AnalysisGraph(Graph pGraph)
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
		eFlow.SetValue("1");
		eFlow.SetVisible(false);
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
					eFlow.SetIndependent(true);
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
			System.out.println(temp.substring(0, temp.length() - 2));
		}
		System.out.println();*/
		
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
				blnForward = (eFlow.GetTarget() == vTgt);		// Direction of independent flow, A -> B = Forward, A <- B = Backward
				strIndFlowLabel = eFlow.GetValue();				// If the flow has an assigned value, use that instead of the flow label
				if (strIndFlowLabel.length() == 0)
					strIndFlowLabel = eFlow.GetLabel();
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
					strEquation = eFlow.GetEquation();
					strEquation += ((blnForward == (eFlow.GetTarget() == vTgt)) ? ((strEquation.length() == 0) ? "" : " + ") : " - ") + strIndFlowLabel;				
					eFlow.SetEquation(strEquation);
				}
			}
		}
		pGraph.ResetGraph(false);
		// Print equation of all flows
		for (Edge e : pGraph.GetEdge())
		{
			if (!e.GetIndependent())
				System.out.println(e.GetLabel() + " = " + e.GetEquation());
		}
		System.out.println();
		// Print Edge time cost
		for (Edge e : pGraph.GetEdge())
			if(e.GetVisible())
		    System.out.println("C"+e.GetLabel() + " = " + e.GetTimecost());
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
		         if(flow.GetSource().GetLabel().equals(cycle.get(0).GetLabel()))
		        	 c=cycle.get(0);
		         else c=cycle.get(cycle.size()-1);
		         for(String key : loopstr.keySet()){
		        	 if(loopstr.get(key).contains(cycleStr.get(index))) 
		        	 {flow.SetLooptype(loopVertices.get(key).get(0).GetLooptype());break;}
		         }
		         if(c.GetLooptype()!=null) flow.SetCondition(c.GetLooptype());
		         else if(c.GetType() !=null) flow.SetCondition(c.GetType());
			} index++;
		}
		System.out.println();
		for(Edge e : independentEdges) 
			if(e.GetVisible()){
			if(e.GetLooptype()!=null)
			System.out.println(e.GetLabel()+ ": " + e.GetCondition()+" in "+e.GetLooptype());
			else System.out.println(e.GetLabel()+ ": " + e.GetCondition());
			}
	}
	
	public static void main(String[] args)
	{
		new Test();
	}
}
