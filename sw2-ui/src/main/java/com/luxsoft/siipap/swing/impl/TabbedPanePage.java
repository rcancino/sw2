package com.luxsoft.siipap.swing.impl;

import java.awt.Font;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import com.luxsoft.siipap.swing.Page;
import com.luxsoft.siipap.swing.View;

/**
 * Implementacion de Page que utiliza una instancia de JTabbedPane para
 * almacenar Vistas
 * 
 * @author Ruben Cancino
 *
 */
public class TabbedPanePage implements Page{
	
	private JTabbedPane container;
	
	public JComponent getContainer() {
		if(container==null){
			container=new JTabbedPane();
		}
		return container;
	}

	public void addView(View view) {
		System.out.println("Agregando vista: "+view.getId());
		container.addTab("",null, view.getContent());
		JPanel panel=new JPanel();
		panel.add(new JLabel("My Title"));
		panel.add(createXButton());
		container.setTabComponentAt(container.getTabCount()-1, panel);
	}

	public void close() {
		System.out.println("TODO: Implementar este metodo");
	}
	
	public static  JButton createXButton() {
	    JButton closeButton = new JButton("X");
	    closeButton.setOpaque(false);
	    closeButton.setMargin(null);
	    closeButton.setFont(closeButton.getFont().deriveFont(Font.BOLD).deriveFont((float) 10));
	    closeButton.setBorder(new EmptyBorder(1, 1, 1, 1));
	    closeButton.setFocusable(false);
	    return closeButton;
	  }

	public List<View> getViews() {
		// TODO Auto-generated method stub
		return null;
	}

}
