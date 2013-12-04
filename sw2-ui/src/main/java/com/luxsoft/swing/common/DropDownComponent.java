/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.luxsoft.swing.common;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.metal.MetalComboBoxIcon;

/**
 *
 * @author ruben
 */
public class DropDownComponent extends JComponent
        implements ActionListener, AncestorListener {

    protected JComponent drop_down_comp;
    protected JComponent visible_comp;
    protected JButton arrow;
    protected JWindow popup;
    private boolean hidePopupButton=false;
    
    public DropDownComponent(JComponent vcomp, JComponent ddcomp,boolean hidePopup) {
        this.drop_down_comp = ddcomp;
        this.visible_comp = vcomp;
        this.hidePopupButton=hidePopup;
        initComponents();
        setupLayout();
    }

    public DropDownComponent(JComponent vcomp, JComponent ddcomp) {
        this(vcomp,ddcomp,false);
    }

    protected void initComponents() {
        arrow = new JButton(new MetalComboBoxIcon());
        Insets insets = arrow.getMargin();
        arrow.setMargin(new Insets(insets.top, 1, insets.bottom, 1));
        arrow.addActionListener(this);
        arrow.addAncestorListener(this);
    }

    public void hidePopup() {
        if(popup!=null && popup.isVisible()){
            popup.setVisible(false);
        }
    }

    private void setupLayout() {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = c.BOTH;
        gbl.setConstraints(visible_comp, c);
        add(visible_comp);
        c.weightx = 0;
        c.gridx++;
        gbl.setConstraints(arrow, c);
        add(arrow);
        arrow.setVisible(!hidePopupButton);
    }

    public void actionPerformed(ActionEvent e) {
        //Build poup window
        popup=new JWindow(getFrame(null));
        popup.getContentPane().add(drop_down_comp);
        popup.addWindowFocusListener(new WindowAdapter() {

            @Override
            public void windowLostFocus(WindowEvent e) {
                popup.setVisible(false);
            }

        });
        showPopupWindow();
    }

    public void showPopupWindow(){
        popup.pack();
        Point pt=visible_comp.getLocationOnScreen();
        pt.translate(0, visible_comp.getHeight());
        popup.setLocation(pt);
        popup.toFront();
        popup.setVisible(true);
        popup.requestFocusInWindow();
    }
    
    protected Frame getFrame(Component comp){
        if(comp==null)
            comp=this;
        if(comp.getParent() instanceof Frame)
            return (Frame)comp.getParent();
        else
            return getFrame(comp.getParent());

    }

    public void ancestorAdded(AncestorEvent event) {
        hidePopup();
    }

    public void ancestorRemoved(AncestorEvent event) {
        hidePopup();
    }

    public void ancestorMoved(AncestorEvent event) {
        if(event.getSource()!=popup)
            hidePopup();
    }
    
    public void open(){
    	
    }
}
