package control_flow;

import static graph.Constant.*;
import graph.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.*;

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
       addMouseListener(new PanelMouseListener());
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
       	
       	for (Vertex v : graph.GetVertex())
       	{
       		if (v.GetVisible())		// In case we want to hide a vertex and without having to delete it
       		{
	       		g2.setColor(v.GetColor());
	       		g2.fillOval(v.GetX(), v.GetY(), 10, 10);
	       		g2.drawString(v.GetLabel(), v.GetX() + 13, v.GetY() + 10);
       		}
       	}
       	for (Edge e : graph.GetEdge())
       	{
       		if (e.GetVisible())		// In case we want to hide an edge (ie. virtual edge from END -> START)
       		{
	       		g2.setColor(e.GetColor());
	           	pCenter1 = new Point (e.GetTarget().GetX() + VERTEXSIZE / 2, e.GetTarget().GetY() + VERTEXSIZE / 2);
	           	pCenter2 = new Point (e.GetSource().GetX() + VERTEXSIZE / 2, e.GetSource().GetY() + VERTEXSIZE / 2);
	
	            dblAngle = Math.atan2(pCenter1.y - pCenter2.y, pCenter1.x - pCenter2.x);
	            pEnd = new Point(pCenter1.x + (int)(Constant.VERTEXSIZE / 2 * Math.cos(dblAngle + Math.PI)), pCenter1.y + (int)(VERTEXSIZE / 2 * Math.sin(dblAngle + Math.PI)));
	            pStart = new Point(pCenter2.x + (int)(VERTEXSIZE / 2 * Math.cos(dblAngle)), pCenter2.y + (int)(VERTEXSIZE / 2 * Math.sin(dblAngle)));
	
	            intCurve = e.GetCurve();
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
	       		DrawCurve(g2, pStart.x, pStart.y, pEnd.x, pEnd.y, dblAngle, intCurve, e.GetLabel());
	        	g.drawString(e.GetLabel(), intXText, intYText);
       		}
       	}
    }
    
    // Need to somehow merge this into the curve function so we can get the solid arrow for curves
    // I copied this from the web and have no idea how this works
    private void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h)
    {
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx*dx + dy*dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy/D, cos = dx/D;

        x = xm*cos - ym*sin + x1;
        ym = xm*sin + ym*cos + y1;
        xm = x;

        x = xn*cos - yn*sin + x1;
        yn = xn*sin + yn*cos + y1;
        xn = x;

        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};

        g.drawLine(x1, y1, x2, y2);
        g.fillPolygon(xpoints, ypoints, 3);
     }
    
    public void DrawCurve(Graphics2D g, int xS, int yS, int xT, int yT, double pAngle, int pCurve, String pLabel)
    {
       	int xM, yM;
       	//Path2D.Double p;
       	
       	xM = (int) ((xS + xT)/2 + pCurve * 50 * Math.sin(pAngle));
       	yM = (int) ((yS + yT)/2 - pCurve * 50 * Math.cos(pAngle));
       	
       	/*p = new Path2D.Double();
       	p.moveTo(xS, yS);
       	p.curveTo(xS, yS, xM, yM, xT, yT);
       	g.draw(p);
       	*/

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
    
    private class PanelMouseListener extends javax.swing.event.MouseInputAdapter
    {
         public void mouseClicked(MouseEvent e) 
         {
        	 //System.out.println(e.getX() + "," + e.getY());        	 
         }
     }
}
