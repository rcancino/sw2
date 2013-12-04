package com.luxsoft.sw3.tesoreria.ui.consultas;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jdesktop.swingx.JXTable;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.panel.SimpleInternalFrame;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.reportes.ComisionTarjetasReportForm;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;

import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.tesoreria.model.CargoAbonoPorCorte;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjeta;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjetaDet;
import com.luxsoft.sw3.tesoreria.model.TipoDeAplicacion;
import com.luxsoft.sw3.tesoreria.ui.forms.CorteDeTarjetaForm;
import com.luxsoft.sw3.tesoreria.ui.forms.CorteDeTarjetaFormModel;
import com.luxsoft.sw3.tesoreria.ui.selectores.SelectorDePagosConTarjeta;


/**
 * Panel para el mantenimiento de cortes de tarjeta
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class MantenimientoContableParaCortesDeTarjetaPanel extends AbstractMasterDatailFilteredBrowserPanel<CorteDeTarjeta,CorteDeTarjetaDet> {
	
	

	public MantenimientoContableParaCortesDeTarjetaPanel() {
		super(CorteDeTarjeta.class);
	}
	
	public void init(){
		addProperty(
				"id"
				,"sucursal.nombre"
				,"fecha"
				,"corte"
				,"cuenta.banco.clave"
				,"total"
				,"tipoDeTarjeta"
				,"ingreso.id"
				,"comentario"
				
				);
		addLabels(
				"Id",
				"Sucursal"
				,"Fecha"
				,"Corte"
				,"Cuenta"
				,"Total"
				,"Tarjeta"
				,"Ingreso"
				,"Comentario"
				
				);
		//setDefaultComparator(GlazedLists.beanPropertyComparator(SolicitudDeDeposito.class, "id"));
		installTextComponentMatcherEditor("Id", "id");
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		
		
		installTextComponentMatcherEditor("Total", "total");
		installTextComponentMatcherEditor("Tipo de Tarjeta", "tipoDeTarjeta");
		
		
		
		manejarPeriodo();		
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {
	}
	

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={
				"corte.id"
				,"pago.nombre"
				,"pago.fecha"
				,"pago.total"				
				,"pago.tarjeta.nombre"
				,"pago.tarjeta.debito"
				,"pago.comisionBancaria"
				,"pago.autorizacionBancaria"
				//,"pago.info"
				};
		String[] names={
				"Corte"
				,"Nombre"
				,"Fecha"
				,"Total"
				,"Tarjeta"
				,"Debito"
				,"Comisión"
				,"Referencia"
				//,"Info"
				};
		return GlazedLists.tableFormat(CorteDeTarjetaDet.class,props,names);
	}

	@Override
	protected Model<CorteDeTarjeta, CorteDeTarjetaDet> createPartidasModel() {
		return new Model<CorteDeTarjeta, CorteDeTarjetaDet>(){
			public List<CorteDeTarjetaDet> getChildren(CorteDeTarjeta parent) {
				String hql="from CorteDeTarjetaDet p left join fetch p.corte cc" +
						" where p.corte.id=?";
				return getHibernateTemplate().find(hql,parent.getId());
			}			
		};
	}	
	
	public void open(){
		load();
	}
	
	
	
	public JComponent buildDetailGridPanel(JXTable detailGrid){
		JScrollPane sp=new JScrollPane(detailGrid);
		JTabbedPane tabPanel=new JTabbedPane();
		tabPanel.addTab("Bancos", buildBancosPanel());
		tabPanel.addTab("Pagos", sp);
		
		return tabPanel;
	}
	
	private EventSelectionModel cargoAbonoSelectionModel;
	
	private JComponent buildBancosPanel(){
		
		EventList eventLst=selectionModel.getSelected();
		EventList partidasList=new CollectionList(eventLst,new Model(){
			public List getChildren(Object parent) {
				final CorteDeTarjeta corte=(CorteDeTarjeta)parent;
				return getHibernateTemplate().executeFind(new HibernateCallback() {					
					public Object doInHibernate(Session session) throws HibernateException,	SQLException {
						CorteDeTarjeta target=(CorteDeTarjeta)session.load(CorteDeTarjeta.class, corte.getId());
						List res=new ArrayList(target.getAplicaciones().size());
						for(CargoAbonoPorCorte ca:target.getAplicaciones()){
							ca.getSucursal().getNombre();
							res.add(ca);
						}
						return res;
					}
				});	
			}
		});
		
		SortedList sortedList=new SortedList(partidasList,null);
		TableFormat tf=GlazedLists.tableFormat(CargoAbonoPorCorte.class
				,new String[]{"corte.id","sucursal.nombre","tipo","cargoAbono.id","importe","cargoAbono.importe","cargoAbono.fecha","cargoAbono.fechaDeposito"}
				,new String[]{ "Corte","Sucursal","Tipo","C/A (Bancos)","Importe ","Importe (C/A)","Fecha","Fecha Dep"}
			);
		JXTable bancosGrid=ComponentUtils.getStandardTable();
		EventTableModel tm=new EventTableModel(sortedList,tf);
		bancosGrid.setModel(tm);
		cargoAbonoSelectionModel=new EventSelectionModel(sortedList);
		cargoAbonoSelectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
		bancosGrid.setSelectionModel(cargoAbonoSelectionModel);
		TableComparatorChooser.install(detailGrid, sortedList, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE_WITH_UNDO);
		JScrollPane sp=new JScrollPane(bancosGrid);
		return sp;
	}

	@Override
	public Action[] getActions() {
		if(actions==null){
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getDeleteAction()
				//,getEditAction()
				,getViewAction()
				,addAction(null, "imprimir", "Imprimir")
				};
		}
		return actions;
	}
	
	@Override
	protected List<Action> createProccessActions() {		
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("","reporteDeComisionTarjetas", "Rep. Pagos con Tarjeta"));
		//procesos.add(addAction("","modificarTarjeta","Cambiar Tarjeta"));
		//procesos.add(addAction("","reasignarComision","Cambiar Comisión"));
		procesos.add(addAction("","modificarFechaDeposito","Cambiar Fecha Dep"));
		return procesos;
	}
	
	public void reporteDeComisionTarjetas(){
		ComisionTarjetasReportForm.run();
	}

	@Override
	protected List<CorteDeTarjeta> findData() {
		String hql="from CorteDeTarjeta c where c.fecha between ? and ?";
		return ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{
					periodo.getFechaInicial()
					,periodo.getFechaFinal()
					}
		);
	}
	
	@Override
	protected CorteDeTarjeta doInsert() {		
		final CorteDeTarjetaFormModel model=new CorteDeTarjetaFormModel();
		final CorteDeTarjetaForm form=new CorteDeTarjetaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			CorteDeTarjeta target=model.commit();
			target=ServiceLocator2.getIngresosManager().registrarCorte(target);
			return target;
		}
		return null;
	}

	@Override
	protected void doSelect(Object bean) {
		CorteDeTarjeta corte=(CorteDeTarjeta)bean;
		List<CorteDeTarjeta> found=getHibernateTemplate().find("from CorteDeTarjeta c left join fetch c.partidas p where c.id=?",corte.getId());
		if(!found.isEmpty()){
			final CorteDeTarjetaFormModel model=new CorteDeTarjetaFormModel(found.get(0));
			model.setReadOnly(true);
			final CorteDeTarjetaForm form=new CorteDeTarjetaForm(model);
			form.open();
		}
	}
	
	public void modificarTarjeta(){
		SelectorDePagosConTarjeta selector=new SelectorDePagosConTarjeta();
		selector.setPeriodo(periodo);
		selector.open();
	}
	
	@Override
	public boolean doDelete(CorteDeTarjeta bean) {
		ServiceLocator2.getIngresosManager().eliminarCorte(bean);
		return true;
	}
	
	public void reasignarComision(){
		CorteDeTarjeta selected=(CorteDeTarjeta)getSelectedObject();
		if(selected!=null){
			if(selected.getTipoDeTarjeta().startsWith("AMEX")){
				final DefaultFormModel model=new DefaultFormModel(Bean.proxy(ModificacionDeImportes.class));
				CorteDeTarjeta target=inicializar(selected);
				for(CargoAbonoPorCorte ca:target.getAplicaciones()){
					if(ca.getTipo().equals(TipoDeAplicacion.COMISION_AMEX))
						model.setValue("comision", ca.getImporte().abs());
					else if(ca.getTipo().equals(TipoDeAplicacion.IMPUESTO))
						model.setValue("impuesto", ca.getImporte().abs());
				}
				final ModificacionDeComisionForm form=new ModificacionDeComisionForm(model);
				form.open();
				if(!form.hasBeenCanceled()){
					BigDecimal importe=(BigDecimal)model.getValue("comision");
					importe=importe.abs();
					BigDecimal impuesto=(BigDecimal)model.getValue("impuesto");
					impuesto=impuesto.abs();
					for(CargoAbonoPorCorte ca:target.getAplicaciones()){
						if(ca.getTipo().equals(TipoDeAplicacion.COMISION_AMEX)){
							ca.getCargoAbono().setImporte(importe.multiply(BigDecimal.valueOf(-1)));
							System.out.println("Importe: "+ca.getCargoAbono().getImporte());
						}
						else if(ca.getTipo().equals(TipoDeAplicacion.IMPUESTO)){
							ca.getCargoAbono().setImporte(impuesto.multiply(BigDecimal.valueOf(-1)));
							System.out.println("Impuesto: "+ca.getCargoAbono().getImporte());
						}
					}
					target=ServiceLocator2.getIngresosManager().actualizarCorte(target);
					selectionModel.clearSelection();
					/*
					int index=source.indexOf(selected);
					if(index!=-1){
						source.set(index, target);
						
						selectionModel.setSelectionInterval(index, index);
					}*/					
				}
			}
		}
	}
	
	
	public void modificarFechaDeposito(){
		CorteDeTarjeta selected=(CorteDeTarjeta)getSelectedObject();
		if(selected!=null){
			if(selected.getTipoDeTarjeta().startsWith("AMEX")){
				CorteDeTarjeta target=inicializar(selected);
				String pattern="Cambiar la fecha deposito " +
						"del corte {0} ";
				String msg=MessageFormat.format(pattern, target.getId());
				if(MessageUtils.showConfirmationMessage(msg, "Cambio de fecha")){
					Date fecha=SelectorDeFecha.seleccionar();
					for(CargoAbonoPorCorte ca:target.getAplicaciones()){
						ca.getCargoAbono().setFechaDeposito(fecha);
						ServiceLocator2.getUniversalDao().save(ca.getCargoAbono());
					}					
					setSelected(target);
				}
			}
		}
	}
	
	private CorteDeTarjeta inicializar(final CorteDeTarjeta source){
		return (CorteDeTarjeta)getHibernateTemplate().execute(new HibernateCallback() {					
			public Object doInHibernate(Session session) throws HibernateException,	SQLException {
				CorteDeTarjeta target=(CorteDeTarjeta)session.load(CorteDeTarjeta.class, source.getId());				
				for(CargoAbonoPorCorte ca:target.getAplicaciones()){
					ca.getSucursal().getNombre();					
				}
				return target;
			}
		});	
	}

	protected HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}
	
private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		
		private JLabel total1=new JLabel();
		private JLabel total2=new JLabel();
		private JLabel total3=new JLabel();
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			total1.setHorizontalAlignment(SwingConstants.RIGHT);
			total2.setHorizontalAlignment(SwingConstants.RIGHT);
			total3.setHorizontalAlignment(SwingConstants.RIGHT);
			builder.appendSeparator("Resumen ");
			builder.append("Total",total1);
			//builder.append("Ventas (Prom)",total2);
			//builder.append("Por Pedir",total3);
			
			builder.getPanel().setOpaque(false);
			getFilteredSource().addListEventListener(this);
			updateTotales();
			return builder.getPanel();
		}
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				
			}
			updateTotales();
		}
		
		public void updateTotales(){
			
			double valorTotal1=0;
			//double toneladasVentas=0;
			//double toneladasPorPedir=0;
			
			for(Object obj:getFilteredSource()){
				CorteDeTarjeta a=(CorteDeTarjeta)obj;
				valorTotal1+=a.getTotal().doubleValue();
				//toneladasVentas+=a.getToneladasPromVenta();
				//toneladasPorPedir+=a.getToneladasPorPedir();
			}
			total1.setText(nf.format(valorTotal1));
			//total2.setText(nf.format(toneladasVentas));
			//total3.setText(nf.format(toneladasPorPedir));
		}
		
		private NumberFormat nf=NumberFormat.getNumberInstance();
		
	}
	
	public static class ModificacionDeComisionForm extends AbstractForm{

		public ModificacionDeComisionForm(IFormModel model) {
			super(model);
			setTitle("Modificación de comisión");
		}

		@Override
		protected JComponent buildFormPanel() {
			final FormLayout layout=new FormLayout("p,2dlu,100dlu","");
			final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Comisión",getControl("comision"));
			builder.append("Impuesto",getControl("impuesto"));
			return builder.getPanel();
		}
		
	}
	
	public static class ModificacionDeImportes{
		
		private BigDecimal comision;
		
		private BigDecimal impuesto;

		public BigDecimal getComision() {
			return comision;
		}

		public void setComision(BigDecimal comision) {
			this.comision = comision;
		}

		public BigDecimal getImpuesto() {
			return impuesto;
		}

		public void setImpuesto(BigDecimal impuesto) {
			this.impuesto = impuesto;
		}

		
		
		
	}

}
