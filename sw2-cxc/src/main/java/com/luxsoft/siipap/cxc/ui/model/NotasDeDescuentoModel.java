package com.luxsoft.siipap.cxc.ui.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.AplicacionDeNota;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDescuento;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDescuento.TipoDeDescuento;
import com.luxsoft.siipap.cxc.rules.NotaDescuentoRules;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;

public class NotasDeDescuentoModel extends DefaultFormModel{
	
	
	private EventList<AplicacionDeNota> aplicaciones;
	private EventList<NotaDeCreditoDescuento> notas;
	private NotaDescuentoRules rules;
	
	public NotasDeDescuentoModel(final List<Cargo> cargos){
		super(Bean.proxy(DescModel.class));
		generarAplicaciones(cargos);
	}
	
	private DescModel getDescModel(){
		return (DescModel)getBaseBean();
	}
	
	protected void init(){
		rules=new NotaDescuentoRules();
		aplicaciones=new BasicEventList<AplicacionDeNota>();
		notas=new BasicEventList<NotaDeCreditoDescuento>();
	}
	
	public void asignarFolio(){
		getDescModel().setFolio(ServiceLocator2.getCXCManager().buscarProximaNota());
	}
	
	public void generarAplicaciones(final List<Cargo> cargos){		
		aplicaciones.clear();
		aplicaciones.addAll(rules.generarAplicaciones(cargos));
		notas.clear();
	}
	
	public EventList<AplicacionDeNota> getAplicaciones() {
		return aplicaciones;
	}
	
	public String getCliente(){
		if(aplicaciones.isEmpty())
			return "NO DISPONIBLE";
		else
			return aplicaciones.get(0).getCargo().getCliente().getNombreRazon();
	}
	
	
	public EventList<NotaDeCreditoDescuento> getNotas() {
		for(NotaDeCreditoDescuento d:notas){
			d.setFolio(getDescModel().getFolio());
		}
		return notas;
	}
	
	public void procesar(){		
		if(notas.isEmpty()){
			
			List<AplicacionDeNota> copy=new ArrayList<AplicacionDeNota>();
			copy.addAll(aplicaciones);
			notas.addAll(rules.generarNotas(copy));
			for(NotaDeCredito nota:notas){
				nota.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
				nota.setFecha(getSelectedFecha());
				for(Aplicacion a:nota.getAplicaciones()){
					a.setFecha(getSelectedFecha());	
					if(a.getCargo().getDescuentoFinanciero()>0){
						nota.setComentario("DESCUENTO POR PRONTO PAGO");
						NotaDeCreditoDescuento nd=(NotaDeCreditoDescuento)nota;
						nd.setTipoDeDescuento(TipoDeDescuento.FINANCIERO);
					}
				}
			}
		}
		int proxima=ServiceLocator2.getCXCManager().buscarProximaNota();
		getDescModel().setFolio(proxima);
		validate();
	}

	public void reset(){
		if(!notas.isEmpty()){
			notas.clear();
		}
	}

	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(notas.isEmpty()){
			support.addError("", "Sin notas por generar");
		}if( (getDescModel().getFolio()==null)||(getDescModel().getFolio().doubleValue()<=0))
			support.addError("folio", "Se requiere el folio");
	}

	public Date getSelectedFecha(){
		return (Date)getValue("fecha");
	}

	public  static class DescModel{
		
		private Date fecha=new Date();
		private Integer folio=new Integer(0);
		public Date getFecha() {
			return fecha;
		}
		public void setFecha(Date fecha) {
			this.fecha = fecha;
		}
		public Integer getFolio() {
			return folio;
		}
		public void setFolio(Integer folio) {
			this.folio = folio;
		}
		
		
	}

}
