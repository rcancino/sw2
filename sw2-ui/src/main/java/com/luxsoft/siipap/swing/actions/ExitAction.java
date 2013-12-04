package com.luxsoft.siipap.swing.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.luxsoft.siipap.swing.Application;

/**
 * Accion para cerrar instancia Singleton de Application 
 * 
 * @author Ruben Cancino 
 *
 */
public class ExitAction extends AbstractAction{
	
	private Application application;

	public void actionPerformed(ActionEvent e) {
		if(getApplication()!=null){
			getApplication().close();
		}
		
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}
	
	

}
