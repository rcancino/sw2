package com.luxsoft.siipap.swing.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;


import net.infonode.gui.colorprovider.FixedColorProvider;
import net.infonode.tabbedpanel.theme.BlueHighlightTheme;
import net.infonode.tabbedpanel.theme.ClassicTheme;
import net.infonode.tabbedpanel.theme.DefaultTheme;
import net.infonode.tabbedpanel.theme.GradientTheme;
import net.infonode.tabbedpanel.theme.LookAndFeelTheme;
import net.infonode.tabbedpanel.theme.ShapedGradientTheme;
import net.infonode.tabbedpanel.theme.SmallFlatTheme;
import net.infonode.tabbedpanel.theme.TabbedPanelTitledTabTheme;
import net.infonode.tabbedpanel.titledtab.TitledTab;

public class InfoNodeUtils {
	
	public  TabbedPanelTitledTabTheme[] getTabbedPanelTitledThems(){
		return themes;
	}
	
	public static JButton createCloseTabButton(final TitledTab tab) {
	    JButton closeButton = createXButton();
	    closeButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        // Closing the tab by removing it from the tabbed panel it is a
	        // member of
	        tab.getTabbedPanel().removeTab(tab);
	      }
	    });
	    return closeButton;
	  }
	
	/**
	   * Creates a JButton with an X
	   *
	   * @return the button
	   */
	  public static  JButton createXButton() {
	    JButton closeButton = new JButton("X");
	    closeButton.setOpaque(false);
	    closeButton.setMargin(null);
	    closeButton.setFont(closeButton.getFont().deriveFont(Font.BOLD).deriveFont((float) 10));
	    closeButton.setBorder(new EmptyBorder(1, 1, 1, 1));
	    closeButton.setFocusable(false);
	    return closeButton;
	  }
	
	public static TabbedPanelTitledTabTheme getDefaultTabbedPanelTitledTheme(){
		return new GradientTheme(false, true);
	}
	
	private  TabbedPanelTitledTabTheme[] themes = new TabbedPanelTitledTabTheme[]{
    		new DefaultTheme()
    		, new LookAndFeelTheme()
    		, new ClassicTheme()
    		, new BlueHighlightTheme()
    		, new SmallFlatTheme()
    		, new GradientTheme()
    		, new GradientTheme(true, true)
    		, new ShapedGradientTheme()
    		, new ShapedGradientTheme(
                0f,
                0f,
                new FixedColorProvider(
                    new Color(150, 150, 150)),
                null) {
              public String getName() {
                return super.getName() +
                       " Flat with no Slopes";
              }
            }
    		};
	
	//public static List

}
