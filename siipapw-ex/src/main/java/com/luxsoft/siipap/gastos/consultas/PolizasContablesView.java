package com.luxsoft.siipap.gastos.consultas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.contabilidad.Poliza;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.contabilidad.ExportadorDePolizasGastos;
import com.luxsoft.siipap.service.contabilidad.ExportadorGenericoDePolizas;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.swing.views2.AbstractInternalTaskView;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;

/**
 * Consulta para el mantenimiento de Polizas de pagos 
 * 
 * @author Ruben Cancino
 *
 */
public class PolizasContablesView extends DefaultTaskView{
	
	private InternalTaskTab gastosTab;
	private PolizaGenericaTaskView gastosView;
	private InternalTaskTab pagosTab;
	
	
	@Override
	public void open() {
	}
	
	@Override
	public void close() {
		if(gastosView!=null){
			gastosView.close();
			gastosView=null;
		}
	}
	
	
	@Override
	protected void instalarTaskElements() {
		Action gastosAction=new AbstractAction("Provisión gastos"){
			public void actionPerformed(ActionEvent e) {
				mostrarGastos();
			}
		};
		consultas.add(gastosAction);
		Action pagosAction=new AbstractAction("Pagos"){
			public void actionPerformed(ActionEvent e) {
				mostrarPagos();
			}
		};
		consultas.add(pagosAction);
	}

	protected void instalarTaskPanels(final JXTaskPaneContainer container){
		this.taskContainer.remove(procesos); //We don't need this
		this.detalles.setTitle("Resumen");
		this.detalles.setExpanded(true);
	}
	
	
	
	private void mostrarGastos(){
		if(gastosTab==null){
			PolizaDeProvisionView panel=new PolizaDeProvisionView();
			gastosView=new PolizaGenericaTaskView(panel);
			gastosView.setTitle("Gastos");
			gastosTab=new InternalTaskTab(gastosView);
		}
		addTab(gastosTab);
		gastosTab.getTaskView().load();
	}
	
	private void mostrarPagos(){
		if(pagosTab==null){
			PolizaDePagosView panel=new PolizaDePagosView();
			panel.setPrefijoDeArchivo("E");
			PolizaGenericaTaskView adapter=new PolizaGenericaTaskView(panel);
			adapter.setTitle("Pagos");
			pagosTab=new InternalTaskTab(adapter);
		}
		addTab(pagosTab);
	}
	
	
	private class PolizaGenericaTaskView extends AbstractInternalTaskView{		
		
		private final PolizaGenericaPanel browser;
		
		public PolizaGenericaTaskView(final PolizaGenericaPanel browser){
			this.browser=browser;
		}

		public JComponent getControl() {
			return browser.getControl();
		}
		
		@Override
		public void instalOperacionesAction(JXTaskPane operaciones) {
			operaciones.add(browser.getLoadAction());
			operaciones.add(browser.getGenerarPolizaAction());
		}

		
		@Override
		public void installFiltrosPanel(JXTaskPane filtros) {
			filtros.add(browser.getFilterPanel());
		}

		@Override
		public void installDetallesPanel(JXTaskPane detalle) {
			detalle.add(browser.getTotalesPanel());
		}
		
	}
	
	public  class PolizaDePagosView extends PolizaGenericaPanel implements ActionListener{
		JXDatePicker datePicker;
		
		protected void init(){
			
			addProperty(new String[]{"fecha","tipo","concepto","debe","haber","cuadre","fecha","year","mes"});
			addLabels(  new String[]{"Fecha","tipo","concepto","Debe","Haber","Cuadre","fecha","year","mes"});
			
			installTextComponentMatcherEditor("A Favor", "afavor");
			installTextComponentMatcherEditor("Cuenta", "cuenta");
			installTextComponentMatcherEditor("Año", "year");
			installTextComponentMatcherEditor("Mes", "mes");
			datePicker=new JXDatePicker();
			datePicker.setFormats(new String[]{"dd/MM/yyyy"});
			datePicker.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			load();
		}
		
		protected void installCustomComponentsInFilterPanel(DefaultFormBuilder builder){
			builder.append("Fecha",datePicker);
		}
		
		public List<Poliza> findData(){
			return ServiceLocator2.getComprasDeGastosManager().generarPolizaDePagos2(datePicker.getDate());
		}
		
		protected void generarPoliza(){
			System.out.println("Usando nuevo generador.....");
			if(!getSelected().isEmpty()){
				final ExportadorDePolizasGastos manager=new ExportadorDePolizasGastos();
				for(Object o:getSelected()){
					Poliza poliza=(Poliza)o;
					File file=manager.exportar(poliza,getPrefijoDeArchivo());
					if(file!=null){
						MessageUtils.showMessage("Poliza generada:\n"+file.getAbsolutePath(), "Poliza de gastos");
					}
				}
			}
		}
	}
	
	public  class PolizaDeProvisionView extends PolizaGenericaPanel implements ActionListener{
		
		JXDatePicker datePicker;
		JXDatePicker datePicker2;
		
		protected void init(){
			
			addProperty(new String[]{"fecha","tipo","concepto","debe","haber","cuadre","fecha","year","mes"});
			addLabels(  new String[]{"Fecha","tipo","concepto","Debe","Haber","Cuadre","fecha","year","mes"});
			
			installTextComponentMatcherEditor("A Favor", "afavor");
			installTextComponentMatcherEditor("Cuenta", "cuenta");
			installTextComponentMatcherEditor("Año", "year");
			installTextComponentMatcherEditor("Mes", "mes");
			datePicker=new JXDatePicker();
			datePicker.setFormats(new String[]{"dd/MM/yyyy"});
			//datePicker.addActionListener(this);
			
			datePicker2=new JXDatePicker();
			datePicker2.setFormats(new String[]{"dd/MM/yyyy"});
			datePicker2.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			load();
		}
		
		protected void installCustomComponentsInFilterPanel(DefaultFormBuilder builder){
			builder.append("Fecha Inicial",datePicker);
			builder.append("Fecha Final",datePicker2);
		}
		
		public List<Poliza> findData(){
			Periodo p=new Periodo(datePicker.getDate(),datePicker2.getDate());
			Poliza res=ServiceLocator2.getComprasDeGastosManager().generarPolizaDeProvision(p);
			res.setFecha(p.getFechaFinal());
			List<Poliza> list=new ArrayList<Poliza>();
			list.add(res);
			return list;
		}

		@Override
		protected void executeLoadWorker(SwingWorker worker) {
			TaskUtils.executeSwingWorker(worker);
		}
		
		protected void generarPoliza(){
			System.out.println("Usando nuevo generador.....");
			if(!getSelected().isEmpty()){
				final ExportadorDePolizasGastos manager=new ExportadorDePolizasGastos();
				for(Object o:getSelected()){
					Poliza poliza=(Poliza)o;
					File file=manager.exportar(poliza,getPrefijoDeArchivo());
					if(file!=null){
						MessageUtils.showMessage("Poliza generada:\n"+file.getAbsolutePath(), "Poliza de gastos");
					}
				}
			}
		}
		
	}

}
