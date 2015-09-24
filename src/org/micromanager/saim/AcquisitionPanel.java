 ///////////////////////////////////////////////////////////////////////////////
//FILE:          AcquisitionPanel
//PROJECT:       SAIM-calibration
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nico Stuurman, Kate Carbone
//
// COPYRIGHT:    University of California, San Francisco 2015
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
package org.micromanager.saim;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;
import java.lang.Math;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mmcorej.CMMCore;
import mmcorej.TaggedImage; 
import net.miginfocom.swing.MigLayout;
import org.micromanager.api.ScriptInterface;
import org.micromanager.saim.gui.GuiUtils;
import org.micromanager.utils.FileDialogs;
import org.micromanager.MMStudio;
import org.micromanager.utils.MMScriptException;

/**
 *
 * @author nico
 */
public class AcquisitionPanel extends JPanel implements ICalibrationObserver{

    ScriptInterface gui_;
    CMMCore core_;
    Preferences prefs_;

    private final String ANGLESTEPSIZE = "acq.anglestepsize";
    private final String STARTANGLE = "acq.startangle";
    private final String ENDANGLE = "acq.endangle";
    private final String DOUBLEZERO = "acq.doulbezero";
    private final String SAVEIMAGES = "acq.saveimages";
    private final String DIRROOT = "acq.dirroot";
    private final String NAMEPREFIX = "acq.nameprefix";
    private final String COEFF3 = "acq.coeff3";
    private final String COEFF2 = "acq.coeff2";
    private final String COEFF1 = "acq.coeff1";
    private final String COEFF0 = "acq.coeff0";

    private final JSpinner angleStepSizeSpinner_;
    private final JTextField startAngleField_;
    private final JTextField endAngleField_;
    private final JCheckBox doubleZeroCheckBox_;
    private final JPanel calPanel_;
    private final JCheckBox saveImagesCheckBox_;

    private final JFileChooser dirRootChooser_;
    private final JTextField dirRootField_;
    private final JButton dirRootButton_;
    private final JTextField namePrefixField_;
    private final JToggleButton runButton_;
    private JTextField coeff3Field_;
    private JTextField coeff2Field_;
    private JTextField coeff1Field_;
    private JTextField coeff0Field_;

    public AcquisitionPanel(ScriptInterface gui, Preferences prefs) {
        super(new MigLayout(
                "",
                ""));
        gui_ = gui;
        core_ = gui_.getMMCore();
        prefs_ = prefs;
        this.setName("Acquisition");

        // Setup Panel
        JPanel setupPanel = new JPanel(new MigLayout(
                "", ""));
        setupPanel.setBorder(GuiUtils.makeTitledBorder("Setup"));
        final Dimension componentSize = new Dimension(150, 30);

        // set angle step size
        setupPanel.add(new JLabel("Angle Step Size (degrees):"));
        angleStepSizeSpinner_ = new JSpinner(new SpinnerNumberModel(
                prefs_.getInt(ANGLESTEPSIZE, 100), 0, 400, 1));
        angleStepSizeSpinner_.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                prefs_.putInt(ANGLESTEPSIZE, (Integer) angleStepSizeSpinner_.getValue());
            }
        });
        setupPanel.add(angleStepSizeSpinner_, "span, growx, wrap");

        // set start angle
        setupPanel.add(new JLabel("Start Angle:"));
        startAngleField_ = new JTextField(
                ((Integer) (prefs_.getInt(STARTANGLE, 0))).toString());
        setTextAttributes(startAngleField_, componentSize);
        startAngleField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.putDouble(STARTANGLE, Integer.parseInt(
                        startAngleField_.getText()));
            }
        });
        setupPanel.add(startAngleField_, "span, growx, wrap");

        // set end angle
        setupPanel.add(new JLabel("End Angle:"));
        endAngleField_ = new JTextField(
                ((Integer) (prefs_.getInt(ENDANGLE, 0))).toString());
        setTextAttributes(endAngleField_, componentSize);
        endAngleField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.putDouble(ENDANGLE, Integer.parseInt(
                        endAngleField_.getText()));
            }
        });
        setupPanel.add(endAngleField_, "span, growx, wrap");

        // set double zero position
        doubleZeroCheckBox_ = new JCheckBox("Double Zero Position");
        doubleZeroCheckBox_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (doubleZeroCheckBox_.isSelected()) {
                    prefs_.putBoolean(DOUBLEZERO, true);
                } else {
                    prefs_.putBoolean(DOUBLEZERO, false);
                }
            }
        });
        setupPanel.add(doubleZeroCheckBox_, "span 2, growx, wrap");

        // Calibration Values
        calPanel_ = new JPanel(new MigLayout(
                "", ""));
        calPanel_.setBorder(GuiUtils.makeTitledBorder("Calibration Values"));
        final Dimension calBoxSize = new Dimension(130, 30);

        //Set calibration values
        //x3 coefficient
        calPanel_.add(new JLabel("x^3: "));
        coeff3Field_ = new JTextField(
                prefs_.get(COEFF3, ""));
        setTextAttributes(coeff3Field_, componentSize);
        coeff3Field_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(COEFF3,
                        coeff3Field_.getText());
            }
        });
        calPanel_.add(coeff3Field_, "span, center, wrap");

        //x2 coefficient
        calPanel_.add(new JLabel("x^2: "));
        coeff2Field_ = new JTextField(
                prefs_.get(COEFF2, ""));
        setTextAttributes(coeff2Field_, componentSize);
        coeff2Field_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(COEFF2,
                        coeff2Field_.getText());
            }
        });
        calPanel_.add(coeff2Field_, "span, center, wrap");

        //x coefficient
        calPanel_.add(new JLabel("x: "));
        coeff1Field_ = new JTextField(
                prefs_.get(COEFF1, ""));
        setTextAttributes(coeff1Field_, componentSize);
        coeff1Field_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(COEFF3,
                        coeff1Field_.getText());
            }
        });
        calPanel_.add(coeff1Field_, "span, center, wrap");

        //x0 constant
        calPanel_.add(new JLabel("x^0: "));
        coeff0Field_ = new JTextField(
                prefs_.get(COEFF0, ""));
        setTextAttributes(coeff0Field_, componentSize);
        coeff0Field_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(COEFF0,
                        coeff0Field_.getText());
            }
        });
        calPanel_.add(coeff0Field_, "span, center, wrap");
        
        // Acquire Panel
        JPanel acquirePanel = new JPanel(new MigLayout(
                "", ""));
        acquirePanel.setBorder(GuiUtils.makeTitledBorder("Acquire"));
        final Dimension acqBoxSize = new Dimension(130, 30);

        // set directory root file chooser
        dirRootChooser_ = new JFileChooser(
                prefs_.get(DIRROOT, ""));
        dirRootChooser_.setCurrentDirectory(new java.io.File("."));
        dirRootChooser_.setDialogTitle("Directory Root");
        dirRootChooser_.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirRootChooser_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.putDouble(DIRROOT, Integer.parseInt(dirRootChooser_.getName()));
            }
        });

        // set directory root text field
        acquirePanel.add(new JLabel("Directory Root:"));
        dirRootField_ = new JTextField(
                prefs_.get(DIRROOT, ""));
        setTextAttributes(dirRootField_, componentSize);
        dirRootField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(DIRROOT,
                        dirRootField_.getText());
            }
        });
        acquirePanel.add(dirRootField_);

        //set directory chooser button
        dirRootButton_ = new JButton("...");
        dirRootButton_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setRootDirectory();
            }
        });
        acquirePanel.add(dirRootButton_, "wrap");

        // set name prefix
        acquirePanel.add(new JLabel("Name Prefix:"));
        namePrefixField_ = new JTextField(
                prefs_.get(NAMEPREFIX, ""));
        setTextAttributes(namePrefixField_, componentSize);
        namePrefixField_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs_.put(NAMEPREFIX,
                        namePrefixField_.getText());
            }
        });
        acquirePanel.add(namePrefixField_, "span, growx, wrap");

        // set save images
        saveImagesCheckBox_ = new JCheckBox("Save Images");
        saveImagesCheckBox_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (saveImagesCheckBox_.isSelected()) {
                    prefs_.putBoolean(SAVEIMAGES, true);
                } else {
                    prefs_.putBoolean(SAVEIMAGES, false);
                }
            }
        });
        acquirePanel.add(saveImagesCheckBox_, "span 2, growx, wrap");
        
        // set run button
        runButton_ = new JToggleButton("Run Acquisition");
        runButton_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO add your handling code here:
                if (runButton_.isSelected()) {
                    runButton_.setText("Abort Acquisition");
                    RunAcquisition();
                } else {
                    runButton_.setText("Run Acquisition");
                }
            }
        });
        acquirePanel.add(runButton_, "span 3, center, wrap");

        // Combine them all
        add(setupPanel, "span, growx, wrap");
        add(calPanel_, "span, growx, wrap");
        add(acquirePanel, "span, growx, wrap");
    }

    /**
     * Utility function to set attributes for JTextFields in the dialog
     *
     * @param jtf JTextField whose attributes will be set
     * @param size Desired minimum size
     */
    private void setTextAttributes(JTextField jtf, Dimension size) {
        jtf.setHorizontalAlignment(JTextField.RIGHT);
        jtf.setMinimumSize(size);
    }
   

    public void calibrationChanged(double x3, double x2, double x1, double x0){
        String coeff0 = new DecimalFormat("0.#########").format(x0);
        prefs_.put(COEFF0, coeff0);
        coeff0Field_.setText(coeff0);
        
        String coeff1 = new DecimalFormat("0.#########").format(x1);
        prefs_.put(COEFF1, coeff1);
        coeff1Field_.setText(coeff1);

        String coeff2 = new DecimalFormat("0.#########").format(x2);
        prefs_.put(COEFF2, coeff2);
        coeff2Field_.setText(coeff2);

        String coeff3 = new DecimalFormat("0.#########").format(x3);
        prefs_.put(COEFF3, coeff3);
        coeff3Field_.setText(coeff3);
    }
    
    protected void setRootDirectory() {
        File result = FileDialogs.openDir(null,
                "Please choose a directory root for image data",
                MMStudio.MM_DATA_SET);
        if (result != null) {
            dirRootField_.setText(result.getAbsolutePath());
            //acqEng_.setRootName(result.getAbsolutePath());
        }
    }

    /**
     * User is supposed to set up the acquisition in the micromanager panel.
     * This function will acquire images at angle positions defined by
     * calibration.
     *
     */
    private void RunAcquisition() {
        try {
            // Set these variables to the correct values and leave
            final String deviceName = "TITIRF";
            final String propName = "Position";
            int startAngle = Integer.parseInt(startAngleField_.getText());
            int angleStepSize = prefs_.getInt(ANGLESTEPSIZE, 0);
            
            // Usually no need to edit below this line
            int nrAngles = Math.abs(startAngle) * 2 / angleStepSize;
            
            gui_.closeAllAcquisitions();
            final String acq = gui_.getUniqueAcquisitionName(namePrefixField_.getText());
            
            gui_.openAcquisition(acq,
                    "", 1, 1, nrAngles + 2, 1,
                    true, // Show
                    false); // Save <--change this to save files in root directory
            
            // First take images from start to 90 degrees
            int pos = startAngle;
            int nrAngles1 = nrAngles / 2;
            for (int a = 0;
                    a <= nrAngles1;
                    a++) {
                double val = tirfPosFromAngle(pos);
                gui_.message("Image: " + Integer.toString(a) + ", angle: " + Integer.toString(pos) + ", val: " + Double.toString(val));
                core_.setProperty(deviceName, propName, val);
                core_.waitForDevice(deviceName);
                //gui.sleep(250);
                core_.snapImage();
                TaggedImage taggedImg = core_.getTaggedImage();
                taggedImg.tags.put("Angle", pos);
                gui_.addImageToAcquisition(acq, 0, 0, a, 0, taggedImg);
                pos += angleStepSize;
            }
            
            // then take images from 0 degrees to (0 - startposition) degrees
            int pos1 = 0;
            int nrAngles2 = nrAngles / 2 + 1;
            for (int b = 0;
                    b <= nrAngles1 + nrAngles2;
                    b++) {
                double val = tirfPosFromAngle(pos1);
                gui_.message("Image: " + Integer.toString(b) + ", angle: " + Integer.toString(pos1) + ", val: " + Double.toString(val));
                core_.setProperty(deviceName, propName, val);
                core_.waitForDevice(deviceName);
                //gui.sleep(250);
                core_.snapImage();
                TaggedImage taggedImg = core_.getTaggedImage();
                taggedImg.tags.put("Angle", pos1);
                gui_.addImageToAcquisition(acq, 0, 0, b, 0, taggedImg);
                pos1 += angleStepSize;
            }
            
            
            gui_.closeAcquisition(acq);
        } catch (Exception ex) {
            ex.printStackTrace();
            ij.IJ.log(ex.getMessage());
            ij.IJ.error("Something went wrong.  Aborting!");
        } finally {
            runButton_.setSelected(false);
            runButton_.setText("Run Acquisition");
        }

    }

    private int tirfPosFromAngle(double angle) {
        // TirfPosition = slope * angle plus Offset
        // Output motor position must be an integer to be interpreted by TITIRF

        double tempPos = (Double.parseDouble(coeff3Field_.getText()) * Math.pow(angle, 3)
                + Double.parseDouble(coeff2Field_.getText()) * Math.pow(angle, 2)
                + Double.parseDouble(coeff1Field_.getText()) * angle
                + Double.parseDouble(coeff0Field_.getText()));
        int pos = Math.round((float) tempPos);
        return pos;
    }

}