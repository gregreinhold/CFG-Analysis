<<<<<<< HEAD
package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class SelectFolderPanel extends SelectPanel{

	private static final long serialVersionUID = 3854815323217708709L;
	protected JTextField _txtSaveFileName;

	// constructor
	public SelectFolderPanel(String lblSelectFileText, UIFrame frame) {
		super(lblSelectFileText, UIConstants.NO_FOLDER_SELECTED, frame);
	}

	// Adds the ActionListener to the open button.
	// Forces user to choose the directory to which they want to save their flow graph file.
	@Override
	protected void addButtonActionListener(JButton btnOpenButton) {
		btnOpenButton.addActionListener(new ActionListener(){
			
		    @Override
		    public void actionPerformed(ActionEvent e) {
	        	File selectedFile = null;
	            try {
	            	JFileChooser jfc = new JFileChooser();
	            	jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	            	jfc.setAcceptAllFileFilterUsed(false);	// remove "All Files" option
	            	jfc.showOpenDialog(null);
	                jfc.setVisible(true);
	                selectedFile = new File(jfc.getSelectedFile().getPath());

	            } catch (Exception exc) {
	            	_lblFileSelected.setText(UIConstants.OPEN_COMMAND_FAILED + exc.getLocalizedMessage());
	            }

	            if (selectedFile != null) {
	            	_selectedFile = selectedFile;
	            	_lblFileSelected.setText(">>> " + _selectedFile.getAbsolutePath());
	            }
	            
	            _frame.updateGoButtonEnabled();
	        }
		});
	}
	
	// will add a JLabel and JTextField to the Panel that allows the user to enter a file name to be saved to.
	@Override
	protected void addSaveFileName() {
		JLabel lblSaveFile = new JLabel("Enter file name for flow graph:");
		add(lblSaveFile, "cell 0 3");
		
		_txtSaveFileName = new JTextField("DefaultFileName");
		_txtSaveFileName.setSize(200, 10);
		add(_txtSaveFileName, "cell 0 4,growx");
	}
	
	// Requires the additional condition that the JTextField cannot be empty
	@Override
	public Boolean hasFileSelected(){
		return super.hasFileSelected() 
				&& !_lblFileSelected.getText().equals(UIConstants.NO_FOLDER_SELECTED) 
				&& !_txtSaveFileName.getText().equals("");
	}

}
=======
package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class SelectFolderPanel extends SelectPanel{

	private static final long serialVersionUID = 3854815323217708709L;
	protected JTextField _txtSaveFileName;

	// constructor
	public SelectFolderPanel(String lblSelectFileText, UIFrame frame) {
		super(lblSelectFileText, UIConstants.NO_FOLDER_SELECTED, frame);
	}

	// Adds the ActionListener to the open button.
	// Forces user to choose the directory to which they want to save their flow graph file.
	@Override
	protected void addButtonActionListener(JButton btnOpenButton) {
		btnOpenButton.addActionListener(new ActionListener(){
			
		    @Override
		    public void actionPerformed(ActionEvent e) {
	        	File selectedFile = null;
	            try {
	            	JFileChooser jfc = new JFileChooser();
	            	jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	            	jfc.setAcceptAllFileFilterUsed(false);	// remove "All Files" option
	            	jfc.showOpenDialog(null);
	                jfc.setVisible(true);
	                selectedFile = new File(jfc.getSelectedFile().getPath());

	            } catch (Exception exc) {
	            	_lblFileSelected.setText(UIConstants.OPEN_COMMAND_FAILED + exc.getLocalizedMessage());
	            }

	            if (selectedFile != null) {
	            	_selectedFile = selectedFile;
	            	_lblFileSelected.setText(">>> " + _selectedFile.getAbsolutePath());
	            }
	            
	            _frame.updateGoButtonEnabled();
	        }
		});
	}
	
	// will add a JLabel and JTextField to the Panel that allows the user to enter a file name to be saved to.
	@Override
	protected void addSaveFileName() {
		JLabel lblSaveFile = new JLabel("Enter file name for flow graph:");
		add(lblSaveFile, "cell 0 3");
		
		_txtSaveFileName = new JTextField("DefaultFileName");
		_txtSaveFileName.setSize(200, 10);
		add(_txtSaveFileName, "cell 0 4,growx");
	}
	
	// Requires the additional condition that the JTextField cannot be empty
	@Override
	public Boolean hasFileSelected(){
		return super.hasFileSelected() 
				&& !_lblFileSelected.getText().equals(UIConstants.NO_FOLDER_SELECTED) 
				&& !_txtSaveFileName.getText().equals("");
	}

}
>>>>>>> refs/remotes/origin/master
