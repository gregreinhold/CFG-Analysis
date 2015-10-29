<<<<<<< HEAD
package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SelectFilePanel extends SelectPanel {
	
	private static final long serialVersionUID = -1827134053709772044L;
	protected FileType _fileType;

	// constructor
	public SelectFilePanel(String lblSelectFileText, FileType fileType, UIFrame frame) {
		super(lblSelectFileText, UIConstants.NO_FILE_SELECTED, frame);
		
		_fileType = fileType;
	}

	// Adds the ActionListener to the open button.
	// Will require them to select a file of type _fileType.
	@Override
	protected void addButtonActionListener(JButton btnOpenButton) {
		btnOpenButton.addActionListener(new ActionListener(){
			
		    @Override
		    public void actionPerformed(ActionEvent e) {
	        	File selectedFile = null;
	            try {
	
	            	JFileChooser jfc = new JFileChooser();
	            	switch(_fileType){
	            	case GraphML:
		            	jfc.addChoosableFileFilter(new FileNameExtensionFilter("GraphML File","graphml"));
	            		break;
	            	case Code:
		            	jfc.addChoosableFileFilter(new FileNameExtensionFilter("Java file","java"));
		            	jfc.addChoosableFileFilter(new FileNameExtensionFilter("Text file","txt"));
	            		break;
	            	}
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

	// Does nothing since this is opening a file, not saving a file
	@Override
	protected void addSaveFileName() {
		// Do nothing
	}
	
	@Override
	public Boolean hasFileSelected(){
		return super.hasFileSelected() 
				&& !_lblFileSelected.getText().equals(UIConstants.NO_FILE_SELECTED);
	}

}
=======
package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SelectFilePanel extends SelectPanel {
	
	private static final long serialVersionUID = -1827134053709772044L;
	protected FileType _fileType;

	// constructor
	public SelectFilePanel(String lblSelectFileText, FileType fileType, UIFrame frame) {
		super(lblSelectFileText, UIConstants.NO_FILE_SELECTED, frame);
		
		_fileType = fileType;
	}

	// Adds the ActionListener to the open button.
	// Will require them to select a file of type _fileType.
	@Override
	protected void addButtonActionListener(JButton btnOpenButton) {
		btnOpenButton.addActionListener(new ActionListener(){
			
		    @Override
		    public void actionPerformed(ActionEvent e) {
	        	File selectedFile = null;
	            try {
	
	            	JFileChooser jfc = new JFileChooser();
	            	FileFilter filter = null;
	            	switch(_fileType){
	            	case GraphML:
	            		filter = new FileNameExtensionFilter("GraphML File","graphml");
	            		break;
	            	case Java:
	            		filter = new FileNameExtensionFilter("Java file","java");
	            		break;
	            	}         	
	            	jfc.setFileFilter(filter);
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

	// Does nothing since this is opening a file, not saving a file
	@Override
	protected void addSaveFileName() {
		// Do nothing
	}
	
	@Override
	public Boolean hasFileSelected(){
		return super.hasFileSelected() 
				&& !_lblFileSelected.getText().equals(UIConstants.NO_FILE_SELECTED);
	}

}
>>>>>>> refs/remotes/origin/master
