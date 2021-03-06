package com.luxsoft.sw3.impap.ui.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;


import com.jgoodies.binding.value.ValueHolder;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.compras.model.ListaDePreciosDet;
import com.luxsoft.siipap.cxp.model.CXPAnalisisDet;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.model.CXPUtils;
import com.luxsoft.siipap.cxp.model.ContraReciboDet;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * PresentationModel para la forma de analisis de factura
 * 
 * @author Ruben Cancino
 *
 */
public class AnalisisDeEntradaModel extends MasterDetailFormModel{
	
	

	public AnalisisDeEntradaModel(CXPFactura factura) {
		super(factura);		
	}
	
	public CXPFactura getAnalisis(){
		return (CXPFactura)getBaseBean();
	}

	@Override
	public boolean manejaTotalesEstandares() {
		return false;
	}
	
	@Override
	protected EventList createPartidasList(){
		EventList res=super.createPartidasList();
		if(getAnalisis().getId()!=null){
			res.addAll(getAnalisis().getPartidas());
			getAnalisis().actualizarTotalesAnalizado();
		}
		return res;
	}
	
	protected void initEventHandling(){
		getModel("proveedor").addValueChangeListener(new ProveedorHandler());
		getModel("fecha").addValueChangeListener(new FechaHandler());
		
		ImporteHandler impHandler=new ImporteHandler();
		getModel("importe").addValueChangeListener(impHandler);			
		getModel("flete").addValueChangeListener(impHandler);
		getModel("cargos").addValueChangeListener(impHandler);
		getModel("retencionfletePor").addValueChangeListener(impHandler);		
		
		
		reciboHolder=new ValueHolder(0l);
		if(getAnalisis().getRecibo()!=null)
			updateForContraRecibo(getAnalisis().getRecibo());
	}
	
	
	/**
	 * Procesa un grupo de entradas para generar el analisis de las mismas 
	 *  
	 * @param entradas
	 */
	public void procesarEntradas(final List<EntradaPorCompra> entradas){
		CollectionUtils.forAllDo(entradas, new Closure(){
			public void execute(Object input) {
				EntradaPorCompra e=(EntradaPorCompra)input;
				Assert.isTrue(e.getProveedor().equals(getAnalisis().getProveedor()));
				CXPAnalisisDet det=new CXPAnalisisDet();
				det.setEntrada(e);
				det.setCantidad(e.getPendienteDeAnalisis());
				if(getAnalisis().agregarPartida(det)){
					calcularPrecios(det);
					source.add(det);
				}
			}
		});
		getAnalisis().actualizarTotalesAnalizado();
	}
	
	
	public void calcularPrecios(final CXPAnalisisDet det){
		
		Proveedor prov=getAnalisis().getProveedor();
		Producto prod=det.getEntrada().getProducto();
		Currency moneda=getAnalisis().getMoneda();
		Date fecha=det.getEntrada().getFechaCompra();
		ListaDePreciosDet precio=null;
		if(fecha!=null)			
			precio=CXPServiceLocator.getInstance().getListaDePreciosDao().buscarPrecioVigente(prod, moneda,prov,fecha);
		if(precio==null) return;
		Assert.isTrue(moneda.equals(precio.getPrecio().getCurrency()),"El precio no es de la misma moneda");
		if(logger.isDebugEnabled()){
			logger.debug(MessageFormat
					.format("Aplicando precio {0} para producto:{1}\n Lista utilizada: {2}", precio.getPrecio(),det.getEntrada().getProducto(),precio.getLista().getId()));
		}
		
		det.setPrecio(precio.getPrecio().amount());
		det.setDesc1(precio.getDescuento1());
		det.setDesc2(precio.getDescuento2());
		det.setDesc3(precio.getDescuento3());
		det.setDesc4(precio.getDescuento4());
		det.calcularImporte();
	}
	
	public void commitEditing(final CXPAnalisisDet det){
		int index=source.indexOf(det);
		if(index!=-1)
			source.set(index, det);
	}

	public ValueHolder reciboHolder;
	
	public void setRecibo(ContraReciboDet recibo) {
		if(isContrareciboAsignable()&& (recibo!=null)){
			getAnalisis().setRecibo(recibo);
			updateForContraRecibo(recibo);
		}
	}
	
	private void updateForContraRecibo(final ContraReciboDet recibo){
		if(isContrareciboAsignable()&& (recibo!=null)){
			reciboHolder.setValue(recibo.getId());
			getAnalisis().getPartidas().clear();
			source.clear();
			getAnalisis().setMoneda(recibo.getMoneda());
			getAnalisis().setDocumento(recibo.getDocumento());
			Date fecha=recibo.getFecha();
			getAnalisis().setFecha(fecha);
		}
	}
	
	public void actualizarVencimiento(){
		final Proveedor p=getAnalisis().getProveedor();		
		if(p!=null){
			Date fecha=getAnalisis().getFecha();
			Date revision=fecha;
			if(getAnalisis().getRecibo()!=null)
				revision=getAnalisis().getRecibo().getRecibo().getFecha();
			Date vto   = CXPUtils.calcularVencimiento(revision, fecha, p);
			Date vtoDf = CXPUtils.calcularVencimientoDescuentoF(revision, fecha,p);
			getAnalisis().setFecha(fecha);
			getAnalisis().setRevision(revision);
			getAnalisis().setVencimiento(vto);
			getAnalisis().setVencimientoDF(vtoDf);
		}
	}
	
	public boolean deleteDetalle(final Object obj){
		CXPAnalisisDet det=(CXPAnalisisDet)obj;
		boolean eliminado= getAnalisis().eliminarPartida(det);
		source.remove(det);
		getAnalisis().actualizarTotalesAnalizado();
		return eliminado;
	}
	
	public void afterEdit(Object partida){
		int index=source.indexOf(partida);
		Assert.isTrue(index>=0,"El bean en edici�n debe existir en  el EventList de las partidas, probablemente existe un error en el equals del bean");
		final Object element=source.get(index);
		CXPAnalisisDet det=(CXPAnalisisDet)partida;
		boolean res=getAnalisis().getPartidas().contains(det);
		
		Assert.isTrue(res
				,"El bean en edicion no existe en el modelo no estan sicronizadas " +
						"la colleccion del modelo con el GlazedList de partidas\n" +
						""+ToStringBuilder.reflectionToString(element, ToStringStyle.MULTI_LINE_STYLE, false));
		source.set(index, partida);
		getAnalisis().actualizarTotalesAnalizado();
	}

	private class ProveedorHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if(getAnalisis().getId()==null){
				getAnalisis().getPartidas().clear();
				source.clear();
				getAnalisis().actualizarTotalesAnalizado();
			}
			
			if(!getAnalisis().getProveedor().getCobraFlete()){
				setValue("flete", BigDecimal.ZERO);
			}
			boolean totalMng=getAnalisis().getProveedor().getCobraFlete();
			getComponentModel("flete").setEnabled(totalMng);
			actualizarVencimiento();
		}
	}
	
	public boolean isContrareciboAsignable(){
		if(getAnalisis().getId()==null){
			return true;
		}else{
			return getAnalisis().getPartidas().isEmpty();
		}
	}
	
	private class ImporteHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			BigDecimal imp=(BigDecimal)evt.getNewValue();
			if(imp==null) imp=BigDecimal.ZERO;
			System.out.println("Actualizando importes.....");
			CantidadMonetaria importe=CantidadMonetaria.pesos(getAnalisis().getImporte().doubleValue());
			CantidadMonetaria cargos=CantidadMonetaria.pesos(getAnalisis().getCargos().doubleValue());
			importe=importe.add(cargos);
			CantidadMonetaria impuesto=MonedasUtils.calcularImpuesto(importe);
			
			CantidadMonetaria flete=CantidadMonetaria.pesos(getAnalisis().getFlete().doubleValue());
			CantidadMonetaria impuestoFlete=CantidadMonetaria.pesos(getAnalisis().getImpuestoflete().doubleValue());
			CantidadMonetaria retFlete=CantidadMonetaria.pesos(getAnalisis().getRetencionflete().doubleValue());
			CantidadMonetaria total=importe.add(impuesto).add(flete).add(impuestoFlete).subtract(retFlete);
			
			getAnalisis().setImpuesto(impuesto.amount());
			getAnalisis().setTotal(total.amount());
		}		
	}
	
	private class FechaHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			actualizarVencimiento();
		}
		
	}
	
}
