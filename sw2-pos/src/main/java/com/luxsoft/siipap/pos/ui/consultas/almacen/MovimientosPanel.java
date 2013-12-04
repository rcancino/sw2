package com.luxsoft.siipap.pos.ui.consultas.almacen;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.inventarios.model.AutorizacionDeMovimiento;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.reports.AnaliticoXMovimientoReportForm;
import com.luxsoft.siipap.pos.ui.reports.Discrepancias;
import com.luxsoft.siipap.pos.ui.reports.ExistenciasReportForm;
import com.luxsoft.siipap.pos.ui.reports.InventarioCertificadoReportForm;
import com.luxsoft.siipap.pos.ui.reports.KardexCertReportForm;
import com.luxsoft.siipap.pos.ui.reports.KardexReportForm;
import com.luxsoft.siipap.pos.ui.reports.MaterialEnRecorteReportForm;
import com.luxsoft.siipap.pos.ui.reports.ResumenDeMovReportForm;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeExistencias;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.forms.MovimientoController;
import com.luxsoft.sw3.ui.forms.MovimientoDeInventarioForm;
import com.luxsoft.sw3.ui.services.AutorizacionesFactory;
import com.luxsoft.sw3.ui.services.KernellUtils;

/**
 * Panel para el mantenimiento de traslados
 * 
 * @author Ruben Cancino
 *
 */
public class MovimientosPanel extends AbstractMasterDatailFilteredBrowserPanel<Movimiento, MovimientoDet>{

	//private Sucursal sucursal;
	
	public MovimientosPanel() {
		super(Movimiento.class);
		//sucursal=Services.getInstance().getConfiguracion().getSucursal();
		
	}
	
	protected void init(){		
		super.init();
		addProperty("documento","fecha","concepto","porInventario","comentario");
		addLabels("Docto","Fecha","Concepto","Por Inv","Comentario");
		//installTextComponentMatcherEditor("Sucursal", new String[]{"sucursal.nombre"});
		manejarPeriodo();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"sucursal.nombre","fecha","clave"
				,"descripcion"
				,"cantidad"
				,"comentario"};
		String[] labels={"Sucursal","Fecha","Clave"
				,"Descripcion"
				,"cantidad"
				,"comentario"};
		return GlazedLists.tableFormat(MovimientoDet.class, props,labels);
	}

	@Override
	protected Model<Movimiento, MovimientoDet> createPartidasModel() {
		return new CollectionList.Model<Movimiento, MovimientoDet>(){
			public List<MovimientoDet> getChildren(Movimiento parent) {
				String hql="from MovimientoDet det where det.movimiento.id=?";
				return Services.getInstance().getHibernateTemplate().find(hql, parent.getId());
			}
		};
	}

	@Override
	protected Movimiento doInsert() {
		Movimiento target=new Movimiento();
		MovimientoController controller=new MovimientoController(target);
		MovimientoDeInventarioForm form=new MovimientoDeInventarioForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			return controller.persist();
		}
		return null;
	}
	
	

	@Override
	protected Movimiento doEdit(Movimiento bean) {
		if(KernellUtils.validarAcceso(POSRoles.GERENTE_DE_INVENTARIOS.name())){
			Movimiento target=getFreshCopy(bean.getId());
			MovimientoController controller=new MovimientoController(target);
			MovimientoDeInventarioForm form=new MovimientoDeInventarioForm(controller);
			form.open();
			if(!form.hasBeenCanceled()){
				return controller.persist();
			}
			return bean;
		}
		return bean;
	}
	
	private Movimiento getFreshCopy(String id){
		return Services.getInstance().getInventariosManager().getMovimientoDao().get(id);
	}

	@Override
	protected void afterInsert(Movimiento bean) {		
		super.afterInsert(bean);
		if(MessageUtils.showConfirmationMessage("Imprimir el movimiento", "Movimientos de inventario")){
			print(bean);
		}
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getDeleteAction()
				,getEditAction()
				,getViewAction()
				,CommandUtils.createPrintAction(this, "print")
				,addAction(null,"existencias","Existencias")
				};
		return actions;
	}
	
	
 
	protected List<Action> createProccessActions(){
		List<Action> actions=new ArrayList<Action>();
		actions.add(addAction("", "kardex", "Kardex"));
		actions.add(addAction("", "reporteAnaliticoPorMovimiento", "Analítico por Mov."));
		actions.add(addAction("", "reporteDiscrepanciasDeInventario", "Discrepancias"));
		actions.add(addAction("", "reporteExistenciaInv", "Reporte de Existencias"));
		actions.add(addAction("", "reporteMaterialEnRecorte", "Recorte"));
		actions.add(addAction("", "reporteResumenDeMovimientos", "Resumen de Movs."));
		actions.add(addAction("", "reporteKardexCertificacion", "Kardex Certif."));
		actions.add(addAction("", "reporteInventarioCertificado", "Inventario Certif."));
		
		actions.add(addAction("", "recalcularExistencias", "Recalcular Exi."));
		return actions;
	}
	
	public void existencias(){
		SelectorDeExistencias.find();
	}
	
	public void reporteKardexCertificacion(){
		KardexCertReportForm.run();
	}
	
	public void reporteInventarioCertificado(){
		InventarioCertificadoReportForm.run();
	}
	
	
	public void reporteAnaliticoPorMovimiento(){
		AnaliticoXMovimientoReportForm.runReport();
	}
	
	public void reporteDiscrepanciasDeInventario(){
		Discrepancias.run();
		
	}
	
	public void reporteExistenciaInv(){
		ExistenciasReportForm.run();
	}
	
	public void reporteMaterialEnRecorte() {
		MaterialEnRecorteReportForm.run();		
	}
	
	public void reporteResumenDeMovimientos() {
		ResumenDeMovReportForm.run();
		
	}
	
	public void kardex(){
		KardexReportForm.run();
	}

	

	@Override
	public boolean doDelete(Movimiento bean) {
		/*AutorizacionDeMovimiento aut=AutorizacionesFactory.getAutorizacionParaCancelarMovimiento();
		if(aut!=null){*/
		if(KernellUtils.validarAcceso(POSRoles.GERENTE_DE_INVENTARIOS.name())){
			String comentario=JOptionPane.showInputDialog(getControl(), "Comentario para la cancelación");
			//bean.setAutorizacion(aut);
			bean.setComentario(StringUtils.substring(comentario, 0, 250));
			Services.getInstance().getInventariosManager().eliminarMovimiento(bean);
			
		}		
		return true;
	}
	
	

	@Override
	protected List<Movimiento> findData() {
		String hql="from Movimiento m where m.sucursal.id=? and m.fecha between ? and ? ";
		return Services.getInstance().getHibernateTemplate().find(hql
				,new Object[]{Services.getInstance().getConfiguracion().getSucursal().getId(),periodo.getFechaInicial(),periodo.getFechaFinal()});
	}
	
	public void print(){
		if(getSelectedObject()!=null){
			Movimiento m=(Movimiento)getSelectedObject();
			Map params=new HashMap();
			params.put("MOVI_ID", m.getId());
			ReportUtils2.runReport("invent/MovGenerico.jasper", params);
		}
	}
	
	
	public void print(Movimiento m){
		Map params=new HashMap();
		params.put("MOVI_ID", m.getId());
		ReportUtils2.runReport("invent/MovGenerico.jasper", params);
	}
	
	public void recalcularExistencias(){
		RecalculoForm form=new RecalculoForm();
		form.open();
		if(!form.hasBeenCanceled()){
			if(form.isTodos()){
				calcularExistencias();
			}else{
				if(form.getProducto()!=null){
					String clave=form.getProducto().getClave();
					calcularExistencias(clave);
				}
			}
				
		}
	}
	
	private void calcularExistencias(){
		final Date fecha=new Date();
		final int mes=Periodo.obtenerMes(fecha)+1;
		final int year=Periodo.obtenerYear(fecha);
		System.out.println("Recalculando existencia para Año:"+year+ " Mes: "+mes+" Fecha:"+fecha);
		SwingWorker worker=new SwingWorker(){			
			protected Object doInBackground() throws Exception {
				Services.getInstance().getExistenciasDao()
				.actualizarExistencias(Services.getInstance().getConfiguracion().getSucursal().getId(),year , mes);
				return "OK";
			}
			protected void done() {
				try {
					get();
					MessageUtils.showMessage("Existencias actualizadas", "Recalculo de existencias");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}			
			
		};
		TaskUtils.executeSwingWorker(worker);
	}
	
	private void calcularExistencias(final String prodClave){
		final Date fecha=new Date();
		final int mes=Periodo.obtenerMes(fecha)+1;
		final int year=Periodo.obtenerYear(fecha);
		final long sucursalId=Services.getInstance().getConfiguracion().getSucursal().getId();
		SwingWorker worker=new SwingWorker(){			
			protected Object doInBackground() throws Exception {
				Services.getInstance().getExistenciasDao()
				.actualizarExistencias(
						sucursalId
						,prodClave
						,year 
						, mes);
				return "OK";
			
			}
			protected void done() {
				try {
					get();
					MessageUtils.showMessage("Existencias actualizadas", "Recalculo de existencias");
				} catch (Exception e) {
					e.printStackTrace();
					
				}
				
			}				
			
		};
		TaskUtils.executeSwingWorker(worker);
	}
	
	public  class RecalculoForm extends SXAbstractDialog{
		
		private JComboBox productoControl;
		
		private JCheckBox todosBox;
		

		public RecalculoForm() {
			super("Costeo de inventarios");
		}
		
		private void initComponents(){
			productoControl=createProductosControl();
			todosBox=new JCheckBox("Todos",false);
			todosBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					productoControl.setEnabled(!todosBox.isSelected());
				}
			});
		}

		@Override
		protected JComponent buildContent() {
			initComponents();
			JPanel panel=new JPanel(new BorderLayout());			
			final FormLayout layout=new FormLayout(
					"p,2dlu,70dlu,3dlu,p,2dlu,70dlu:g,2dlu,p",
					"");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Producto",productoControl,5);
			builder.append(todosBox);
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			
			return panel;
		}
		
		private JComboBox createProductosControl(){		
			final JComboBox box = new JComboBox();			
			EventList source =null;
			source=GlazedLists.eventList(Services.getInstance().getProductosManager().buscarInventariablesActivos());
			final TextFilterator filterator = GlazedLists
				.textFilterator(new String[] { "clave","descripcion" });
			AutoCompleteSupport support = AutoCompleteSupport.install(box,source, filterator);
			support.setFilterMode(TextMatcherEditor.CONTAINS);
			support.setStrict(false);
			final EventComboBoxModel model = (EventComboBoxModel) box.getModel();
			return box;
		}
		
		public Producto getProducto(){
			return (Producto)productoControl.getSelectedItem();
		}
		
		public boolean isTodos(){
			return todosBox.isSelected();
		}
		
	}

}
