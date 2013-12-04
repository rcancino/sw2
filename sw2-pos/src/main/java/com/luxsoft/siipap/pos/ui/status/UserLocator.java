package com.luxsoft.siipap.pos.ui.status;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.Application;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.sw3.services.POSDBUtils;

public class UserLocator extends AbstractControl{
	
	JLabel userInfo;

	@Override
	protected JComponent buildContent() {
		userInfo=new JLabel("Usuario:     		 ");
		sincronize();
		return userInfo;
	}
	
	public void sincronize(){		
		SwingWorker worker=new SwingWorker(){			
			protected Object doInBackground() throws Exception {
				if(Application.isLoaded()){
					return KernellSecurity.instance().getCurrentUserName();
				}else
					return "ERROR NO DB";
			}

			@Override
			protected void done() {
				try {
					String res=(String)get();
					if(res!=null){
						userInfo.setText(" Usuario: "+res);
					}
					else{
						userInfo.setText(" Usuario: ND");
					}
					userInfo.setToolTipText("IP: "+KernellSecurity.getIPAdress());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		};
		worker.execute();
	}

}
