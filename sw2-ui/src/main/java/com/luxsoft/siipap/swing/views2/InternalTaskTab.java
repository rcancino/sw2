package com.luxsoft.siipap.swing.views2;

import net.infonode.tabbedpanel.titledtab.TitledTab;

/**
 * Extiende TitledTab para usar un InternalTaskView como
 * delegado
 * 
 * @author Ruben Cancino
 *
 */
public class InternalTaskTab extends TitledTab{
	
	private final InternalTaskView taskView;

	public InternalTaskTab(InternalTaskView view) {
		super(view.getTitle()
				,view.getIcon()
				,view.getControl()
				,null);
		this.taskView=view;
	}

	public AbstractInternalTaskView getTaskView() {
		return (AbstractInternalTaskView)taskView;
	}
	
	

}
