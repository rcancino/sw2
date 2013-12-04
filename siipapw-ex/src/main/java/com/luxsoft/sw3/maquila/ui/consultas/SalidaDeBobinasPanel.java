package com.luxsoft.sw3.maquila.ui.consultas;

import javax.swing.Action;

import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;
import com.luxsoft.sw3.maquila.model.SalidaDeBobinas;
import com.luxsoft.sw3.maquila.ui.forms.SalidaDeBobinasForm;
import com.luxsoft.sw3.maquila.ui.selectores.SelectorDeBobinasDisponibles;
import com.luxsoft.sw3.maquila.ui.selectores.SelectorDeMaqs;
import com.luxsoft.sw3.services.MaquilaManager;

/**
 * Panel para el mantenimiento de Salidas de bobinas 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SalidaDeBobinasPanel extends FilteredBrowserPanel<SalidaDeBobinas>{

	public SalidaDeBobinasPanel() {
		super(SalidaDeBobinas.class);		
	}
	
	public void init(){
		addProperty(
				"id"
				,"fecha"
				,"origen.almacen.nombre"
				,"origen.id"
				,"origen.entradaDeMaquilador"
				,"origen.fecha"
				,"origen.disponibleKilos"
				,"producto.clave"
				,"producto.descripcion"				
				,"cantidad"
				,"destino.documento"
				,"destino.remision"
				,"destino.fecha"
				,"destino.unidad"
				,"comentario"
				);
		addLabels(
				"Id"
				,"Fecha"
				,"Almacén"
				,"Ent (Id)"
				,"Ent (Maq)"
				,"Ent (Fec)"
				,"Disponible"
				,"Bobina"
				,"Descripción"				
				,"Cantidad"
				,"Maq"
				,"Maq (Rem)"
				,"Maq (Fecha)"
				,"Unidad"
				,"Comentario"
				);
		manejarPeriodo();
		periodo=Periodo.periodoDeloquevaDelYear();
		installTextComponentMatcherEditor("Almacén", "origen.almacen.nombre");
		installTextComponentMatcherEditor("Entrada (Maq)", "origen.entradaDeMaquilador");
		installTextComponentMatcherEditor("Bobina", "producto.clave","producto.descripcion");
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getDeleteAction()
				,getEditAction()
				,getViewAction()
				};
		return actions;
	}
	
	@Override
	protected SalidaDeBobinas doInsert() {
		EntradaDeMaterialDet origen=SelectorDeBobinasDisponibles.seleccionar(null);		
		if(origen!=null){
			EntradaDeMaquila destino=SelectorDeMaqs.find(origen.getProducto());
			if(destino!=null){
				SalidaDeBobinas salida=new SalidaDeBobinas();
				salida.setOrigen(origen);
				salida.setDestino(destino);
				if(destino.getPendiente()<=origen.getDisponibleKilos().doubleValue())
					salida.setCantidad(destino.getPendiente());
				else
					salida.setCantidad(origen.getDisponibleKilos().doubleValue());
				salida.setFecha(destino.getFecha());
				SalidaDeBobinasForm form=new SalidaDeBobinasForm(salida);
				form.open();
				if(!form.hasBeenCanceled()){
					SalidaDeBobinas target=(SalidaDeBobinas)form.getModel().getBaseBean();
					target=getManager().salvarSalidaDeBobina(target);
					return target;
				}
			}
		}
		return null;
	}
	

	@Override
	public boolean doDelete(SalidaDeBobinas bean) {
		getManager().eliminarSalidaDeBobina(bean);
		return true;
	}

	private MaquilaManager getManager(){
		return ServiceLocator2.getMaquilaManager();
	}

}
