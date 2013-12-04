package com.luxsoft.siipap.swing.controls;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import com.luxsoft.siipap.swing.Application;
import com.luxsoft.siipap.util.DBUtils;

public class DataBaseLocator extends AbstractControl{
	
	

	@Override
	protected JComponent buildContent() {
		JLabel l=new JLabel();
		Icon i=getIconFromResource("images2/server_go.png");
		l.setIcon(i);
		l.setText("DB:");
		sincronize();
		/*
		l.addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2){
					System.out.println("Sincronizando");
					sincronize();
				}
			}			
		});
		*/
		return l;
	}
	
	public void sincronize(){		
		SwingWorker worker=new SwingWorker(){			
			protected Object doInBackground() throws Exception {
				if(Application.isLoaded()){
					return DBUtils.getAplicationDB_URL();
				}else
					return "ERROR NO DB";
			}

			@Override
			protected void done() {
				try {
					//getControl().setToolTipText(get().toString());
					((JLabel)getControl()).setText(get().toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		};
		worker.execute();
	}
	
	

}
