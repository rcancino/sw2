package com.luxsoft.siipap.swing.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXBusyLabel;

import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

@SuppressWarnings("unchecked")
public class TaskUtils {
	
	private static TaskDialog dialog=null;
	
	
	public static void executeSwingWorker(final SwingWorker worker){
		executeSwingWorker(worker, "Trabajando...", "Cargando información");
		/*
		JDialog dialog=new JDialog();
		dialog.getContentPane().add(new BusyHeader("Trabajando ...","Cargando información de la base de datos"));
		dialog.setModal(true);
		dialog.pack();
		ScreenUtils.locateOnScreenCenter(dialog);
		SwingWorkerWaiter waiter=new SwingWorkerWaiter(dialog);
		worker.addPropertyChangeListener(waiter);		
		worker.execute();
		dialog.setVisible(true);
		*/		
	}
	
	public static void executeSwingWorker(final SwingWorker worker,final String title,final String desc){
		/*
		JDialog dialog=new JDialog();
		dialog.getContentPane().add(new BusyHeader(title,desc));
		dialog.setModal(true);
		dialog.pack();
		ScreenUtils.locateOnScreenCenter(dialog);v
		*/
		
		TaskDialog dialog=getDialog();
		dialog.setHeaderTitle(title);
		dialog.setHeaderDesc(desc);
		
		
		final SwingWorkerWaiter waiter=new SwingWorkerWaiter(dialog);
		worker.addPropertyChangeListener(waiter);		
		worker.execute();
		//dialog.open();
		//worker.removePropertyChangeListener(waiter);
		
	}
	
	
	public static void executeSwingWorkerInDialog(final SwingWorker worker,final String title,final String desc){
		/*
		JDialog dialog=new JDialog();
		dialog.getContentPane().add(new BusyHeader(title,desc));
		dialog.setModal(true);
		dialog.pack();
		ScreenUtils.locateOnScreenCenter(dialog);v
		*/
		
		TaskDialog dialog=getDialog();
		dialog.setHeaderTitle(title);
		dialog.setHeaderDesc(desc);
		
		
		final SwingWorkerWaiter waiter=new SwingWorkerWaiter(dialog);
		worker.addPropertyChangeListener(waiter);		
		worker.execute();
		dialog.open();
		//worker.removePropertyChangeListener(waiter);
		
	}
	
	private static TaskDialog getDialog(){
		if(dialog==null){
			dialog=new TaskDialog();
		}
		return dialog;
	}
	
	public static class TaskDialog extends SXAbstractDialog{
		
		private BusyHeader header;
		
		public TaskDialog(){
			this("Trabajando...", "Cargando información");
		}
		
		public TaskDialog(Frame owner,String title,String desc){
			super(owner,title);
			header=new BusyHeader(title,desc);
			setModal(true);
		}
		
		public TaskDialog(String title,String desc) {
			super("Cargando datos");
			header=new BusyHeader(title,desc);
			setModal(true);
		}

		

		@Override
		protected JComponent buildContent() {			
			return header;
		}
		
		public void setHeaderTitle(String title){
			header.setTitle(title);
		}
		public void setHeaderDesc(String desc){
			header.setDescription(desc);
		}
	}
	
	public static class BusyHeader extends HeaderPanel{
		
		private JLabel bussyLabel;

		public BusyHeader(String title, String description, Icon icon, ConstantSize minimumWidth, ConstantSize minimumHeight) {
			super(title, description, icon, minimumWidth, minimumHeight);
			// TODO Auto-generated constructor stub
		}

		public BusyHeader(String title, String description, Icon icon, JComponent backgroundComponent, ConstantSize minimumWidth, ConstantSize minimumHeight) {
			super(title, description, icon, backgroundComponent, minimumWidth,
					minimumHeight);
			// TODO Auto-generated constructor stub
		}

		public BusyHeader(String title, String description, Icon icon) {
			super(title, description, icon);
			// TODO Auto-generated constructor stub
		}

		public BusyHeader(String title, String description) {
			super(title, description);
			// TODO Auto-generated constructor stub
		}
			
		protected JLabel createTitleLabel() {
			JXBusyLabel l=new JXBusyLabel();
			l.setBusy(true);
			l.setForeground(Color.BLACK);
			l.setFont(l.getFont().deriveFont(Font.BOLD));
			bussyLabel=l;
	        return l;
	    }
		
		public void setDescription(String s){
			bussyLabel.setText(s);
		}
		
	}
	
	private static SimpleDateFormat df=new SimpleDateFormat("dd/MM/yyyy:mm:hh:ss");
	
	public static String getHora(){
		return df.format(new Date());
	}
	
	public static void executeTask(final SwingWorker worker){
		worker.execute();
	}

}
