package com.luxsoft.siipap.pos.ui.status;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import com.luxsoft.siipap.swing.Application;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;


public class POSDataBaseLocator extends AbstractControl{
	
	private JLabel label;

	@Override
	protected JComponent buildContent() {
		
		Icon i=getIconFromResource("images2/server_go.png");
		label=new JLabel(i);
		label.setText("Buscando conexión");
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
		return label;
	}
	
	public void sincronize(){		
		SwingWorker worker=new SwingWorker(){			
			protected Object doInBackground() throws Exception {
				if(Application.isLoaded()){
					return POSDBUtils.getAplicationDB_URL();
				}else
					return "ERROR NO DB";
			}

			@Override
			protected void done() {
				try {
					//getControl().setToolTipText(get().toString());
					String res=get().toString();
					if(res.startsWith("ERROR")){
						Icon i=getIconFromResource("images2/server_delete.png");
						label.setIcon(i);
					}
					((JLabel)getControl()).setText(get().toString());
					if(Application.isLoaded()){
						String title=Application.instance().getMainFrame().getTitle();
						String suc=Services.getInstance().getConfiguracion().getSucursal().getNombre();
						Application.instance().getMainFrame().setTitle(title+"  ["+suc+"]");
						System.err.println("*********************"+suc);
						
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		};
		worker.execute();
	}
	
	

}
