package com.luxsoft.siipap.cxp.ui.consultas;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker.StateValue;

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXStatusBar;

import com.luxsoft.siipap.swing.controls.BusyLabel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.swing.views2.AbstractTaskView;

/**
 * Ventanan para la consulta alterna de informacion de cuentas por pagar 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class EstadoDeCuentaWindow extends JXFrame implements PropertyChangeListener{
	
	private AbstractTaskView view;
	private JXStatusBar status;
	private JLabel info=new JLabel("Listo");
	private JProgressBar progress;
	private BusyLabel busyLabel;
	
	private JComponent content;
	
	
	
	private void build(){
		if(content==null){
			
			view=new AbstractTaskView();
			view.getContent();
			view.removerTaskPanel(view.getProcesosTaskPanel());
			addComponent(view.getContent());
			setStartPosition(StartPosition.CenterInScreen);
			view.open();
			view.getConsultasTaskPanel().setExpanded(false);
			//
			
			status=new JXStatusBar();
			
			JXStatusBar.Constraint c1 = new JXStatusBar.Constraint(); 
		    c1.setFixedWidth(200);
			status.add(info,c1);
			
			progress=new JProgressBar();	
			progress.setStringPainted(true);
			progress.setString("");
			progress.setBorderPainted(false);
			
			JXStatusBar.Constraint c2 = new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FIXED);
			status.add(progress,c2);
			
			busyLabel=new BusyLabel();
			inicioCarga();
			status.add(busyLabel,c1);
			
			setStatusBar(status);
			pack();
		}
	}
	
	public void open(){
		if(content==null){
			build();
		}
		setVisible(true);
	}
	
	
	private void inicioCarga(){
		setWaiting(true);
		//progress.setIndeterminate(true);
		//progress.setString("Cargando datos");
		busyLabel.setBusy(true);
		busyLabel.setText("Cargando datos");
		
	}

	DateFormat df=new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");
	
	private void terminoCarga(){
		info.setText(" Ultima actualización:  "+df.format(new Date()));
		setWaiting(false);
		//progress.setIndeterminate(false);
		//progress.setString("");
		busyLabel.setBusy(false);
		busyLabel.setText("");
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("state")){			
			if(evt.getNewValue().equals(StateValue.STARTED)){					
				inicioCarga();
			}else if(evt.getNewValue().equals(StateValue.DONE))
				terminoCarga();
		}
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SWExtUIManager.setup();
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				EstadoDeCuentaWindow app=new EstadoDeCuentaWindow();
				app.open();
			}
			
		});
	}
	
	

}
