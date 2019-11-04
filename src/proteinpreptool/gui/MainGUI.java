package gui;

/*******************************************************************************
 *
 *	Filename   :	MainGUI.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Main GUI window. Here, the user can setup and start a run. Bit of a
 *	bodge, needs some optimisation
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/

import controller.ConsoleController;
import controller.ProteinPrepToolException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

import static controller.ConsoleController.MESSAGE.EXIT;
import static controller.ConsoleController.MESSAGE.START;
import static controller.ConsoleController.hpp;

public class MainGUI implements Runnable {



    private enum guiSections {
        REFERENCE_CORDINATES, REFERENCE_TOPOLOGY, TRAJECTORY, OUTPUT_STRUCTURE, OUTPUT_TOPOLOGY, OUTPUT_EIGVAL, OUTPUT_EIGVEC
    }

    private static final String WINDOW_NAME = "ProteinPrepTool";
    private static final String[] HELP_MESSAGES = new String[]{
            "Select the reference structure PDB file. This should be the starting structure from your MD simulation. ",
            "Select the format of your topology file.",
            "Select the reference topology file. This should be the same file as was used for the MD simulation.",
            "Select the file format of the trajectory.",
            "Select the trajectory files. Press shift to choose multiple files. ",
            "Specify how many eigenvalues to be calculated. The higher this value, the longer the computation will" +
                    " take, but the better the approximation of flexibility will be. ",
            "Specify a location to save the “closest to average” structure. This is the structure with the smallest "
                    + "RMSD to the average, taken from the MD trajectory. ",
            "Specify a location to save the Haptimol FlexiDock compatible topology file.",
            "Specify a location to save the calculated eigenvalues.",
            "Specify a location to save the calculated eigenvectors.",
            "Verify generated topology file is compatible with the GROMACS forcefield. ",
            "Select the directory containing GROAMCS’ force fields. " +
                    "This allows ProteinPrepTool to validate the created topology with the forcefield for compatibility. ",
            "Select the desired forcefield. It should be the same one that was used during the MD trajectory. ",
            "Additional Settings"};

    //field IDs.
    private static final int FIELD_ID_REF_STUCT_PDB = 0;
    private static final int FIELD_ID_TOP_FORMAT = 1;
    private static final int FIELD_ID_REF_STRUCT_TOP = 2;
    private static final int FIELD_ID_TRAJ_FORMAT = 3;
    private static final int FIELD_ID_TRAJ_FILES = 4;
    private static final int FIELD_ID_NUM_EIGS = 5;
    private static final int FIELD_ID_NVS_STRUCT = 6;
    private static final int FIELD_ID_COMP_TOP = 7;
    private static final int FIELD_ID_EIGVALS = 8;
    private static final int FIELD_ID_EIGVECS = 9;
    private static final int FIELD_ID_VERIFY_TOP = 10;
    private static final int FIELD_ID_GROMACS_ROOT = 11;
    private static final int FIELD_ID_SELECTED_FF = 12;

    //format: Label, Extension 1, Extension 2.... Extension N
    public static final String[] validReferenceCoordinateExtensions = {"Protein Databank Files", "pdb"};
    public static final String[] validReferenceTopoloyExtensions = {"Topology Files", "top", "prmtop", "psf"};
    public static final String[] validReferenceTrajectoryExtensions = {"Trajectory Files", "nc"};

    public static final String[] validOutputCoordinateFileStructureExtensions = {"Protein Databank Files", "pdb"};
    public static final String[] validOutputTopologyFileStructureExtensions = {"Topology Files", "top"};
    public static final String[] validOutputEigenvaluesExtensions = {"Comma Separated Values", "csv"};
    public static final String[] validOutputEigenvectorsExtensions = {"Comma Separated Values", "csv"};

    boolean readyToRun;

    JFileChooser jfc;

    private JFrame frame;
    private JPanel mainGuiPanel;
    private JTextField refStructPdbTextField;
    private JTextField trajectoryPathTextField;
    private JTextField refStructTopTextField;
    private JTextField nearestViableStructureSavePathTextField;
    private JTextField eigenvectorsSavePathTextField;
    private JTextField eigenvaluesSavePathTextField;
    private JTextField compatibleTopologySavePathTextField;
    private JTextField numEigenValsTextField;
    private JButton rsPDBbrowseButton;
    private JButton startButton;
    private JButton quitButton;
    private JButton rsTOPbrowseButton;
    private JButton trajectoryBrowseButton;
    private JButton nvsSaveBrowseButton;
    private JButton compTopBrowseButton;
    private JButton eigenValuesBrowsButton;
    private JButton eigenVectorsBrowseButton;
    private JComboBox topFormatList;
    private JComboBox trajFormatList;
  
    private JButton gffdBrowseButton;
    private JTextField forceFieldDirBar;
    private JComboBox forceFieldComboBox;
    private JCheckBox verifyFFCompataibilityCheckBox;
    private JLabel ffLabel;
    private JLabel gffrdLabel;
    private JTextPane helpMessageBox;
    private JLabel topFormatLabel;
    private JLabel trajFormatLabel;
    private JLabel resStructPDBLabel;
    private JLabel resStructTopLabel;
    private JLabel trajFileLabel;
    private JLabel numEigsLabel;
    private JLabel nvsPdbLabel;
    private JLabel comptopLabel;
    private JLabel eigValsLabel;
    private JLabel eigVecLabel;
    private JButton configButton;

    private String biomoleculeName;
    private FileNameExtensionFilter[] fileExtensions;
    private ArrayList<File> ffFiles;
    private boolean gromacsWarned;

    public MainGUI() throws ProteinPrepToolException {

        gromacsWarned = false;

        jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileExtensions = new FileNameExtensionFilter[guiSections.values().length];
        biomoleculeName = "protein";
//        if(ConsoleController.TEST_MODE)
//            jfc.setSelectedFile(new File("C:\\njm\\protein_movement_data\\1GGG\\trajectory"));
        readyToRun = false;
        for (guiSections gs : guiSections.values()) {
            fileExtensions[gs.ordinal()] = createFileNameExtension(gs);
        }
        rsPDBbrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLoadPath(refStructPdbTextField, false, false, fileExtensions[guiSections.REFERENCE_CORDINATES.ordinal()], false, "");
                if (refStructPdbTextField.getText().length() > 3) {
                    String s = refStructPdbTextField.getText();
                    String[] parts = s.split("\\.");

                    if (parts[0].length() > 0) {
                        biomoleculeName = parts[0];
                    }

                }

            }
        });
        rsTOPbrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int value = topFormatList.getSelectedIndex();
                FileNameExtensionFilter tmp = new FileNameExtensionFilter(validReferenceTopoloyExtensions[0], validReferenceTopoloyExtensions[value + 1]);

                handleLoadPath(refStructTopTextField, false, false, tmp, false, "");
            }
        });
        trajectoryBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int value = trajFormatList.getSelectedIndex();
                FileNameExtensionFilter tmp = new FileNameExtensionFilter(validReferenceTrajectoryExtensions[0], validReferenceTrajectoryExtensions[value + 1]);

                handleLoadPath(trajectoryPathTextField, true, false, fileExtensions[guiSections.TRAJECTORY.ordinal()], false, "");
            }
        });
        nvsSaveBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLoadPath(nearestViableStructureSavePathTextField, false, false, fileExtensions[guiSections.OUTPUT_STRUCTURE.ordinal()], true, biomoleculeName + "_nvs.pdb");
            }
        });
        compTopBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLoadPath(compatibleTopologySavePathTextField, false, false, fileExtensions[guiSections.OUTPUT_TOPOLOGY.ordinal()], true, biomoleculeName + "_nvs.top");
            }
        });
        eigenValuesBrowsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLoadPath(eigenvaluesSavePathTextField, false, false, fileExtensions[guiSections.OUTPUT_EIGVAL.ordinal()], true, biomoleculeName + "_eigenvalues.csv");
            }
        });
        eigenVectorsBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLoadPath(eigenvectorsSavePathTextField, false, false, fileExtensions[guiSections.OUTPUT_EIGVEC.ordinal()], true, biomoleculeName + "_eigenvectors.csv");
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConsoleController.postMessage(EXIT);
//                Container frame = quitButton.getParent();
//                do
//                    frame = frame.getParent();
//                while (!(frame instanceof JFrame));
//                ((JFrame) frame).dispose();
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Container frame = startButton.getParent();
                //first, check all settings are A-OK.

                boolean ffProvided = verifyFFCompataibilityCheckBox.isSelected();

                try {
                    String ffPath = null;
                    String ffName = "";
                    if (ffProvided) {
                        int ffIndex = forceFieldComboBox.getSelectedIndex();
                        ffPath = ffFiles.get(ffIndex).getAbsolutePath();
                        ffName = (String) forceFieldComboBox.getSelectedItem();
                    }

                    hpp.initialise(refStructPdbTextField.getText(), refStructTopTextField.getText(), trajectoryPathTextField.getText(), nearestViableStructureSavePathTextField.getText(), compatibleTopologySavePathTextField.getText(), eigenvaluesSavePathTextField.getText(), eigenvectorsSavePathTextField.getText(), numEigenValsTextField.getText(), (ConsoleController.TOP_FORMAT) topFormatList.getSelectedItem(), (ConsoleController.TRAJ_FORMAT) trajFormatList.getSelectedItem(), ffName, ffPath);

                } catch (ProteinPrepToolException pex) {
                    JOptionPane.showMessageDialog(frame, pex.getFriendlyMessage());
                    return;
                }

                ConsoleController.postMessage(START);
                do frame = frame.getParent(); while (!(frame instanceof JFrame));
                ((JFrame) frame).setVisible(false);

                //if so, hide the JFrame, set OK, set all relevent strings to correct paths, and return to ConsoleControl
            }
        });

        for (ConsoleController.TOP_FORMAT tf : ConsoleController.TOP_FORMAT.values()) {
            topFormatList.addItem(tf);
        }

        for (ConsoleController.TRAJ_FORMAT tf : ConsoleController.TRAJ_FORMAT.values()) {
            trajFormatList.addItem(tf);
        }
        mainGuiPanel.addComponentListener(new ComponentAdapter() {
        });
        forceFieldDirBar.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                updateComboBox(forceFieldDirBar.getText());
            }
        });
        gffdBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = jfc.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    forceFieldDirBar.setText(jfc.getSelectedFile().getAbsolutePath());
                    updateComboBox(forceFieldDirBar.getText());
                }
            }
        });
        verifyFFCompataibilityCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (verifyFFCompataibilityCheckBox.isSelected()) {

                    forceFieldComboBox.setEnabled(true);
                    forceFieldDirBar.setEnabled(true);
                    gffdBrowseButton.setEnabled(true);
                    gffrdLabel.setEnabled(true);
                    ffLabel.setEnabled(true);

                } else {
                    if (!gromacsWarned) {
                        JOptionPane.showMessageDialog(null, "If a path to the GROMACS " + "forcefields is not provided, the protein topologies cannot be validated for compatibility");
                        gromacsWarned = true;
                    }

                    forceFieldComboBox.setEnabled(false);
                    forceFieldDirBar.setEnabled(false);
                    gffdBrowseButton.setEnabled(false);
                    gffrdLabel.setEnabled(false);
                    ffLabel.setEnabled(false);

                }

            }
        });
        refStructPdbTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);
                displayHelpMessage(0);
            }
        });

        forceFieldComboBox.addItem("Select GROMACS forcefield directory");

        addMouseAdapter(refStructPdbTextField, FIELD_ID_REF_STUCT_PDB);
        addMouseAdapter(rsPDBbrowseButton, FIELD_ID_REF_STUCT_PDB);
        addMouseAdapter(resStructPDBLabel, FIELD_ID_REF_STUCT_PDB);

        addMouseAdapter(topFormatList, FIELD_ID_TOP_FORMAT);
        addMouseAdapter(topFormatLabel, FIELD_ID_TOP_FORMAT);

        addMouseAdapter(refStructTopTextField, FIELD_ID_REF_STRUCT_TOP);
        addMouseAdapter(rsTOPbrowseButton, FIELD_ID_REF_STRUCT_TOP);
        addMouseAdapter(resStructTopLabel, FIELD_ID_REF_STRUCT_TOP);

        addMouseAdapter(trajFormatList, FIELD_ID_TRAJ_FORMAT);
        addMouseAdapter(trajFormatLabel, FIELD_ID_TRAJ_FORMAT);

        addMouseAdapter(trajectoryPathTextField, FIELD_ID_TRAJ_FILES);
        addMouseAdapter(trajectoryBrowseButton, FIELD_ID_TRAJ_FILES);
        addMouseAdapter(trajFileLabel, FIELD_ID_TRAJ_FILES);

        addMouseAdapter(numEigenValsTextField, FIELD_ID_NUM_EIGS);
        addMouseAdapter(numEigsLabel, FIELD_ID_NUM_EIGS);

        addMouseAdapter(nearestViableStructureSavePathTextField, FIELD_ID_NVS_STRUCT);
        addMouseAdapter(nvsSaveBrowseButton, FIELD_ID_NVS_STRUCT);
        addMouseAdapter(nvsPdbLabel, FIELD_ID_NVS_STRUCT);

        addMouseAdapter(compatibleTopologySavePathTextField, FIELD_ID_COMP_TOP);
        addMouseAdapter(compTopBrowseButton, FIELD_ID_COMP_TOP);
        addMouseAdapter(comptopLabel, FIELD_ID_COMP_TOP);

        addMouseAdapter(eigenvaluesSavePathTextField, FIELD_ID_EIGVALS);
        addMouseAdapter(eigenValuesBrowsButton, FIELD_ID_EIGVALS);
        addMouseAdapter(eigValsLabel, FIELD_ID_EIGVALS);

        addMouseAdapter(eigenvectorsSavePathTextField, FIELD_ID_EIGVECS);
        addMouseAdapter(eigenVectorsBrowseButton, FIELD_ID_EIGVECS);
        addMouseAdapter(eigVecLabel, FIELD_ID_EIGVECS);

        addMouseAdapter(verifyFFCompataibilityCheckBox, FIELD_ID_VERIFY_TOP);

        addMouseAdapter(gffdBrowseButton, FIELD_ID_GROMACS_ROOT);
        addMouseAdapter(forceFieldDirBar, FIELD_ID_GROMACS_ROOT);
        addMouseAdapter(gffrdLabel, FIELD_ID_GROMACS_ROOT);

        addMouseAdapter(forceFieldComboBox, FIELD_ID_SELECTED_FF);
        addMouseAdapter(ffLabel, FIELD_ID_SELECTED_FF);

        helpMessageBox.setBackground(mainGuiPanel.getBackground());
    }

    private void updateComboBox(String path) {

        ArrayList<String> avvFF = scanDirectoryForItpFiles(path);

        if (avvFF.size() > 0) {
            resetComboBox();
            for (String str : avvFF) {
                forceFieldComboBox.addItem(str);
            }
        } else {
            resetComboBox();
            forceFieldComboBox.addItem("Select GROMACS forcefield directory.");
        }
    }

    private void resetComboBox() {
        forceFieldComboBox.removeAllItems();
    }

    private ArrayList<String> scanDirectoryForItpFiles(String path) {

        ArrayList<String> avvFF = new ArrayList<>();
        ffFiles = new ArrayList<>();

        File rootFile = new File(path);

        if (!rootFile.exists()) {
            return avvFF;
        }

        File[] avvFiles = rootFile.listFiles();

        for (File f : avvFiles) {
            if (f.isDirectory() && f.getName().endsWith(".ff")) {
                boolean nonBondedFound = false;
                File[] subfiles = f.listFiles();
                if (subfiles != null) {
                    for (int i = 0; i < subfiles.length && !nonBondedFound; i++) {
                        File sf = subfiles[i];
                        if (sf.getName().equalsIgnoreCase("ffnonbonded.itp")) {
                            avvFF.add(f.getName());
                            ffFiles.add(sf);
                            nonBondedFound = true;
                        }
                    }
                }
            }
        }

        if (avvFF.size() == 0 && rootFile.getName().endsWith(".ff")) {
            boolean nonBondedFound = false;
            for (int i = 0; i < avvFiles.length && !nonBondedFound; i++) {
                File sf = avvFiles[i];
                if (sf.getName().equalsIgnoreCase("ffnonbonded.itp")) {
                    avvFF.add(rootFile.getName());
                    nonBondedFound = true;
                    ffFiles.add(sf);
                }
            }
        }

        return avvFF;

    }

    private void handleLoadPath(JTextField field, boolean selectMultiple, boolean allowDirectory, FileNameExtensionFilter flt, boolean isSave, String saveName) {

        if (selectMultiple) {
            jfc.setMultiSelectionEnabled(true);

        } else {
            jfc.setMultiSelectionEnabled(false);
        }

        jfc.resetChoosableFileFilters();
        jfc.setFileFilter(flt);

        if (allowDirectory) {
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        } else {
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }

        int result = JFileChooser.CANCEL_OPTION;
        if (isSave) {
            jfc.setSelectedFile(new File(saveName));
            result = jfc.showSaveDialog(null);
        } else {
            jfc.setSelectedFile(new File(""));
            result = jfc.showOpenDialog(null);
        }

        if (result == JFileChooser.APPROVE_OPTION) {
            if (selectMultiple) {
                File[] selectedFiles = jfc.getSelectedFiles();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < selectedFiles.length; i++) {
                    File f = selectedFiles[i];
                    sb.append(f.getAbsolutePath());
                    if (i + 1 < selectedFiles.length) {
                        sb.append(", ");
                    }
                }
                field.setText(sb.toString());
            } else {
                File selectedFile = jfc.getSelectedFile();
                field.setText(selectedFile.getAbsolutePath());
            }

        }

    }

    @Override
    public void run() {

        frame = new JFrame(WINDOW_NAME);
        //    try {
        frame.setContentPane(mainGuiPanel);
//        } catch (ProteinPrepToolException e) {
//            e.printStackTrace();
//        }

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                cleanExit();
            }
        });

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void show() {
        frame.setVisible(true);
    }

    private FileNameExtensionFilter createFileNameExtension(guiSections mode) throws ProteinPrepToolException {

        String[] arrayToLoad;

        switch (mode) {
            case REFERENCE_CORDINATES:
                arrayToLoad = validReferenceCoordinateExtensions;
                break;
            case REFERENCE_TOPOLOGY:
                arrayToLoad = validReferenceTopoloyExtensions;
                break;
            case TRAJECTORY:
                arrayToLoad = validReferenceTrajectoryExtensions;
                break;
            case OUTPUT_STRUCTURE:
                arrayToLoad = validOutputCoordinateFileStructureExtensions;
                break;
            case OUTPUT_TOPOLOGY:
                arrayToLoad = validOutputTopologyFileStructureExtensions;
                break;
            case OUTPUT_EIGVAL:
                arrayToLoad = validOutputEigenvaluesExtensions;
                break;
            case OUTPUT_EIGVEC:
                arrayToLoad = validOutputEigenvectorsExtensions;
                break;
            default:
                throw new ProteinPrepToolException("Invalid state");

        }

        if (arrayToLoad.length < 2) {
            throw new ProteinPrepToolException("Extension array is invalid");
        }

        String[] tarray = new String[arrayToLoad.length - 1];
        for (int i = 0; i < tarray.length; i++) {
            tarray[i] = arrayToLoad[i + 1];
        }

        FileNameExtensionFilter flc = new FileNameExtensionFilter(arrayToLoad[0], tarray);
        return flc;

    }

    public void endGUI() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    private void cleanExit() {

        ConsoleController.postMessage(EXIT);
        if (frame != null && frame.isVisible()) {

            frame.dispose();

        }

    }

    private void displayHelpMessage(int messageID) {

        Dimension d = helpMessageBox.getSize();
        helpMessageBox.setPreferredSize(d);

        helpMessageBox.setText(HELP_MESSAGES[messageID]);

    }

    private void addMouseAdapter(JComponent e, int index){

        e.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);
                displayHelpMessage(index);
            }
        });

    }

    public boolean isReadyToRun() {
        return readyToRun;
    }
}
