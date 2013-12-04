package com.luxsoft.siipap.swing.controls;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.management.ManagementFactory;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.log4j.Logger;

public class MemoryMonitor extends AbstractControl{
	
	private JProgressBar pb;
	@SuppressWarnings("unused")
	private Logger logger=Logger.getLogger(getClass());

	@Override
	protected JComponent buildContent() {
		
		long max=ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax()/1000;
		long min=ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getInit()/1000;
		long committed=ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted()/1000;
		pb=new JProgressBar((int)min,(int)max);
		//pb=new JProgressBar(0,100);
		pb.setToolTipText("Commited:" +NumberFormat.getNumberInstance().format(committed));
		pb.setStringPainted(true);
		pb.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2){
					sinc();
				}
			}			
		});
		pb.addComponentListener(new ComponentAdapter(){

			@Override
			public void componentResized(ComponentEvent e) {
				sinc();
			}

			
			
		});
		return pb;
	}
	
	final String pattern="Mínima: {0}" +
			"\tMáxima: {1}" +
			"\tUsada :{2}" +
			"\tComprometida: {3}";
	
	public void update(){		
		int val=(int)ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed()/1000;
		int min=(int)ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getInit()/1000;
		int max=(int)ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax()/1000;
		int com=(int)ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted()/1000;		
		if(pb!=null){
			pb.setValue(val);
			pb.setToolTipText(MessageFormat.format(pattern,min,max,val,com ));
		}
	}
	
	private void sinc(){
		
		/**
		final Runnable runner=new Runnable(){

			public void run() {
				while(true){
					//SystemUtils.sleep(1000);
					//logger.debug("Actualizando memoria");
					update();
				}				
			}			
		};		
		final Thread t=new Thread(runner);
		t.start();
		**/	
		TimerTask task=new TimerTask(){

			@Override
			public void run() {
				update();
				
			}
			
		};
		Timer timer=new Timer("memotyMonitor",true);
		timer.schedule(task, 1000,5000);
	}
	
	public static void main(String[] args) {
		final MemoryMonitor m=new MemoryMonitor();
		final SXAbstractDialog dialog=new SXAbstractDialog("Test"){
			
			@Override
			protected JComponent buildContent() {
				JPanel p=new JPanel(new BorderLayout());
				Header h=new Header("Memory Monitor","Test...");
				p.add(h.getHeader(),BorderLayout.CENTER);
				
				p.add(m.getControl(),BorderLayout.SOUTH);
				//m.sinc();
				return p;
			}
			
		};
		dialog.open();
	}

}
