package com.luxsoft.siipap.cxc.ui.consultas;

import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.ChequeDevuelto;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.cxc.service.CXCManager;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.cxc.ui.form.GeneracionDeFichas;
import com.luxsoft.siipap.cxc.ui.model.FichasFormModel;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.reports.RelacionDePagos;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.cfd.CFDPrintServicesCxC;

/**
 * Browser con los pagos registrados
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PagosPanel extends AbstractMasterDatailFilteredBrowserPanel<Pago, Aplicacion>{

	public PagosPanel() {
		super(Pago.class);
		
	}
	
	protected void init(){
		super.init();
		addProperty("nombre","cobrador.nombres","sucursal.nombre","fecha","total","disponible","banco","info","depositoInfo","comentario","enviado","origen","anticipo");
		addLabels("Cliente","Cobrador","Sucursal","Fecha","Total","Disponible","Banco","Referencia","Deposito","Comentario","Enviado","Origen","Anticipo");
		manejarPeriodo();
		setDetailTitle("Aplicaciones");
		//installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Origen",GlazedLists.textFilterator("origen"), new JTextField("CRE"));
		installTextComponentMatcherEditor("Cliente", "nombre");
		installTextComponentMatcherEditor("Cobrador", "cobrador.nombres");
		installTextComponentMatcherEditor("Banco", "banco");
		TextFilterator<Pago> tf1=new TextFilterator<Pago>(){
			public void getFilterStrings(List<String> baseList, Pago element) {
				String info=element.getInfo();
				if(!StringUtils.isBlank(info))
					baseList.add(info);
			}
			
		};
		installTextComponentMatcherEditor("Referencia",tf1, new JTextField());
		installTextComponentMatcherEditor("Total", "total");
		
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodo(-5);
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,addAction(CXCActions.CancelarAbono.getId(), "cancelarPago", "Cancelar Pago")
				,addAction(CXCActions.PagarConCheque.getId(), "cheque", "Cheque")
				//,addAction(CXCActions.PagarConDeposito.getId(), "deposito", "Deposito")
				,addAction(CXCActions.PagarConTarjeta.getId(), "tarjeta", "Tarjeta")
				,addAction(CXCActions.PagarConEfectivo.getId(), "efectivo", "Efectivo")
				,getSecuredEditAction(CXCActions.ModificarPago.getId())
				,addContextAction(new ChequeDevueltoPredicate(), CXCActions.GenerarChequeDevuelto.getId(), "generarChequeDevuelto", "Cheque devuelto")
				,addAction(CXCActions.GenerarFichasDeDeposito.getId(), "generarDepositos", "Depositos (Fichas)")
				,addAction(CXCActions.ReportesDePagos.getId(), "generarReporteDePagos", "Resumen de pagos")				};
		return actions;
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"cargo.documento","cargo.fecha","cargo.total","importe"};
		String[] names={"Docto","Fecha D.","Total","Importe"};
		return GlazedLists.tableFormat(Aplicacion.class, props,names);
	}

	@Override
	protected Model<Pago, Aplicacion> createPartidasModel() {
		return new Model<Pago, Aplicacion>(){
			public List<Aplicacion> getChildren(Pago parent) {
				//return parent.getAplicaciones();
				return ServiceLocator2.getHibernateTemplate()
				.find("from Aplicacion a where a.abono.id=?",parent.getId());
			}
		};
	}

	public void cheque(){
		Pago pago=CXCUIServiceFacade.registrarPagoConCheque();
		if(pago!=null){
			source.add(pago);
			cheque();
		}
	}
	
	public void tarjeta(){
		Pago pago=CXCUIServiceFacade.registrarPagoConTarjeta();
		if(pago!=null){
			source.add(pago);
			tarjeta();
		}
	}
	/*
	public void deposito(){
		Pago pago=CXCUIServiceFacade.registrarPagoConDeposito();
		if(pago!=null){
			source.add(pago);
			deposito();
		}
		
	}*/
	
	public void efectivo(){
		Pago pago=CXCUIServiceFacade.registrarPagoEnEfectivo();
		if(pago!=null){
			source.add(pago);
			efectivo();
		}
	}
	
	public void generarChequeDevuelto(){
		if(getSelected().isEmpty()) return;
		PagoConCheque pago=(PagoConCheque)getSelectedObject();
		
		if(MessageUtils.showConfirmationMessage(
				"Generar cargo y nota de cargo por cheque devuelto para el documento: "+pago.getNumero()
				, "Cargo por Cheque devuelto")){
			
			Date fecha=SelectorDeFecha.seleccionar("Fecha registrada por el banco");
			int index=source.indexOf(pago);
			ChequeDevuelto cheque=getManager().generarChequeDevuelto(pago,fecha);
			
			PagoConCheque pp=(PagoConCheque)getManager().getAbono(cheque.getCheque().getId());
			if(index!=-1){
				source.set(index, pp);
				MessageUtils.showMessage("Cargo generado: "+cheque, "Cargo por Cheque");
			}
			List<NotaDeCargo> res=ServiceLocator2.getHibernateTemplate().find("from NotaDeCargo n where n.cheque.id=?",pp.getId());
			if(!res.isEmpty()){
				NotaDeCargo nc=res.get(0);
				CFDPrintServicesCxC.imprimirNotaDeCargoElectronica(nc.getId());
			}
			
		}
		
	}
	
	@Override
	protected Pago doEdit(Pago bean) {
		if(bean instanceof PagoConCheque){
			return CXCUIServiceFacade.editarPagoConCheque((PagoConCheque)bean);
		}else if(bean instanceof PagoConDeposito){
			return CXCUIServiceFacade.editarPagoConDeposito(bean.getId());
		}else if(bean instanceof PagoConDeposito){
			return CXCUIServiceFacade.editarPagoEnEfectivo(bean.getId());
		}else if(bean instanceof PagoConTarjeta){
			return CXCUIServiceFacade.editarPagoConTarjeta(bean.getId());
		}
		return null;
	}

	public void generarDepositos(){	
		if(getSelected().isEmpty()) return;
		final FichasFormModel model=new FichasFormModel();
		
		model.generarFichas(getSelected());
		
		if(model.getPartidas().isEmpty()){
			MessageUtils.showMessage("No existe pagos depositables pendientes en el periodo"
					, "Registro de depositos");
			return;
		}
		model.setValue("cuenta", ServiceLocator2.getConfiguracion().getCuentaPreferencial());
		GeneracionDeFichas form=new GeneracionDeFichas(model);
		form.open();
		if(!form.hasBeenCanceled()){
			model.comit();
			for(Ficha f:model.getFichas()){
				try {
					//f=(Ficha)ServiceLocator2.getUniversalDao().save(f);
					f=ServiceLocator2.getDepositosManager().save(f);
					ServiceLocator2.getIngresosManager().registrarIngresoPorFicha(f);
					//actualizarPagos(f);
					selectionModel.clearSelection();
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}
				
			}				
		}else{
			model.cancel();
		}
	}
	
	private void actualizarPagos(final Ficha f){
		try {
			List<String> pagos=ServiceLocator2.getHibernateTemplate().find(
					"select d.pago.id  from FichaDet d left join fetch d.pago p where d.ficha.id=?", f.getId());
			for(String id:pagos)
				refrescarAbono(id);
		} catch (Exception e) {
			logger.error(e);
		}
		
	}
	
	public void cancelarPago(){
		if(getSelectedObject()!=null){			
			Pago a=(Pago)getSelectedObject();
			if(MessageUtils.showConfirmationMessage("Cancelar  :\n"+a.toString()
					, "Cancelación de documentos")){				
				getManager().cancelarPago(a.getId(), null);
				refrescarAbono(a.getId());
			}
		}	
	}
	
	/**
	 * Refresca desde la base de datos el abono indicado
	 * 
	 * @param id
	 */
	public void refrescarAbono(String id){
		try {
			Pago a=(Pago)getManager().getAbono(id);
			for(int index=0;index<source.size();index++){
				Pago other=(Pago)source.get(index);
				if(other.getId().equals(a.getId()))
					source.set(index, a);
			}			
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}		
	}
	
	public void generarReporteDePagos(){
		RelacionDePagos report=new RelacionDePagos();
		report.actionPerformed(null);
	}

	@Override
	protected List<Pago> findData() {		
		return ServiceLocator2.getCXCManager().getPagoDao().buscarPagosEnTesoreria(periodo);
	}
	
	public CXCManager getManager(){
		return ServiceLocator2.getCXCManager();
	}

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}
	
/*	private class ChequeDevueltoPredicate implements Predicate{

		public boolean evaluate(Object bean) {
			if( bean instanceof PagoConCheque ){
				PagoConCheque pp=(PagoConCheque)bean;
					return 
					(StringUtils.isBlank(pp.getComentario()) ||
							(!pp.getComentario().startsWith("DEV"))
							);
			}
			else
				return false;
		}
		
	}*/
	
	private class ChequeDevueltoPredicate implements Predicate{

		public boolean evaluate(Object bean) {
			if( bean instanceof PagoConCheque ){
				PagoConCheque pp=(PagoConCheque)bean;
					return 
					(StringUtils.isBlank(pp.getComentario()) ||
							(!pp.getComentario().startsWith("DEV"))
							);
			}
			else
				return false;
		}
		
	}
	
	
	/*
	public void fechaBanco(){
			if(!getSelected().isEmpty()){
				Date fecha=(Date) SelectorDeFecha.seleccionar();
				if(fecha!=null){
					for(Object o:getSelected()){					
						Cargo c=(Cargo)o;
						c.getJuridico().setEntregado(fecha);
						ServiceLocator2.getUniversalManager().save(c.getJuridico());
					}
					
				}
			}
		}*/
	

}
