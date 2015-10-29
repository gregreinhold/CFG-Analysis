package control_flow;

import static graph.GraphConstants.*;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;

import javax.swing.JPanel;

import graph.*;


@SuppressWarnings("serial")
public class DrawingPanel extends JPanel
{   	
	Graph graph;
	
    public DrawingPanel(Graph pGraph) 
    {
       super();
       this.setBackground(java.awt.Color.white);
       this.setPreferredSize(new java.awt.Dimension(500, 500));
       this.setSize(new java.awt.Dimension(500, 500));
       graph = pGraph;
    }
    
    public void paintComponent(Graphics g) 
    {
    	super.paintComponent(g);
       	Graphics2D g2 = (Graphics2D) g;
       	
       	double dblAngle;
       	int intCurve, intXText, intYText;
        
       	Point pEnd, pStart;			// Start point of source and target vertex
       	Point pCenter1, pCenter2;	// Center of vertices
       	
       	for (Vertex v : graph.getVerticesList())
       	{
       		if (v.getVisible())		// In case we want to hide a vertex and without having to delete it
       		{
	       		g2.setColor(v.getColor());
	       		g2.fillOval(v.getX(), v.getY(), 10, 10);
	       		g2.drawString(v.getLabel(), v.getX() + 13, v.getY() + 10);
       		}
       	}
       	for (Edge e : graph.getEdgeList())
       	{
       		if (e.getVisible())		// In case we want to hide an edge (ie. virtual edge from END -> START)
       		{
	       		g2.setColor(e.getColor());
	           	pCenter1 = new Point (e.getTarget().getX() + VERTEXSIZE / 2, e.getTarget().getY() + VERTEXSIZE / 2);
	           	pCenter2 = new Point (e.getSource().getX() + VERTEXSIZE / 2, e.getSource().getY() + VERTEXSIZE / 2);
	
	            dblAngle = Math.atan2(pCenter1.y - pCenter2.y, pCenter1.x - pCenter2.x);
	            pEnd = new Point(pCenter1.x + (int)(GraphConstants.VERTEXSIZE / 2 * Math.cos(dblAngle + Math.PI)), pCenter1.y + (int)(VERTEXSIZE / 2 * Math.sin(dblAngle + Math.PI)));
	            pStart = new Point(pCenter2.x + (int)(VERTEXSIZE / 2 * Math.cos(dblAngle)), pCenter2.y + (int)(VERTEXSIZE / 2 * Math.sin(dblAngle)));
	
	            intCurve = e.getCurve();
	            if (intCurve == 0)
	            {
	               	intXText = (int) ((pStart.x + pEnd.x)/2 + 5 * Math.abs(Math.sin(dblAngle)));
	               	intYText = (int) ((pStart.y + pEnd.y)/2 - 5 * Math.abs(Math.cos(dblAngle)));
	            }
	            else
	            {
	               	intXText = (int) ((pStart.x + pEnd.x)/2 + 30 * intCurve * Math.sin(dblAngle));
	               	intYText = (int) ((pStart.y + pEnd.y)/2 - 10 * Math.cos(dblAngle));
	            }
	       		DrawCurve(g2, pStart.x, pStart.y, pEnd.x, pEnd.y, dblAngle, intCurve, e.getLabel());
	        	g.drawString(e.getLabel(), intXText, intYText);
       		}
       	}
    }
    
    public void DrawCurve(Graphics2D g, int xS, int yS, int xT, int yT, double pAngle, int pCurve, String pLabel)
    {
       	int xM, yM;
       	
       	xM = (int) ((xS + xT)/2 + pCurve * 50 * Math.sin(pAngle));
       	yM = (int) ((yS + yT)/2 - pCurve * 50 * Math.cos(pAngle));

       	GeneralPath path = new GeneralPath();
		float arrSize = 7; // Size of the arrow segments
		float adjSize = (float)(arrSize/Math.sqrt(2));
		float ex = xT - xM;
		float ey = yT - yM;
		float abs_e = (float)Math.sqrt(ex*ex + ey*ey);
		ex /= abs_e;
		ey /= abs_e;
		
		// Creating quad arrow
		path.moveTo(xS, yS);
		path.quadTo(xM, yM, xT, yT);
		path.lineTo(xT + (ey-ex)*adjSize, yT - (ex + ey)*adjSize);
		path.moveTo(xT, yT);
		path.lineTo(xT - (ey + ex)*adjSize, yT + (ex - ey)*adjSize);
		g.draw(path);
    }
}
