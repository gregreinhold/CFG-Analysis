package ui;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JSplitPane;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JScrollPane;

import control_flow.ProgramExecutor;

import ui.SelectPanel.FileType;
import net.miginfocom.swing.MigLayout;

public class UIFrame extends JFrame {
	
	private static final long serialVersionUID = -8669948542495580754L;
	private SelectPanel _codePanel, _cfgPanel, _saveFilePanel;
	private JTextArea _programOutputText;
	private JButton _btnGo;
	
	public UIFrame() {
		super("Execution Time Equation Generator");
		getContentPane().setLayout(new MigLayout("", "[2000px]", "[2000px]"));
    	this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

		JSplitPane splitPane = initializeSplitPane();
		
		// initialize each half of the JSplitPane
		initializeLeftPanel(splitPane);
		initializeRightPanel(splitPane);
		
		this.setSize(1000,500);
	   	this.setVisible(true);
	}
	
	// initializes the JSplitPane that splits the JFrame into two sections (left and right)
	private JSplitPane initializeSplitPane(){
		JSplitPane splitPane = new JSplitPane();
		getContentPane().add(splitPane, "cell 0 0,grow");
		splitPane.setDividerLocation(600);
		return splitPane;
	}
	
	// initializes the left half of the JSplitPane, 
	// including the three panels that require user input and the Go button
	private void initializeLeftPanel(JSplitPane splitPane){
		JPanel leftPanel = new JPanel();
		splitPane.setLeftComponent(leftPanel);
		leftPanel.setSize(500, 500);
		leftPanel.setLayout(new MigLayout("", "[1000px]", "[59px][59px][59px]push[23px]"));
		
		initializeCodePanel(leftPanel);		
		initializeCFGPanel(leftPanel);		
		initializeSaveFlowGraphPanel(leftPanel);
		initializeGoButton(leftPanel);
	}
	
	// initializes the JPanel asking the user to select a Java code file
	private void initializeCodePanel(JPanel leftPanel){
		_codePanel = new SelectFilePanel("Select file containing Java code to be analyzed:", FileType.Java, this);
		leftPanel.add(_codePanel, "cell 0 0,grow");
	}
	
	// initializes the JPanel asking the user to select a GraphML file containing the CFG
	private void initializeCFGPanel(JPanel leftPanel){
		_cfgPanel = new SelectFilePanel("Select file containing CFG:", FileType.GraphML, this);
		leftPanel.add(_cfgPanel, "cell 0 1,grow");
	}
	
	// initializes the JPanel asking the user to select a location and name for the flow graph GraphML file
	private void initializeSaveFlowGraphPanel(JPanel leftPanel){
		_saveFilePanel = new SelectFolderPanel("Select save location of flow graph file:", this);
		leftPanel.add(_saveFilePanel, "cell 0 2,grow");
	}
	
	// initializes the Go Button as well as the creation and execution of the ProgramExecutor class.
	// The ProgramExecutor runs the necessary code to create the execution time equation
	private void initializeGoButton(JPanel leftPanel){
		_btnGo = new JButton("Go!");
		leftPanel.add(_btnGo, "cell 0 3,alignx center,aligny top");
		
		final UIFrame frame = this;
		
		_btnGo.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// when the button is clicked, clear the right panel and run the ProgramExecutor using the input files
 				try{
 					new ProgramExecutor().execute(_cfgPanel._selectedFile, 
 											  _codePanel._selectedFile, 
 											  _saveFilePanel._selectedFile, 
 											  ((SelectFolderPanel)_saveFilePanel)._txtSaveFileName.getText(), 
 											  frame);
 				} catch(Exception e){
 					resetOutputDisplay();
 					appendLineToOutputDisplay("EXCEPTION OCCURRED");
 					appendLineToOutputDisplay("Type: " + e.getClass().getCanonicalName());
 					appendLineToOutputDisplay("Message: " + e.getMessage());
 				}
 			}
        });
		
		_btnGo.setEnabled(false);
	}
	
	// Initializes the right half of the JSplitPane with a scrollable text area
	private void initializeRightPanel(JSplitPane splitPane){
		_programOutputText = new JTextArea(2,50);
		_programOutputText.setMargin(new Insets(5,5,5,5));
		_programOutputText.setEditable(false);
		JScrollPane rightPanel = new JScrollPane(_programOutputText);
		splitPane.setRightComponent(rightPanel);
		
		_programOutputText.setText("Cost equation will be displayed here.");
	}
	
	// Will update whether or not the Go Button should be Enabled based on
	// if all of the SelectPanels have a file selected
	public void updateGoButtonEnabled(){
		_btnGo.setEnabled(_codePanel.hasFileSelected()&&_cfgPanel.hasFileSelected()&&_saveFilePanel.hasFileSelected());
	}
	
	// Add another line containing string to the right panel output
	public void appendLineToOutputDisplay(String string){
		_programOutputText.append(string + "\n");
	}
	
	// Clear all text in the right panel output
	public void resetOutputDisplay(){
		_programOutputText.setText("");
	}

}
