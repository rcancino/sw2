package com.luxsoft.siipap.compras.ui.consultas;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.luxsoft.siipap.compras.dao.Compra2Dao;

import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.compras.ui.form.CompraCentralizadaForm;
import com.luxsoft.siipap.compras.ui.form.CompraCentralizadaFormModel;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.ComprasManager;

/**
 * Service Facade para los servicios de UI relacionados con las compras
 * centralizadas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ComprasCentralizadasController {
	
	public Compra2 generarCompraDeImportacion(){
		CompraCentralizadaFormModel controller=new CompraCentralizadaFormModel();
		controller.setValue("sucursal", ServiceLocator2.getUniversalDao().get(Sucursal.class, 1L));
		controller.setValue("importacion", Boolean.TRUE);
		CompraCentralizadaForm form=new CompraCentralizadaForm(controller);
		form.setTitle("Compra de Importación");
		form.setImportacion(true);
		form.open();
		if(!form.hasBeenCanceled()){
			Compra2 target=controller.getCompra();
			return getManager().save(target);
		}
		return null;
	}
	
	public Compra2 generarCompra(){
		CompraCentralizadaFormModel controller=new CompraCentralizadaFormModel();
		CompraCentralizadaForm form=new CompraCentralizadaForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			Compra2 target=controller.getCompra();
			return getManager().save(target);
		}
		return null;
	}
	
	public Compra2 editar(Compra2 source){
		Compra2 target=getManager().buscarInicializada(source.getId());
		if(!target.isCancelable()){
			MessageUtils.showMessage("La compra: "+target.getFolio()
					+ " No se puede editar ya que tiene recepciones parciales",
					"Edición de compras");
			return null;
		}
			
		CompraCentralizadaFormModel controller=new CompraCentralizadaFormModel(target);
		CompraCentralizadaForm form=new CompraCentralizadaForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			target=controller.getCompra();
			for(CompraUnitaria det : target.getPartidas()){
				det.actualizar();
			}
			
			return getManager().saveCentralizada(target);
		}
		return source;
	}
	
	public void mostrarCompra(final String id){
		Compra2 compra=getManager().buscarInicializada(id);
		CompraCentralizadaFormModel controller=new CompraCentralizadaFormModel(compra);
		controller.setReadOnly(true);
		CompraCentralizadaForm form=new CompraCentralizadaForm(controller);
		form.open();
	}
	
	public Compra2 consolidar(List<String> ids){
		try {
			Compra2 res=  getManager().consolidarCompras(ids);
			if(res!=null){
				MessageUtils.showMessage("Compra consolidada exitosamente. Folio: "+res.getFolio(), "Consolidación de Compras");
				return res;
			}
		} catch (Exception e) {
			MessageUtils.showMessage(ExceptionUtils.getRootCauseMessage(e), "Error al consolidar");
		}
		
		return null;
	}
	
	public Compra2 actualizarPrecios(Compra2 compra){
		if(compra.isEspecial())
			return compra;
		getManager().actualizarPrecios(compra);
		return getManager().get(compra.getId());
	}
	
	public List<Compra2> buscarPendientes(){		
		return getCompraDao().buscarPendientes();
	}
	
	public List<Compra2> buscarCompras(Periodo periodo) {		
		return getCompraDao().buscarCompras(periodo);
	}
	
	public List<CompraUnitaria> buscarPartidas(final Compra2 compra){
		return getCompraDao().buscarPartidas(compra); 
	}
	
	private Compra2Dao getCompraDao(){
		return (Compra2Dao)ServiceLocator2.instance()
			.getContext().getBean("compra2Dao");
	}
	
	
	
	public Compra2 cancelar(Object bean){
		Compra2 compra=(Compra2)bean;
		if(bean!=null){
			Compra2 target=getCompraDao().inicializarCompra(compra.getId());
			if(!target.isCancelable()){
				MessageUtils.showMessage("La compra: "+target.getFolio()
						+ " No se puede cancelar ya que tiene recepciones parciales",
						"Cancelación de compras");
				return null;
			}else{
				target=getManager().cancelar(target,true);
				return target;
			}
		}
		return null;
	}
	
	public Compra2 depurar(Object bean){
		Compra2 compra=(Compra2)bean;
		if(MessageUtils.showConfirmationMessage("Depurar la orden de compra: "+compra.getFolio(), "Depuracióin")){
			Compra2 source=getCompraDao().inicializarCompra(compra.getId());
			Date fecha=ServiceLocator2.obtenerFechaDelSistema();
			
			if(MessageUtils.showConfirmationMessage("Parcial", "Depuración")){
				List<CompraUnitaria> seleccionadas=SelectorPartidasDeCompraPorDepurar.seleccionar(source);
				if(!seleccionadas.isEmpty()){
					for(CompraUnitaria uni:seleccionadas){
						if(!uni.isDepurada()){
							uni.setDepuracion(fecha);
							uni.setDepurado(uni.getSolicitado()-uni.getRecibido());				
						}
					}
				}
			}else{
				source.setDepuracion(fecha);
				for(CompraUnitaria uni:source.getPartidas()){
					if(!uni.isDepurada()){
						uni.setDepuracion(fecha);
						uni.setDepurado(uni.getSolicitado()-uni.getRecibido());				
					}
				}
				if(source.getCierre()==null)
					source.setCierre(fecha);
				
			}
			source.setReplicado(null);
			//return getCompraDao().save(source);
			ServiceLocator2.getHibernateTemplate().saveOrUpdate(source);
			return getCompraDao().inicializarCompra(source.getId());
			
		}else
			return null;
		
	}
	
	public ComprasManager getManager(){
		return ServiceLocator2.getComprasManager();
	}
	
	public static void imprimir(Compra2 compra){
		compra=ServiceLocator2.getComprasManager().buscarInicializada(compra.getId());
		final Map map=new HashMap();
		map.put("COMPRA_ID", compra.getId());
		map.put("CLAVEPROV", "NO");
		if(MessageUtils.showConfirmationMessage("Con claves del proveedor", "Impresión de Orden de Compra")){
			map.put("CLAVEPROV", "SI");
		}
		ReportUtils.viewReport(ReportUtils.toReportesPath("compras/OrdenDeCompraSuc.jasper"), map);
		if(compra.getConsolidada()){
			ReportUtils.viewReport(ReportUtils.toReportesPath("compras/OrdenDeCompraConsolidada.jasper"), map);
		}
			
	}

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
				ComprasCentralizadasController controller=new ComprasCentralizadasController();
				controller.generarCompra();
				System.exit(0);
			}

		});
	}
}
