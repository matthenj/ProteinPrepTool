package gui;

/*******************************************************************************
 *
 *	Filename   :	ProgressWindow.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Progress window :- shown during exection.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.ConsoleController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static java.lang.Thread.sleep;

public class ProgressWindow extends SwingWorker<Void, Void> {
    private JTextArea messageWindow;
    private JProgressBar progressBar;
    private JLabel windowTitle;
    private JPanel progressBarPanel;
    private JPanel progressWindow;
    private JButton closeButton;
    private JScrollPane textareaScrollPane;
    private JFrame frame;
    //  private Task task;
    private boolean shutdownInit;

    public ProgressWindow() {
        shutdownInit = false;

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanExit(true);
            }
        });

        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setValue((Integer) evt.getNewValue());
                    messageWindow.setText(ConsoleController.log.toString());

                }
            }
        });
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //if computing, check you want to quit.


            }
        });
    }

    @Override
    protected Void doInBackground() throws Exception {

        int progress = ConsoleController.hpp.getProgress();

        while (progress < 100) {

            try {
                sleep(1000);
            } catch (InterruptedException ex) {

            }
            System.out.println("loopin'");

            progress = ConsoleController.hpp.getProgress();
            this.setProgress(progress);

        }

        return null;

    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);


        }
    }

    public static void createandshowUI(ProgressWindow prg) {


        JFrame frame = new JFrame("Progress...");

        frame.setContentPane(prg.progressWindow);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                prg.cleanExit(true);
            }
        });


        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        //prg.execute();


    }

    public void updateProcess(int prg) {
        this.setProgress(prg);
    }


    public void cleanExit(boolean firedFromGUI) {

        //if computing, prompt for "Are you sure?"
        if(shutdownInit)
            return;



        if (ConsoleController.computeThreadRunning) {
            int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Confirmation",
                    JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.NO_OPTION)
                return;

        }

        shutdownInit = true;


        if(firedFromGUI)
            ConsoleController.postMessage(ConsoleController.MESSAGE.EXIT);

        Container frame = closeButton.getParent();
        if(frame != null) {
            do
                frame = frame.getParent();
            while (!(frame instanceof JFrame) && frame != null);
            if(frame != null) {
                ((JFrame) frame).dispose();
            }
        }

    }


}
