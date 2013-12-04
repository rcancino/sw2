package com.luxsoft.siipap.gastos.operaciones;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JRViewer;

import org.jdesktop.swingx.JXTaskPane;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.gastos.GasActions;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.Requisicion.Estado;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.matchers.FechaMayorAMatcher;
import com.luxsoft.siipap.swing.matchers.FechaMenorAMatcher;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.swing.views2.AbstractInternalTaskView;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;
import com.luxsoft.siipap.tesoreria.TesoreriaActions;
import com.luxsoft.siipap.tesoreria.movimientos.RequisicionForm;

public class RequisicionesDePagoView extends DefaultTaskView{
	
	private InternalTaskTab reqTab;
	
	
	public RequisicionesDePagoView(){
		
	}
	
	

	protected void instalarTaskElements(){
		final Action mostrarReqs=new AbstractAction("Requisiciones"){
			public void actionPerformed(ActionEvent e) {
				mostrarRequisiciones();				
			}
			
		};
		configAction(mostrarReqs,TesoreriaActions.ShowRequisicionesView.getId());
		consultas.add(mostrarReqs);
	}
	
	public void mostrarRequisiciones(){
		if(reqTab==null){
			RequiscionesGlobales view=new RequiscionesGlobales();
			view.setTitle("Requisiciones");
			reqTab=new InternalTaskTab(view);
		}
		reqTab.getTaskView().load();
		addTab(reqTab);
	}
	
	
	public static class RequiscionesGlobales extends AbstractInternalTaskView{
		
		RequisicionesBrowser browser;

		public JComponent getControl() {
			if(browser==null){
				browser=new RequisicionesBrowser();
			}
			return browser.getControl();
		}
		
		@Override
		public void instalOperacionesAction(JXTaskPane operaciones) {
			for(Action a:browser.getActions()){
				operaciones.add(a);
			}
			
		}
		
		@Override
		public void instalProcesosActions(JXTaskPane procesos) {
			procesos.add(browser.getRevisionAction());
			procesos.add(browser.getCancelarRevisionAction());
			procesos.add(browser.cerateRequisicionReport());
			procesos.add(browser.createRequisiscionDetReport());
			procesos.add(browser.getPeriodoLabel());
		}

		@Override
		public void installFiltrosPanel(JXTaskPane filtros) {
			filtros.add(browser.getFilterPanel());
		}

		@Override
		public void installDetallesPanel(JXTaskPane detalle) {
			detalle.add(browser.getTotalesPanel());
			detalle.setExpanded(true);
		}
		
		
		
	}
	
	@SuppressWarnings("unchecked")
	public static class RequisicionesBrowser extends FilteredBrowserPanel<Requisicion>{
		private Map<String, Object>parametros;
		final static String[] props={"id","afavor","fecha","total.amount","estado.name","origen"};
		final static String[] labels={"Id","A Favor","Fecha","Total","estado","Origen"};
		
		
		private Action revision;
		private Action cancelarRevision;
		public FechaMayorAMatcher fechaIniSelector;
		public FechaMenorAMatcher fechaFinSelector;
		
		public RequisicionesBrowser() {
			super(Requisicion.class);
			setProperties(props);
			setLabels(labels);
			installTextComponentMatcherEditor("Id", "id");
			installTextComponentMatcherEditor("A Favor", "afavor");
			installTextComponentMatcherEditor("Estado", "estado");
			installTextComponentMatcherEditor("Origen", "origen");
			fechaIniSelector=new FechaMayorAMatcher();
			fechaIniSelector.setDateField("fecha");
			fechaFinSelector=new FechaMenorAMatcher();
			fechaFinSelector.setDateField("fecha");
			installCustomMatcherEditor("F. Inicial", fechaIniSelector.getFechaField(), fechaIniSelector);
			installCustomMatcherEditor("F. Final", fechaFinSelector.getFechaField(), fechaFinSelector);
			parametros=new HashMap<String, Object>();
		}
		
		public Action getRevisionAction(){
			if(revision==null){
				revision=new AbstractAction("revision"){
					public void actionPerformed(ActionEvent e) {
						revision();
					}					
				};
				CommandUtils.configAction(revision, GasActions.RevisionDeRequisicion.getId(), null);
			}
			
			return revision;
		}
		public Action getCancelarRevisionAction(){
			if(cancelarRevision==null){
				cancelarRevision=new AbstractAction("cancelarRevision"){
					public void actionPerformed(ActionEvent e) {
						cancelarRevision();
					}					
				};
				CommandUtils.configAction(cancelarRevision, GasActions.CancelarRevisionDeRequisicion.getId(), null);
			}
			return cancelarRevision;
		}
		
		public boolean validarPeriodo(final Requisicion req){
			if(periodo.isBetween(req.getFecha())){
				return true;
			}else{
				MessageUtils.showMessage("La fecha de la requisicion no corresponde al periodo\n imposible actualizar", "Req erronea");
				return false;
			}

		}

		@Override
		public boolean doDelete(Requisicion bean) {
			try {
				if(bean.getEstado().equals(Estado.SOLICITADA)){					
					ServiceLocator2.getRequisiciionesManager().eliminarRequisicionAutomatica(bean.getId());
					return true;
				}
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		@Override
		protected Requisicion doInsert() {
			Requisicion req=RequisicionDeGastosForm.showForm();
			if(req!=null){
				req.setOrigen(Requisicion.GASTOS);				
				if(validarPeriodo(req)){
					Requisicion res= ServiceLocator2.getRequisiciionesManager().save(req);
					return res;
				}
				return null;
				
			}
			return null;
		}

		@Override
		protected Requisicion doEdit(Requisicion bean) {
			Requisicion target=ServiceLocator2.getRequisiciionesManager().get(bean.getId());
			boolean readOnly=!target.getEstado().equals(Estado.SOLICITADA);
			if(!readOnly){
				target=RequisicionForm.showForm(target,readOnly);
				if(target!=null){
					if(validarPeriodo(target)){
						target= ServiceLocator2.getRequisiciionesManager().save(target);
						return target;
					}
					return null;
				}
			}
			return null;
		}

		@Override
		protected void doSelect(Object bean) {
			Requisicion r=(Requisicion)bean;
			RequisicionForm.showForm(r,true);
		}

		
		private void revision(){
			if(!getSelected().isEmpty()){
				for(Object row:getSelected()){
					Requisicion r=(Requisicion)row;
					if(!validarPeriodo(r))
						return;
					Requisicion clone=ServiceLocator2.getRequisiciionesManager().get(r.getId());
					boolean res=clone.revision();
					if(res){
						clone=ServiceLocator2.getRequisiciionesManager().save(clone);
						if(clone!=null){
							int index=source.indexOf(r);
							source.set(index, clone);
						}
					}
					
				}
			}
		}
		
		
		private void cancelarRevision(){
			if(!getSelected().isEmpty()){
				for(Object row:getSelected()){
					Requisicion r=(Requisicion)row;
					if(!validarPeriodo(r))
						return;
					Requisicion clone=ServiceLocator2.getRequisiciionesManager().get(r.getId());
					boolean res=clone.cancelarRevision();
					if(res){
						clone=ServiceLocator2.getRequisiciionesManager().save(clone);
						if(clone!=null){
							int index=source.indexOf(r);
							source.set(index, clone);
						}
					}
					
				}
			}
		}
		
		@Override
		protected List<Requisicion> findData() {
			if(periodo==null){
				manejarPeriodo();
			}
			return ServiceLocator2.getRequisiciionesManager().buscarRequisicionesDeGastos(periodo);
		}
		
		
		
		@Override
		protected void executeLoadWorker(SwingWorker worker) {
			TaskUtils.executeSwingWorker(worker);
		}



		private JPanel totalPanel;
		private JLabel granTotal=new JLabel();
		private NumberFormat nf=NumberFormat.getCurrencyInstance(Locale.US);
		
		@SuppressWarnings("unchecked")
		public JPanel getTotalesPanel(){
			if(totalPanel==null){
				final FormLayout layout=new FormLayout("p,2dlu,f:max(100dlu;p):g","");
				DefaultFormBuilder builder=new DefaultFormBuilder(layout);
				granTotal.setHorizontalAlignment(SwingConstants.RIGHT);
				builder.append("Total",granTotal);
				totalPanel=builder.getPanel();
				totalPanel.setOpaque(false);
				getFilteredSource().addListEventListener(new TotalesHandler());
				
			}
			return totalPanel;
		}
		
		private class TotalesHandler implements ListEventListener{
			public void listChanged(ListEvent listChanges) {
				if(listChanges.next()){
					updateTotales();
				}
			}
			
			private void updateTotales(){
				BigDecimal tot=BigDecimal.ZERO;
				for(Object  r:getFilteredSource()){
					Requisicion c=(Requisicion)r;
					tot=tot.add(c.getTotal().multiply(c.getTipoDeCambio()).amount());
				}
				granTotal.setText(nf.format(tot.doubleValue()));
				
			}
			
		}
		
		private Action createRequisiscionDetReport(){
			AbstractAction a=new AbstractAction(){

				public void actionPerformed(ActionEvent e) {
					if(!selectionModel.getSelected().isEmpty()){
						Requisicion req=(Requisicion)selectionModel.getSelected().get(0);
						System.out.println("requisiscion "+req.getId());
						parametros.put("ID", req.getId());
						ReportUtils.viewReport(ReportUtils.toReportesPath("tesoreria/Requisicion.jasper"), getParametros());

					}
					
				}
				
			};
			a.putValue(Action.NAME,"Imprime Reporte Det");
			return a;
		}
		
		private Action cerateRequisicionReport(){
			AbstractAction a=new AbstractAction(){

				public void actionPerformed(ActionEvent e) {
							if(fechaIniSelector.getFechaField().getText().isEmpty() && fechaFinSelector.getFechaField().getText().isEmpty()){
								MessageUtils.showMessage("Debe Capturar las fechas para Mostrar El Reporte","Message..");
							}if(!fechaIniSelector.getFechaField().getText().isEmpty() && !fechaFinSelector.getFechaField().getText().isEmpty()){
							showRequisiscionReport req=new showRequisiscionReport();
							req.open();
							}
				}
				
			};
			a.putValue(Action.NAME, "Imprime Reporte Gral");
			return a;
		}
		public Map<String, Object> getParametros() {
			return parametros;
		}
		
		SimpleDateFormat format =new SimpleDateFormat("dd/MM/yyyy");
		
		private class showRequisiscionReport extends SXAbstractDialog{
			
			public showRequisiscionReport() {
				super("Reporte...");
			}

			public JComponent displayReport(){
				String user=KernellSecurity.instance().getCurrentUserName();
				
				try {
					
					Date fecha_ini = format.parse(fechaIniSelector.getFechaField().getText().toString());
					Date fecha_fin=format.parse(fechaFinSelector.getFechaField().getText().toString());
					parametros.put("FECHA_INI",fecha_ini);
	                parametros.put("FECHA_FIN",fecha_fin );
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				 
	                net.sf.jasperreports.engine.JasperPrint jasperPrint = null;
	                DefaultResourceLoader loader = new DefaultResourceLoader();
	                Resource res = loader.getResource(ReportUtils.toReportesPath("tesoreria/RequicisionGral.jasper"));
	                try
	                {
	                    java.io.InputStream io = res.getInputStream();
	                    try
	                    {
	                    	JTable table=getGrid();
	                        jasperPrint = JasperFillManager.fillReport(io, getParametros(), new JRTableModelDataSource(table.getModel()));
	                    }
	                    catch(JRException e)
	                    {
	                        e.printStackTrace();
	                    }
	                }
	                catch(IOException ioe)
	                {
	                    ioe.printStackTrace();
	                }
	                JRViewer jasperViewer = new JRViewer(jasperPrint);
	                jasperViewer.setPreferredSize(new Dimension(1000, 600));
	                return jasperViewer;

				}

			@Override
			protected JComponent buildContent() {
				return displayReport();
			}

			@Override
			protected void setResizable() {
			setResizable(true);
			}
			
		}
		
		
		
	}
	
	

	

}
