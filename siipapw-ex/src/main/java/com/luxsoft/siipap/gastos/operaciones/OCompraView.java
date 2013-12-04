package com.luxsoft.siipap.gastos.operaciones;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JRViewer;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.PieDataset;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.jfreechart.EventListPieDataset;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.gastos.GasActions;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GCompraRow;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.gastos.ComprasDeGastosManager;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.matchers.FechaMayorAMatcher;
import com.luxsoft.siipap.swing.matchers.FechaMenorAMatcher;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.views2.AbstractInternalTaskView;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;
import com.luxsoft.siipap.swing.views2.InternalTaskView;

public class OCompraView extends DefaultTaskView{
	
	private InternalTaskTab comprasTab;
	private InternalTaskTab resumenTab;
	private InternalTaskTab facturasTab;

	protected void instalarTaskElements(){
		
		final Action showCompras=new AbstractAction("Compras"){
			public void actionPerformed(ActionEvent e) {
				mostrarCompras();				
			}
			
		};
		configAction(showCompras, GasActions.showComprasView.getId());
		consultas.add(showCompras);
		
		final Action showFacturas=new AbstractAction("Facturas"){
			public void actionPerformed(ActionEvent e) {
				mostrarFacturas();
			}			
		};
		consultas.add(showFacturas);
		final Action showResumen=new AbstractAction("Resumen"){
			public void actionPerformed(ActionEvent e) {
				mostrarResumen();				
			}
			
		};
		configAction(showResumen, GasActions.showResumenDeGastos.getId());
		consultas.add(showResumen);
		
	}
	
	protected void instalarTaskPanels(final JXTaskPaneContainer container){
		this.taskContainer.remove(procesos); //We don't need this
		this.detalles.setTitle("Resumen");
		this.detalles.setExpanded(true);
	}
	
	private ComprasView comprasView;
	
	private void mostrarCompras(){
		if(logger.isDebugEnabled()){
			logger.debug("Mostrando la consulta de compras");
		}
		if(comprasTab==null){
			comprasView=new ComprasView();
			comprasView.setTitle("Gastos");
			comprasTab=new InternalTaskTab(comprasView);
		}
		addTab(comprasTab);
		
	}
	
	private void mostrarResumen(){
		if(logger.isDebugEnabled()){
			logger.debug("Mostrando la consulta de resumen");
		}
		if(comprasView==null) return;
		if(resumenTab==null){
			resumenTab=new InternalTaskTab(new ResumenDeGastos(comprasView.getFilterCompras()));
		}
		addTab(resumenTab);
		
	}
	
	private void mostrarFacturas(){
		if(facturasTab==null){			
			facturasTab=new InternalTaskTab(new FacturasTaskView());
			
		}
		addTab(facturasTab);
	}
	
	/**
	 * InternalTaskView para mostrar las compras asi como las tareas relevantes
	 * 
	 * 
	 * @author Ruben Cancino
	 *
	 */
	class ComprasView extends AbstractInternalTaskView{
		
		ComprasPanel browser;
		private NumberFormat nf=NumberFormat.getCurrencyInstance(Locale.US);

		public JComponent getControl() {
			browser=new ComprasPanel();
			return browser.getControl();
		}

		@Override
		public void instalOperacionesAction(JXTaskPane operaciones) {
			for(Action a:browser.getActions()){				
				operaciones.add(a);
			}
			operaciones.add(browser.getPeriodoLabel());
		}

		@Override
		public void installFiltrosPanel(JXTaskPane filtros) {			
			filtros.add(browser.getFilterPanel());
		}		
		@Override
		public void installDetallesPanel(JXTaskPane detalle) {
			detalle.add(getTotalesPanel());
		}

		@SuppressWarnings("unchecked")
		public EventList<GCompra> getEventList(){
			return browser.getSource();
		}
		
		
		private JLabel granTotal=new JLabel();
		private JPanel totalPanel;
		
		@SuppressWarnings("unchecked")
		private JPanel getTotalesPanel(){
			if(totalPanel==null){
				final FormLayout layout=new FormLayout("p,2dlu,f:max(100dlu;p):g","");
				DefaultFormBuilder builder=new DefaultFormBuilder(layout);
				granTotal.setHorizontalAlignment(SwingConstants.RIGHT);
				builder.append("Total",granTotal);
				totalPanel=builder.getPanel();
				totalPanel.setOpaque(false);
				browser.getFilteredSource().addListEventListener(new TotalesHandler());
				
			}
			return totalPanel;
		}
		
		@SuppressWarnings("unchecked")
		public EventList<GCompra> getFilterCompras(){
			return browser.getFilteredSource();
		}
		
		private class TotalesHandler implements ListEventListener{
			public void listChanged(ListEvent listChanges) {
				if(listChanges.next()){
					updateTotales();
				}
			}
			
			private void updateTotales(){
				BigDecimal tot=BigDecimal.ZERO;
				for(Object  r:browser.getFilteredSource()){
					GCompraRow c=(GCompraRow)r;
					tot=tot.add(c.getTotal());
				}
				granTotal.setText(nf.format(tot.doubleValue()));
				
			}
			
		}
		
		public  class ComprasPanel extends FilteredBrowserPanel<GCompraRow>{
			private Map<String, Object>parametros;
			
			final String[] props={"id","sucursal","departamento","proveedor","tipo","total","fecha","factura","year","mes"};
			final String[] names={"Id","Sucursal","Departamento","Proveedor","Tipo","Total","Fecha","Factura","Año","Mes"};
			
			private Action registrarFacturas;
			//private CheckBoxMatcher<GCompraRow> atendidasBox;
			//private CheckBoxMatcher<GCompraRow> porAtenderBox;
			private EstadoMatcherEditor estadoMatcher;
			private FechaMayorAMatcher fechaMayorEditor;
			private FechaMenorAMatcher fechaMenorEditor;

			@SuppressWarnings("unchecked")
			public ComprasPanel() {
				super(GCompraRow.class);
				addProperty(props);
				addLabels(names);
				parametros=new HashMap<String, Object>();
				/*
				atendidasBox=new CheckBoxMatcher<GCompraRow>(){
					@Override
					protected Matcher<GCompraRow> getSelectMatcher(Object... obj) {
						final Matcher<GCompraRow> matcher=new Matcher<GCompraRow>(){
							public boolean matches(GCompraRow item) {								
								return !StringUtils.isBlank(item.getFactura());
							}
						};
						return matcher;
					}
				};
				porAtenderBox=new CheckBoxMatcher<GCompraRow>(){
					@Override
					protected Matcher<GCompraRow> getSelectMatcher(Object... obj) {
						final Matcher<GCompraRow> matcher=new Matcher<GCompraRow>(){
							public boolean matches(GCompraRow item) {								
								return StringUtils.isBlank(item.getFactura());
							}
						};
						return matcher;
					}
				};
				*/
				
				fechaMayorEditor=new FechaMayorAMatcher();
				fechaMenorEditor=new FechaMenorAMatcher();
				installTextComponentMatcherEditor("Sucursal", "sucursal");
				installTextComponentMatcherEditor("Departamento", "departamento");
				installTextComponentMatcherEditor("Tipo", "tipo");
				installTextComponentMatcherEditor("Proveedor", "proveedor");
				installTextComponentMatcherEditor("CXPFactura", "factura");
				installTextComponentMatcherEditor("Mes", "mes");
				installTextComponentMatcherEditor("Id", "id");
				installCustomMatcherEditor("F Inicial", fechaMayorEditor.getFechaField(), fechaMayorEditor);
				installCustomMatcherEditor("F Final", fechaMenorEditor.getFechaField(), fechaMenorEditor);
				estadoMatcher=new EstadoMatcherEditor();
				installCustomMatcherEditor("Estado", estadoMatcher.getSelector(), estadoMatcher);
				//installCustomMatcherEditor("Por Atender", porAtenderBox.getBox(), porAtenderBox);
				//installCustomMatcherEditor("Atendidas", atendidasBox.getBox(), atendidasBox);
				
			}
			
			protected void manejarPeriodo(){
				periodo=Periodo.getPeriodoEnUnMes(new Date());
			}
			
			protected GCompraRow doInsert(){
				GCompra bean=new GCompra();
				bean=OCompraForm.showForm(bean);				
				if(bean!=null){
					if(!validarCompra(bean))
						return null;
					GCompra res= getManager().save(bean);
					return new GCompraRow(res);
				}
				return null;
			}

			@Override
			protected void doSelect(Object obj) {
				GCompraRow bean=(GCompraRow)obj;
				GCompra c=getManager().get(bean.getId());
				if(c.getId()==null) return;				
				c=getManager().get(c.getId());
				OCompraForm.showForm(c,true);
			}

			@Override
			public boolean doDelete(GCompraRow bean) {
				if(!periodo.isBetween(bean.getFecha()))
					return false;
				try {
					ServiceLocator2.getGCompraDao().remove(bean.getId());
					return true;
				} catch (Exception e) {
					MessageUtils.showError("Error al eliminar compra",e);
					return false;
				}
			}

			@Override
			protected GCompraRow doEdit(final GCompraRow bean) {				
				GCompra c=getManager().get(bean.getId());
				if(!validarCompra(c))
					return bean;
				c=OCompraForm.showForm(c);
				if(c!=null){
					
					GCompra res=getManager().save(c);
					return new GCompraRow(res);
				}
				return bean;
			}
			
			
		
			@SuppressWarnings("unchecked")
			@Override
			public Action[] getActions() {				
				if(actions==null)
					actions=new Action[]{
						getLoadAction()
						,getInsertAction()
						,getDeleteAction()
						,getEditAction()
						,getViewAction()
						,getRegistrarFacturas()
						,createActionReport()
						,createActionReportTwo()
						,addAction(GasActions.RegistrarFacturas.getId(), "copiar", "Copiar")
						};
				return actions;
			}

			public Action getRegistrarFacturas(){
				if(registrarFacturas==null){
					registrarFacturas=new AbstractAction("generarRequisicion"){
						public void actionPerformed(ActionEvent e) {
							registrarFactura();
						}
					};
				} 
				configAction(registrarFacturas,GasActions.RegistrarFacturas.getId());
				return registrarFacturas;
			}
			
			/**
			 * Genera una requisicion a partir de ordenes de compra si facturas asociadas
			 *
			 */
			@SuppressWarnings("unchecked")
			private void registrarFactura(){
				if(!selectionModel.isSelectionEmpty()){
					final List<GCompraRow> selection=new ArrayList<GCompraRow>();
					selection.addAll(selectionModel.getSelected());
					
					for(int index=0;index<selection.size(); index++){
						
						GCompraRow rr=selection.get(index);
						//if(StringUtils.isEmpty(rr.getFactura()))
							//continue;
						GCompra c=getManager().get(rr.getId());
						c=RecepcionDeFacturasForm.showForm(c);
						if(c!=null){
							c=ServiceLocator2.getComprasDeGastosManager().save(c);
							int row=source.indexOf(rr);
							rr=new GCompraRow(c);   //Update grid
							source.set(row, rr);
							
						}
					}					
				}
			}
			
			@Override
			protected List<GCompraRow> findData() {
				if(periodo==null)
					manejarPeriodo();
				return getManager().buscarComprasRow(periodo);
			}

			private ComprasDeGastosManager getManager(){
				return ServiceLocator2.getComprasDeGastosManager();
			}
			
			public void copiar(){
				GCompraRow row=(GCompraRow)getSelectedObject();
				if(row!=null){
					GCompra source=getManager().get(row.getId());
					GCompra target=new GCompra();
					BeanUtils.copyProperties(source, target,new String[]{"id","version","facturas","partidas"});
					for(GCompraDet det:source.getPartidas()){
						GCompraDet detTarget=new GCompraDet();
						BeanUtils.copyProperties(det, detTarget,new String[]{"id","version"});
						target.agregarPartida(detTarget);
					}
					target.setComentario("Copia de la compra: "+row.getId());
					target.actualizarTotal();
					target=getManager().save(target);
					GCompraRow newRow=new GCompraRow(target);
					this.source.add(newRow);
					//doEdit(newRow);
				}
			}


			public Map<String, Object> getParametros() {
				return parametros;
			}
			
			private Action createActionReport(){
				AbstractAction a=new AbstractAction(){

					public void actionPerformed(ActionEvent arg0) {
						if(fechaMayorEditor.getFechaField().getText().isEmpty() && fechaMenorEditor.getFechaField().getText().isEmpty()){
							MessageUtils.showMessage("Debe Capturar las Fechas para ejecutar elm Reporte", "Message");
						}
						if(!fechaMayorEditor.getFechaField().getText().isEmpty() && !fechaMenorEditor.getFechaField().getText().isEmpty()){
							showReport cmp=new showReport();
							cmp.open();
						}
						
					}
					
				};
				a.putValue(Action.NAME, "Generar Reporte Gral");
				
				return a;
			}
			
			
			private boolean validarCompra(GCompra compra){
				if(periodo.isBetween(compra.getFecha()))
					return true;
				else{
					MessageUtils.showMessage("La compra no corresponde al periodo, imposible actualizar", "Manejo de Compras");
					return false;
				}
			}
			
			
			private class showReport extends SXAbstractDialog{
				
				public showReport() {
					super("Reporte...");
				}

				public JComponent displayReport(){
					  parametros.put("FILTRO",estadoMatcher.getSelector().getSelectedItem().toString() );
					  parametros.put("FECHA_INI", fechaMayorEditor.getFechaField().getText().toString());
					  parametros.put("FECHA_FIN", fechaMenorEditor.getFechaField().getText().toString());
		                net.sf.jasperreports.engine.JasperPrint jasperPrint = null;
		                DefaultResourceLoader loader = new DefaultResourceLoader();
		                Resource res = loader.getResource(ReportUtils.toReportesPath("gastos/GastosCompras.jasper"));
		                try
		                {
		                    java.io.InputStream io = res.getInputStream();
		                    try
		                    {
		                    	JTable table=browser.getGrid();
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
			
			private Action createActionReportTwo(){
				AbstractAction c=new AbstractAction(){
					public void actionPerformed(ActionEvent arg0) {
						if(!selectionModel.getSelected().isEmpty()){
							GCompraRow g=(GCompraRow)selectionModel.getSelected().get(0);
							parametros.put("ID",g.getId());
							parametros.put("HECHO",KernellSecurity.instance().getCurrentUserName());
							String path=ReportUtils.toReportesPath("gastos/GastosComprasDet.jasper");
							ReportUtils.viewReport(path, getParametros());
						}
					}
					
				};
				c.putValue(Action.NAME, "Generar Reporte Det");
				return c;
			}

			
		}		
	}
	
	
	private class FacturasTaskView extends AbstractInternalTaskView{
		Map<String, Object> parametros;
		private NumberFormat nf=NumberFormat.getCurrencyInstance(Locale.US);
		private  FacturasView browser;
		private JLabel granTotal=new JLabel();
		private JPanel totalPanel;
		
		public FacturasTaskView(){
			parametros=new HashMap<String, Object>();
			browser=new FacturasView();
			setTitle("Facturas");
		}

		public JComponent getControl() {
			return browser.getControl();
		}
		
		@Override
		public void instalOperacionesAction(JXTaskPane operaciones) {
			
			for(Action a:browser.getActions()){				
				operaciones.add(a);
			}
			operaciones.add(browser.getPeriodoLabel());
			operaciones.add(createActionReport());
		}

		@Override
		public void installFiltrosPanel(JXTaskPane filtros) {			
			filtros.add(browser.getFilterPanel());
		}		
		@Override
		public void installDetallesPanel(JXTaskPane detalle) {
			detalle.add(getTotalesPanel());
		}

		@SuppressWarnings("unchecked")
		public EventList<GCompra> getEventList(){
			return browser.getSource();
		}
		
		
		@SuppressWarnings("unchecked")
		private JPanel getTotalesPanel(){
			if(totalPanel==null){
				final FormLayout layout=new FormLayout("p,2dlu,f:max(100dlu;p):g","");
				DefaultFormBuilder builder=new DefaultFormBuilder(layout);
				granTotal.setHorizontalAlignment(SwingConstants.RIGHT);
				builder.append("Total",granTotal);
				totalPanel=builder.getPanel();
				totalPanel.setOpaque(false);
				browser.getFilteredSource().addListEventListener(new TotalesHandler());
				
			}
			return totalPanel;
		}
		
		@SuppressWarnings("unchecked")
		public EventList<GCompra> getFilterCompras(){
			return browser.getFilteredSource();
		}
		
		private class TotalesHandler implements ListEventListener{
			public void listChanged(ListEvent listChanges) {
				if(listChanges.next()){
					updateTotales();
				}
			}
			
			private void updateTotales(){
				BigDecimal tot=BigDecimal.ZERO;
				for(Object  r:browser.getFilteredSource()){
					GFacturaPorCompra c=(GFacturaPorCompra)r;
					tot=tot.add(c.getTotatMN().amount());
				}
				granTotal.setText(nf.format(tot.doubleValue()));
				
			}
			
		}
		
		
		private Action createActionReport(){
			AbstractAction a=new AbstractAction(){

				public void actionPerformed(ActionEvent arg0) {
					if(browser.fechaInicialSelector.getFechaField().getText().isEmpty() && browser.fechaFinalSelector.getFechaField().getText().isEmpty()){
						MessageUtils.showMessage("debe Capturar las fechas para ejecutar el reporte", "Message");
					}
					if(!browser.fechaInicialSelector.getFechaField().getText().isEmpty() && !browser.fechaFinalSelector.getFechaField().getText().isEmpty()){
						showReport sr=new showReport();
						sr.open();
					}
				
				}
				
			};
			a.putValue(Action.NAME, "Imprimir Reporte");
			return a;
		}
		
	private class showReport extends SXAbstractDialog{
			
			public showReport() {
				super("Reporte...");
			}

			public JComponent displayReport(){
					parametros.put("FECHA_INI", browser.fechaInicialSelector.getFechaField().getText());
	                parametros.put("FECHA_FIN", browser.fechaFinalSelector.getFechaField().getText());
	                
	                parametros.put("FILTRO",browser.getEdoMatcherEditor().getSelector().getSelectedItem().toString());
	                net.sf.jasperreports.engine.JasperPrint jasperPrint = null;
	                DefaultResourceLoader loader = new DefaultResourceLoader();
	                Resource res = loader.getResource(ReportUtils.toReportesPath("gastos/GastosFacturas.jasper"));
	                try
	                {
	                    java.io.InputStream io = res.getInputStream();
	                    try
	                    {
	                    	JTable table=browser.getGrid();
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

	public Map<String, Object> getParametros() {
		return parametros;
	}
		
		
		
	}
	
	/**
	 * {@link InternalTaskView} para mostrar resumen de los gastos por diversos
	 * conceptos
	 * 
	 * @author Ruben Cancino
	 *
	 */
	@SuppressWarnings("unchecked")
	public static class ResumenDeGastos extends AbstractInternalTaskView{
		
		/**
		 * La lista de los gastos a resumir
		 * 
		 */
		private final EventList<GCompra> source;
		private GroupingList<GCompra> porSucursal;
		private GroupingList<GCompra> porDepartamento;
		private GroupingList<GCompra> porYear;
		private GroupingList<GCompra> porMes;
		
		public ResumenDeGastos(final EventList<GCompra> source){
			this.source=source;
			setTitle("Grafícas");
		}

		public JComponent getControl() {
			final JPanel panel=new JPanel(new BorderLayout());
			panel.add(new Header("Análisis general","Resumen de gastos por diversos conceptos").getHeader(),BorderLayout.NORTH);
			panel.add(buildGraphView(),BorderLayout.CENTER);
			return panel;
		}
		
		private JComponent buildGraphView(){
			
			final FormLayout layout=new FormLayout("p:g(.5),2dlu,p:g(.5)"
					,"t:p:g(.5),3dlu,t:p:g(.5)");
			PanelBuilder builder=new PanelBuilder(layout);
			CellConstraints cc=new CellConstraints();
			builder.add(buildPorSucursal(),cc.xy(1, 1));
			builder.add(buildPorDepartamento(),cc.xy(3, 1));
			builder.add(buildPorYear(),cc.xy(1, 3));
			builder.add(buildPorMes(),cc.xy(3, 3));
			return builder.getPanel();
		}		
		
		private JComponent buildPorSucursal(){
			porSucursal=new GroupingList<GCompra>(source,GlazedLists.beanPropertyComparator(GCompra.class, "sucursal.nombre"));
			
			final FunctionList.Function keyFunction=new FunctionList.Function(){
				
				public Object evaluate(Object sourceValue) {
					List<GCompra> compras=(List<GCompra>)sourceValue;
					return compras.get(0).getSucursal().getNombre();
				}				
			};
			
			final FunctionList.Function valueFunction=new FunctionList.Function(){

				public Object evaluate(Object sourceValue) {
					List<GCompra> compras=(List<GCompra>)sourceValue;
					BigDecimal tot=BigDecimal.ZERO;
					for(GCompra c:compras){
						tot=tot.add(c.getTotal());
					}
					return tot;
				}				
			};
			
			final PieDataset dataSet=new EventListPieDataset(porSucursal,keyFunction,valueFunction);
			final JFreeChart chart=ChartFactory.createPieChart3D("X Sucursal", dataSet, true, true, false);
			final ChartPanel panel=new ChartPanel(chart,true);
			panel.setPreferredSize(new Dimension(300,300));
			return panel;
		}
		
		
		private JComponent buildPorDepartamento(){
			porDepartamento=new GroupingList<GCompra>(source,GlazedLists.beanPropertyComparator(GCompra.class, "departamento.clave"));
			
			final FunctionList.Function keyFunction=new FunctionList.Function(){
				public Object evaluate(Object sourceValue) {
					List<GCompra> compras=(List<GCompra>)sourceValue;
					return compras.get(0).getDepartamento().getClave();
				}				
			};
			
			final FunctionList.Function valueFunction=new FunctionList.Function(){

				public Object evaluate(Object sourceValue) {
					List<GCompra> compras=(List<GCompra>)sourceValue;
					BigDecimal tot=BigDecimal.ZERO;
					for(GCompra c:compras){
						tot=tot.add(c.getTotal());
					}
					return tot;
				}				
			};
			
			final PieDataset dataSet=new EventListPieDataset(porDepartamento,keyFunction,valueFunction);
			
			final JFreeChart chart=ChartFactory.createPieChart3D("X Departamento", dataSet, true, true, false);
			final ChartPanel panel=new ChartPanel(chart,true);
			panel.setPreferredSize(new Dimension(300,300));
			return panel;
		}		
		
		private JComponent buildPorYear(){
			porYear=new GroupingList<GCompra>(source,GlazedLists.beanPropertyComparator(GCompra.class, "year"));
			
			final FunctionList.Function keyFunction=new FunctionList.Function(){
				public Object evaluate(Object sourceValue) {
					List<GCompra> compras=(List<GCompra>)sourceValue;
					return compras.get(0).getYear();
				}				
			};
			
			final FunctionList.Function valueFunction=new FunctionList.Function(){

				public Object evaluate(Object sourceValue) {
					List<GCompra> compras=(List<GCompra>)sourceValue;
					BigDecimal tot=BigDecimal.ZERO;
					for(GCompra c:compras){
						tot=tot.add(c.getTotal());
					}
					return tot;
				}				
			};
			
			final PieDataset dataSet=new EventListPieDataset(porYear,keyFunction,valueFunction);
			
			final JFreeChart chart=ChartFactory.createPieChart3D("X Año", dataSet, true, true, false);
			
			final ChartPanel panel=new ChartPanel(chart,true);
			panel.setPreferredSize(new Dimension(300,300));
			return panel;
		}
		
		private JComponent buildPorMes(){
			porMes=new GroupingList<GCompra>(source,GlazedLists.beanPropertyComparator(GCompra.class, "mes"));
			
			final FunctionList.Function keyFunction=new FunctionList.Function(){
				public Object evaluate(Object sourceValue) {
					List<GCompra> compras=(List<GCompra>)sourceValue;
					return compras.get(0).getMes();
				}				
			};
			
			final FunctionList.Function valueFunction=new FunctionList.Function(){

				public Object evaluate(Object sourceValue) {
					List<GCompra> compras=(List<GCompra>)sourceValue;
					BigDecimal tot=BigDecimal.ZERO;
					for(GCompra c:compras){
						tot=tot.add(c.getTotal());
					}
					return tot;
				}				
			};
			
			final PieDataset dataSet=new EventListPieDataset(porMes,keyFunction,valueFunction);
			
			final JFreeChart chart=ChartFactory.createPieChart3D("X Mes", dataSet, true, true, false);
			final ChartPanel panel=new ChartPanel(chart,true);
			panel.setPreferredSize(new Dimension(300,300));
			return panel;
		}
		
	}
	 
	
	private class EstadoMatcherEditor extends AbstractMatcherEditor<GCompraRow> implements ActionListener{
		
		private JComboBox selector;
		
		public EstadoMatcherEditor(){
			String[] vals={"Todas","Por Atender","Atenidas"};
			selector=new JComboBox(vals);
			selector.addActionListener(this);
		}
		
		public JComboBox getSelector(){
			return selector;
		}


		public void actionPerformed(ActionEvent e) {
			String val=selector.getSelectedItem().toString();
			if("Por Atender".equalsIgnoreCase(val)){
				fireChanged(new Matcher<GCompraRow>(){
					public boolean matches(GCompraRow item) {
						return StringUtils.isBlank(item.getFactura());
					}					
				});
			}else if("Atenidas".equalsIgnoreCase(val)){
				fireChanged(new Matcher<GCompraRow>(){
					public boolean matches(GCompraRow item) {
						return !StringUtils.isBlank(item.getFactura());
					}					
				});
			}
			else
				fireMatchAll();
				
			
		}
		
		class AtendidasMatcher implements Matcher<GCompraRow> {
			public boolean matches(GCompraRow item) {								
				return !StringUtils.isBlank(item.getFactura());
			}
		};
		
		
				
		
	}
	
}
