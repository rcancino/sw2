package com.luxsoft.siipap.cxc.ui.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxc.model.AbstractNotasRules;
import com.luxsoft.siipap.cxc.model.AplicacionDeNota;
import com.luxsoft.siipap.cxc.model.NotaBonificacionRules;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDet;
import com.luxsoft.siipap.cxc.model.NotaRules;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion.ModeloDeCalculo;
import com.luxsoft.siipap.cxc.rules.CXCUtils;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeFacturas;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * FormModel para la generacion y mantenimiento general de notas de credito 
 * 
 * @author Ruben Cancino
 *
 */
public class NotaDeCreditoBonificacionFormModel2 extends DefaultFormModel implements PropertyChangeListener{
	
	protected EventList<NotaDeCreditoDet> aplicaciones;
	
	protected NotaRules rules=new AbstractNotasRules();

	public NotaDeCreditoBonificacionFormModel2(NotaDeCredito bean, boolean readOnly) {
		super(bean, readOnly);
		setRules(new NotaBonificacionRules());
		getModel("descuento").addValueChangeListener(this);
		getModel("total").addValueChangeListener(this);
		getModel("total").addValueChangeListener(new TotalHandler());
		getModel("modo").addValueChangeListener(new ModoHandler());
		String[] props={
				"venta.documento"
				,"venta.fecha"
				,"venta.total"
				,"venta.devoluciones"
				,"venta.bonificaciones"
				,"venta.descuentos"
				,"venta.saldoSinPagos"
				,"venta.saldoCalculado"
				,"importe"
				};
		String[] labels={
				"Docto"
				,"Fecha"
				,"Tot"
				,"Devoluciones"
				,"Bonificaciones"
				,"Descuentos"
				,"Saldo (S.P)"
				,"Saldo (Neto)"
				,"Importe"};
		setProperties(props);
		setLabels(labels);
	}
	
	protected void init(){
		aplicaciones=new BasicEventList<NotaDeCreditoDet>();
		
	}
	
	public NotaDeCreditoBonificacion getNotaBonificacion(){
		return (NotaDeCreditoBonificacion)getNota();
	}
	
	public NotaDeCredito getNota(){
		NotaDeCredito nota= (NotaDeCredito)getBaseBean();
		/*
		for(Aplicacion a:nota.getAplicaciones()){
			a.setFecha(nota.getFecha());
		}
		*/
		return nota;
	}

	/**
	 * Read-Only view de la lista de aplicaciones para esta nota
	 * Garantizamos que las aplicaciones solo se puedan agregar o eliminar
	 * mediante esta clase
	 * 
	 * @return
	 */
	public EventList<NotaDeCreditoDet> getAplicaciones() {
		return GlazedLists.readOnlyList(aplicaciones);
	}
	
	
	
	public void generarConceptos(List<Venta> ventas){
		if(ventas.isEmpty())
			return;
		for(Venta venta:ventas){
			NotaDeCreditoDet det=getNota().agregarConcepto();
			det.setVenta(venta);
			aplicaciones.add(det);
		}
		actualizar();		
	}
	
	
	/**
	 * Actualiza las aplicaciones para el grid UI
	 */
	public void actualizarAplicacionesUI(){
		for(int index=0;index<aplicaciones.size();index++){
			NotaDeCreditoDet a=aplicaciones.get(index);
			aplicaciones.set(index,a);
		}
	}
	
	public void eliminarAplicaciones(final List<NotaDeCreditoDet> data){
		for(NotaDeCreditoDet det:data){
			boolean res=getNota().getConceptos().remove(det);
			if(res)
				aplicaciones.remove(det);
		}
		actualizar();
	}
	
	/**
	 * Util para determinar comportamiento en UI
	 * 
	 * @return
	 */
	public boolean isPorPorcentaje(){
		if(aplicaciones.isEmpty())
			return false;
		else
			return getNotaBonificacion().getModo().equals(ModeloDeCalculo.DESCUENTO);
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		//Validamos el comentario si no existen partidas
		if(aplicaciones.isEmpty()){
			String comentario=getNota().getComentario();
			if(StringUtils.isBlank(comentario)){
				support.addError("", "Sin aplicaciones se requiere el comentario");
			}
		}
	}
	
	public void actualizar(){
		logger.info("Aplicando business rules para la nota:"+getNota());
		ModeloDeCalculo m=getNotaBonificacion().getModo();
		if(m.equals(ModeloDeCalculo.DESCUENTO))
			actualizarConDescuento();
		else
			CXCUtils.prorratearElImporteEnConceptos(getNotaBonificacion());
		actualizarAplicacionesUI();
		
	}
	
	private void actualizarConDescuento(){
		double descuento=getNotaBonificacion().getDescuento();
		BigDecimal total=BigDecimal.ZERO;
		for(NotaDeCreditoDet det:aplicaciones){			
			CantidadMonetaria importe=det.getVenta().getSaldoSinPagosCM();
			importe=importe.multiply(descuento);
			det.setImporte(importe.amount());
			det.setDescuento(descuento);
			total=total.add(det.getImporte());
		}
		setValue("total", total);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if(!aplicaciones.isEmpty())
			actualizar();
		
	}	
	
	
	/**
	 * Solo detecta el cambio en el total para acualizar el importe
	 * y el impuesto
	 *  
	 * @author Ruben Cancino Ramos
	 *
	 */
	private class TotalHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {			
			CantidadMonetaria tot=getNota().getTotalCM();
			if(tot==null)
				tot=CantidadMonetaria.pesos(0);
			CantidadMonetaria importe=MonedasUtils.calcularImporteDelTotal(tot);
			getNota().setImporte(importe.amount());
			getNota().setImpuesto(MonedasUtils.calcularImpuesto(importe).amount());
			
		}
		
	}
	
	private class ModoHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			setValue("descuento", new Double(0));
			setValue("total", BigDecimal.ZERO);
			
			
		}
		
	}
	
	/**
	 * Genera un {@link TableFormat} adecuado para las aplicaciones de la nota
	 * 
	 * @return
	 */
	public TableFormat<NotaDeCreditoDet> createTableformat(){
		return GlazedLists.tableFormat(NotaDeCreditoDet.class,getProperties(), getLabels());
	}
	
	private String[] properties;
	private String[] labels;

	public String[] getProperties() {
		return properties;
	}

	public void setProperties(String... properties) {
		this.properties = properties;
	}

	public String[] getLabels() {
		return labels;
	}

	public void setLabels(String... labels) {
		this.labels = labels;
	}

	public NotaRules getRules() {
		return rules;
	}

	public void setRules(NotaRules rules) {
		this.rules = rules;
	}
	

	/**
	 *  
	 */
	public void commit(){
		for(NotaDeCreditoDet det:aplicaciones){
			
			//AplicacionDeNota aplicacion=AbstractNotasRules
				//.generarAplicaionParaNotaDeBonificacion(getNota(), det.getVenta());
			AplicacionDeNota aplicacion=new AplicacionDeNota();
			aplicacion.setAbono(getNotaBonificacion());
			aplicacion.setCargo(det.getVenta());
			aplicacion.setFecha(getNota().getFecha());
			
			BigDecimal disponible=det.getImporte();
			BigDecimal saldoNeto=det.getVenta().getSaldoCalculado();
			BigDecimal porAplicar=saldoNeto;
			if(saldoNeto.doubleValue()>=disponible.doubleValue()){
				porAplicar=disponible;
			}
			aplicacion.setImporte(porAplicar);
			getNotaBonificacion().agregarAplicacion(aplicacion);
			if(StringUtils.isBlank(det.getComentario())){
				det.setComentario(getNota().getComentario());
			}
		}
		
	}

	public void clienteChanged() {
		getAplicaciones().clear();
		
		
	}
	
	public void asignarCliente(String clave){
		if(!StringUtils.isBlank(clave)){
			if("1".equals(clave)){
				getNota().setCliente(null);
			}else{
				Cliente cliente=ServiceLocator2.getClienteManager().buscarPorClave(clave);				
				getNota().setCliente(cliente);
				
			}
			
		}
	}
	
	public void asignarFacturaSelectiva(){
		Venta venta=SelectorDeFacturas.buscarVentaSelectiva();
		if(venta!=null){
			if(venta.getCliente().equals(getNota().getCliente())){
				generarConceptos(Arrays.asList(venta));
			}else{
				MessageUtils.showMessage(
						"La venta no corresponde al cliente: "+getNota().getCliente().getNombre()
						+"\n corresponde a:"+venta.getCliente().getNombre()
						, "Error");
			}
			
		}
	}
	
	
}
