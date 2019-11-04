package gui;

/*******************************************************************************
 *
 *	Filename   :	ForcefieldMissMatchWindow.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Window is tied to the Topology/Forcefield verification class. This
 *	window is opened and presented to the user if there are atom types in
 *	the topology file which are not present in the selected forcefield.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.ConsoleController;
import controller.pdbTopTrajFFVerification.TopologyForcefieldCompatibilityTest;
import controller.TopFile.TopologyFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;

public class ForcefieldMissMatchWindow extends JPanel {


    private static final int MAX_Y_SIZE = 400;
    private static final String HELP_MESSAGE_DIALOG = ConsoleController.applicationName + " attempts to verify the protein's topology with the specified GROMACS forcefield." + System.lineSeparator() +
            "If the topology contains atom types that are not contained within the forcefield, this window is shown." +System.lineSeparator() + System.lineSeparator() +
            "Here, you must select atom types with equivalent C6/C12 or Sigma/Epsilon values. "+System.lineSeparator() + "Alternatively, you can skip this step, and add the relevant atom types to " +
            "the forcefield before running Haptimol FlexiDock.";


    private static final String SKIP_MESSAGE_DIALOG = "Are you sure you want to skip this step?" + System.lineSeparator() + "You will have to manually modify the forcefield before using it with Haptimol FlexiDock!";
    private static final String CONFIRM_MESSAGE_DIALOG = "Are you sure? Topology will be updated."+ System.lineSeparator() + "Incorrect pairings could produce incorrect results within Haptimol FlexiDock";
    private static JFrame frame;


    JLabel result;
    String currentPattern;
    ArrayList<JComboBox<String>> selectionBoxes;
    String[] unknownStrings;

    JButton okayButton;
    JButton skipButton;
    JButton helpButton;
    boolean skipWarned;
    TopologyFile topFileToUpdate;


    private void setSize(Component c, Dimension d){

        c.setMinimumSize(new Dimension(d));
        c.setPreferredSize(new Dimension(d));
        c.setMaximumSize(new Dimension(d));
    }


    public ForcefieldMissMatchWindow(TopologyForcefieldCompatibilityTest tfct) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        String[] availableAtoms = tfct.getAvailableAtomTypes();
        unknownStrings = tfct.getUnknownAtoms();
        topFileToUpdate = tfct.getTopologyFile();
        Arrays.sort(availableAtoms);

        okayButton = new JButton("OK");
        skipButton = new JButton("Skip");
        helpButton = new JButton("Help");

        Dimension buttonSize = new Dimension(75, 25);

        setSize(okayButton, buttonSize);
        setSize(skipButton, buttonSize);
        setSize(helpButton, buttonSize);


        JPanel mainPanel = new JPanel();
//        mainPanel.setLayout(new BoxLayout(mainPanel ,
//                BoxLayout.Y_AXIS));
        currentPattern = availableAtoms[0];
        selectionBoxes = new ArrayList<>();

        for(int i = 0; i < unknownStrings.length; i++){

            JComboBox tmp = new JComboBox(availableAtoms);
            tmp.setEditable(false);
            selectionBoxes.add(tmp);
        }


        //Set up the UI for selecting a pattern.
        String message1 = "<html>The specified topology contains atom types not found in the selected forcefield. Please select suitable replacements for each missing type.</html>";


        JLabel l = new JLabel(message1);

        l.setPreferredSize(new Dimension(340, 60));




        JComboBox patternList = new JComboBox(availableAtoms);
        patternList.setEditable(true);

        //Create the UI for displaying result.
        JLabel resultLabel = new JLabel("Current Date/Time",
                JLabel.LEADING); //== LEFT
        result = new JLabel(" ");
        result.setForeground(Color.black);
        result.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.black),
                BorderFactory.createEmptyBorder(5,5,5,5)
        ));

        //Lay out everything.

        JPanel unknownPatternPanel = new JPanel();
        unknownPatternPanel.setLayout(new BoxLayout(unknownPatternPanel,
                BoxLayout.PAGE_AXIS));
        //patternPanel.add(patternLabel1);
        //patternPanel.add(patternLabel2);
        patternList.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension boxArea = new Dimension(150, 30);

        for (int i = 0; i < selectionBoxes.size(); i++) {
            JComboBox jcb = selectionBoxes.get(i);
            JPanel lPanel = new JPanel();
            JPanel rPanel = new JPanel();
            JPanel combined = new JPanel();




            lPanel.setLayout(new BoxLayout(lPanel, BoxLayout.X_AXIS));
            rPanel.setLayout(new BoxLayout(rPanel, BoxLayout.X_AXIS));
            combined.setLayout(new BoxLayout(combined, BoxLayout.X_AXIS));


            lPanel.setMinimumSize(boxArea);
            lPanel.setMaximumSize(boxArea);
            lPanel.setPreferredSize(boxArea);

            combined.setMinimumSize(new Dimension(310, 40));
            combined.setPreferredSize(new Dimension(310, 40));
            combined.setMaximumSize(new Dimension(310, 40));

            JLabel tp = new JLabel(unknownStrings[i]);
            lPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            lPanel.add(tp);
            rPanel.add(jcb);

            combined.add(lPanel);
            combined.add(rPanel);
            combined.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

            unknownPatternPanel.add(combined);
        }

        unknownPatternPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(5,5,5,5)));

        JScrollPane jsp = new JScrollPane(unknownPatternPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        int y = (40 * selectionBoxes.size())+10;

        y = (y > MAX_Y_SIZE) ? MAX_Y_SIZE : y;

        jsp.setPreferredSize(new Dimension(340, y));
        //jsp.setMinimumSize(new Dimension(320, 300));


        //jsp.add(unknownPatternPanel);
        mainPanel.add(l);
        mainPanel.add(jsp);
        mainPanel.setPreferredSize(new Dimension(340,y+100));
        add(mainPanel);

        JPanel controls = new JPanel();
        controls.setMinimumSize(new Dimension(310, 40));
        controls.setPreferredSize(new Dimension(310, 40));
        controls.setMaximumSize(new Dimension(310, 40));

        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JOptionPane.showMessageDialog(frame, HELP_MESSAGE_DIALOG);
            }
        });

        skipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int result = JOptionPane.YES_OPTION;
                if(!skipWarned){
                        result = JOptionPane.showConfirmDialog(null, SKIP_MESSAGE_DIALOG, "Are you sure?", JOptionPane.YES_NO_OPTION);
                        skipWarned = true;
                }

                if(result == JOptionPane.YES_OPTION){
                    tfct.skip();
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                }


            }
        });

        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int result = JOptionPane.showConfirmDialog(null, CONFIRM_MESSAGE_DIALOG, "Are you sure?", JOptionPane.YES_NO_OPTION);
                if(result == JOptionPane.YES_OPTION){
                    processTopologyUpdate();
                    tfct.recoveryComplete();
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));

                }
            }
        });

        controls.add(helpButton);
        controls.add(skipButton);
        controls.add(okayButton);



        add(controls);

        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        skipWarned = false;

    } //constructor


    public void processTopologyUpdate(){



        for (int i = 0; i < this.unknownStrings.length; i++) {
            String oldStr = this.unknownStrings[i];
            String newStr = (String)this.selectionBoxes.get(i).getSelectedItem();
            this.topFileToUpdate.updateAtomRecord(oldStr, newStr);
        }

    }



    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public static void createAndShowGUI(TopologyForcefieldCompatibilityTest tfct) {
        //Create and set up the window.
        frame = new JFrame("Forcefield Topology Missmatch");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] availableOptions = {"C1", "N", "O"};
        String[] unknownStrings = {"V", "C2", "P", "S","V"};

        //Create and set up the content pane.
        JComponent newContentPane = new ForcefieldMissMatchWindow(tfct);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

//    public static void createWindow() {
//        //Schedule a job for the event-dispatching thread:
//        //creating and showing this application's GUI.
//        javax.swing.SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                createAndShowGUI();
//            }
//        });
//    }


}
