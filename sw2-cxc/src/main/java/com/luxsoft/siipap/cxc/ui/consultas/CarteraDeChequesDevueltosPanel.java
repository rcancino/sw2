package com.luxsoft.siipap.cxc.ui.consultas;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.ChequeDevuelto;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargoDet;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.old.ImpresionUtils;
import com.luxsoft.siipap.cxc.rules.CXCUtils;
import com.luxsoft.siipap.cxc.service.CXCManager;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.cxc.ui.consultas.CuentasPorCobrarPanel.JuridicoAltaPredicate;
import com.luxsoft.siipap.cxc.ui.form.NotaDeCargoForm;
import com.luxsoft.siipap.cxc.ui.form.NotaDeCargoFormModel;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeDisponibles;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.reports.ChequesDevueltosReportForm;
import com.luxsoft.siipap.reports.ChequesDevueltos_RelacionForm;
import com.luxsoft.siipap.reports.RelacionDeChequesDevueltosReport;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.CFDPrintServicesCxC;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.cxc.forms.SolicitudDeDepositoForm;
import com.luxsoft.sw3.cxc.selectores.SelectorDeSolicitudDeDepositos;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

/**
 * Lista de cargos relacionados con cheques devueltos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CarteraDeChequesDevueltosPanel extends FilteredBrowserPanel<Cargo>{

	public CarteraDeChequesDevueltosPanel() {
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
				,"total"
				,"saldoCalculado"
				,"comentario"
				,"cargos"
				,"cargosAplicados"
				,"impreso"
				,"fechaDeEntrega"
				,"juridico"
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
				,"Total"
				,"Saldo"
				,"Comentario"
				,"Cargo"
				,"Nota de Cargo"
				,"Impreso"
				,"Recibido"
				,"Jur"
				};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("N.Fiscal", "numeroFiscal");
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		installTextComponentMatcherEditor("Total", "total");
		
		manejarPeriodo();
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
				}, CXCActions.ImprimirNotaDeCargo.getId(), "cambiarFechaDeNotaDeCargo", "Cambiar fecha N.C")
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
				
			//	,addContextAction(new CancelacionDeCargoPredicate(), CXCActions.CancelarNotaDeCargo.getId(), "cancelarCargo", "Cancelar Cargo" )
				,addAction(CXCActions.GenerarNotaDeCargo.getId(), "generarNotaCargo", "Generar N.Cargo" )
				
			
				
				,addAction(CXCActions.RefrescarSeleccion.getId(), "refreshSelection", "Refrescar(Sel)" )
				,addContextAction(new JuridicoAltaPredicate(), CXCActions.JuridicoAlta.getId(), "mandarJuridico", "Mandar a Jurídico")
				,addContextAction(new SelectionPredicate(), CXCActions.ConsultarDisponibles.getId(), "disponibles", "Disponibles")
				
				,addMultipleContextAction(new RegistrarRecepcionPredicate(), CXCActions.JuridicoAlta.getId(), "registrarRecepcion", "Recepción")
				,addMultipleContextAction(new CancelarRecepcionPredicate(), CXCActions.JuridicoAlta.getId(), "cancelarRecepcion", "CanRecepción")
				
				
				,addAction( CXCActions.GenerarChequeDevuelto.getId(), "imprimirFormatosDeCargo", "Imprimir Cargos")
				,addAction(CXCActions.GenerarChequeDevuelto.getId(), "imprimirReplacionDeCargos", "Relación de Cheques")
				
				,addAction( CXCActions.ReportesDeCarteraDeCheques.getId(), "reporteDePendientes", "Cheques Pendientes")
				,addAction( CXCActions.GenerarChequeDevuelto.getId(), "reporteDeRecepcion", "Cheques Devueltos")
				,addContextAction(new CuentasPorCobrarPanel.PagoDiferencias(), CXCActions.GenerarPagoDiferencias.getId(), "pagarDiferencias", "Pago de diferencias")
				,addAction(null, "reporteRelacionDeChequesDevueltos", "Cheques Devueltos Por Cte")
				};
		return actions;
	}
	
	

	@Override
	protected List<Cargo> findData() {
		String hql1="from Cargo c where c.fecha between ? and ? and c.origen=\'CHE\' ";
		List<Cargo> res=ServiceLocator2.getHibernateTemplate().find(hql1, new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
		CollectionUtils.filter(res, new org.apache.commons.collections.Predicate(){
			public boolean evaluate(Object object) {
				Cargo c=(Cargo)object;
				return c.getJuridico()==null;
			}
		});
		return res;
	}
	
	public void aplicarPago(){
		CXCUIServiceFacade.aplicarPago();
	}
	
	public void generarBonificacion(){
		if(getSelected().isEmpty())
			CXCUIServiceFacade.generarNotaDeBonificacion(OrigenDeOperacion.CHE);
		else{
			List<Cargo> cargos=new ArrayList<Cargo>();
			cargos.addAll(getSelected());
			CXCUIServiceFacade.generarNotaDeBonificacion(OrigenDeOperacion.CHE,cargos);
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
	
	public void pagarDiferencias(){
		Cargo c=(Cargo)getSelectedObject();
		if(MessageUtils.showConfirmationMessage("Aplicar pago automático por: "+c.getSaldoCalculado(), "Pago de diferencias")){
			CXCUIServiceFacade.aplicarPagoDiferencias(c);
			refreshSelection();
		}
	}
	
	public void imprimirNotaDeCargo(){
		Cargo c=(Cargo)getSelectedObject();
		if(c!=null){
			CFDPrintServicesCxC.imprimirNotaDeCargoElectronica(c.getId());
			/*int index=source.indexOf(c);
			if(index!=-1){
				//ImpresionUtils.imprimirNotaDeCargo(c.getId());
				//NotaDeCargo nc=(NotaDeCargo)ServiceLocator2.getCXCManager().getCargo(c.getId());
				//ComprobanteFiscal cf=ServiceLocator2.getCFDManager().cargarComprobante(nc);
				//CFDPrintServicesCxC.imprimirNotaDeCargoElectronica(c.getId());
				//c=ServiceLocator2.getCXCManager().getCargo(c.getId());
				//source.set(index, c);
				//selectionModel.clearSelection();
				//selectionModel.setSelectionInterval(index, index);
			} */
		}
	}
	
	public void cambiarFechaDeNotaDeCargo(){
		
		NotaDeCargo c=(NotaDeCargo)getSelectedObject();
		if(c!=null){
			int index=source.indexOf(c);
			if(index!=-1){
				Date fecha=SelectorDeFecha.seleccionar(c.getFecha());
				if(fecha!=null){
					c.setFecha(fecha);
					c=(NotaDeCargo)ServiceLocator2.getCXCManager().save(c);
					source.set(index, c);
				}
			} 
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
	
	public void registrarRecepcion(){
		if(!getSelected().isEmpty()){
			Date fecha=SelectorDeFecha.seleccionar();
			if(fecha!=null){
				for(Object o:getSelected()){
					if(o instanceof ChequeDevuelto){
						ChequeDevuelto c=(ChequeDevuelto)o;
						c.getCheque().setRecepcionDevolucion(fecha);
						ServiceLocator2.getUniversalManager().save(c.getCheque());
					}
					
				}
				refreshSelection();
			}
		}
	}
	
	public void cancelarRecepcion(){
		if(!getSelected().isEmpty()){
			if(MessageUtils.showConfirmationMessage("Cancelar entrega al abogado", "Cancelaciones")){
				for(Object o:getSelected()){
					if(o instanceof ChequeDevuelto){
						ChequeDevuelto c=(ChequeDevuelto)o;
						c.getCheque().setRecepcionDevolucion(null);
						ServiceLocator2.getUniversalManager().save(c.getCheque());
					}
					
				}
				refreshSelection();
			}
		}
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
	
	public void generarNotaCargo(){
		
		final NotaDeCargoFormModel model=new NotaDeCargoFormModel();
		model.asignarFolio();
		model.setOrigen(OrigenDeOperacion.CHE);
		final NotaDeCargoForm form=new NotaDeCargoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			NotaDeCargo res=model.commit();
			boolean especial=res.isEspecial();
			if(especial){
				System.out.println("Generando una nota de cargo especial....");
				NotaDeCargoDet det=res.getConceptos().iterator().next();
				String comentario="ESTE COMPROBANTE ES COMPLEMENTO DEL EXPEDIDO CON EL " +
						";Documento No: "+det.getVenta().getDocumento()
						+"-"+det.getVenta().getNumeroFiscal()
						+" Fecha:"+new SimpleDateFormat("dd/MM/yyyy").format(det.getVenta().getFecha());
				
				res.setComentario(comentario);
			}
			res=(NotaDeCargo)getManager().save(res);			
			ImpresionUtils.imprimirNotaDeCargo(res.getId(),especial);
			source.add(res);
		}
		
		
		/*	
		ChequeDevuelto c=(ChequeDevuelto)getSelectedObject();
		if(c!=null){
			NotaDeCargo nc=getManager().generarNotaDeCargoPorChequeDevuelto(c);
			if(MessageUtils.showConfirmationMessage("Desa imprimir la nota de cargo?", "Notas de cargo para cheques devueltos")){
				ImpresionUtils.imprimirNotaDeCargo(nc.getId());
				nc=(NotaDeCargo)getManager().getCargo(nc.getId());
			}
			source.add(nc);
		}
		*/
	}
	
	public void registrarDeposito(){
		SolicitudDeDeposito sol=SolicitudDeDepositoForm.generar(OrigenDeOperacion.CHE);
		if(sol!=null){
			sol=ServiceLocator2.getSolicitudDeDepositosManager().save(sol);
			MessageUtils.showMessage("Solicitud generada: "+sol.getDocumento(), "Solicitud de depositos");
		}
	}
	
	public void consultarSolicitudes(){
		SelectorDeSolicitudDeDepositos.buscar(OrigenDeOperacion.CHE);
	}
	
	public void imprimirFormatosDeCargo(){
		ChequesDevueltosReportForm.run();
	}
	
	public void imprimirReplacionDeCargos(){
		ChequesDevueltos_RelacionForm.run();
	}
	
	public void reporteDePendientes(){
		ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/ChequesPendientesPorRecibir.jasper"), new HashMap<String, Object>());
	}
	
	public void reporteRelacionDeChequesDevueltos(){
		RelacionDeChequesDevueltosReport report=new RelacionDeChequesDevueltosReport();
		report.run();
		
	}
	
	public void reporteDeRecepcion(){
		Date fecha=SelectorDeFecha.seleccionar();
		if(fecha!=null){
			Map map=new HashMap();
			map.put("FECHA_INI", fecha);
			ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/ChequesDevueltos.jasper"),map);
		}
	}
	
private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	private class CancelacionDeCargoPredicate implements Predicate{

		public boolean evaluate(Object bean) {
			Cargo cc=(Cargo)bean;
			if(cc==null)
				return false;
			if(cc.getCancelacion()!=null)
				return false;
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
	
	private class GenerarNotaDeCargoPredicate implements Predicate{
		public boolean evaluate(Object bean) {
			if(bean instanceof ChequeDevuelto){
				if(bean!=null){
					ChequeDevuelto ch=(ChequeDevuelto)bean;
					return ch.getCargosAplicados().doubleValue()==0;
				}else
					return false;
			}
			return false;
			
		}
	}
	
	private class RegistrarRecepcionPredicate implements MultiplePredicate{

		public boolean evaluate(List data) {
			for(Object o:data){
				Cargo c=(Cargo)o;
				if(c instanceof ChequeDevuelto){
					ChequeDevuelto ch=(ChequeDevuelto)c;
					return ch.getCheque().getRecepcionDevolucion()==null;
				}
				return false;
			}
			return true;
		}
		
	}
	
	private class CancelarRecepcionPredicate implements MultiplePredicate{

		public boolean evaluate(List data) {
			for(Object o:data){
				Cargo c=(Cargo)o;
				if(c instanceof ChequeDevuelto){
					ChequeDevuelto ch=(ChequeDevuelto)c;
					return ch.getCheque().getRecepcionDevolucion()!=null;
				}
				return false;
			}
			return true;
		}
		
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

}
