package com.luxsoft.sw3.bi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Action;

import org.apache.activemq.thread.Task;
import org.apache.commons.lang.time.DateUtils;
import org.jdesktop.swingx.JXTable;
import org.jfree.ui.DateCellRenderer;

import com.luxsoft.siipap.cxc.CXCRoles;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.reports.CfdisNoEnviadosReport;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.Renderers.ToHourConverter;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.cfdi.CFDI_EnvioServices;
import com.luxsoft.sw3.cfdi.parches.CancelacionesDeCargos;
import com.luxsoft.sw3.cfdi.parches.CancelacionesEspecialDeNotasDeCredito;
import com.luxsoft.sw3.cfdi.parches.ValidarUUID;
import com.luxsoft.sw3.solicitudes.SolicitudDeModificacion;
import com.luxsoft.sw3.solicitudes.SolicitudDeModificacionForm;
import com.luxsoft.sw3.solicitudes.SolicitudDeModificacionFormModel;




/**
 * Consulta para la atencion de solicitud de modificaciones
 *  
 * @author Ruben Cancino 
 *
 */
public class AtencionDeSolicitudesDeModificacionPanel extends FilteredBrowserPanel<SolicitudDeModificacion>{

	public AtencionDeSolicitudesDeModificacionPanel() {
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
				,getViewAction()
				,addRoleBasedAction(CXCRoles.AUTORIZAR_MODIFICACIONES_DE_DATOS.name(),"atender", "Atender")
				,addRoleBasedAction(CXCRoles.AUTORIZAR_MODIFICACIONES_DE_DATOS.name(),"reporte", "Reporte")				
				
				};
		}
		return actions;
	}
	
	private Action prenderActualizacion;
	private Action apagarActualizacion;
	
	private Action prenderEnvio;
	private Action apagarEnvio;
	
	
	@Override
	protected List<Action> createProccessActions(){
		
		prenderActualizacion=addAction("", "prenderActualizacionAutomatica", "Encender Actualizacion automatica");
		
		apagarActualizacion=addAction("", "apagarActualizacionAutomatica", "Apagar Actualizacion automatica");
		apagarActualizacion.setEnabled(false);
		
		prenderEnvio=addAction("", "prenderEnvioAutomatico", "Encender Envio Cfdi");

		apagarEnvio=addAction("", "apagarEnvioAutomatico", "Apagar Envio Cfdi");
		apagarEnvio.setEnabled(false);
		
		List<Action> actions=new ArrayList<Action>();
		actions.add(addAction(null, "actualizarExistencias", "Actualizar Exist."));
		actions.add(prenderActualizacion);
		actions.add(apagarActualizacion);
		
		actions.add(prenderEnvio);
		actions.add(apagarEnvio);
		actions.add(addAction("", "envioDeCfdiDiaAnterior", "Enviar Cfdi dia ant."));
		actions.add(addAction("", "xmlNoEnviados", "Reporte De No Enviados"));
	
		actions.add(addAction("", "cancelacionDeCargos", "Cancelación De Cargos"));
		actions.add(addAction("", "cancelacionDeNotas", "Cancelación De Notas"));
		actions.add(addAction("", "validarUUID", "Revision UUID's"));
		
		return actions;
	}

	@Override
	protected List<SolicitudDeModificacion> findData() {
		String hql="from SolicitudDeModificacion s where s.fecha between  ? and ? order by s.estado ";
		return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});	
	}

	@Override
	public void open() {
		//load();
		timer=new Timer();
		timer.schedule(task, 1000, 30000);
		
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
	public void close() {
		super.close();
		//System.out.println("Cancelando tarea de cargado en background..");
		task.cancel();
		timer.purge();
	}
	
	private Timer timerActualizar;
	
	TimerTask actualizar=new TimerTask() {
			public void run() {
			actualizarExistenciasAutomatico();
		}
	};
	
	 public void start() {
		 System.out.println("Arrancando Actualizacion de Existencias automatico");
		 timerActualizar=new Timer();
         timerActualizar.schedule(actualizar,1000, 3600000 );
     
     }

     public void cancel() {

    	 System.out.println("Deteniendo Actualizacion de Existencias automatico");
    	 actualizar.cancel();
         timerActualizar.purge();

     }
     
     
     
     private Timer timerEnvioCfdi;
     
     TimerTask enviar=new TimerTask() {
			public void run() {
				envioDeCfdi();
		}
	};
	
	
	
	 public void startEnvio() {
		 System.out.println("Arrancando Actualizacion de Existencias automatico");
		 timerEnvioCfdi=new Timer();
         timerEnvioCfdi.schedule(enviar,1000, 3600000 );
     
     }

     public void cancelEnvio() {

    	 System.out.println("Deteniendo Actualizacion de Existencias automatico");
    	 enviar.cancel();
         timerEnvioCfdi.purge();

     }
	
	public void envioDeCfdi(){
		CFDI_EnvioServices service=ServiceLocator2.getCFDIEnvioServices();
		service.madarPorCorreo();
	}
	
	
	public void envioDeCfdiDiaAnterior(){
		CFDI_EnvioServices service=ServiceLocator2.getCFDIEnvioServices();
		 Date dia=DateUtils.addDays(new Date(),-1);
		service.madarPorCorreo(dia);
		MessageUtils.showMessage("Los CFDI's pendientes de ayer han sido enviados", "Envio de CFDI's");
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {

		grid.getColumnExt("Solicitado").setCellRenderer(new DateCellRenderer(new SimpleDateFormat("dd/MM/yyyy :hh:mm")));
		grid.getColumnExt("Autorizado").setCellRenderer(new DateCellRenderer(new SimpleDateFormat("dd/MM/yyyy :hh:mm")));
		grid.getColumnExt("Atendido").setCellRenderer(new DateCellRenderer(new SimpleDateFormat("dd/MM/yyyy :hh:mm")));
		
		
		//grid.getColumnExt("Recibi").setVisible(false);
		//grid.getColumnExt("Surtidor").setVisible(false);
		
	}
	
	public void atender(){
		SolicitudDeModificacion row=(SolicitudDeModificacion)getSelectedObject();
		if(row!=null){
			final AtenecionDeModificacionForm form=new AtenecionDeModificacionForm();
			form.open();
			if(!form.hasBeenCanceled()){
				row.setAtendio(form.getModificacion().getAtendio());
				row.setComentarioDeAtencion(form.getModificacion().getComentarioDeAtencion());
				row.setEstado(form.getModificacion().getEstado());
				ServiceLocator2.getHibernateTemplate().merge(row);
				load();
			}
		}
	}
	
	
	public void actualizarExistencias(){
		SincronizadorDeExistencias sync= new SincronizadorDeExistencias();
		sync.addSucursal(2L,3L,5L,6L,9L,11L).actualizarExistenciasOficinas(new Date());
		MessageUtils.showMessage("Existencias Actualizadas", "Actualizacion de Existencias");
	}
	
	public void actualizarExistenciasAutomatico(){
		SincronizadorDeExistencias sync= new SincronizadorDeExistencias();
		sync.addSucursal(2L,3L,5L,6L,9L,11L).actualizarExistenciasOficinas(new Date());
		System.out.println("Existencias Actualizadas");
	}
	
	
	public void prenderActualizacionAutomatica(){		
		start();
		prenderActualizacion.setEnabled(false);
		apagarActualizacion.setEnabled(true);
	}
	
	
	public void apagarActualizacionAutomatica(){
		cancel();
		prenderActualizacion.setEnabled(true);
		apagarActualizacion.setEnabled(false);
	}
	
	public void prenderEnvioAutomatico(){		
		startEnvio();
		prenderEnvio.setEnabled(false);
		apagarEnvio.setEnabled(true);
	}
	
	
	public void apagarEnvioAutomatico(){
		cancelEnvio();
		prenderEnvio.setEnabled(true);
		apagarEnvio.setEnabled(false);
	}
	
		
	public void reporte(){
		//BitacoraClientesCredito.show();
	}
	
	public void xmlNoEnviados(){
		CfdisNoEnviadosReport.run();
	}
	
	public void cancelacionDeCargos(){
	CancelacionesDeCargos task=new CancelacionesDeCargos("certificadopapel");
			Date dia=DateUtils.addDays(new Date(),-1);
			task.cancelacion(new Periodo(dia,dia));
			MessageUtils.showMessage("Cargos Cancelados", "Cancelación De Cargos");
	}
	
	public void cancelacionDeNotas(){
		CancelacionesEspecialDeNotasDeCredito task=new CancelacionesEspecialDeNotasDeCredito("certificadopapel");
		task.cancelacion(new Date());
		MessageUtils.showMessage("Notas Canceladas", "Cancelación De Notas");
	}
	
	public void validarUUID(){
		ValidarUUID task=new ValidarUUID();
		Date dia=DateUtils.addDays(new Date(),-1);
		task.validacion(new Periodo(dia,dia));
		MessageUtils.showMessage("UUID's Corregidos", "Correccion de UUID's");
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
