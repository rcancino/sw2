package com.luxsoft.sw3.bi;

import java.util.List;

import javax.swing.Action;

import org.apache.commons.lang.ArrayUtils;
import org.jdesktop.swingx.JXTable;

import com.luxsoft.siipap.cxc.CXCRoles;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.solicitudes.SolicitudDeModificacion;
import com.luxsoft.sw3.solicitudes.SolicitudDeModificacionForm;
import com.luxsoft.sw3.solicitudes.SolicitudDeModificacionFormModel;



/**
 * Consulta para el monitoreo de entregas
 *  
 * @author Ruben Cancino 
 *
 */
public class SolicitudDeModificacionesPanel extends FilteredBrowserPanel<SolicitudDeModificacion>{

	public SolicitudDeModificacionesPanel() {
		super(SolicitudDeModificacion.class);		
	}
	
	protected void init(){
		String[] props={"sucursal","folio","fecha","modulo","tipo","estado","documento","estado","usuario","descripcion"};
		addProperty(props);
		addLabels("Sucursal","Folio","Fecha","Modulo","tipo","estado","documento","estado","usuario","descripcion");
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
				//,addRoleBasedAction(CXCRoles.AUTORIZAR_MODIFICACIONES_DE_DATOS.name(),"atender", "Atender")
				//,addRoleBasedAction(CXCRoles.AUTORIZAR_MODIFICACIONES_DE_DATOS.name(),"reporte", "Reporte")
				};
		}
		User user=KernellSecurity.instance().getCurrentUser();
		if(user!=null && user.hasRole(CXCRoles.AUTORIZAR_MODIFICACIONES_DE_DATOS.name())){
			ArrayUtils.add(actions, addRoleBasedAction(CXCRoles.AUTORIZAR_MODIFICACIONES_DE_DATOS.name(),"atender", "Atender"));
			ArrayUtils.add(actions, addRoleBasedAction(CXCRoles.AUTORIZAR_MODIFICACIONES_DE_DATOS.name(),"reporte", "Reporte"));
		}
		return actions;
	}

	@Override
	protected List<SolicitudDeModificacion> findData() {
		String hql="from SolicitudDeModificacion s where s.fecha between  ? and ?";
		return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});	
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
	
	public void atender(){
		SolicitudDeModificacion row=(SolicitudDeModificacion)getSelectedObject();
		if(row!=null){
			if(row.getAutorizo()!=null){
				MessageUtils.showMessage("Solicitud autorizada o rechazada no se puede modificar", "Solicitud de modificaciones");
				return;
			}
			final SolicitudDeModificacionFormModel model=new SolicitudDeModificacionFormModel(
					(SolicitudDeModificacion)ServiceLocator2.getHibernateTemplate().get(SolicitudDeModificacion.class, row.getId()),
					ServiceLocator2.getConfiguracion().getSucursal()
					);
			model.setManager(ServiceLocator2.getSolicitudDeModificacionesManager());
			final SolicitudDeModificacionForm form=new SolicitudDeModificacionForm(model);
			form.setHibernateTemplate(ServiceLocator2.getHibernateTemplate());
			form.open();
			if(!form.hasBeenCanceled()){
				model.commit();
				load();
			}
		}
	}
	
	@Override
	protected SolicitudDeModificacion doInsert() {
		final SolicitudDeModificacionFormModel model=new SolicitudDeModificacionFormModel(
				ServiceLocator2.getConfiguracion().getSucursal());
		model.setManager(ServiceLocator2.getSolicitudDeModificacionesManager());
		final SolicitudDeModificacionForm form=new SolicitudDeModificacionForm(model);
		form.setHibernateTemplate(ServiceLocator2.getHibernateTemplate());
		form.open();
		if(!form.hasBeenCanceled()){
			return model.commit();
		}
		return null;
	}
	
	public void reporte(){
		//BitacoraClientesCredito.show();
	}
	
	@Override
	protected void doSelect(Object bean) {
		SolicitudDeModificacion selected=(SolicitudDeModificacion)bean;
		SolicitudDeModificacionFormModel model=new SolicitudDeModificacionFormModel(selected, selected.getSucursal());
		model.setReadOnly(true);
		model.setManager(ServiceLocator2.getSolicitudDeModificacionesManager());
		SolicitudDeModificacionForm form=new SolicitudDeModificacionForm(model);
		form.setHibernateTemplate(ServiceLocator2.getHibernateTemplate());
		form.open();
	}

}
