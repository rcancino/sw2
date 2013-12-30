package com.luxsoft.siipap.cxc.ui;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.util.Assert;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.AutorizacionesCxC;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.CargoPorDiferencia;
import com.luxsoft.siipap.cxc.model.Juridico;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargoDet;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDescuento;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.cxc.model.PagoDeDiferencias;
import com.luxsoft.siipap.cxc.model.PagoEnEspecie;
import com.luxsoft.siipap.cxc.rules.CXCUtils;
import com.luxsoft.siipap.cxc.rules.NotaDescuentoRules;
import com.luxsoft.siipap.cxc.service.CXCManager;
import com.luxsoft.siipap.cxc.service.ClienteServices;
import com.luxsoft.siipap.cxc.ui.form.AplicacionDeDiferenciasForm;
import com.luxsoft.siipap.cxc.ui.form.AplicacionDePagoForm;
import com.luxsoft.siipap.cxc.ui.form.DescuentoEspecialForm;
import com.luxsoft.siipap.cxc.ui.form.JuridicoForm;
import com.luxsoft.siipap.cxc.ui.form.NotaDeCargoForm;
import com.luxsoft.siipap.cxc.ui.form.NotaDeCargoFormModel;
import com.luxsoft.siipap.cxc.ui.form.NotaDeCreditoBonificacionForm;
import com.luxsoft.siipap.cxc.ui.form.NotaDeCreditoBonificacionForm2;
import com.luxsoft.siipap.cxc.ui.form.NotaDeCreditoBonificacionFormModel;
import com.luxsoft.siipap.cxc.ui.form.NotaDeCreditoBonificacionFormModel2;
import com.luxsoft.siipap.cxc.ui.form.NotaDeDescuentoForm;
import com.luxsoft.siipap.cxc.ui.form.NotaDevolucionFormModel;
import com.luxsoft.siipap.cxc.ui.form.NotaDevoucionForm;
import com.luxsoft.siipap.cxc.ui.model.AplicacionDePagoModel;
import com.luxsoft.siipap.cxc.ui.model.DescuentoEspecialFormModel;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeRMD2;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.ValidationUtils;
import com.luxsoft.siipap.ventas.model.DescuentoEspecial;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.CFDPrintServicesCxC;


/**
 * Metodos estaticos para las tareas generales del modulo de cuentas
 * por pagar
 * 
 * @author Ruben Cancino
 * TODO Mover a una clase de instancia administrada en un contexto de Spring
 *
 */
public class CXCUIServiceFacade {
	
	
	
	private static Logger logger=Logger.getLogger(CXCUIServiceFacade.class);
	
	/***** SECCION DE CACHES (ALGUNAS LISTAS MUY USUADAS) ******/
	// TODO Mover a Una clase de cache
	
	private static EventList<Cliente> clientes=new BasicEventList<Cliente>();
	
	/**
	 * 
	 * 
	 * @return
	 */
	public static EventList<Cliente> getClientes(){
		if(clientes.isEmpty()){
			relodClientes();
		}
		return clientes;
	}
	
	public static void relodClientes(){
		logger.info("Cargando clientes cache...");
		clientes.clear();
		clientes.addAll(ServiceLocator2.getClienteManager().getAll());
	}
	
	private static List<String> bancos=new ArrayList<String>();
	
	public static List<String> getBancos(){
		if(bancos.isEmpty()){
			for(Banco b:ServiceLocator2.getLookupManager().getBancos()){
				bancos.add(b.getClave());
			}
		}
		return bancos;
	}
	
	
	
	private static EventList<Cuenta> cuentasDeBanco=new BasicEventList<Cuenta>();
	
	public static EventList<Cuenta> getCuentasDeBanco(){
		if(cuentasDeBanco.isEmpty()){
			cuentasDeBanco.addAll(ServiceLocator2.getLookupManager().getCuenta());
		}
		return cuentasDeBanco;
	}
	
	/*** SECCION DE PAGOS ***/
	

	/**
	 * Registra un pago con tarjeta
	 * 
	 * @return 
	 */
	public static PagoConTarjeta registrarPagoConTarjeta(){
		PagoConTarjeta bean=new PagoConTarjeta();
		final PagoConTarjetaFormModel model=new PagoConTarjetaFormModel(bean,false);
		PagoConTarjeta proxy=PagoConTarjetaForm.showForm(model);
		if(proxy!=null){			
			PagoConTarjeta res=(PagoConTarjeta) ServiceLocator2.getCXCManager().salvarPago(proxy);
			try {
				//ServiceLocator2.getIngresosManager().registrarIngreso(res);
			} catch (Exception e) {
				logger.info("Imposible registrar el pago en tesoreria",e);
				e.printStackTrace();
			}
			
			return res;
		}else
			return null;
	}
	
	public static PagoConTarjeta editarPagoConTarjeta(String id){
		PagoConTarjeta pago=(PagoConTarjeta)getManager().getAbono(id);
		if(!pago.getAplicaciones().isEmpty()){
			MessageUtils.showMessage("El pago tiene aplicaciones no se puede editar", "Pagos");
			return null;
		}
		final PagoConTarjetaFormModel model=new PagoConTarjetaFormModel(pago,false);
		PagoConTarjeta proxy=PagoConTarjetaForm.showForm(model);
		if(proxy!=null){			
			return ServiceLocator2.getCXCManager().salvarPago(proxy);
		}else
			return null;
	}
	
	/**
	 * Registra un pago con efectivo
	 * 
	 * @return
	 */
	public static PagoConEfectivo registrarPagoEnEfectivo(){
		
		PagoFormModel model=new PagoFormModel(new PagoConEfectivo(),false);		
		PagoConEfectivo proxy=PagoConEfectivoForm.showForm(model);
		if(proxy!=null){			
			return ServiceLocator2.getCXCManager().salvarPago(proxy);
		}else
			return null;
	}
	
	public static PagoConEfectivo editarPagoEnEfectivo(String id){
		PagoConEfectivo pago=(PagoConEfectivo)getManager().getAbono(id);
		if(!pago.getAplicaciones().isEmpty()){
			MessageUtils.showMessage("El pago tiene aplicaciones no se puede editar", "Pagos");
			return null;
		}
		PagoFormModel model=new PagoFormModel(pago,false);		
		PagoConEfectivo proxy=PagoConEfectivoForm.showForm(model);
		if(proxy!=null){			
			return ServiceLocator2.getCXCManager().salvarPago(proxy);
		}else
			return null;
	}
	
	/**
	 * Registra un pago con cheque
	 * 
	 * @return
	 */
	public static PagoConCheque registrarPagoConCheque(){		
		PagoConChequeFormModel model=new PagoConChequeFormModel(new PagoConCheque(),false);
		PagoConCheque proxy=PagoConChequeForm.showForm(model);
		if(proxy!=null){			
			PagoConCheque pago= ServiceLocator2.getCXCManager().salvarPago(proxy);
			if(pago!=null){
				agregarCuenta(pago.getClave(),pago.getCuentaDelCliente());
			}
			return pago;
		}else
			return null;
	}
	
	public static PagoConCheque editarPagoConCheque(final PagoConCheque source){
		PagoConCheque pago=(PagoConCheque)getManager().getAbono(source.getId());
		if(!pago.getAplicaciones().isEmpty()){
			MessageUtils.showMessage("El pago tiene aplicaciones no se puede editar", "Pagos");
			return null;
		}
		PagoConChequeFormModel model=new PagoConChequeFormModel(pago,false);
		PagoConCheque proxy=PagoConChequeForm.showForm(model);
		if(proxy!=null){			
			PagoConCheque res= ServiceLocator2.getCXCManager().salvarPago(proxy);
			if(res!=null){
				agregarCuenta(pago.getClave(),res.getCuentaDelCliente());
			}
			return res;
		}else
			return null;
	}
	
	
	/**
	 * Agrega en un sub proceso la cuenta al grupo de cuentas del cliente 
	 * 
	 * @param pago
	 */
	private static void agregarCuenta(final String clave,final String cuenta){
		final SwingWorker worker= new SwingWorker(){			
			protected Object doInBackground() throws Exception {
				//logger.info("Agregando cuenta de banco al cliente");
				ServiceLocator2.getClienteManager().agregarCuenta(clave, cuenta);
				return "OK";
			}			
			protected void done() {
				try {
					get();
				} catch (InterruptedException e) {					
					e.printStackTrace();
				} catch (ExecutionException e) {					
					e.printStackTrace();
				}
			}			
		};
		worker.execute();
	}
	
	
	/**
	 * Registra un pago con deposito o transferencia
	 * 
	 * @return
	 */
	public static PagoConDeposito registrarPagoConDeposito(){		
		PagoConDepositoFormModel model=new PagoConDepositoFormModel(new PagoConDeposito(),false);
		PagoConDeposito proxy=PagoConDepositoForm.showForm(model);
		if(proxy!=null){			
			return ServiceLocator2.getCXCManager().salvarPago(proxy);
		}else
			return null;
	}
	
	public static PagoConDeposito editarPagoConDeposito(String id ){
		PagoConDeposito pago=(PagoConDeposito)getManager().getAbono(id);
		if(!pago.getAplicaciones().isEmpty()){
			MessageUtils.showMessage("El pago tiene aplicaciones no se puede editar", "Pagos");
			return null;
		}
		PagoConDepositoFormModel model=new PagoConDepositoFormModel(pago,false);
		PagoConDeposito proxy=PagoConDepositoForm.showForm(model);
		if(proxy!=null){			
			return ServiceLocator2.getCXCManager().salvarPago(proxy);
		}else
			return null;
	}
	
	
	/******** SECCION DE NOTAS DE CREDITO ***************/
	
	/**
	 * Genera y en su caso persiste una o mas notas de credito de descuento 
	 * para el grupo de facturas indicadas
	 * 
	 * @param facturas
	 * @return La lista de notas de credito persistidas o nulo si no se persistieron
	 */
	public static List<NotaDeCreditoDescuento> generarNotasDeDescuento(final List<Cargo> facturas){
		Assert.notEmpty(facturas);
		CXCUtils.validarMismoCliente(facturas);
		CXCUtils.validarMismoTipoDeOperacion(facturas);
		String clave=facturas.get(0).getClave();
		int atrasos=NotaDescuentoRules.getAtrasos(clave);
		if(atrasos>0){
			MessageUtils.showMessage("El cliente tiene "+atrasos+" cargos / facturas con atrasos, no proceden descuentos financieros", "Descuentos cancelados");
			return new ArrayList<NotaDeCreditoDescuento>(0);
		}
		List<NotaDeCreditoDescuento> notas=NotaDeDescuentoForm.showForm(facturas);
		final List<NotaDeCreditoDescuento> res=new ArrayList<NotaDeCreditoDescuento>();
		if(notas!=null){
			for(NotaDeCreditoDescuento nota:notas){
				try {
					ValidationUtils.debugValidation(nota);
					nota=(NotaDeCreditoDescuento)ServiceLocator2.getCXCManager().salvarNota(nota);
					//ImpresionUtils.imprimirNotaGeneral(nota.getId());
					res.add(nota);
					CFDPrintServicesCxC.imprimirNotaDeCreditoElectronica(nota.getId());
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e);
				}				
			}			
		}
		return res;
	}
	
	
	/**
	 * Genera una o dos notas de credito por devolucion
	 * 
	 * @return
	 */
	public static List<NotaDeCreditoDevolucion> generarNotasDeDevolucion(){
		List<Devolucion> devs=SelectorDeRMD2.seleccionar(OrigenDeOperacion.CRE);
		//List<Devolucion> devs=SelectorDeRMD2.seleccionar(OrigenDeOperacion.CRE);
		if(!devs.isEmpty()){
			NotaDevolucionFormModel fmodel=new NotaDevolucionFormModel(devs.get(0));
			fmodel.asignarFolio();
			NotaDevoucionForm form=new NotaDevoucionForm(fmodel);
			form.open();
			if(!form.hasBeenCanceled()){
				List<NotaDeCreditoDevolucion> notas=new ArrayList<NotaDeCreditoDevolucion>();
				for(NotaDeCreditoDevolucion n:fmodel.procesar()){
					NotaDeCreditoDevolucion nres=(NotaDeCreditoDevolucion)ServiceLocator2.getCXCManager().salvarNota(n);
					//ImpresionUtils.imprimirNotaDevolucion(nres.getId());
					CFDPrintServicesCxC.imprimirNotaDeCreditoElectronica(nres.getId());
					notas.add(nres);
				}
				return notas;
			}else
				return new ArrayList<NotaDeCreditoDevolucion>(0);
			
		}else
			return new ArrayList<NotaDeCreditoDevolucion>(0);
	}
	
	public static NotaDeCreditoBonificacion generarNotaDeBonificacion(OrigenDeOperacion origen){
		
		NotaDeCreditoBonificacion nota=new NotaDeCreditoBonificacion();
		nota.setOrigen(origen);
		nota.setSucursal((Sucursal)ServiceLocator2.getUniversalDao().get(Sucursal.class, 1L));
		//NotaDeCreditoBonificacionFormModel model=new NotaDeCreditoBonificacionFormModel(nota,false);
		NotaDeCreditoBonificacionFormModel2 model=new NotaDeCreditoBonificacionFormModel2(nota,false);
		
		//model.loadClientes();
		//model.asignarFolio();
		//NotaDeCreditoBonificacion res=NotaDeCreditoBonificacionForm.showForm(model, origen);
		NotaDeCreditoBonificacion res=NotaDeCreditoBonificacionForm2.showForm(model, origen);
		if(res!=null){
			res=(NotaDeCreditoBonificacion)ServiceLocator2.getCXCManager().salvarNota(res);
			//ImpresionUtils.imprimirNotaBonificacion(res.getId());
			//CFDPrintServicesCxC.imprimirNotaDeCreditoElectronica(res.getId());
			return res;
		}
		return null;
		
	}
	
	public static NotaDeCreditoBonificacion generarNotaDeBonificacion(OrigenDeOperacion origen,List<Cargo> cargos){
		boolean val=CXCUtils.validarMismoCliente(cargos);
		if(!val){
			MessageUtils.showMessage("Los cargos seleccionados no son del mismo cliente", "Nota de credito");
			return null;
		}
		Cliente c=cargos.get(0).getCliente();
		
		NotaDeCreditoBonificacion nota=new NotaDeCreditoBonificacion();
		//NotaDeCreditoBonificacionFormModel model=new NotaDeCreditoBonificacionFormModel(nota,false);
		NotaDeCreditoBonificacionFormModel2 model=new NotaDeCreditoBonificacionFormModel2(nota,false);
		nota.setSucursal((Sucursal)ServiceLocator2.getUniversalDao().get(Sucursal.class, 1L));
		model.setValue("cliente", c);
		List<Venta> ventas=new ArrayList<Venta>();
		for(Cargo ca:cargos){
			if(ca instanceof Venta)
				ventas.add((Venta)ca);
		}
		if(ventas.isEmpty())
			return null;
		model.generarConceptos(ventas);		
		NotaDeCreditoBonificacion res=NotaDeCreditoBonificacionForm2.showForm(model, origen);
		
		if(res!=null){
			res.setOrigen(origen);
			res=(NotaDeCreditoBonificacion)ServiceLocator2.getCXCManager().salvarNota(res);
			//CFDPrintServicesCxC.imprimirNotaDeCreditoElectronica(res.getId());
			//ImpresionUtils.imprimirNotaBonificacion(res.getId());
			return (NotaDeCreditoBonificacion)buscarNotaDeCreditoInicializada(res.getId());
			//return res;
		}
		return null;
		
	}
	
	/**
	 * Genera Notas de descuento y sus aplicaciones para los cargos calificados
	 * relacionados con el abono 
	 * 
	 * @param abono
	 * @return
	 */
	public static List<NotaDeCreditoDescuento> aplicarDescuentos(final List<Cargo> cargos){
		CollectionUtils.filter(cargos,new Predicate(){
			public boolean evaluate(Object object) {
				Cargo c=(Cargo)object;
				if(c.isPrecioBruto() || procedeDescuentoFinanciero(c)){
					return c.getDescuentos().doubleValue()==0;
				}
				return false;
			}			
		});
		if(!cargos.isEmpty()){
			boolean res=MessageUtils.showConfirmationMessage("Algunos de los cargos califican para nota de descuento desea aplicarlas?"
					,"Abonos automaticos");
			if(res)
				return generarNotasDeDescuento(cargos);
		}
		return null;
	}	
	
	public static boolean procedeDescuentoFinanciero(final Cargo c){		
		return c.getDescuentoFinanciero()>0;
	}
	
	public static void aplicarPago(){
		logger.info("Aplicando pago");		
		final AplicacionDePagoModel aplicacionDePagoModel=new AplicacionDePagoModel();		
		final AplicacionDePagoForm form=new AplicacionDePagoForm(aplicacionDePagoModel);
		form.open();
		if(!form.hasBeenCanceled()){			
			Abono abono=aplicacionDePagoModel.procesar();
			boolean autorizar=AutorizacionesCxC.requiereAutorizacion(abono);
			if(autorizar){
				if(aplicacionDePagoModel.getAutorizacion()==null){
					return;
				}
			}
			getManager().salvarAbono(abono);
			
			final List<Cargo> afectados=new ArrayList<Cargo>();
			for(String id:aplicacionDePagoModel.getCargosAfectados()){
				Cargo af=getManager().getCargo(id);
				afectados.add(af);
			}
			CXCUIServiceFacade.persistirDescuentoFinancieros(afectados);
			if(!afectados.isEmpty())
				CXCUIServiceFacade.aplicarDescuentos(afectados);
			aplicarPago();
		}		
	}
	
	public static void aplicarPago(final Cliente c){
		logger.info("Aplicando pago");		
		final AplicacionDePagoModel aplicacionDePagoModel=new AplicacionDePagoModel();		
		final AplicacionDePagoForm form=new AplicacionDePagoForm(aplicacionDePagoModel);
		form.open();
		if(!form.hasBeenCanceled()){			
			Abono abono=aplicacionDePagoModel.procesar();
			getManager().salvarAbono(abono);
			
			final List<Cargo> afectados=new ArrayList<Cargo>();
			for(String id:aplicacionDePagoModel.getCargosAfectados()){
				Cargo af=getManager().getCargo(id);
				afectados.add(af);
			}
			CXCUIServiceFacade.persistirDescuentoFinancieros(afectados);
			if(!afectados.isEmpty())
				CXCUIServiceFacade.aplicarDescuentos(afectados);
			aplicarPago(c);
		}		
		
	}
	
	/**
	 * Persiste el descuento financiero en la columna de descuento a partir de 
	 * las reglas de negocios para el manejo de descuentos finanancieros 
	 * 
	 * @param cargos
	*/
	public static void persistirDescuentoFinancieros(final List<Cargo> cargos){
		for(int index=0;index<cargos.size();index++){
			Cargo c=(Cargo)cargos.get(index);
			try {
				double df=c.getDescuentoFinanciero();
				if(df>0){
					c.setDescuentoGeneral(df/100);
				}
				c=getManager().save(c);
				cargos.set(index,c);
			} catch (Exception e) {
				logger.error(e);
			}
		}		
	}
	 
	
	public static void aplicarPagoDiferencias(final Cargo c){
		boolean cambiaria=false;
		if(c instanceof Venta){
			Venta fac=(Venta)c;
			if(fac.getTipoSiipap().equals("X")){
				cambiaria=MessageUtils.showConfirmationMessage(
						"La factura es en dolares, la diferencia es cambiara?", "Pago automático");
			}
		}
		PagoDeDiferencias res=getManager().generarPagoPorDiferencia(c,cambiaria);
		MessageUtils.showMessage("Pago generado:\n "+res, "Pagos automáticos");
	}
	
	public static void aplicarPagoEnEspecie(Cargo c,String comentario){
		c=getManager().getCargo(c.getId());
		PagoEnEspecie res=getManager().generarPagoEnEspecie(c, comentario);
		MessageUtils.showMessage("Pago generado:\n "+res, "Pago en especie");
	}
	
	/**
	 * Genera una nota de cargo
	 * 
	 * @return
	 */
	public static NotaDeCargo generarNotaDeCargo(){
		final NotaDeCargoFormModel model=new NotaDeCargoFormModel();
		//model.asignarFolio(); 
		final NotaDeCargoForm form=new NotaDeCargoForm(model);
		model.setOrigen(OrigenDeOperacion.CRE);
		form.open();
		if(!form.hasBeenCanceled()){
			NotaDeCargo res=model.commit();
			boolean especial=res.isEspecial();
			//NotaDeCargoForm.showObject(res);
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
			CFDPrintServicesCxC.imprimirNotaDeCargoElectronica(res.getId());
			//ImpresionUtils.imprimirNotaDeCargo(res.getId(),especial);
			return res;
		}
		return null;
	}
	
	public static NotaDeCargo generarNotaDeCargo(OrigenDeOperacion origen){
		final NotaDeCargoFormModel model=new NotaDeCargoFormModel();
		model.asignarFolio();
		final NotaDeCargoForm form=new NotaDeCargoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			NotaDeCargo res=model.commit();
			res.setOrigen(origen);
			//NotaDeCargoForm.showObject(res);
			res=(NotaDeCargo)getManager().save(res);
			CFDPrintServicesCxC.imprimirNotaDeCargoElectronica(res.getId());
			//ImpresionUtils.imprimirNotaDeCargo(res.getId());
			return res;
		}
		return null;
	}
	
	public static NotaDeCargo generarNotaDeCargo(List<Cargo> cargos){
		boolean val=CXCUtils.validarMismoCliente(cargos);		
		if(!val){
			MessageUtils.showMessage("Los cargos seleccionados no son del mismo cliente", "Nota de credito");
			return null;
		}
		CXCUtils.validarMismoTipoDeOperacion(cargos);
		Cliente c=cargos.get(0).getCliente();
		final NotaDeCargoFormModel model=new NotaDeCargoFormModel();
		model.asignarFolio();
		model.getCargo().setCliente(c);
		model.getCargo().setOrigen(cargos.get(0).getOrigen());
		model.setOrigen(cargos.get(0).getOrigen());
		for(Cargo car:cargos){
			model.agregarVenta(car);
		}
		final NotaDeCargoForm form=new NotaDeCargoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			NotaDeCargo res=model.commit();
			//NotaDeCargoForm.showObject(res);
			res=(NotaDeCargo)getManager().save(res);
			CFDPrintServicesCxC.imprimirNotaDeCargoElectronica(res.getId());
			//ImpresionUtils.imprimirNotaDeCargo(res.getId());
			return res;
		}
		return null;
	}
	
	/**
	 * Actualiza los descuentos para la lsita de cargos
	 *  
	 * @param cargos La lista de los cargos ya con los descuentos actualizados
	 * @return
	 */
	public static List<Cargo> actualizarDescuentos(final List<Cargo> cargos){
		
		for(int index=0;index<cargos.size();index++){
			Cargo c=cargos.get(index);
			if(c instanceof Venta){
				Venta venta=(Venta)c;
				try {
					ServiceLocator2.getDescuentosManager().actualizarDescuento(venta);
					cargos.set(index, venta);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}
		return cargos;
	}
	
	public static Venta asignarDescuentoEspecial(final Venta venta){
		
		if(venta.isPrecioNeto()) return null;
		if(venta.getSaldo().doubleValue()<5) return null;
		
		final DescuentoEspecialFormModel model=new DescuentoEspecialFormModel(venta);
		final DescuentoEspecialForm form=new DescuentoEspecialForm(model);
		form.open();
		if(!form.hasBeenCanceled()){			
			DescuentoEspecial de=model.commit();
			de.getAutorizacion().setAutorizo("ADMIN");
			DescuentoEspecial result= ServiceLocator2.getDescuentosManager().asignarDescuentoEspecial(de);
			Venta res= (Venta)result.getCargo();
			return ServiceLocator2.getVentasManager().get(res.getId());
		}
		return null;
	}
	
	public static Cargo generarJuridico(final Cargo cargo){
		Juridico jur=JuridicoForm.showForm(cargo);
		if(jur!=null){
			getManager().generarJuridico(jur);
			return getManager().getCargo(cargo.getId());
		}
		return cargo;
	}
	
	public static void consultarJuridico(final Juridico jur){
		JuridicoForm.showForm(jur,true);
	}
	public static Cargo eliminarJuridico(final Cargo cargo){
		getManager().cancelarJuridico(cargo.getJuridico());
		return getManager().getCargo(cargo.getId());
	}
	
	/**
	 * Salda el abono por diferencia cambiaria
	 * 
	 * @param abono
	 */
	public static Abono generarAplicacionPorDiferencia(final Abono source){
		if(source.getDisponible().doubleValue()<=0)
			return null;
		AplicacionDeDiferenciasForm form=new AplicacionDeDiferenciasForm(source);
		form.open();
		if(!form.hasBeenCanceled()){
			Date fecha=form.fechaInicial.getDate();
			CargoPorDiferencia.TipoDiferencia tipo=(CargoPorDiferencia.TipoDiferencia)form.tipo.getSelectedItem();
			Abono target=getManager().generarAplicacionPorDiferencia(source, fecha, tipo);
			return getManager().getAbono(target.getId());
		}
		return null;
	}
	
	public static NotaDeCredito buscarNotaDeCreditoInicializada(final String id){
		
		return (NotaDeCredito)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				NotaDeCredito res=(NotaDeCredito)session.get(NotaDeCredito.class, id);
				res.getCliente().getTelefonosRow();
				if(res.getConceptos()!=null && !res.getConceptos().isEmpty()){
					res.getConceptos().iterator().next();
				}
				if(!res.getAplicaciones().isEmpty()){
					res.getAplicaciones().iterator().next();
				}
				if(res instanceof NotaDeCreditoDevolucion){
					NotaDeCreditoDevolucion ndev=(NotaDeCreditoDevolucion)res;
					ndev.getDevolucion().getPartidas().iterator().next();
					ndev.getDevolucion().isTotal();
				}
				return res;
			}
			
		});
		
	} 
	
	public static CXCManager getManager(){
		return ServiceLocator2.getCXCManager();
	}
	
	
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				//List<Cargo> cargos=ServiceLocator2.getHibernateTemplate().find("from Venta v where v.clave=? and v.fecha=?",new Object[]{"M100076",DateUtil.toDate("2/04/2009")});
				//generarNotasDeDescuento(cargos);
				//generarNotaDeCargo(cargos);
				//generarNotaDeCargo();
				//generarNotaDeBonificacion(OrigenDeOperacion.CRE,cargos);
				//PagoConCheque pago=(PagoConCheque)ServiceLocator2.getCXCManager().getAbono("8a8a8189-2154211f-0121-5423042a-0024");
				//editarPagoConCheque(pago);
				//System.out.println(ToStringBuilder.reflectionToString(pago,ToStringStyle.MULTI_LINE_STYLE));
				//aplicarPago();
				/*
				Venta v=ServiceLocator2.getVentasManager().get("8a8a81c7-1fbdb76d-011f-bdb8ccbd-0003");
				v=asignarDescuentoEspecial(v);
				System.out.println("Descuento especial: "+v);
				
*/				//generarNotasDeDevolucion();
				//generarNotaDeBonificacion(OrigenDeOperacion.CRE);
				Cargo cargo=ServiceLocator2.getCXCManager().getCargo("8a8a81e7-30dbe995-0130-dc11a329-003f");
				generarJuridico(cargo);
				System.exit(0);
			}			
		});		
	}

}
