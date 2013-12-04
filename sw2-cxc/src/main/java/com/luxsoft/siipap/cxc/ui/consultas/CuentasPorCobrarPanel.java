package com.luxsoft.siipap.cxc.ui.consultas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.GlazedListsSwing;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.ChequeDevuelto;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.old.ImpresionUtils;
import com.luxsoft.siipap.cxc.rules.CXCUtils;
import com.luxsoft.siipap.cxc.service.CXCManager;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.cxc.ui.model.CuentasPorCobrarModel;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeDisponibles;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.matchers.FechaMayorAMatcher;
import com.luxsoft.siipap.swing.matchers.FechaMenorAMatcher;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Panel para el mantenimiento y administracion de las cuentas por cobrar
 * 
 * @author Ruben Cancino
 *
 */
public class CuentasPorCobrarPanel extends FilteredBrowserPanel<Cargo> implements PropertyChangeListener{
	
	
	private final DateFormat df=new SimpleDateFormat("dd/MM/yyyy");
	
	private final CuentasPorCobrarModel model;
	
	
	public CuentasPorCobrarPanel(final CuentasPorCobrarModel model) {
		super(Cargo.class);
		this.model=model;
		model.addPropertyChangeListener(this);
		source=GlazedListsSwing.swingThreadProxyList(this.model.getCuentasPorCobrar());
		
		Comparator c=GlazedLists.beanPropertyComparator(Cargo.class, "fecha");
		setDefaultComparator(c);
	} 

	@Override
	protected EventList getSourceEventList() {
		return model.getCuentasPorCobrar();
	}
	
	protected void init(){
		String[] props=new String[]{"origen","tipoDocto"
				,"documento","numeroFiscal","tipoSiipap"
				,"precioNeto","fecha","vencimiento","reprogramarPago","atraso","sucursal.nombre","clave","nombre","total"
				,"devoluciones","bonificaciones"
				,"descuentos"
				,"descuentoNota","pagos"
				,"saldoCalculado","saldo","origen","cargosAplicados","cargosPorAplicar"
				,"cargosImpPorAplicar"
				,"descuentoFinanciero"				
				};
		String[] names=new String[]{
				"Origen","Tipo"
				,"Docto","N.Fiscal","TipSip"
				,"PN","Fecha","Vto","Rep. Pago","Atr","Suc","Cliente","Nombre","Total","Devs","Bonific"
				,"Descuentos"
				,"Desc (Nota)","Pagos"
				,"Saldo","Saldo Oracle","Origen","Car (Aplic)","Car(%)"
				,"Car($)"
				,"DF"
				};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("N.Fiscal", "numeroFiscal");
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		TextFilterator<Cargo> fechaFilterator=new TextFilterator<Cargo>(){
			public void getFilterStrings(List<String> baseList, Cargo element) {
				baseList.add(df.format(element.getFecha()));
			}
			
		};
		TextFilterator<Cargo> vtoFilterator=new TextFilterator<Cargo>(){
			public void getFilterStrings(List<String> baseList, Cargo element) {
				baseList.add(df.format(element.getVencimiento()));
			}
			
		};
		installTextComponentMatcherEditor("Fecha ", fechaFilterator,new JTextField(10));
		installTextComponentMatcherEditor("Vto ", vtoFilterator,new JTextField(10));
		//installTextComponentMatcherEditor("Cobrador", "cliente.cobrador.nombres");
		//installTextComponentMatcherEditor("Vendedor", "cliente.vendedor.nombres");
		
		FechaMayorAMatcher desdeMatcher=new FechaMayorAMatcher();		
		installCustomMatcherEditor("Desde ",desdeMatcher.getFechaField(), desdeMatcher);
		FechaMenorAMatcher hastaMatcher=new FechaMenorAMatcher();		
		installCustomMatcherEditor("Hasta ",hastaMatcher.getFechaField(), hastaMatcher);
		
		JuridicoMathcerEditor jm=new JuridicoMathcerEditor();
		installCustomMatcherEditor("Jurídico", jm.getBox(), jm);
		
	}

	@Override
	protected List<Cargo> findData() {
		return model.buscarCuentasPorCobrar();
	}	
	
	protected void executeLoadWorker(final SwingWorker worker){		
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	protected void afterLoad() {
		model.notificarCambioDeCuentas();
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null){
			this.actions=new Action[]{
				getLoadAction()
				,addAction(CXCActions.AplicarPago.getId(), "aplicarPago", "Aplicar abono")
				,addAction(CXCActions.GenerarNotaDeDescuento.getId(), "generarNotaDeDescuento", "Descuento (N.C)" )
				,addAction(CXCActions.GenerarNotaDeBonificacion.getId(), "generarBonificacion", "Bonificacion" )
				,addAction(CXCActions.GenerarNotaDeDevolucion.getId(), "generarNotaDevolucion", "Devolución" )
				//,addMultipleContextAction(new MismoOrigenPredicate(),CXCActions.GenerarNotaDeCargo.getId(), "generarNotaCargo", "Generar N.Cargo" )
				,addAction(CXCActions.GenerarNotaDeCargo.getId(), "generarNotaCargo", "Generar N.Cargo" )
				,addContextAction(new PagoDiferencias(), CXCActions.GenerarPagoDiferencias.getId(), "pagarDiferencias", "Pago de diferencias")
				,addAction(CXCActions.RefrescarSeleccion.getId(), "refreshSelection", "Refrescar(Sel)" )
				,addAction(CXCActions.RecalcularRevisionCobro.getId(), "recalcularRevision", "Recalcular Revision" )
				,addContextAction(new CancelacionDeCargoPredicate(), CXCActions.CancelarNotaDeCargo.getId(), "cancelarCargo", "Cancelar Cargo" )
				,addAction("cxc.exportacionDeSaldos", "exportarSaldo", "Exportar saldo" )
				,addContextAction(new SelectionPredicate(), CXCActions.ConsultarDisponibles.getId(), "disponibles", "Disponibles")
				,addContextAction(new JuridicoAltaPredicate(), CXCActions.JuridicoAlta.getId(), "mandarJuridico", "Mandar a Jurídico")
				,addContextAction(new JuridicoViewPredicate(), CXCActions.JuridicoView.getId(), "consultarJuridico", "Consultar Jurídico")
				,addContextAction(new JuridicoViewPredicate(), CXCActions.JuridicoBaja.getId(), "cancelarJuridico", "Cancelar Jurídico")
				,addContextAction(new Predicate(){
					public boolean evaluate(Object bean) {
						if(bean!=null){
							if(bean instanceof NotaDeCargo){
								NotaDeCargo nc=(NotaDeCargo)bean;
								return nc.getImpreso()==null;
							}
						}
						return false;
							
					}
				}, CXCActions.ImprimirNotaDeCargo.getId(), "imprimirNotaDeCargo", "Imprimir Nota de C")
				};
		}
		return actions;
	}
	
	public void aplicarPago(){
		CXCUIServiceFacade.aplicarPago();
	}	
	
	
	public void pagarDiferencias(){
		Cargo c=(Cargo)getSelectedObject();
		if(MessageUtils.showConfirmationMessage("Aplicar pago automático por: "+c.getSaldoCalculado(), "Pago de diferencias")){
			CXCUIServiceFacade.aplicarPagoDiferencias(c);
			refreshSelection();
		}
	}
	
	public void cancelarCargo(){
		if(getSelectedObject()!=null){
			Object o=getSelectedObject();
			if(o instanceof NotaDeCargo){
				NotaDeCargo cargo=(NotaDeCargo)getSelectedObject();
				if(MessageUtils.showConfirmationMessage("Cancelar cargo:"+cargo.getDocumento(), "Cancelación de cargo")){
					getManager().cancelarNotaDeCargo(cargo.getId());
					refreshSelection();
				}
			}else{
				ChequeDevuelto ch=(ChequeDevuelto)o;
				if(MessageUtils.showConfirmationMessage("Cancelar cargo por cheque devuelto:"+ch.getDocumento(), "Cancelación de cargo")){
					getManager().cancelarChequeDevuelto(ch.getId());
					refreshSelection();
				}
			}
			
		}
	}
	
	public void generarNotaDeDescuento(){
		ExecutionSelectionTemplate<Cargo> template=new ExecutionSelectionTemplate<Cargo>(){
			public List<Cargo> execute(List<Cargo> selected) {
				CXCUIServiceFacade.generarNotasDeDescuento(selected);
				return selected;
			}
		};
		execute(template);
		
	}
	
	public void generarBonificacion(){
		if(getSelected().isEmpty())
			CXCUIServiceFacade.generarNotaDeBonificacion(origen);
		else{
			List<Cargo> cargos=new ArrayList<Cargo>();
			cargos.addAll(getSelected());
			CXCUIServiceFacade.generarNotaDeBonificacion(origen,cargos);
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
			cargos.addAll(getSelected());
			for(Cargo c:cargos){
				if(c instanceof NotaDeCargo)
					CXCUIServiceFacade.generarNotaDeCargo();
			}
			CXCUIServiceFacade.generarNotaDeCargo(cargos);
		}
	}
	
	public void recalcularRevision(){
		model.actualizarRevision();
	}
	
	protected void doExecute(final ExecutionSelectionTemplate<Cargo> template){
		List<Cargo> res=obtenerCopia(getSelected());
		template.execute(res);
	}
	
	public void refreshSelection(){
		for(Object row:getSelected()){
			Cargo old=(Cargo)row;
			int index=source.indexOf(row);
			Cargo fresh=getManager().getCargo(old.getId());
			if(index!=-1){
				logger.info("Venta refrescada:"+fresh);
				source.set(index,fresh);
			}
		}
	}
	
	public void actualizarDescuentos(){
		
	}
	
	public void disponibles(){
		Cargo selected=(Cargo)getSelectedObject();
		SelectorDeDisponibles.buscar(selected.getCliente());
	}
	
	public void asignarDescuentoEspecial(){
		Venta v=(Venta)getSelectedObject();
		if(v!=null){
			int index=source.indexOf(v);
			v=CXCUIServiceFacade.asignarDescuentoEspecial(v);
			if(v!=null){
				source.set(index,v);
			}
		}
	}
	
	@Override
	protected void doSelect(Object bean) {
		if(bean!=null && (bean instanceof Venta) ){
			Venta v=(Venta)getSelectedObject();
			boolean res=FacturaForm.show(v.getId());
			if(res)
				refreshSelection();
		}else if(bean!=null && (bean instanceof NotaDeCargo)){
			NotaDeCargo cargo=(NotaDeCargo)getSelectedObject();
			CargoView.show(cargo.getId());
		}
	}	

	/**
	 * Genera una copia de los cargos
	 * 
	 * @param cargos
	 * @return
	 */
	protected List<Cargo> obtenerCopia(final Collection<Cargo> cargos){
		final List<Cargo> data=new ArrayList<Cargo>(cargos.size());
		for(Cargo c:cargos){
			data.add(getManager().getCargo(c.getId()));
		}
		return data;		
	}
	
	public void exportarSaldo(){
		if(getSelectedObject()!=null){
			Cargo c=(Cargo)getSelectedObject();
			BigDecimal saldo=ServiceLocator2.getClienteServices().getSaldo(c.getCliente());
			String res=ServiceLocator2.getClienteServices().exportarSaldo(c.getCliente(), saldo);
			String pattern="Exportación de saldo para: \n {0} \nSaldo actual: {1} \n Res:{2}";
			MessageUtils.showMessage(MessageFormat.format(pattern, c.getCliente().getNombreRazon(),saldo,res), "Exportación de información");
		}
	}
	
	public void mandarJuridico(){
		if(getSelectedObject()!=null){
			Cargo c=(Cargo)getSelectedObject();
			int index=source.indexOf(c);
			if(index!=-1){
				c=CXCUIServiceFacade.generarJuridico(c);
				source.set(index, c);
			}
			
		}
	}
	
	public void consultarJuridico(){
		if(getSelectedObject()!=null){
			Cargo c=(Cargo)getSelectedObject();
			CXCUIServiceFacade.consultarJuridico(c.getJuridico());
		}
	}
	
	public void cancelarJuridico(){
		if(getSelectedObject()!=null){
			Cargo c=(Cargo)getSelectedObject();
			int index=source.indexOf(c);
			if(index!=-1){
				if(MessageUtils.showConfirmationMessage("Cancelar trámite jurídico para el cargo:\n "+c, "Trámite Jurídico")){
					c=CXCUIServiceFacade.eliminarJuridico(c);
					source.set(index, c);
				}
			}
			
		}
	}
	
	public void imprimirNotaDeCargo(){
		Cargo c=(Cargo)getSelectedObject();
		if(c!=null){
			int index=source.indexOf(c);
			if(index!=-1){
				ImpresionUtils.imprimirNotaDeCargo(c.getId());
				c=ServiceLocator2.getCXCManager().getCargo(c.getId());
				source.set(index, c);
				selectionModel.clearSelection();
				selectionModel.setSelectionInterval(index, index);
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
	
	private OrigenDeOperacion origen=OrigenDeOperacion.CRE;

	public OrigenDeOperacion getOrigen() {
		return origen;
	}

	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getNewValue().equals(SwingWorker.StateValue.STARTED)){
			getLoadAction().setEnabled(false);
		}else if(evt.getNewValue().equals(SwingWorker.StateValue.DONE)){
			getLoadAction().setEnabled(true);
			if(grid!=null)
				grid.packAll();
		}
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
			CantidadMonetaria saldo=CXCUtils.calcularSaldo(getFilteredSource());
			CantidadMonetaria vencido=CXCUtils.calcularSaldoVencido(getFilteredSource());
			CantidadMonetaria porVencer=CXCUtils.calcularSaldoPorVencer(getFilteredSource());
			CantidadMonetaria d1_30=CXCUtils.getVencido1_30(getFilteredSource());
			CantidadMonetaria d31_60=CXCUtils.getVencido31_60(getFilteredSource());
			CantidadMonetaria d61_90=CXCUtils.getVencido61_90(getFilteredSource());
			CantidadMonetaria d90=CXCUtils.getVencidoMasDe90(getFilteredSource());
			
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

		public boolean evaluate(Object bean) {
			if((bean instanceof NotaDeCargo)){
				Cargo cargo=(Cargo)bean;
				return cargo.getAplicado().doubleValue()==0;
			}else if(bean instanceof ChequeDevuelto){
				Cargo cargo=(Cargo)bean;
				return cargo.getAplicado().doubleValue()==0;
			}
			return false;
		}
		
	}
		
	public static class PagoDiferencias implements Predicate{
		public boolean evaluate(Object bean) {
			if(bean==null) return false;
			Cargo c=(Cargo)bean;
			if(c.getTipoSiipap().equals("X")){
				if(c.getPagos().doubleValue()>0)
					return c.getSaldoCalculado().doubleValue()>0;
			}else{
				if( (c.getSaldo()!=null) && (c.getSaldoCalculado().doubleValue()<100))
					return c.getSaldoCalculado().doubleValue()>0;
			}
			return false;
		}
		
	}
	
	public static  class JuridicoAltaPredicate implements Predicate{

		public boolean evaluate(Object bean) {
			Cargo c=(Cargo)bean;
			return (
					(c!=null )
					&& (!c.isJuridico()
							));
		}
		
	}
	
	public static  class JuridicoViewPredicate implements Predicate{

		public boolean evaluate(Object bean) {
			Cargo c=(Cargo)bean;
			return (
					(c!=null )
					&& (c.isJuridico()
							));
		}
		
	}
	
	private class JuridicoMathcerEditor extends CheckBoxMatcher<Cargo>{

		@Override
		protected Matcher<Cargo> getSelectMatcher(Object... obj) {
			return new JurMacher();
		}
		private class JurMacher implements Matcher<Cargo>{
			public boolean matches(Cargo item) {
				return item.isJuridico();
			}
			
		}
		
	}
	
	private class MismoOrigenPredicate implements MultiplePredicate{

		public boolean evaluate(List data) {
			if(data.isEmpty())return true;
			OrigenDeOperacion origen=null;
			for(Object o:data){
				Cargo c=(Cargo)o;
				if(origen==null){
					origen=c.getOrigen();
					continue;
				}
				if(!c.getOrigen().equals(origen)) 
					return false;
			}
			return true;
		}
		
	}
	
}
