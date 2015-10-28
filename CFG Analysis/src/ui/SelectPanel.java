package ui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;

import java.io.File;

public abstract class SelectPanel extends JPanel{
	
	private static final long serialVersionUID = 2613768516722767005L;
	protected JLabel _lblSelectFile, _lblFileSelected;
    protected File _selectedFile;
    protected UIFrame _frame;
	
	public SelectPanel(String lblSelectFileText, String notSelectedYet, UIFrame frame) {
		_frame = frame;
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[grow]", "[][][]"));
		
		initializeSelectFileLabel(lblSelectFileText);		
		initializeFileSelectedLabel(notSelectedYet);		
		initializeOpenButton();		
		addSaveFileName();
	}
	
	// initializes the label with the text passed in by the user
	protected void initializeSelectFileLabel(String lblSelectFileText){
		_lblSelectFile = new JLabel(lblSelectFileText);
		add(_lblSelectFile, "cell 0 0");
	}
	
	// initializes the label that specifies which file is selected 
	// with a statement saying a file is not yet selected
	protected void initializeFileSelectedLabel(String notSelectedYet){
		_lblFileSelected = new JLabel(notSelectedYet);
		add(_lblFileSelected, "cell 0 1");
	}

	// initializes the button that will allow the user to change the selected file
	protected void initializeOpenButton(){
//		ImageIcon image = new ImageIcon(getClass().getResource("/CFG Analysis/src/images/OpenFolder.gif"));

		JButton btnOpenButton = new JButton("Change File...");
		add(btnOpenButton, "cell 0 2");
		
		addButtonActionListener(btnOpenButton);
	}
	
	// returns true if the file has been selected and loaded successfully
	public Boolean hasFileSelected(){
		return !_lblFileSelected.getText().contains(UIConstants.OPEN_COMMAND_FAILED);
	}
	
	// adds the ActionListener to the open button. Specified in subclasses
	protected abstract void addButtonActionListener(JButton btnOpenButton);
	
	// adds JTextField for user to enter file name (if necessary)
	protected abstract void addSaveFileName();

	// contains enums of the different File Types that a user can load
	public enum FileType{
		GraphML, Java;
	}
}
