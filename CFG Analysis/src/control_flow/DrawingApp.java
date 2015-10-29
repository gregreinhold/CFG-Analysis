package control_flow;

import graph.Graph;

//import javax.swing.*;

@SuppressWarnings("serial")
public class DrawingApp extends javax.swing.JFrame 
{
	//private JPanel _radioButtonPanel;
    private DrawingPanel _drawPanel;
  
    public DrawingApp(Graph graph) 
    {
    	super("CFG Analysis");
    	this.setSize(500, 500);
    
    	_drawPanel = new DrawingPanel(graph);
	    
	   //JPanel _southPanel = new JPanel(new GridLayout(0,1));
	   //_southPanel.add(_radioButtonPanel);
	   
    	this.add(_drawPanel, java.awt.BorderLayout.CENTER);
	   //this.add(_southPanel, java.awt.BorderLayout.SOUTH);
	    
    	this.pack();
	   	this.setVisible(true);
    }
}
