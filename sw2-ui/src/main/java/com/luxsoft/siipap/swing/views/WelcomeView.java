/*
 * Copyright (c) 2002-2005 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * This software is the proprietary information of JGoodies Karsten Lentzsch.
 * Use is subject to license terms.
 *
 */

package com.luxsoft.siipap.swing.views;

import java.awt.Component;

import javax.swing.*;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;




import com.jgoodies.uifextras.panel.GradientBackgroundPanel;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.swing.AbstractView;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;

/**
 * This panel is displayed after a successful application startup to welcome
 * the user. It provides access to actions that are most useful at the
 * application start.
 * 
 * TODO: Support to host the last actions executed
 * 
 */
public final class WelcomeView extends AbstractView  {

    
    
    private Component welcomeLabel;
    private Component logoLabel;
    private Component selectLabel;
    
    
    
    
    // Instance Creation ******************************************************
    
    public WelcomeView() {
    	super("welcomeView");
    }
    
    
   
    

    // Building *************************************************************
    
    private void initComponents() {
        welcomeLabel = new JLabel("Welcome to");
        logoLabel    = new JLabel(getResourceLocator().getIcon(ResourcesUtils.APPLICATION_LOGO));
        selectLabel  = new JLabel("Select one of the options below.");
        
        
    }
    
    
    protected JComponent createActionComponent(Action a) {
        ActionLabel label = new ActionLabel(a);
        label.setIcon(getResourceLocator().getIcon(ResourcesUtils.ARROW_ICON_ID));
        return label;
    }

    
    /**
     * Builds and returns the panel.
     */
    protected JComponent buildContent() {
        initComponents();
        
        FormLayout layout = new FormLayout(
                "9dlu, left:pref:grow",
                "b:pref, c:pref, t:pref, 9dlu, pref, 6dlu, pref");
        PanelBuilder builder = new PanelBuilder(layout, new GradientBackgroundPanel(false));
        builder.getPanel().setOpaque(false);
        builder.setBorder(Borders.DLU14_BORDER);

        CellConstraints cc = new CellConstraints();

        builder.add(welcomeLabel,        cc.xyw(1, 1, 2));
        builder.add(logoLabel,           cc.xyw(1, 2, 2, "left, center"));
        builder.add(selectLabel,         cc.xyw(1, 3, 2));
        

        return builder.getPanel();
    }


	
}