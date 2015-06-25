package com.luxsoft.siipap.pos.ui.selectores;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.ListUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.sw3.services.Services;

public class SelectorDeSolicitudesPendietes extends AbstractSelector<SolicitudDeTraslado>{
	
	private Sucursal sucursal;

	public SelectorDeSolicitudesPendietes() {
		super(SolicitudDeTraslado.class, "Solicitudes pendientes por atender");
	}

	@Override
	protected List<SolicitudDeTraslado> getData() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar  fechaActual=Calendar.getInstance();
         fechaActual.add(Calendar.DATE, -7);
	     Date fechaCorte=fechaActual.getTime();
	    Object[] params={getSucursal().getId(),fechaCorte};
	    String hql="from SolicitudDeTraslado sol where sol.origen.id=? and sol.atendido is null and fecha>=? and sol.noAtender is false and ifnull(sol.comentario,'')<>'CANCELACION AUTOMATICA'";
		return Services.getInstance().getHibernateTemplate().find(hql,params);
	}

	@Override
	protected TableFormat<SolicitudDeTraslado> getTableFormat() {
		String[] props={"sucursal.nombre","documento","fecha","comentario","clasificacion"};
		String[] names={"Sucursal","Docto","Fecha","Comentario","Clasificacion"};
		return GlazedLists.tableFormat(SolicitudDeTraslado.class, props,names);
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
	
	public static List<SolicitudDeTraslado> seleccionar(Long sucursalId){
		Sucursal sucursal=(Sucursal)Services
			.getInstance()
			.getUniversalDao()
			.get(Sucursal.class, sucursalId);
		return seleccionar(sucursal);
	}
	
	public static List<SolicitudDeTraslado> seleccionar(){
		return seleccionar(Services.getInstance().getConfiguracion().getSucursal());
	}
	
	public static List<SolicitudDeTraslado> seleccionar(Sucursal sucursal){
		SelectorDeSolicitudesPendietes form=new SelectorDeSolicitudesPendietes();
		form.setSucursal(sucursal);
		form.open();
		if(!form.hasBeenCanceled()){
			List<SolicitudDeTraslado> selected=new ArrayList<SolicitudDeTraslado>();
			selected.addAll(form.getSelectedList());
			return selected;
		}
		return ListUtils.EMPTY_LIST; 
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
				System.out.println(seleccionar(2L));
				System.exit(0);
			}

		});
	}
	

}
