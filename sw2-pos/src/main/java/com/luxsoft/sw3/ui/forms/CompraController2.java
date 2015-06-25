package com.luxsoft.sw3.ui.forms;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.luxsoft.siipap.compras.dao.Compra2Dao;
import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;

import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.ComprasManager;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.selectores.SelectorPartidasDeCompraPorDepurar;

/**
 * Controlador y PresentationModel para la fomra de mantenimiento de Compra
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CompraController2 {
	
	
	
	
	protected Logger logger=Logger.getLogger(getClass());

	public CompraController2() {
		
	}
	
	
	/** Soporte a al panel de mantenimiento **/
	
	public List<CompraUnitaria> buscarPartidas(final Compra2 compra){
		return getManager().buscarPartidas(compra);
	}
	public List<Compra2> buscarCompras(Periodo p){
		return getManager().buscarCompras(p);
	}
	public List<Compra2> buscarComprasPendientes(){
		return getManager().buscarPendientes();
	}
	
	public Compra2 generarCompra(){		
		CompraController controller=new CompraController();
		CompraForm form=new CompraForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){			
			Compra2 target= controller.getCompra();
			return persistir(target);
		}
		return null;
	}
	
	public Compra2 modificarCompra(Compra2 compra){
		Assert.isNull(compra.getDepuracion(), "La compra ya se ha depurado");
		Assert.isNull(compra.getCierre(), "La compra ya se ha cerrado");		
		Compra2 target=getManager().obtenerCopiaModificable(compra.getId());
		CompraController controller=new CompraController(target);
		CompraForm form=new CompraForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){			
			target= controller.getCompra();
			
			for(CompraUnitaria det : target.getPartidas()){
				det.actualizar();
			}
			
			return persistir(target);
		}
		return compra;
	}
	
	public void eleiminarCompra(Compra2 compra){
		getManager().remove(compra.getId());
	}	
	
	public Compra2 cerrarCompra(Compra2 compra){
		return getManager().cerrar(compra, getFechaActual());
	}
	
	public Compra2 cancelarCierreDeCompra(Compra2 compra){
		return getManager().cancelarCierre(compra);
	}
	
	public Compra2 depurarCompra(Compra2 compra){
		if(compra.isImportacion())
			return compra;
		if(MessageUtils.showConfirmationMessage("Depurar la orden de compra: "+compra.getFolio(), "Depuracióin")){
			Compra2 source=getCompraDao().inicializarCompra(compra.getId());
			Date fecha=new Date();			
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
			Compra2 res= getCompraDao().save(source);
			return res;
			
		}else
			return null;
	}
	
	
	public Compra2 cancelarCompra(Compra2 compra){
		return getManager().cancelar(compra,false);
	}
	
	public void imprimirCompra(Compra2 compra){		
		ReportUtils2.imprimirCompra(compra);
	}
	
	public void mandarPorEmail(Compra2 compra){
		throw new UnsupportedOperationException("TO BE IMPLEMENTED");
	}
	
	protected Compra2 persistir(Compra2 target){
		target.setImportado(null);
		return getManager().save(target);
	}
	
	/** IoC colaboradores **/

	private ComprasManager getManager(){
		return Services.getInstance().getComprasManager();
	}
	
	private Compra2Dao getCompraDao(){
		return (Compra2Dao)Services.getInstance()
			.getContext().getBean("comprasDao");
	}
	
	private Sucursal getSucursal(){
		return Services.getInstance().getConfiguracion().getSucursal();
	}
	
	Date fechaActual;
	
	private Date getFechaActual(){
		if(fechaActual==null)
			fechaActual=Services.getInstance().obtenerFechaDelSistema();
		return fechaActual;
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
				CompraController2 controller=new CompraController2();
				controller.generarCompra();
				
				System.exit(0);
			}

		});
	}
}
