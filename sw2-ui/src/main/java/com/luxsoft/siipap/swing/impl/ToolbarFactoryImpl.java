package com.luxsoft.siipap.swing.impl;

import javax.swing.JToolBar;

import com.jgoodies.looks.BorderStyle;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.swing.ToolbarFactory;
import com.luxsoft.siipap.swing.actions.ActionManager;



public class ToolbarFactoryImpl implements ToolbarFactory{
	
	private JToolBar toolBar;
	private ActionManager actionManager;
	
	public ToolbarFactoryImpl(){
		
	}
	
	
	public JToolBar getToolbar(){
		if(toolBar==null){
			toolBar=buildToolbar();
		}
		return toolBar;
	}
	
	protected JToolBar buildToolbar() {
		
		// Set a hint so that JGoodies Looks will detect it as being in the header.
		ToolBarBuilder builder = new ToolBarBuilder("ToolBar", HeaderStyle.BOTH);
		// Unlike the default, use a separator border.
		builder.getToolBar().putClientProperty(
				WindowsLookAndFeel.BORDER_STYLE_KEY, BorderStyle.SEPARATOR);
		builder.getToolBar().putClientProperty(
				PlasticLookAndFeel.BORDER_STYLE_KEY, BorderStyle.SEPARATOR);

		builder.addGap(2);		
		addCustomButtons(builder);
		builder.addLargeGap();
		builder.addGlue();
		//builder.add(ActionManager.get(DLGSwingModel.OPEN_HELP_CONTENTS_ID));
		builder.addGap(2);
		return builder.getToolBar();
	}
	
	/**
	 * Template method for adding custom buttons
	 * @param builder
	 */
	protected void addCustomButtons(ToolBarBuilder builder){
		
	}


	public ActionManager getActionManager() {
		return actionManager;
	}


	public void setActionManager(ActionManager actionManager) {
		this.actionManager = actionManager;
	}
	
	

}
