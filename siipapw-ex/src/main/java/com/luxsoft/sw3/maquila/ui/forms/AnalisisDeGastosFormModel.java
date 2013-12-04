package com.luxsoft.sw3.maquila.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.maquila.model.AnalisisDeGastos;
import com.luxsoft.sw3.maquila.model.AnalisisDeMaterial;
import com.luxsoft.sw3.maquila.ui.selectores.SelectorDeMaqsParaAnalisis;





/**
 * Controlador y PresentationModel para la fomra y mantenimiento al analisis de material
 * {@link AnalisisDeMaterial}
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnalisisDeGastosFormModel extends DefaultFormModel {
	
	private EventList<EntradaDeMaquila> partidasSource;
	private EventList<Producto> productos;
	
	
	protected Logger logger=Logger.getLogger(getClass());

	public AnalisisDeGastosFormModel() {
		super(new AnalisisDeGastos());
	}
	
	public AnalisisDeGastosFormModel(AnalisisDeGastos analisis) {
		super(analisis);
	}
	
	protected void init(){		
		if(getAnalisis().getId()==null){
			partidasSource=GlazedLists.eventList(new ArrayList<EntradaDeMaquila>());
		}
		else
			partidasSource=GlazedLists.eventList(getAnalisis().getEntradas());
		getModel("importeFlete").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				getAnalisis().actualizarCostos();
			}
			
		});
		getModel("importeMaquilador").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				getAnalisis().actualizarCostos();
			}
			
		});
		
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getAnalisis().getEntradas().size()==0){
			support.addError("", "Debe registrar por lo menos una partida");
		}		
		super.addValidation(support);
	}
	
	public EventList<EntradaDeMaquila> getPartidasSource() {
		return partidasSource;
	}	
	
	public AnalisisDeGastos getAnalisis(){
		return (AnalisisDeGastos)getBaseBean();
	}
	
	protected EventList<Producto> getProductos(){
		if(productos==null){
			productos=new BasicEventList<Producto>();
		}
		return productos;
	}
	
	public void insertar(){
		List<EntradaDeMaquila> entradas=SelectorDeMaqsParaAnalisis.seleccionar(true);
		for(EntradaDeMaquila det:entradas){
			boolean ok=getAnalisis().agregarEntrada(det);
			if(ok){
				getAnalisis().actualizarCostos();
				afterInserPartida(det);
				partidasSource.add(det);
				
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "Análisis de bobinas");
			}
		}
	}
	
	public void afterInserPartida(EntradaDeMaquila det){		
		validate();
	}
	
	public void elminarPartida(int index){
		EntradaDeMaquila det=partidasSource.get(index);
		if(det!=null){
			boolean ok=getAnalisis().eliminarEntrada(det);
			if(ok){
				partidasSource.remove(index);
				getAnalisis().actualizarCostos();
				validate();
				return;
			}
		}
		System.out.println("Existe un error en la seleccion de partidas");
	}
	
	
	
	protected EntradaDeMaquila beforeUpdate(final EntradaDeMaquila source){
		EntradaDeMaquila target=new EntradaDeMaquila();
		BeanUtils.copyProperties(source, target, new String[]{"id","version","log"});
		return target;
	}
	
	protected void afterUpdate(EntradaDeMaquila source,EntradaDeMaquila target){		
		BeanUtils.copyProperties(target,source, new String[]{"id","version","log"});
		validate();
	}
	
	
	/**
	 * Actualiza el impuesto y el total de lo maquilado a partir del importe
	 * 
	 */
	public void actualizarTotalMaquilaConImporte() {
		
		BigDecimal imp=getAnalisis().getImporteMaquilador();		
		getAnalisis().setImpuestoMaquilador(MonedasUtils.calcularImpuesto(imp));
		getAnalisis().setTotalMaquilador(MonedasUtils.calcularTotal(imp));
	}
	
	public void actualizarTotalMaquilaConTotal() {
		BigDecimal imp=getAnalisis().getTotalMaquilador();
		getAnalisis().setImporteMaquilador(MonedasUtils.calcularImporteDelTotal(imp));
		getAnalisis().setImpuestoMaquilador(MonedasUtils.calcularImpuesto(getAnalisis().getImporteMaquilador()));
	}
	
	/**
	 * Actualiza el impuesto y el total del flete a partir del importe
	 * 
	 */
	public void actualizarTotalFleteConImporte() {
		
		BigDecimal imp=getAnalisis().getImporteFlete();		
		getAnalisis().setImpuestoFlete(MonedasUtils.calcularImpuesto(imp));
		getAnalisis().setTotalFlete(MonedasUtils.calcularTotal(imp));
	}
	
	public void actualizarTotalFleteConTotal() {
		BigDecimal imp=getAnalisis().getTotalFlete();
		getAnalisis().setImporteFlete(MonedasUtils.calcularImporteDelTotal(imp));
		getAnalisis().setImpuestoFlete(MonedasUtils.calcularImpuesto(getAnalisis().getImporteFlete()));
	}
}
