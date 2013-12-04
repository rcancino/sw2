package com.luxsoft.sw3.solicitudes;

import java.util.List;

import javax.swing.Action;

import org.jdesktop.swingx.JXTable;

import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.sw3.services.Services;



/**
 * Consulta para el monitoreo de entregas
 *  
 * @author Ruben Cancino 
 *
 */
public class SolicitudDeModificacionesPOSPanel extends FilteredBrowserPanel<SolicitudDeModificacion>{

	public SolicitudDeModificacionesPOSPanel() {
		super(SolicitudDeModificacion.class);		
	}
	
	protected void init(){
		String[] props={"sucursal","folio","fecha","modulo","tipo","documento","estado","usuario","descripcion"};
		addProperty(props);
		addLabels("Sucursal","Folio","Fecha","Modulo","tipo","documento","estado","usuario","descripcion");
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Usuario", "usuario");
		installTextComponentMatcherEditor("Documento", "documento");
		manejarPeriodo();
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null){
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getViewAction()
				
				};
		}
		return actions;
	}

	@Override
	protected List<SolicitudDeModificacion> findData() {
		String hql="from SolicitudDeModificacion s where s.fecha between  ? and ?";
		return Services.getInstance().getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});	
	}

	@Override
	public void open() {
		load();
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {

		//grid.getColumnExt("modificado").setCellRenderer(new DefaultTableRenderer(new Renderers.));
		
		//grid.getColumnExt("Recibió").setVisible(false);
		//grid.getColumnExt("Surtidor").setVisible(false);
		
	}
	
	
	
	@Override
	protected SolicitudDeModificacion doInsert() {
		final SolicitudDeModificacionFormModel model=new SolicitudDeModificacionFormModel(
				Services.getInstance().getConfiguracion().getSucursal());
		model.setManager(Services.getInstance().getSolicitudDeModificacionesManager());
		final SolicitudDeModificacionForm form=new SolicitudDeModificacionForm(model);
		form.setHibernateTemplate(Services.getInstance().getHibernateTemplate());
		form.open();
		if(!form.hasBeenCanceled()){
			return model.commit();
		}
		return null;
	}
	
	@Override
	protected void doSelect(Object bean) {
		SolicitudDeModificacion selected=(SolicitudDeModificacion)bean;
		SolicitudDeModificacionFormModel model=new SolicitudDeModificacionFormModel(selected, selected.getSucursal());
		model.setReadOnly(true);
		model.setManager(Services.getInstance().getSolicitudDeModificacionesManager());
		SolicitudDeModificacionForm form=new SolicitudDeModificacionForm(model);
		form.setHibernateTemplate(Services.getInstance().getHibernateTemplate());
		form.open();
	}
	

}
