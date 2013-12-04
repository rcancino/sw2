package com.luxsoft.siipap.cxc.ui.consultas;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXStatusBar;

import com.jgoodies.binding.value.ValueHolder;
import com.luxsoft.siipap.cxc.model.MovimientoDeCuenta;
import com.luxsoft.siipap.cxc.service.EstadoDeCuentaManager;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.swing.views2.AbstractTaskView;

public class EstadoDeCuentaGeneralPanel extends FilteredBrowserPanel<MovimientoDeCuenta>{

	public EstadoDeCuentaGeneralPanel() {
		super(MovimientoDeCuenta.class);
	}
	
	
	@Override
	protected void init() {
		setTitle("Estado de cuenta general");
		addProperty("nombre","tipo","documento","sucursal","fecha","revision"
				,"vencimiento","referencia","descuento","total","aplicable"
				,"saldoAcumulado","saldoCargo","saldoAFavor","notaAFavor","aplicacionesAnteriores"
				);
		
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		manejarPeriodo();
		setDefaultPanel(true);
		
	}
	
	
	


	protected JComponent buildContent() {
		JComponent c=super.buildContent();
		c.setPreferredSize(new Dimension(750,600));
		return c;
	}
	
	public void load(){
		if(getCliente()==null) return;
		super.load();
	}
	
	@Override
	protected List<MovimientoDeCuenta> findData() {
		return getManager().buscarMovimientos(getCliente(),periodo);
	}
	
	private ValueHolder clienteHolder=new ValueHolder(new Cliente("A010210","ARTES GRAFICAS SANABRIA, S.A. DE C.V."));
	
	public Cliente getCliente(){
		return (Cliente)clienteHolder.getValue();
	}
	public void setCliente(Cliente c){
		clienteHolder.setValue(c);
	}

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		if(watcher!=null)
			worker.addPropertyChangeListener(watcher);
		worker.execute();
		//TaskUtils.executeSwingWorker(worker);
	}
	
	private PropertyChangeListener watcher;

	public void registerWatcher(PropertyChangeListener watcher){
		this.watcher=watcher;
	}

	private EstadoDeCuentaManager getManager(){
		return ServiceLocator2.getCXCManager().getEstadoDeCuentaManager();
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				addAction("refresh", "load", "Cargar")
				};
		return actions;
	}
	
	
	public static class MovimientosGlobalesFrame extends JXFrame implements PropertyChangeListener{
		
		private EstadoDeCuentaGeneralPanel browser;
		private AbstractTaskView view;
		private JXStatusBar status;
		private JLabel info=new JLabel("Listo");
		private JProgressBar progress;
		

		public MovimientosGlobalesFrame() {
			super("",false);
			
		}
		
		private JComponent content;
		
		
		
		private void build(){
			if(content==null){
				browser=new EstadoDeCuentaGeneralPanel();
				browser.registerWatcher(this);
				view=new AbstractTaskView();
				view.getPanels().add(browser);
				
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
				
				setStatusBar(status);
				pack();
			}
		}
		
		
		 
		public void setVisible(boolean v){
			build();
			super.setVisible(v);
		}
		
		private void inicioCarga(){
			setWaiting(true);
			progress.setIndeterminate(true);
			progress.setString("Cargando movimientos");
		}

		DateFormat df=new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");
		
		private void terminoCarga(){
			info.setText(" Ultima carga:  "+df.format(new Date()));
			setWaiting(false);
			progress.setIndeterminate(false);
			progress.setString("");
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if(evt.getPropertyName().equals("state")){
				System.out.println(evt.getNewValue());
				if(evt.getNewValue().equals(StateValue.STARTED)){					
					inicioCarga();
				}else if(evt.getNewValue().equals(StateValue.DONE))
					terminoCarga();
			}
			
		}
		
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SWExtUIManager.setup();
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				MovimientosGlobalesFrame app=new MovimientosGlobalesFrame();
				
				app.setVisible(true);
			}
			
		});
	}

}
