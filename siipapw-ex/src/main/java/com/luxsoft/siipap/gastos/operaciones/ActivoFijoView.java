package com.luxsoft.siipap.gastos.operaciones;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JRViewer;

import org.hibernate.validator.NotNull;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.beans.Model;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.dao.gastos.ActivoFijoDao;
import com.luxsoft.siipap.gastos.GasActions;
import com.luxsoft.siipap.gastos.selectores.SelectorDeFacturasDeGastos;
import com.luxsoft.siipap.model.gastos.ActivoFijo;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.INPC;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.matchers.FechaMayorAMatcher;
import com.luxsoft.siipap.swing.matchers.FechaMenorAMatcher;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.FormatUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.views2.AbstractInternalTaskView;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;
import com.luxsoft.siipap.swing.views2.InternalTaskView;

public class ActivoFijoView extends DefaultTaskView{
	
	private InternalTaskTab fiscalContableTab;
	private InternalTaskTab resumenTab;
	private ContableFiscalView contableView;
	private final EventList<ActivoFijo> source;
	
	public ActivoFijoView(){
		source=GlazedLists.threadSafeList(new BasicEventList<ActivoFijo>());
	}

	protected void instalarTaskElements(){
		
		final Action showCompras=new AbstractAction("Activos"){
			public void actionPerformed(ActionEvent e) {
				mostrarContableFiscal();				
			}
			
		};
		configAction(showCompras, GasActions.ShowActivoFijoFiscalView.getId());
		consultas.add(showCompras);
		
		final Action showResumen=new AbstractAction("Resumen"){
			public void actionPerformed(ActionEvent e) {
				mostrarResumen();				
			}
			
		};
		configAction(showResumen, GasActions.ShowActivoFijoResumen1.getId());
		consultas.add(showResumen);
		
	}
	
	protected void instalarTaskPanels(final JXTaskPaneContainer container){
		this.taskContainer.remove(procesos); //We don't need this
		this.detalles.setTitle("Resumen");
		this.detalles.setExpanded(true);
	}
	
	
	
	private void mostrarContableFiscal(){		
		if(fiscalContableTab==null){
			contableView=new ContableFiscalView();
			contableView.setTitle("Contableº");
			fiscalContableTab=new InternalTaskTab(contableView);
		}
		addTab(fiscalContableTab);		
	}
	
	private void mostrarResumen(){		
		if(contableView==null) return;
		if(resumenTab==null){
			resumenTab=new InternalTaskTab(new ResumenDeActivos(source));
		}
		addTab(resumenTab);		
	}
	
	/**
	 * 
	 * InternalTaskView para mostrar los activos con datos Contables-Fiscales
	 * 
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private  class ContableFiscalView extends AbstractInternalTaskView{
		
		private ActivosPanel browser;
		private JLabel totalMOI=new JLabel();
		private JLabel actualizacionDate=new JLabel();
		private JPanel totalPanel;
		private NumberFormat nf=NumberFormat.getCurrencyInstance(Locale.US);

		public JComponent getControl() {
			browser=new ActivosPanel(source);
			return browser.getControl();
		}

		@Override
		public void instalOperacionesAction(JXTaskPane operaciones) {
			for(Action a:browser.getActions()){				
				operaciones.add(a);
			}
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
		private JPanel getTotalesPanel(){
			if(totalPanel==null){
				final FormLayout layout=new FormLayout("p,2dlu,f:max(100dlu;p):g","");
				DefaultFormBuilder builder=new DefaultFormBuilder(layout);
				totalMOI.setHorizontalAlignment(SwingConstants.RIGHT);
				actualizacionDate.setHorizontalAlignment(SwingConstants.RIGHT);
				builder.append("Actualización",actualizacionDate);
				builder.append("Total MOI",totalMOI);
				totalPanel=builder.getPanel();
				totalPanel.setOpaque(false);
				browser.getFilteredSource().addListEventListener(new TotalesHandler());
				
			}
			return totalPanel;
		}
		
		
		
		/**
		 * Handler para actualizar los totales 
		 * 
		 * TODO: Crear un Panel parametrizable para 
		 *  
		 * @author Ruben Cancino
		 *
		 */
		private class TotalesHandler implements ListEventListener{
			public void listChanged(ListEvent listChanges) {
				if(listChanges.next()){
					updateTotales();
				}
			}
			
			private void updateTotales(){
				BigDecimal tot=BigDecimal.ZERO;
				for(Object  r:browser.getFilteredSource()){
					ActivoFijo c=(ActivoFijo)r;
					tot=tot.add(c.getMoi());
				}
				totalMOI.setText(nf.format(tot.doubleValue()));
				if(!source.isEmpty()){
					ActivoFijo af=(ActivoFijo)source.get(0);
					actualizacionDate.setText(new SimpleDateFormat("MM/yyyy").format(af.getFechaActualizacion()));
				}
			}
			
		}
		
		public  class ActivosPanel extends FilteredBrowserPanel<ActivoFijo>{
			
			final String[] props={
					"id"
					,"rubro.descripcion"
					,"documento"
					,"producto.descripcion"
					,"moi"
					,"fechaDeAdquisicion"
					,"depreciacionInicial"
					,"remanenteInicial"
					,"ultimaFechaActualizable"
					,"tasaDepreciacion"
					,"depreciacionDelEjercicio"
					,"depreciacionAcumulada"
					,"remanente"
					,"ultimoINPC.indice"
					,"inpcOriginal.indice"
					,"factorDeActualizacion"
					,"depreciacionActualizada"
					};
			
			final String[] names={
					"Id"
					,"Concepto"
					,"Docto"
					,"Descripcion"
					,"MOI"
					,"Fecha (Adq)"
					,"Depreciacio Acu Ant"
					,"Remanente"
					,"Fecha (Act)"
					,"Tasa"
					,"Dep (Ejercicio)"
					,"Dep (Acu)"
					,"Saldo"
					,"INPC (Ultimo)"
					,"INPC (Original)"
					,"Factor"
					,"Dep Actual"
					};
			
			private Action registroManual;
			private Action registroPorCompra;
			private Action registroPorProveedor;
			private Action enajenar;
			private Action actualizar;
			public FechaMayorAMatcher fechaInicialSelector;
			public FechaMenorAMatcher fechaFinalSelector;

			@SuppressWarnings("unchecked")
			public ActivosPanel(final EventList<ActivoFijo> source) {
				super(ActivoFijo.class);
				setSource(source);
				initActions();
				addProperty(props);
				addLabels(names);
				fechaInicialSelector=new FechaMayorAMatcher();
				fechaInicialSelector.setDateField("fechaDeAdquisicion");
				fechaFinalSelector=new FechaMenorAMatcher();
				fechaFinalSelector.setDateField("fechaDeAdquisicion");
				installTextComponentMatcherEditor("Concepto", "rubro.descripcion");
				installTextComponentMatcherEditor("Sucursal", "sucursal");
				installTextComponentMatcherEditor("Departamento", "departamento");
				installTextComponentMatcherEditor("Descripción", "producto.descripcion");
				installTextComponentMatcherEditor("Documento", "documento");
				installCustomMatcherEditor("F Inicial", fechaInicialSelector.getFechaField(), fechaInicialSelector);
				installCustomMatcherEditor("F Final", fechaFinalSelector.getFechaField(), fechaFinalSelector);

			}
			
			private void initActions(){				
				registroManual=new ForwardAction(0);
				registroPorCompra=new ForwardAction(1);
				registroPorProveedor=new ForwardAction(2);
				enajenar=new DispatchingAction(this,"enajenar");
				actualizar=new DispatchingAction(this,"actualizar");
				configAction(registroManual, GasActions.RegistrarActivoManual.getId());
				configAction(registroPorProveedor, GasActions.RegistrarActivoPorProveedor.getId());
				configAction(registroPorCompra, GasActions.RegistrarActivoPorCompra.getId());
				configAction(enajenar,GasActions.EnajenarActivo.getId());
				configAction(actualizar, GasActions.ActualizarFiscal.getId());
			}
			
			
			

			

			@Override
			public Action[] getActions() {				
				return new  Action[]{getLoadAction(),registroManual,registroPorProveedor,registroPorCompra,enajenar,actualizar
						,getDeleteAction(),getEditAction(),getViewAction(),createActionActivoReport(),createActionGActivoReport()};
			}
			

			@SuppressWarnings("unchecked")
			public ActivoFijo doInsert(final int tipo){
				Object bean=null;
				switch (tipo) {
				case 0:
					bean=doInsertManual();
					break;
				case 1:
					bean= doInsertPorCompra();
					break;
				case 2:
					
					break;
				}				
				if(bean!=null){
					source.add(bean);
					grid.packAll();
				}
				return (ActivoFijo)bean;
				
			}
			
			private ActivoFijo doInsertManual(){
				ActivoFijo af=AF_ManualForm.showForm(new ActivoFijo());
				if(af!=null){
					af=getDao().save(af);
					return af;
				}
				return null;
			}
			private ActivoFijo doInsertPorCompra(){
				GCompraDet gasto=SelectorDeFacturasDeGastos.buscar();
				if(gasto!=null && gasto.getFacturacion()==null){
					MessageUtils.showMessage("Gasto sin facturar", "Activo fijo");
					return null;
				}
				ActivoFijo af=RegistroDeActivoPorCompraForm.registrarActivo(gasto);
				if(af!=null){
					af.actualizar();
					return ServiceLocator2.getActivoFijoManager().salvar(af);
				}
				return null;
			}			
			
			@Override
			public void open() {
				load();
			}
			@SuppressWarnings("unchecked")
			public void actualizar(){
				final ActualizacionFiscal act=new ActualizacionFiscal();
				final DefaultFormModel model=new DefaultFormModel(act);
				final ActualizacionFiscalForm form=new ActualizacionFiscalForm(model);
				form.actualizarInidice(act.getFecha());
				form.open();
				if(!form.hasBeenCanceled()){
					//getControl()ac
					if(source.isEmpty()){
						source.getReadWriteLock().readLock().lock();
						source.addAll(ServiceLocator2.getActivoFijoManager().buscarTodos());
						source.getReadWriteLock().readLock().unlock();
					}
					for(int index=0;index<source.size();index++){
						ActivoFijo af=(ActivoFijo)source.get(index);
						af.setFechaActualizacion(act.getFecha());
						af.setUltimoINPC(act.getIndice());
						af.actualizar();
						af=ServiceLocator2.getActivoFijoManager().salvar(af);
						//getDao().save(af);
						source.set(index, af);
					}
					//load();
				}
			}
			
			/*
			private void actualizarActivos(final Date fecha){
				for(int index=0;index<source.size();index++){
					ActivoFijo af=(ActivoFijo)source.get(index);
					af.setFechaActualizacion(act.getFecha());
					af.setUltimoINPC(act.getIndice());						
					//getDao().save(af);
					source.set(index, af);
				}
				load();
			}
			*/
			/***
			 * Acciones y Reportes de Activo Fijo (General y Detalle)
			 * 
			 * @return
			 */
			private Action createActionActivoReport(){
				AbstractAction a=new AbstractAction(){
					public void actionPerformed(ActionEvent arg0) {
						Map<String, Object> parametros=new HashMap<String, Object>();
						if(!getSelected().isEmpty()){
							ActivoFijo sel=(ActivoFijo)getSelected().get(0);
							parametros.put("ACTIVO_ID", sel.getId());
							String path=ReportUtils.toReportesPath("ActivoFijoDetalle.jasper");
							ReportUtils.viewReport(path,parametros);
						}
						
						
						
					}
					
				};
				
				a.putValue(Action.NAME, "Imprime Detalle");
				return a;
			}
			
			
			private Action createActionGActivoReport(){
				AbstractAction a=new AbstractAction(){

					public void actionPerformed(ActionEvent e) {
						if(browser.fechaInicialSelector.getFechaField().getText().isEmpty() && browser.fechaFinalSelector.getFechaField().getText().isEmpty()){
							MessageUtils.showMessage("Los Campos de fecha no deben estar vacios", "Message..");
						}
						if(!browser.fechaInicialSelector.getFechaField().getText().isEmpty() && !browser.fechaFinalSelector.getFechaField().getText().isEmpty()){
							showReportGral c=new showReportGral();
							c.open();
							
						}
							}
				};
				a.putValue(Action.NAME, "Imprime General");
				return a;
			}
			
			private class showReportGral extends SXAbstractDialog{
				
				public showReportGral() {
					super("Reporte...");
				}

				public JComponent displayReport(){
					Map<String, Object>parametros=new HashMap<String, Object>();
						parametros.put("FECHA_INI",browser.fechaInicialSelector.getFechaField().getText().toString());
		                parametros.put("FECHA_FIN",browser.fechaFinalSelector.getFechaField().getText().toString());
		                
		                net.sf.jasperreports.engine.JasperPrint jasperPrint = null;
		                DefaultResourceLoader loader = new DefaultResourceLoader();
		                Resource res = loader.getResource(ReportUtils.toReportesPath("ActivoFijo_Contable.jasper"));
		                try
		                {
		                    java.io.InputStream io = res.getInputStream();
		                    try
		                    {
		                    	JTable table=browser.getGrid();
		                        jasperPrint = JasperFillManager.fillReport(io, parametros, new JRTableModelDataSource(table.getModel()));
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
			

			@Override
			protected void doSelect(Object bean) {				
				AF_ManualForm.showForm((ActivoFijo)bean,true);
			}

			@Override
			public boolean doDelete(ActivoFijo bean) {
				try {
					getDao().remove(bean.getId());
					return true;
				} catch (Exception e) {
					MessageUtils.showError("Error al eliminar Activo Fijo",e);
					return false;
				}
				
			}

			@Override
			protected ActivoFijo doEdit(final ActivoFijo bean) {				
				ActivoFijo af=getDao().get(bean.getId());
				
				af=AF_ManualForm.showForm(af, false);
				if(af!=null){					
					return getDao().save(af);
				}
				return af;
			}
			
			
			private ActivoFijoDao getDao(){
				return (ActivoFijoDao)ServiceLocator2.instance().getContext().getBean("activoFijoDao");
			}
			
			private void reporte1(){
				ActivoFijo selected=(ActivoFijo)getSelected();
				
			}
			
			
			public class ForwardAction extends AbstractAction{
								
				private final int id;
				
				public ForwardAction(final int id){
					this.id=id;
				}

				public void actionPerformed(ActionEvent e) {
					doInsert(id);
				}
				
			}
			
		}
		
	}
	
	/**
	 * Forma para registrar parametros de actualizacion fiscal
	 * 
	 * @author Ruben Cancino
	 *
	 */
	public static class ActualizacionFiscalForm extends AbstractForm{		

		public ActualizacionFiscalForm(IFormModel model) {
			super(model);
			model.getModel("fecha").addValueChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					Date fecha=(Date)evt.getNewValue();
					actualizarInidice(fecha);
				}
			});
		}
		
		public void actualizarInidice(Date fecha){
			INPC i=ServiceLocator2.getActivoFijoManager().buscarIndice(fecha);
			getModel().setValue("indice", i);
		}

		@Override
		protected JComponent buildFormPanel() {
			FormLayout layout=new FormLayout("p,3dlu,max(p;80dlu):g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			//getControl("fecha").setEnabled(false);
			builder.append("Fecha",getControl("fecha"));
			builder.append("Indice",addReadOnly("indice"));
			return builder.getPanel();
		}

		@Override
		protected JComponent createCustomComponent(String property) {
			if("indice".equals(property)){
				return BasicComponentFactory.createLabel(model.getModel(property),FormatUtils.getToStringFormat());
				/*INPCControl c=new INPCControl(model.getModel(property));
				c.setEnabled(!model.isReadOnly());
				return c;
				*/
			}
			return null;
		}		
		
	}
	
	/**
	 * JavaBean para mantener los parametros de una actualización fiscal
	 *  
	 * 
	 * @author Ruben Cancino
	 *
	 */
	public static class ActualizacionFiscal extends Model{
		
		@NotNull(message="No existe INPC para la fecha indicada")
		private INPC indice;
		
		@NotNull
		private Date fecha=new Date();
		
		public Date getFecha() {
			return fecha;
		}
		public void setFecha(Date fecha) {
			Object old=this.fecha;
			this.fecha = fecha;
			firePropertyChange("fecha", old, fecha);
		}
		
		public INPC getIndice() {
			return indice;
		}
		public void setIndice(INPC indice) {
			Object old=this.indice;
			this.indice = indice;
			firePropertyChange("indice", old, indice);
			//fixDate();
		}		
		
		protected void fixDate(){
			Calendar c=Calendar.getInstance();
			c.set(Calendar.YEAR, getIndice().getYear());
			c.set(Calendar.MONTH, getIndice().getMes()-1);
			c.getTime();
			c.set(Calendar.DATE,c.getMaximum(Calendar.DATE));
			//c.set(Calendar.DATE, )
			setFecha(c.getTime());
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
	public static class ResumenDeActivos extends AbstractInternalTaskView{
		
		/**
		 * La lista de los gastos a resumir
		 * 
		 */
		private final EventList<ActivoFijo> source;
		private GroupingList<ActivoFijo> porSucursal;
		private GroupingList<ActivoFijo> porDepartamento;
		private GroupingList<ActivoFijo> porYear;
		private GroupingList<ActivoFijo> porMes;
		
		public ResumenDeActivos(final EventList<ActivoFijo> source){
			this.source=source;
			setTitle("Grafícas");
		}

		public JComponent getControl() {
			final JPanel panel=new JPanel(new BorderLayout());
			panel.add(new Header("Análisis general","Resumen de activos por diversos conceptos").getHeader(),BorderLayout.NORTH);
			panel.add(buildGraphView(),BorderLayout.CENTER);
			return panel;
		}
		
		private JComponent buildGraphView(){
			/*JPanel panel=new JPanel(new GridLayout(2,2));
			panel.add(buildPorSucursal());
			panel.add(buildPorDepartamento());
			return panel;
			*/
			final FormLayout layout=new FormLayout("p:g(.5),2dlu,p:g(.5)"
					,"t:p:g(.5),3dlu,t:p:g(.5)");
			PanelBuilder builder=new PanelBuilder(layout);
			CellConstraints cc=new CellConstraints();
			//builder.add(buildPorSucursal(),cc.xy(1, 1));
			//builder.add(buildPorDepartamento(),cc.xy(3, 1));
			//builder.add(buildPorYear(),cc.xy(1, 3));
			//builder.add(buildPorMes(),cc.xy(3, 3));
			return builder.getPanel();
		}		
		/**
		private JComponent buildPorSucursal(){
			porSucursal=new GroupingList<ActivoFijo>(source,GlazedLists.beanPropertyComparator(ActivoFijo.class, "sucursal.nombre"));
			
			final FunctionList.Function keyFunction=new FunctionList.Function(){
				
				public Object evaluate(Object sourceValue) {
					List<ActivoFijo> activo=(List<ActivoFijo>)sourceValue;
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
			
			//final JFreeChart pieChart=new JFreeChart("Gastos X Departamento",new PiePlot(dataSet));
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
			
			//final JFreeChart pieChart=new JFreeChart("Gastos X Departamento",new PiePlot(dataSet));
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
			
			//final JFreeChart pieChart=new JFreeChart("Gastos X Departamento",new PiePlot(dataSet));
			final JFreeChart chart=ChartFactory.createPieChart3D("X Mes", dataSet, true, true, false);
			final ChartPanel panel=new ChartPanel(chart,true);
			panel.setPreferredSize(new Dimension(300,300));
			return panel;
		}
		*/
		
	}
	 

}
