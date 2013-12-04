package com.luxsoft.siipap.swing.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.JDialog;
import javax.swing.SwingWorker;

public class SwingWorkerWaiter2 implements PropertyChangeListener{
	
	private WeakReference<JDialog> dialog;
	
	public SwingWorkerWaiter2(JDialog owner){
		dialog=new WeakReference<JDialog>(owner);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if("state".equals(evt.getPropertyName())
			&& SwingWorker.StateValue.DONE.equals(evt.getNewValue())){
			if(dialog.get()!=null)
				dialog.get().setVisible(true);
			
		}else if(SwingWorker.StateValue.STARTED.equals(evt.getNewValue())){
			if(dialog.get()!=null)
				dialog.get().dispose();
		}
				
			
		
		
	}

}
