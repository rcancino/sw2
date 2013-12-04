package com.luxsoft.sw3.bi;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Action;

import org.jdesktop.swingx.JXTable;
import org.jfree.ui.DateCellRenderer;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.Matchers;

import com.luxsoft.siipap.cxc.CXCRoles;
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
public class SolicitudDeModificacionesPendientesPanel extends FilteredBrowserPanel<SolicitudDeModificacion>{

	public SolicitudDeModificacionesPendientesPanel() {
		super(SolicitudDeModificacion.class);		
	}
	
	protected void init(){
		String[] props={"sucursal","folio","fecha","modulo","tipo","documento","estado","usuario","log.creado","autorizo","autorizacion","atendio","log.modificado","descripcion","comentarioDeAtencion"};
		addProperty(props);
		addLabels("Sucursal","Folio","Fecha","Modulo","tipo","documento","estado","Solicito","Solicitado","Autorizo","Autorizado","Atendio","Atendido","Descripcion","C.Atendio");
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Usuario", "usuario");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("Estado", "estado");
		installTextComponentMatcherEditor("Fecha", "fecha");
		manejarPeriodo();
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null){
			actions=new Action[]{
				getLoadAction()
				//,getInsertAction()
				//,getViewAction()
				,addRoleBasedAction(CXCRoles.AUTORIZAR_MODIFICACIONES_DE_DATOS.name(),"atender", "Atender")
				,addRoleBasedAction(CXCRoles.AUTORIZAR_MODIFICACIONES_DE_DATOS.name(),"reporte", "Reporte")
				};
		}
		return actions;
	}

	@Override
	protected List<SolicitudDeModificacion> findData() {
		String hql="from SolicitudDeModificacion s where s.fecha between  ? and ? and  s.estado=?";
		return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal(),SolicitudDeModificacion.ESTADO.PENDIENTE});	
	}
	
	private Timer timer;
	
	TimerTask task=new TimerTask() {
		@Override
		public void run() {
			//System.out.println("Cargando datos en timer......");
			load();
		}
	};

	@Override
	public void open() {
		//load();
		timer=new Timer();
		timer.schedule(task, 1000, 30000);
		
	}
	@Override
	public void close() {
		super.close();
		//System.out.println("Cancelando tarea de cargado en background..");
		task.cancel();
		timer.purge();
	}
	
	@Override
	protected EventList getSourceEventList() {
		EventList eventList=super.getSourceEventList();
		return new FilterList(eventList, Matchers.beanPropertyMatcher(SolicitudDeModificacion.class, "estado", SolicitudDeModificacion.ESTADO.PENDIENTE)); 
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {

		grid.getColumnExt("Solicitado").setCellRenderer(new DateCellRenderer(new SimpleDateFormat("dd/MM/yyyy :hh:mm")));
		grid.getColumnExt("Autorizado").setCellRenderer(new DateCellRenderer(new SimpleDateFormat("dd/MM/yyyy :hh:mm")));
		grid.getColumnExt("Atendido").setCellRenderer(new DateCellRenderer(new SimpleDateFormat("dd/MM/yyyy :hh:mm")));
		
		
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
			model.setValue("estado", SolicitudDeModificacion.ESTADO.AUTORIZADA);
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
