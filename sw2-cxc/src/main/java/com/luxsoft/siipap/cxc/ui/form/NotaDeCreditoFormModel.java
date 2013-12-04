package com.luxsoft.siipap.cxc.ui.form;

import java.util.List;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.AplicacionDeNota;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.AbstractNotasRules;
import com.luxsoft.siipap.cxc.model.NotaRules;
import com.luxsoft.siipap.cxc.ui.AbonoFormModel;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * FormModel para la generacion y mantenimiento general de notas de credito 
 * 
 * @author Ruben Cancino
 *
 */
public class NotaDeCreditoFormModel extends AbonoFormModel{
	
	protected EventList<AplicacionDeNota> aplicaciones;
	
	protected NotaRules rules=new AbstractNotasRules();

	public NotaDeCreditoFormModel(NotaDeCredito bean, boolean readOnly) {
		super(bean, readOnly);
		String[] props={"cargo.documento","cargo.numeroFiscal"
				,"cargo.fecha","cargo.total","cargo.devoluciones"
				,"cargo.bonificaciones"
				,"cargo.descuentos"
				,"cargo.saldoSinPagos"
				,"cargo.saldoCalculado"
				,"cargo.descuentoGeneral"
				,"importe"
				};
		String[] labels={"Docto","Fiscal","Fecha","Tot","Devoluciones"
				,"Bonificaciones","Descuentos"
				,"Saldo (S.P)"
				,"Saldo (Neto)"
				,"Dscto Nota"
				,"Por Aplicar"};
		setProperties(props);
		setLabels(labels);
	}
	
	protected void init(){
		aplicaciones=new BasicEventList<AplicacionDeNota>();
		
	}
	
	public void asignarFolio(){
		//getNota().setFolio(ServiceLocator2.getCXCManager().buscarProximaNota());
	}
	
	public NotaDeCredito getNota(){
		NotaDeCredito nota= (NotaDeCredito)getBaseBean();
		for(Aplicacion a:nota.getAplicaciones()){
			a.setFecha(nota.getFecha());
		}
		return nota;
	}

	/**
	 * Read-Only view de la lista de aplicaciones para esta nota
	 * Garantizamos que las aplicaciones solo se puedan agregar o eliminar
	 * mediante esta clase
	 * 
	 * @return
	 */
	public EventList<AplicacionDeNota> getAplicaciones() {
		return GlazedLists.readOnlyList(aplicaciones);
	}
	
	/**
	 * Permite generar aplicaciones para la list de cuentas por pagar
	 * indicada
	 * 
	 * @param cuentasPorPagar
	 */
	public void aplicar(List<Cargo> cuentas){
		if(cuentas.isEmpty())
			return;
		for(Cargo cuenta:cuentas){
			AplicacionDeNota aplicacion=AbstractNotasRules.generarAplicaionParaNotaDeBonificacion(getNota(), cuenta);
			aplicacion=atenderAltaDeAplicacion(aplicacion);
			if(aplicacion!=null)
				aplicaciones.add(aplicacion);
		}
		actualizar();		
	}
	
	/**
	 * Delega a sub-clases detalle de la generacion. Las sub clases pueden invalidar el proceso
	 * regresando nulo
	 * 
	 * @param a
	 * @return La aplicacion modificada (con o sin modificacion) o nulo si no es valida 
	 */
	public AplicacionDeNota atenderAltaDeAplicacion(AplicacionDeNota a){
		return a;
	}
	
	
	
	/**
	 * Template method para acutalizar  los datos de la nota de credito y
	 * sus  aplicaciones (De ser necesario)
	 * 
	 */
	public void actualizar(){
		actualizarAplicacionesUI();
	}
	
	/**
	 * Actualiza las aplicaciones para el grid UI
	 */
	public void actualizarAplicacionesUI(){
		for(int index=0;index<aplicaciones.size();index++){
			AplicacionDeNota a=aplicaciones.get(index);
			aplicaciones.set(index,a);
		}
	}
	
	public void eliminarAplicaciones(final List<Aplicacion> data){
		for(Aplicacion a:data){
			boolean res=eliminarAplicacion(a);
			if(res)
				aplicaciones.remove(a);
		}
		actualizar();
	}
	
	/**
	 * Elimina la aplicacion del la Nota. Las sub-clases pueden meter reglas adicionales
	 * para la eliminacion
	 * 
	 * @param a
	 * @return
	 */
	public boolean eliminarAplicacion(final Aplicacion a){
		return getNota().eliminarAplicacion(a);
	}
	
	/**
	 * Genera un {@link TableFormat} adecuado para las aplicaciones de la nota
	 * 
	 * @return
	 */
	public TableFormat<AplicacionDeNota> createTableformat(){
		return GlazedLists.tableFormat(AplicacionDeNota.class,getProperties(), getLabels());
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
	
	
	
	
}
