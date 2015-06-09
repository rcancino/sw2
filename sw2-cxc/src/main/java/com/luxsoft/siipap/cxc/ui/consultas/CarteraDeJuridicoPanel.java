package com.luxsoft.siipap.cxc.ui.consultas;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.Juridico;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargoDet;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.old.ImpresionUtils;
import com.luxsoft.siipap.cxc.rules.CXCUtils;
import com.luxsoft.siipap.cxc.service.CXCManager;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.cxc.ui.consultas.CuentasPorCobrarPanel.JuridicoViewPredicate;
import com.luxsoft.siipap.cxc.ui.form.AplicacionDePagoForm;
import com.luxsoft.siipap.cxc.ui.form.JuridicoForm;
import com.luxsoft.siipap.cxc.ui.form.NotaDeCargoForm;
import com.luxsoft.siipap.cxc.ui.form.NotaDeCargoFormModel;
import com.luxsoft.siipap.cxc.ui.model.AplicacionDePagoModel;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeDisponibles;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.reports.EstadoDeCuentaJurReport;
import com.luxsoft.siipap.reports.Juridico_ReciboEntregaAbogadoForm;
import com.luxsoft.siipap.reports.Juridico_RelacionDeTraspasosForm;
import com.luxsoft.siipap.reports.SaldosPendienteXAbogadoReportForm;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cxc.forms.SolicitudDeDepositoForm;
import com.luxsoft.sw3.cxc.selectores.SelectorDeSolicitudDeDepositos;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

/**
 * Panel para el mantenimiento de la cartera de juridico
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CarteraDeJuridicoPanel extends FilteredBrowserPanel<Cargo>{

	public CarteraDeJuridicoPanel() {
		super(Cargo.class);		
	}
	
	protected void init(){
		String[] props=new String[]{
				"origen"
				,"tipoDocto"
				,"documento"
				,"numeroFiscal"				
				,"fecha"
				,"atraso"
				,"sucursal.nombre"
				,"clave","nombre"
				,"juridico.traspaso"
				,"juridico.abogado"
				,"juridico.comentario"
				,"total"
				,"saldoCalculado"
				
				,"cargosAplicados"
				,"impreso"
				,"juridico.entregado"
				};
		String[] names=new String[]{
				"Origen"
				,"Tipo"
				,"Docto"
				,"N.Fiscal"
				,"Fecha"
				,"Atr"
				,"Suc"
				,"Cliente"
				,"Nombre"
				,"Traspaso"
				,"Abogado"
				,"Comentario"
				,"Total"
				,"Saldo"
								
				,"Nota de Cargo"
				,"Impreso"
				,"Entregado"
				};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("N.Fiscal", "numeroFiscal");
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		installTextComponentMatcherEditor("Total", "total");
		//manejarPeriodo();
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,addAction(null,"registrarDeposito","Registrar Deposito")
				,addAction(null,"consultarSolicitudes","Consultar Solicitudes")
				
				,addAction(CXCActions.AplicarPago.getId(), "aplicarPago", "Aplicar abono")
				,addAction(CXCActions.GenerarNotaDeBonificacion.getId(), "generarBonificacion", "Bonificacion" )
				,addAction(CXCActions.RefrescarSeleccion.getId(), "refreshSelection", "Refrescar(Sel)" )
				,addContextAction(new JuridicoViewPredicate(), CXCActions.JuridicoView.getId(), "consultarJuridico", "Consultar Detalle")
				,addContextAction(new JuridicoViewPredicate(), CXCActions.JuridicoBaja.getId(), "cancelarJuridico", "Cancelar Jurídico")
				,addContextAction(new SelectionPredicate(), CXCActions.ConsultarDisponibles.getId(), "disponibles", "Disponibles")
				
				,addMultipleContextAction(new RegistrarEntregaPredicate(), CXCActions.JuridicoBaja.getId(), "registrarEntrega", "Registrar entrega")
				,addMultipleContextAction(new CancelarEntregaPredicate(), CXCActions.JuridicoBaja.getId() , "cancelarEntrega", "Cancelar entrega")
				,addAction(CXCActions.GenerarNotaDeCargo.getId(), "generarNotaCargo", "Generar N.Cargo" )

				,addContextAction(new CuentasPorCobrarPanel.PagoDiferencias(), CXCActions.GenerarPagoDiferencias.getId(), "pagarDiferencias", "Pago de diferencias")
				,addContextAction(new Predicate(){

					public boolean evaluate(Object bean) {
						Cargo c=(Cargo)bean;
						if(c!=null){
							return c.getSaldoCalculado().doubleValue()>0;
						}
						return false;
					}
					
				}, CXCActions.GenerarPagoDiferencias.getId(), "hacerIncobrable", "Pago por Incobrable")
				
				,addAction(CXCActions.ReportesDeCarteraDeJuridico.getId(), "reporteDeTraspasos", "Reporte de Traspasos")
				,addAction(CXCActions.ReportesDeCarteraDeJuridico.getId(), "reporteDeEntrega", "Reporte de Entrega")				
				,addAction("","saldosPendientes", "Saldos pendientes ")
				,addAction("","reporteDeEstadoDeCuentaJur", "Estado de cuenta")
				
				};
		return actions;
	}

	public void saldosPendientes(){
		SaldosPendienteXAbogadoReportForm.run();
	}
	
	
	@Override
	protected List<Cargo> findData() {
		return ServiceLocator2.getHibernateTemplate().find("select j.cargo from Juridico j");
	}
	
	public void aplicarPago(){
		//CXCUIServiceFacade.aplicarPago();
		final AplicacionDePagoModel aplicacionDePagoModel=new AplicacionDePagoModel();	
		aplicacionDePagoModel.setOrigen(OrigenDeOperacion.JUR);
		final AplicacionDePagoForm form=new AplicacionDePagoForm(aplicacionDePagoModel,OrigenDeOperacion.JUR);
		form.open();
		if(!form.hasBeenCanceled()){			
			Abono abono=aplicacionDePagoModel.procesar();
			getManager().salvarAbono(abono);			
		}	
	}
	
	public void generarBonificacion(){
		if(getSelected().isEmpty())
			CXCUIServiceFacade.generarNotaDeBonificacion(OrigenDeOperacion.JUR);
		else{
			List<Cargo> cargos=new ArrayList<Cargo>();
			cargos.addAll(getSelected());
			CXCUIServiceFacade.generarNotaDeBonificacion(OrigenDeOperacion.JUR,cargos);
		}
	}
	
	/*
	@Override
	protected Cargo doEdit(Cargo bean) {
		bean=getManager().getCargo(bean.getId());
		Juridico jur=JuridicoForm.showForm(bean);
		if(jur!=null){
			getManager().generarJuridico(jur);
			return getManager().getCargo(cargo.getId());
		}
		return cargo;
	}
*/
	public void generarNotaCargo(){
		
		final NotaDeCargoFormModel model=new NotaDeCargoFormModel();
		model.asignarFolio();
		model.setOrigen(OrigenDeOperacion.JUR);
		final NotaDeCargoForm form=new NotaDeCargoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			NotaDeCargo res=model.commit();
			boolean especial=res.isEspecial();
			if(especial){				
				NotaDeCargoDet det=res.getConceptos().iterator().next();
				String comentario="ESTE COMPROBANTE ES COMPLEMENTO DEL EXPEDIDO CON EL " +
						";Documento No: "+det.getVenta().getDocumento()
						+"-"+det.getVenta().getNumeroFiscal()
						+" Fecha:"+new SimpleDateFormat("dd/MM/yyyy").format(det.getVenta().getFecha());
				
				res.setComentario(comentario);
			}
			if(!res.getCliente().isFrecuente()){
				res.setOrigen(OrigenDeOperacion.CAM);
			}
			res=(NotaDeCargo)getManager().save(res);
			// Linea agregada por Luis para mandar a Timbra las notas hechas en Juridico
			CXCUIServiceFacade.timbrar(res);
			//ImpresionUtils.imprimirNotaDeCargo(res.getId(),especial);
			source.add(res);
			if(res.getOrigen().equals(OrigenDeOperacion.JUR)){
				CXCUIServiceFacade.generarJuridico(res);
			}
		}
		
	}
	
	public void disponibles(){
		Cargo selected=(Cargo)getSelectedObject();
		SelectorDeDisponibles.buscar(selected.getCliente());
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
	
	
	public void registrarEntrega(){
		if(!getSelected().isEmpty()){
			Date fecha=SelectorDeFecha.seleccionar();
			if(fecha!=null){
				for(Object o:getSelected()){					
					Cargo c=(Cargo)o;
					c.getJuridico().setEntregado(fecha);
					ServiceLocator2.getUniversalManager().save(c.getJuridico());
				}
				refreshSelection();
			}
		}
	}
	
	public void cancelarEntrega(){
		if(!getSelected().isEmpty()){
			
			if(MessageUtils.showConfirmationMessage("Cancelar entrega al abogado", "Cancelaciones")){
				for(Object o:getSelected()){
					Cargo c=(Cargo)o;
					c.getJuridico().setEntregado(null);
					ServiceLocator2.getUniversalManager().save(c.getJuridico());
					refreshSelection();
				}
			}
		}
	}
	
	public void pagarDiferencias(){
		Cargo c=(Cargo)getSelectedObject();
		if(MessageUtils.showConfirmationMessage("Aplicar pago automático por: "+c.getSaldoCalculado(), "Pago de diferencias")){
			CXCUIServiceFacade.aplicarPagoDiferencias(c);
			refreshSelection();
		}
	}
	
	public void hacerIncobrable(){
		Cargo c=(Cargo)getSelectedObject();
		if(MessageUtils.showConfirmationMessage("Monto ha destinar: "+c.getSaldoCalculado(), "Cuentas incobrables")){
			getManager().generarPagoPorIncobrabilidad(c, "");
			refreshSelection();
		}
	}
	
	public void registrarDeposito(){
		SolicitudDeDeposito sol=SolicitudDeDepositoForm.generar(OrigenDeOperacion.JUR);
		if(sol!=null){
			sol=ServiceLocator2.getSolicitudDeDepositosManager().save(sol);
			MessageUtils.showMessage("Solicitud generada: "+sol.getDocumento(), "Solicitud de depositos");
		}
	}
	
	public void consultarSolicitudes(){
		SelectorDeSolicitudDeDepositos.buscar(OrigenDeOperacion.JUR);
	}
	
	public void reporteDeTraspasos(){
		Juridico_RelacionDeTraspasosForm.run();
	}
	
	public void reporteDeEntrega(){
		Juridico_ReciboEntregaAbogadoForm.run();
	}
	
	
	public void reporteDeEstadoDeCuentaJur(){
		EstadoDeCuentaJurReport.run();
	}
	
	public CXCManager getManager(){
		return ServiceLocator2.getCXCManager();
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
	
	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	
	
	
	
private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel saldoTotal=new JLabel();
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			saldoTotal.setHorizontalAlignment(SwingConstants.RIGHT);
			
			builder.append("Saldo",saldoTotal);
			
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
			String pattern="{0}  ({1})";
			saldoTotal.setText(MessageFormat.format(pattern, saldo.amount(),part(saldo,saldo)));
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

	private class RegistrarEntregaPredicate implements MultiplePredicate{

		public boolean evaluate(List data) {
			for(Object o:data){
				Cargo c=(Cargo)o;
				if(c.getJuridico()==null) 
					return false;
				if(c.getJuridico().getEntregado()!=null)
					return false;
			}
			return true;
		}
		
	}
	
	private class CancelarEntregaPredicate implements MultiplePredicate{

		public boolean evaluate(List data) {
			for(Object o:data){
				Cargo c=(Cargo)o;
				if(c.getJuridico()==null) 
					return false;
				if(c.getJuridico().getEntregado()==null)
					return false;
			}
			return true;
		}
		
	}

}
