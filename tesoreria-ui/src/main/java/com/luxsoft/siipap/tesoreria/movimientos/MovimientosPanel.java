package com.luxsoft.siipap.tesoreria.movimientos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JRViewer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.JXTable;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.ObservableElementList.Connector;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.beans.BeanProperty;
import ca.odell.glazedlists.impl.beans.BeanTableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.swing.EventTableModel;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Deposito;
import com.luxsoft.siipap.model.tesoreria.MovimientosPorCuenta;
import com.luxsoft.siipap.model.tesoreria.Origen;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.matchers.FechaMayorAMatcher;
import com.luxsoft.siipap.swing.matchers.FechaMenorAMatcher;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.tesoreria.form.CargoAbonoForm;
import com.luxsoft.siipap.tesoreria.movimientos.DepositoForm.DepositoModel;


import com.luxsoft.sw3.tesoreria.TESORERIA_ROLES;

public class MovimientosPanel extends FilteredBrowserPanel<CargoAbono>{
	
	//private FechaMayorAMatcher fechaMayorEditor;
	//private FechaMenorAMatcher fechaMenorEditor;
	private ObservableElementList<CargoAbono> olist;
	
	
	

	@SuppressWarnings("unchecked")
	public MovimientosPanel() {
		super(CargoAbono.class);
	
		
		addProperty(
				"id"
				,"sucursal.nombre"
				,"cuenta.numero"
				,"cuenta.banco.clave"
				,"fecha"
				,"concep"	
				,"descripcion"				
				,"importe"
				,"revisado"
				,"moneda"
				,"tc"
				,"origen.name"
				,"referencia"				
				,"conciliado"
				,"recibidoChe"
				,"chequeDevueltoRecibido"
				,"comentario"
				,"UserLog.createUser"
				,"UserLog.updateUser"
				
				);
		addLabels("id"
				,"Suc"
				,"Cuenta"
				,"Banco"
				,"Fecha"
				,"Concepto"
				,"Descripcion"
				,"Importe"
				,"Revisado"
				,"Moneda"
				,"T.C"
				,"Origen"
				,"Referencia"				
				,"Conciliación"
				,"CHE Rec"
				,"CHE Rec Fecha"
				,"Comentario"
				,"Usuario"
				,"Modifico"
				
				);
		installTextComponentMatcherEditor("Cuenta", "cuenta.numero");
		installTextComponentMatcherEditor("Banco", "cuenta.banco.clave","cuenta.banco.nombre");
		installTextComponentMatcherEditor("Id", "id");
		installTextComponentMatcherEditor("Origen", "origen.name");
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Concepto", "concep");
		installTextComponentMatcherEditor("Descripcion", "descripcion");
		installTextComponentMatcherEditor("Importe", "importe");
		installTextComponentMatcherEditor("Folio", "referencia");
		//fechaMayorEditor=new FechaMayorAMatcher();
		//fechaMenorEditor=new FechaMenorAMatcher();
		//installCustomMatcherEditor("F Inicial", fechaMayorEditor.getFechaField(), fechaMayorEditor);
		//installCustomMatcherEditor("F Final", fechaMenorEditor.getFechaField(), fechaMenorEditor);
		CheckBoxMatcher<CargoAbono> m1=new CheckBoxMatcher<CargoAbono>(){
			@Override
			protected Matcher<CargoAbono> getSelectMatcher(Object... obj) {
				return Matchers.beanPropertyMatcher(CargoAbono.class, "conciliado", Boolean.TRUE);
			}
			
		};
		installCustomMatcherEditor("Conciliación", m1.getBox(), m1);
		
		CheckBoxMatcher<CargoAbono> m2=new CheckBoxMatcher<CargoAbono>(){
			@Override
			protected Matcher<CargoAbono> getSelectMatcher(Object... obj) {
				return Matchers.beanPropertyMatcher(CargoAbono.class, "recibidoChe", Boolean.TRUE);
			}
			
		};
		installCustomMatcherEditor("Cheques Pendientes", m2.getBox(), m2);
		
		manejarPeriodo();
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodo(-5);
	}
	
	@Override
	protected void adjustMainGrid(JXTable grid) {
		grid.getColumnExt("T.C").setCellRenderer(Renderers.getTipoDeCambioRenderer());
		
		//grid.getColumnExt("Recibió").setVisible(false);
		//grid.getColumnExt("Surtidor").setVisible(false);
		
	}
	
	
	@Override
	protected TableFormat buildTableFormat() {
		//return GlazedLists.tableFormat(beanClazz,getProperties(), getLabels());
		return new CustomTF(beanClazz,getProperties(),getLabels());
	}
	
	@Override
	protected EventList getSourceEventList() {
		EventList<CargoAbono> list=new BasicEventList<CargoAbono>();
		Connector<CargoAbono> conn=GlazedLists.beanConnector(CargoAbono.class,true,new String[]{"revisado"});
		olist=new ObservableElementList<CargoAbono>(list,conn);
		olist.addListEventListener(new ListHandler());
		return olist;
	}

	@Override
	protected void executeLoadWorker(SwingWorker worker) {		
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	protected List<CargoAbono> findData() {
		String hql="from CargoAbono a where a.fecha between ? and ? ";
		return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}

	public void open(){
		//load();
	}
	
	@Override
	protected JComponent buildContent() {
		final JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				super.buildContent(),
				buildDetailPanel());
		sp.setResizeWeight(.6);
		return sp;
	}
	
	@SuppressWarnings("unchecked")
	public Action[] getActions(){
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,addAction(TESORERIA_ROLES.CONTROL_DE_INGRESOS.getId(), "depositar", "Depositar")
				,addAction(TESORERIA_ROLES.CONTROL_DE_INGRESOS.getId(), "retirar", "Retirar")
				,getSecuredEditAction(TESORERIA_ROLES.CONTROL_DE_INGRESOS.getId())
				,createReportMov()
				,getCancelarChequeAction()
				,addAction(TESORERIA_ROLES.CONTROL_DE_INGRESOS.getId(), "cambiarFechaDePago", "Corrección de Fecha (Cob)")
				,addAction(TESORERIA_ROLES.CONTROL_DE_INGRESOS.getId(), "cambiarFechaDeDeposito", "Corrección de Fecha (Dep)")
				,addAction(TESORERIA_ROLES.CONTROL_DE_INGRESOS.getId(), "correccionComision", "Corrección comisión Amex")
				};
		return actions;
	}
	
	private GroupingList<CargoAbono> grupoDeCuentas;
	private EventList<MovimientosPorCuenta> resumenPorCuenta;
	private JXTable resumenGrid;
	
	@SuppressWarnings("unchecked")
	private void initDetailList(){
		grupoDeCuentas=new GroupingList<CargoAbono>(source,GlazedLists.beanPropertyComparator(CargoAbono.class, "cuenta.id"));
		resumenPorCuenta=GlazedLists.threadSafeList(new BasicEventList<MovimientosPorCuenta>());
		SortedList<MovimientosPorCuenta> sortedResumen=new SortedList<MovimientosPorCuenta>(resumenPorCuenta,null);
		resumenGrid=ComponentUtils.getStandardTable();
		final TableFormat<MovimientosPorCuenta> tf=GlazedLists.tableFormat(MovimientosPorCuenta.class
				, new String[]{"cuenta.banco.clave","cuenta.numero","saldo"}
				 ,new String[]{"Banco","Cuenta","Saldo"});
		final EventTableModel<MovimientosPorCuenta> tm=new EventTableModel<MovimientosPorCuenta>(sortedResumen,tf);
		resumenGrid.setModel(tm);
		
	}
	
	protected JComponent buildDetailPanel(){
		initDetailList();
		JPanel panel=new JPanel(new BorderLayout());
		panel.add(new JXHeader("Resumen por cuenta",""),BorderLayout.NORTH);
		JScrollPane sp=new JScrollPane(resumenGrid);
		panel.add(sp,BorderLayout.CENTER);
		return panel;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	protected void dataLoaded(List data) {
		super.dataLoaded(data);
		resumenPorCuenta.clear();
		for(List<CargoAbono> gpo:grupoDeCuentas){
			MovimientosPorCuenta m=MovimientosPorCuenta.generarResumenMovimientos(gpo);
			resumenPorCuenta.add(m);
		}
	}
	
	public Action createReportMov(){
		AbstractAction a=new AbstractAction(){
			public void actionPerformed(ActionEvent arg0) {
				showReportMov sr=new showReportMov();
				sr.open();
			}			
		};
		a.putValue(Action.NAME, "Generar Reporte");
		return a;
	}
	
	private class showReportMov extends SXAbstractDialog{
		
		public showReportMov() {
			super("Reporte...");
		}

		public JComponent displayReport(){
			Map<String, Object>parametros=new HashMap<String, Object>();
			  SimpleDateFormat df=new SimpleDateFormat("dd/MM/yyyy");
			  parametros.put("FECHA_INI",df.format(periodo.getFechaInicial()));
			  parametros.put("FECHA_FIN",df.format(periodo.getFechaFinal()));
                net.sf.jasperreports.engine.JasperPrint jasperPrint = null;
                DefaultResourceLoader loader = new DefaultResourceLoader();
                Resource res = loader.getResource(ReportUtils.toReportesPath("Tesoreria/Movimientos.jasper"));
                try
                {
                    java.io.InputStream io = res.getInputStream();
                    try
                    {
                    	JTable table=getGrid();
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
	
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("", "reporteDeDiferenciasAmex", "Diferencias AMEX"));
		
		
		return procesos;
	}
	

	public void reporteDeDiferenciasAmex(){
		DiferenciasAmexReportForm.run();
		
	}
	@Override
	protected CargoAbono doEdit(CargoAbono bean) {
		//System.out.println("Tratando de editar bean de origen: "+bean.getOrigen());
		if(bean.getOrigen().equals(Origen.TESORERIA)
				||bean.getOrigen().equals(Origen.MOVIMIENTO_MANUAL))
		{
			CargoAbono target=ServiceLocator2.getCargoAbonoDao().get(bean.getId());
			target=CargoAbonoForm.showForm(target);
			return target;
		}else
			return null;
		
	}

	public void depositar(){
		Deposito dep=new Deposito();
		dep.setRetiro(false);
		DepositoModel model=new DepositoModel(dep);
		final DepositoForm form=new DepositoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			dep=model.getDeposito();
			CargoAbono mov=dep.toCargoAbono();
			mov=ServiceLocator2.getCargoAbonoDao().save(mov);
			source.add(mov);
		}
	}
	
	
	public void retirar(){
		Deposito dep=new Deposito();
		dep.setRetiro(true);
		DepositoModel model=new DepositoModel(dep);
		final DepositoForm form=new DepositoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			dep=model.getDeposito();
			CargoAbono mov=dep.toCargoAbono();
			mov=ServiceLocator2.getCargoAbonoDao().save(mov);
			source.add(mov);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void salvar(final CargoAbono bean){
		//System.out.println("Salvando el abono: "+bean);
		//bean.setEntregado(bean.getEntregadoFecha()!=null);
		if(bean.isEntregado())
			bean.setLiberado(true);
		try {
			CargoAbono next=ServiceLocator2.getCargoAbonoDao().save(bean);
			int index=source.indexOf(bean);
			source.set(index, next);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void correccionComision(){
		if(!selectionModel.isSelectionEmpty()){
			CargoAbono comision=(CargoAbono)selectionModel.getSelected().get(0);
			int index=source.indexOf(comision);
			if(index!=-1){
				try {
					String res=JOptionPane.showInputDialog(getControl(), "Digite importe:");
					Assert.isTrue(StringUtils.isNotBlank(res),"Importe incorrecto");
					Assert.isTrue(NumberUtils.isNumber(res),"Importe incorrecto ");
					BigDecimal importe=new BigDecimal(res);
					Assert.isTrue(importe.doubleValue()<0,"El importe de la comisión debe ser negativo");
					
					comision=ServiceLocator2.getIngresosManager().actualizarComisionDeAmex(comision.getId(), importe);
					source.set(index, comision);
				} catch (Exception e) {
					String msg=ExceptionUtils.getRootCauseMessage(e);
					MessageUtils.showMessage(msg, "Error actualizando comisión");
				}
				
			}
		}
	}
	
	private Action cancelarChequeAction;
	
	public Action getCancelarChequeAction(){
		if(cancelarChequeAction==null){
			cancelarChequeAction=new AbstractAction("Cancelar Cheque"){
				public void actionPerformed(ActionEvent e) {
					cancelarCheque();				
				}				
			};
		}
		return cancelarChequeAction;
	}
	
	@SuppressWarnings("unchecked")
	private void cancelarCheque(){
		CargoAbono bean=CancelarChequeForm.showForm();
		if(bean!=null){
			CargoAbono next=ServiceLocator2.getCargoAbonoDao().save(bean);
			source.add(next);
		}
	}
	
	public void cambiarFechaDePago(){
		CargoAbono ca=(CargoAbono)getSelectedObject();
		if(ca!=null){
			if(ca.getPago()!=null){
				if(ca.getPago() instanceof PagoConDeposito){
					String pattern="Cambiar la fecha del Cargo/Abono {0} asi como la fecha del deposito correspondiente";
					String msg=MessageFormat.format(pattern, ca.getId());
					if(MessageUtils.showConfirmationMessage(msg, "Cambio de fecha")){
						Date fecha=SelectorDeFecha.seleccionar();
						if(fecha!=null){
							ServiceLocator2.getIngresosManager().correccionDeFecha(ca,fecha);
							ca=ServiceLocator2.getCargoAbonoDao().get(ca.getId());
							setSelected(ca);
						}
					}
					/*
					if(ca.getPago().getOrigen().equals(OrigenDeOperacion.CRE)
							||ca.getPago().getOrigen().equals(OrigenDeOperacion.JUR)
							||ca.getPago().getOrigen().equals(OrigenDeOperacion.CHE))
					{
						
						
						
					}*/
					
				}
			}
		}
	}
	
	public void cambiarFechaDeDeposito(){
		CargoAbono ca=(CargoAbono)getSelectedObject();
		if(ca!=null){
			if(ca.getPago()!=null){
				if(ca.getPago() instanceof PagoConDeposito){
					String pattern="Cambiar la fecha del Cargo/Abono {0} asi como la fecha del deposito correspondiente";
					String msg=MessageFormat.format(pattern, ca.getId());
					if(MessageUtils.showConfirmationMessage(msg, "Cambio de fecha deposito")){
						Date fecha=SelectorDeFecha.seleccionar();
						if(fecha!=null){
							int index=source.indexOf(ca);
							ServiceLocator2.getIngresosManager().correccionDeFechaDeposito(ca,fecha);
							if(index!=-1){
								ca=ServiceLocator2.getCargoAbonoDao().get(ca.getId());
								source.set(index, ca);
							}
							
							//setSelected(ca);
						}
					}					
				}
			}
		}
	}
	
	private class CustomTF extends BeanTableFormat<CargoAbono>{

		public CustomTF(Class<CargoAbono> beanClass, String[] propertyNames, String[] columnLabels) {
			super(beanClass, propertyNames, columnLabels);
			
		}

		@Override
		public boolean isEditable(CargoAbono baseObject, int column) {
			String prop=propertyNames[column];
			if("revisado".equals(prop)){				
				return true;
			}if("recibidoChe".equals(prop)){
				return true;
			}
			return super.isEditable(baseObject, column);
		}

		/**
	     * Loads the property descriptors which are used to invoke property
	     * access methods using the property names.
	     */
	    @SuppressWarnings("unchecked")
		protected void loadPropertyDescriptors(Class<CargoAbono> beanClass) {
	        beanProperties = new BeanProperty[propertyNames.length];
	        for(int p = 0; p < propertyNames.length; p++) {
	        	String name=propertyNames[p];
	        	if("revisado".equals(name)||"recibidoChe".equals(name)){
	        		beanProperties[p] = new BeanProperty<CargoAbono>(beanClass, propertyNames[p], true, true);
	        	}else
	        		beanProperties[p] = new BeanProperty<CargoAbono>(beanClass, propertyNames[p], true, false);
	            
	        }
	    }
		
		
		
		
		
	}
	
	private class ListHandler implements ListEventListener<CargoAbono>{

		public void listChanged(ListEvent<CargoAbono> listChanges) {
			if(listChanges.next()){
				if(listChanges.getType()==ListEvent.UPDATE){
					CargoAbono bean=listChanges.getSourceList().get(listChanges.getIndex());
					salvar(bean);
				}
			}
			
		}
		
	}

}
