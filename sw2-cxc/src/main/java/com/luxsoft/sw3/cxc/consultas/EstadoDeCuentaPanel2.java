package com.luxsoft.sw3.cxc.consultas;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.ChequeDevuelto;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.old.ImpresionUtils;
import com.luxsoft.siipap.cxc.service.CXCManager;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.cxc.ui.consultas.CargoView;
import com.luxsoft.siipap.cxc.ui.consultas.FacturaForm;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeDisponibles;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.matchers.FechaMayorAMatcher;
import com.luxsoft.siipap.swing.matchers.FechaMenorAMatcher;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cxc.forms.LocalizadorDeRMDForm;
import com.luxsoft.sw3.cxc.utils.CXCUtils2;

/**
 * Panel para el mantenimiento y administracion de las cuentas por cobrar
 * 
 * @author Ruben Cancino
 *
 */
public class EstadoDeCuentaPanel2 extends FilteredBrowserPanel<CargoRow2> {
	
	
	private final DateFormat df=new SimpleDateFormat("dd/MM/yyyy");
	
	//private final CuentasPorCobrarModel model;
	
	
	public EstadoDeCuentaPanel2() {
		super(CargoRow2.class);
		setTitle("Cartera de Crédito");
		//this.model=model;
		source=GlazedLists.eventListOf(null);		
		Comparator c=GlazedLists.beanPropertyComparator(CargoRow2.class, "fecha");
		setDefaultComparator(c);
	}	
	
	protected void init(){
		String[] props={
				"tipo"
				,"documento"
				,"numeroFiscal"
				,"postFechado"
				,"fecha"
				,"vencimiento"
				,"reprogramarPago"
				,"atraso"
				,"sucursalNombre"
				,"clave"
				,"nombre"
				,"total"
				,"devoluciones"
				,"bonificaciones"
				,"descuentos"
				,"pagos"
				,"saldo"
				,"cargoAplicado"
				,"cargo"
				,"importeCargo"
				,"impreso"				
				};
		String[] names={
				"Tipo"
				,"Docto"
				,"Fiscal"
				,"Post F."
				,"Fecha"
				,"Vto"
				,"Pago"
				,"Atraso"
				,"Sucursal"
				,"Cliente"
				,"Nombre"
				,"Total"
				,"Devoluciones"
				,"Bonificaciones"
				,"Descuentos"
				,"Pagos"
				,"Saldo"
				,"Cargo (Aplic)"
				,"Cargo(%)"
				,"Caro($)"
				,"Impreso"
				};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("Tipo", "tipo");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("N.Fiscal", "numeroFiscal");
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		TextFilterator<CargoRow2> fechaFilterator=new TextFilterator<CargoRow2>(){
			public void getFilterStrings(List<String> baseList, CargoRow2 element) {
				baseList.add(df.format(element.getFecha()));
			}
		};
		TextFilterator<CargoRow2> vtoFilterator=new TextFilterator<CargoRow2>(){
			public void getFilterStrings(List<String> baseList, CargoRow2 element) {
				baseList.add(df.format(element.getVencimiento()));
			}
			
		};
		installTextComponentMatcherEditor("Fecha ", fechaFilterator,new JTextField(10));
		installTextComponentMatcherEditor("Vto ", vtoFilterator,new JTextField(10));
		FechaMayorAMatcher desdeMatcher=new FechaMayorAMatcher();		
		installCustomMatcherEditor("Desde ",desdeMatcher.getFechaField(), desdeMatcher);
		FechaMenorAMatcher hastaMatcher=new FechaMenorAMatcher();		
		installCustomMatcherEditor("Hasta ",hastaMatcher.getFechaField(), hastaMatcher);
		
		
	}

	@Override
	protected List<CargoRow2> findData() {
		String sql=SQLUtils.loadSQLQueryFromResource("sql/Cuentas_x_cobrar.sql");
		Object[] params=new Object[]{DateUtil.toDate("31/12/2008"),"CRE"};
		List<CargoRow2> cargos=ServiceLocator2.getJdbcTemplate().query(sql,params, new BeanPropertyRowMapper(CargoRow2.class));
		return cargos;
	}	
	
	protected void executeLoadWorker(final SwingWorker worker){		
		TaskUtils.executeSwingWorker(worker);
	}	

	@Override
	public Action[] getActions() {
		if(actions==null){
			this.actions=new Action[]{
				getLoadAction()
				,addAction(CXCActions.AplicarPago.getId(), "aplicarPago", "Aplicar abono")				
				,addAction(CXCActions.GenerarNotaDeBonificacion.getId(), "generarBonificacion", "Bonificacion" )
				,addAction(CXCActions.GenerarNotaDeDevolucion.getId(), "generarNotaDevolucion", "Devolución" )
				,addAction("", "imprimirRMD", "Imprimir RMD" )
				
				,addAction(CXCActions.GenerarNotaDeCargo.getId(), "generarNotaCargo", "Generar N.Cargo" )
				,addAction(CXCActions.GenerarPagoDiferencias.getId(), "pagarDiferencias", "Pago de diferencias")
				,addAction(CXCActions.RefrescarSeleccion.getId(), "refreshSelection", "Refrescar(Sel)" )				
				,addContextAction(new CancelacionDeCargoPredicate(), CXCActions.CancelarNotaDeCargo.getId(), "cancelarCargo", "Cancelar Cargo" )				
				,addContextAction(new SelectionPredicate(), CXCActions.ConsultarDisponibles.getId(), "disponibles", "Disponibles")
				,addAction(CXCActions.JuridicoAlta.getId(), "mandarJuridico", "Mandar a Jurídico")				
				,addContextAction(new Predicate(){
					public boolean evaluate(Object bean) {
						CargoRow2 row=(CargoRow2)bean;
						if(row!=null){
							if(row.getTipo().equals("CAR"))
								return row.getImpreso()==null;
						}
						return false;
							
					}
				}, CXCActions.ImprimirNotaDeCargo.getId(), "imprimirNotaDeCargo", "Imprimir Nota de C")
				};
		}
		return actions;
	}
	
	private CargoRow2 getCurrentRow(){
		return (CargoRow2)getSelectedObject();
	}
	
	private Cargo getSelectedCargo(){
		CargoRow2 row=(CargoRow2)getSelectedObject();
		if(row!=null){
			return getManager().getCargo(row.getId());
		}else
			return null;
	}
	
	
	public void aplicarPago(){
		CXCUIServiceFacade.aplicarPago();
	}	
	
	
	public void pagarDiferencias(){
		Cargo c=getSelectedCargo();
		if(c==null)
			return;
		if(MessageUtils.showConfirmationMessage("Aplicar pago automático por: "+c.getSaldoCalculado(), "Pago de diferencias")){
			CXCUIServiceFacade.aplicarPagoDiferencias(c);
			refreshSelection();
		}
	}
	
	public void cancelarCargo(){
		CargoRow2 row=getCurrentRow();
		if(row!=null){
			if(row.getTipo().equals("CAR")){
				NotaDeCargo cargo=(NotaDeCargo)getSelectedCargo();
				if(MessageUtils.showConfirmationMessage("Cancelar cargo:"+cargo.getDocumento(), "Cancelación de cargo")){
					getManager().cancelarNotaDeCargo(cargo.getId());
					refreshSelection();
				}
			}else if(row.getTipo().equals("CHE")){
				ChequeDevuelto ch=(ChequeDevuelto)getSelectedCargo();
				if(MessageUtils.showConfirmationMessage("Cancelar cargo por cheque devuelto:"+ch.getDocumento(), "Cancelación de cargo")){
					getManager().cancelarChequeDevuelto(ch.getId());
					refreshSelection();
				}
			}
		}
	}
	
	
	public void generarBonificacion(){
		if(getSelected().isEmpty())
			CXCUIServiceFacade.generarNotaDeBonificacion(OrigenDeOperacion.CRE);
		else{
			List<Cargo> cargos=new ArrayList<Cargo>();
			for(Object o:getSelected()){
				CargoRow2 row=(CargoRow2)o;
				cargos.add(getManager().getCargo(row.getId()));
			}
			CXCUIServiceFacade.generarNotaDeBonificacion(OrigenDeOperacion.CRE,cargos);
			refreshSelection();	
		}		
	}
	
	public void generarNotaDevolucion(){
		CXCUIServiceFacade.generarNotasDeDevolucion();
	}
	
	public void generarNotaCargo(){
		if(getSelected().isEmpty())
			CXCUIServiceFacade.generarNotaDeCargo();
		else{
			List<Cargo> cargos=new ArrayList<Cargo>();
			for(Object o:getSelected()){
				CargoRow2 row=(CargoRow2)o;
				cargos.add(getManager().getCargo(row.getId()));
			}
			for(Cargo c:cargos){
				if(c instanceof NotaDeCargo)
					CXCUIServiceFacade.generarNotaDeCargo();
			}
			CXCUIServiceFacade.generarNotaDeCargo(cargos);
			refreshSelection();
		}
	}
	
	private CargoRow2 refresh(CargoRow2 row){
		String sql=SQLUtils.loadSQLQueryFromResource("sql/Cuentas_x_cobrar_row.sql");
		Object[] params=new Object[]{row.getId()};
		List<CargoRow2> target=ServiceLocator2.getJdbcTemplate().query(sql,params, new BeanPropertyRowMapper(CargoRow2.class));
		return target.isEmpty()?row:target.get(0);
	}
	
	public void refreshSelection(){
		for(Object row:getSelected()){
			CargoRow2 old=(CargoRow2)row;
			int index=source.indexOf(old);
			CargoRow2 fresh=refresh(old);
			if(index!=-1){
				logger.info("Cargo refrescado:"+fresh);
				source.set(index,fresh);
			}
		}
	}
	
	public void disponibles(){
		Cargo selected=getSelectedCargo();
		if(selected!=null)
			SelectorDeDisponibles.buscar(selected.getCliente());
	}
	
	
	@Override
	protected void doSelect(Object o) {
		CargoRow2 row=(CargoRow2)o;
		Cargo bean=getManager().getCargo(row.getId());
		if(bean!=null && (bean instanceof Venta) ){
			Venta v=(Venta)bean;
			boolean res=FacturaForm.show(v.getId());
			if(res)
				refreshSelection();
		}else if(bean!=null && (bean instanceof NotaDeCargo)){
			NotaDeCargo cargo=(NotaDeCargo)bean;
			CargoView.show(cargo.getId());
		}
	}	

	/**
	 * Genera una copia de los cargos
	 * 
	 * @param cargos
	 * @return
	
	protected List<Cargo> obtenerCopia(final Collection<Cargo> cargos){
		final List<Cargo> data=new ArrayList<Cargo>(cargos.size());
		for(Cargo c:cargos){
			data.add(getManager().getCargo(c.getId()));
		}
		return data;		
	}
	 */
	
	public void mandarJuridico(){
		Cargo c=getSelectedCargo();
		if(c!=null){
			//c=(Cargo)getSelectedObject();
			c=CXCUIServiceFacade.generarJuridico(c);
			refreshSelection();			
		}
	}
	
	public void imprimirNotaDeCargo(){
		Cargo c=(Cargo)getSelectedCargo();
		if(c!=null){
			int index=source.indexOf(getSelectedObject());
			if(index!=-1){
				ImpresionUtils.imprimirNotaDeCargo(c.getId());
				c=ServiceLocator2.getCXCManager().getCargo(c.getId());
				source.set(index, c);
				selectionModel.clearSelection();
				selectionModel.setSelectionInterval(index, index);
			} 
		}
	}
	
	public void imprimirRMD(){
		Devolucion d=LocalizadorDeRMDForm.buscar();
		if(d!=null){
			final Map parameters=new HashMap();
			parameters.put("DEVOLUCION", d.getId());
			parameters.put("SUCURSAL", String.valueOf(d.getVenta().getSucursal().getId()));
			ReportUtils.viewReport(ReportUtils.toReportesPath("invent/Devoluciones.jasper"), parameters);
		}else{
			if(MessageUtils.showConfirmationMessage("No localizo el RMD. Desea generar otra busqueda?", "Re impresión de documentos")){
				imprimirRMD();
			}
		}
	}
	
	public void open(){
		if(!source.isEmpty())
			grid.packAll();
	}
	
	public CXCManager getManager(){
		return ServiceLocator2.getCXCManager();
	}
	
	public void close(){
		
	}
	
	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel saldoTotal=new JLabel();
		private JLabel saldoVencido=new JLabel();
		private JLabel porVencer=new JLabel();
		private JLabel vencido1_30=new JLabel();
		private JLabel vencido31_60=new JLabel();
		private JLabel vencido61_90=new JLabel();
		private JLabel vencido91=new JLabel();

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			saldoTotal.setHorizontalAlignment(SwingConstants.RIGHT);
			saldoVencido.setHorizontalAlignment(SwingConstants.RIGHT);
			porVencer.setHorizontalAlignment(SwingConstants.RIGHT);
			vencido1_30.setHorizontalAlignment(SwingConstants.RIGHT);
			vencido31_60.setHorizontalAlignment(SwingConstants.RIGHT);
			vencido61_90.setHorizontalAlignment(SwingConstants.RIGHT);
			vencido91.setHorizontalAlignment(SwingConstants.RIGHT);
			
			builder.append("Saldo",saldoTotal);
			builder.append("Por Vencer",porVencer);
			builder.append("Vencido",this.saldoVencido);
			builder.append("1-30  Días",this.vencido1_30);
			builder.append("31-60 Días",this.vencido31_60);
			builder.append("61-90 Días",this.vencido61_90);
			builder.append(">90 Días",this.vencido91);
			
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
			CantidadMonetaria saldo=CXCUtils2.calcularSaldo(getFilteredSource());
			CantidadMonetaria vencido=CXCUtils2.calcularSaldoVencido(getFilteredSource());
			CantidadMonetaria porVencer=CXCUtils2.calcularSaldoPorVencer(getFilteredSource());
			CantidadMonetaria d1_30=CXCUtils2.getVencido1_30(getFilteredSource());
			CantidadMonetaria d31_60=CXCUtils2.getVencido31_60(getFilteredSource());
			CantidadMonetaria d61_90=CXCUtils2.getVencido61_90(getFilteredSource());
			CantidadMonetaria d90=CXCUtils2.getVencidoMasDe90(getFilteredSource());
			
			String pattern="{0}  ({1})";
			saldoTotal.setText(MessageFormat.format(pattern, saldo.amount(),part(saldo,saldo)));
			this.saldoVencido.setText(MessageFormat.format(pattern, vencido.amount(),part(saldo,vencido)));
			this.porVencer.setText(MessageFormat.format(pattern, porVencer.amount(),part(saldo,porVencer)));
			this.vencido1_30.setText(MessageFormat.format(pattern, d1_30.amount(),part(saldo,d1_30)));
			this.vencido31_60.setText(MessageFormat.format(pattern, d31_60.amount(),part(saldo,d31_60)));
			this.vencido61_90.setText(MessageFormat.format(pattern, d61_90.amount(),part(saldo,d61_90)));
			this.vencido91.setText(MessageFormat.format(pattern, d90.amount(),part(saldo,d90)));
		}
		
		private NumberFormat nf=NumberFormat.getPercentInstance();
		
		private String part(final CantidadMonetaria total,final CantidadMonetaria part){
			
			double res=0;
			if(total.amount().doubleValue()>0){
				res=part.divide(total.amount()).amount().doubleValue();
			}
			return StringUtils.leftPad(nf.format(res),5);
		}
		
	}
	
	private class CancelacionDeCargoPredicate implements Predicate{

		public boolean evaluate(Object o) {
			CargoRow2 bean=(CargoRow2)o;
			if(bean==null)
				return false;
			if(bean.getTipo().equals("CAR")){
				Cargo cargo=getManager().getCargo(bean.getId());
				return cargo.getAplicado().doubleValue()==0;
			}else if(bean.getTipo().equals("CHE")){
				Cargo cargo=(Cargo)getManager().getCargo(bean.getId());
				return cargo.getAplicado().doubleValue()==0;
			}
			return false;
		}		
	}
	
	
	
	/*
	public static void main(String[] args) {
		
		String sql=SQLUtils.loadSQLQueryFromResource("sql/Cuentas_x_cobrar.sql");
		Object[] params=new Object[]{DateUtil.toDate("31/12/2008"),"%","CRE"};
		List<CargoRow2> cargos=ServiceLocator2.getJdbcTemplate().query(sql,params, new BeanPropertyRowMapper(CargoRow2.class));
		System.out.println("Cargos: "+cargos.size());
		
	}*/
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				
				System.exit(0);
			}

		});
	}
}