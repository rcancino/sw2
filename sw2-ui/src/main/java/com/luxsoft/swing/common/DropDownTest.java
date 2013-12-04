/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.luxsoft.swing.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author ruben
 */
public class DropDownTest extends JPanel {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final JButton status = new JButton("Color");
        final JPanel colorPanel = new ColorSelectionPanel();
        final DropDownComponent dropdown = new DropDownComponent(status, colorPanel);
        colorPanel.addPropertyChangeListener("selectedColor", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {                
                dropdown.hidePopup();
                status.setBackground((Color) evt.getNewValue());
            }
        });

        JFrame frame = new JFrame("Drop Down Test");
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add("North", dropdown);
        frame.getContentPane().add("Center", new JLabel("Drop Down Test"));
        frame.pack();
        frame.setSize(300, 300);
        frame.setVisible(true);
    }
}
