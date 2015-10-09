package com.luxsoft.sw3.tesoreria.ui.consultas;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXTable;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.sw3.tesoreria.TESORERIA_ROLES;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;
import com.luxsoft.sw3.tesoreria.ui.forms.SolicitudDeDepositoForm;

/**
 * Panel para el mantenimiento de solicitudes de depositos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SolicitudesParaPagoConDepositoPanel extends FilteredBrowserPanel<SolicitudDeDeposito> implements ApplicationContextAware{
	
	private boolean atendiendo;
	
	private ApplicationContext context;
	
	private JLabel atencioAutomaticaLabel;

	public SolicitudesParaPagoConDepositoPanel() {
		super(SolicitudDeDeposito.class);
	}
	
	public void init(){
		addProperty(
				"sucursal.nombre"
				,"origen"
				,"clave"
				,"nombre"
				,"documento"
				,"fecha"
				,"fechaDeposito"
				,"comentario"
				,"referenciaBancaria"
				,"total"
				,"cuentaDestino.descripcion"
				,"bancoOrigen.clave"
				,"solicita"
				,"salvoBuenCobro"
				,"comentario"
				,"cancelacion"
				,"comentarioCancelacion"
				,"log.modificado"
				,"importado"
				);
		addLabels(
				"Sucursal"
				,"Tipo"
				,"Cliente"
				,"Nombre"
				,"Folio"
				,"Fecha"
				,"Fecha (Dep)"
				,"Comentario"
				,"Referencia"
				,"Total"
				,"Cuenta Dest"
				,"Banco"
				,"Solicita"
				,"SBC"
				,"Comentario"
				,"Cancelacion"
				,"Comentario (Cancel)"
				,"Ultima Mod"
				,"Importado"
				);
		setDefaultComparator(GlazedLists.beanPropertyComparator(SolicitudDeDeposito.class, "importado"));
		manejarPeriodo();
		
		
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodo(-15);
	}
	
	@Override
	protected void installCustomComponentsInFilterPanel(DefaultFormBuilder builder) {
		atencioAutomaticaLabel=new JLabel(ResourcesUtils.getIconFromResource("images2/transmit_add.png"));
		builder.append("Atención (aut)",atencioAutomaticaLabel);
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {		
		grid.getColumnExt("Importado").setVisible(false);
	}
	
	public void open(){
		load();
	}
	
	@Override
	protected void dataLoaded(List data) {
		try {
			source.getReadWriteLock().writeLock().lock();
			source.clear();
			source.addAll(data);
			if(grid!=null)
				grid.packAll();
			afterLoad();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			source.getReadWriteLock().writeLock().unlock();
		}
		
	}

	public void reload(){
		if(isInitialized()){
			load();
		}
	}
	
	private Action prenderAtencion;
	private Action apagarAtencion;

	@Override
	public Action[] getActions() {
		if(actions==null){
			prenderAtencion=addAction("", "prenderAtencionAutomatica", "Encender atención automática");
			prenderAtencion.setEnabled(false);
			apagarAtencion=addAction("", "apagarAtencionAutomatica", "Apagar atención automática");
			actions=new Action[]{
				getLoadAction()
				,addRoleBasedContextAction(null, TESORERIA_ROLES.AUTORIZACION_DEPOSITOS.name(),this, "atenderSeleccion", "Atender")
				,addRoleBasedContextAction(null, TESORERIA_ROLES.AUTORIZACION_DEPOSITOS.name(),this, "cancelar", "Cancelar")
			//	,prenderAtencion
			//	,apagarAtencion
				};
		}
		return actions;
	}

	@Override
	protected List<SolicitudDeDeposito> findData() {
		String hql="from SolicitudDeDeposito s left join fetch s.cliente c" +
				" left join fetch s.cuentaDestino c" +
				" left join fetch s.bancoOrigen b" +
				" left join fetch s.pago p " +
				" where s.cancelacion is null and date(s.fecha) between ? and ?";
		return ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{
					periodo.getFechaInicial()
					,periodo.getFechaFinal()
					}
		);
	}
	
	@Override
	protected void doSelect(Object bean) {
		SolicitudDeDeposito sol=(SolicitudDeDeposito)bean;
		SolicitudDeDepositoForm.consultar(sol);
	}
	
	

	@Override
	protected void installEditors(EventList editors) {
		super.installEditors(editors);
		Matcher matcher=Matchers.beanPropertyMatcher(SolicitudDeDeposito.class, "atendido", Boolean.FALSE);
		editors.add(GlazedLists.fixedMatcherEditor(matcher));
	}
	
	public void atenderSeleccion(){
		if(getSelectedObject()!=null){			
			SolicitudDeDeposito sol=(SolicitudDeDeposito)getSelectedObject();
			try {
				setAtendiendo(true);
				doAtender(sol,false);
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				setAtendiendo(false);
			}
		}
	}

	public void atender(){		
		if(!isInitialized())
			return;
		if(isAtendiendo()){
			logger.info("El usuario actualmente esta atendiendo una solicitud.. en espera");
		}else{
			try {
				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						setAtendiendo(true);
						doAtender();
						setAtendiendo(false);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void doAtender(){
		
		if(getFilteredSource().isEmpty())
			return;
		logger.info("Atendienteo solicitudes pendientes  "+new Date());
		SolicitudDeDeposito sol=(SolicitudDeDeposito)getFilteredSource().get(getNext());
		doAtender(sol);
	}
	
	public void doAtender(SolicitudDeDeposito sol){
		try {
			
			doAtender(sol,true);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		
	}
	
	public void doAtender(SolicitudDeDeposito sol,boolean sbc){
		
		if(sol.getSalvoBuenCobro() && sbc)
			return;
		int index=source.indexOf(sol);
		if(index!=-1){
			sol=refresh(sol.getId());
			if(sol.isAtendido())
				return;
			sol=SolicitudDeDepositoForm.modificarParaAutorizar(sol);
			if(sol!=null){
				if(sol.isAutorizar()){
					//Generar autorizacion
					SolicitudDeDeposito duplicada=ServiceLocator2.getSolicitudDeDepositosManager().buscarDuplicada(sol);
					if(duplicada!=null){
						String pattern="Solicitud duplicada folio:{0} en la sucursal:{1}" +
								"\n solicitó:{2} " +
								"\n{3}" +
								"\n Desea autorizar?";
						String msg=MessageFormat.format(
								pattern, duplicada.getDocumento(),duplicada.getSucursal().getNombre()
								,duplicada.getSolicita()
								,duplicada.getPago()!=null?"AUTORIZADA :"+duplicada.getPagoInfo():"SIN AUTORIZAR");
						sol.setDuplicado(true);
						boolean res=MessageUtils.showConfirmationMessage(msg, "Autorización de depositos");
						if(res){
							sol=ServiceLocator2.getSolicitudDeDepositosManager().autorizar(sol);
						}else
							return;
						
					}else{
						sol=ServiceLocator2.getSolicitudDeDepositosManager().autorizar(sol);
						
						
					}					
					
				}else{
					sol=ServiceLocator2.getSolicitudDeDepositosManager().save(sol);
					
					
				}
				source.set(index, sol);
			}			
		}
	}
	
	public void cancelar(){
		SolicitudDeDeposito sol=(SolicitudDeDeposito)getSelectedObject();
		if(sol!=null){
			setAtendiendo(true);
			if(sol.getPago()!=null){
				MessageUtils.showMessage("Solicitud con abono, no se puede cancelar", "Cancelación de solicitudes");
				return;
			}
			if(MessageUtils.showConfirmationMessage("Cancelar la solicitud:\n "+sol, "Cancelación de solicitudes")){
				
				String comentario=JOptionPane.showInputDialog("Comentario de cancelación", "DATOS INCORRECTOS");
				sol.setCancelacion(new Date());
				sol.setComentarioCancelacion(comentario);
				int index=source.indexOf(sol);
				sol=ServiceLocator2.getSolicitudDeDepositosManager().save(sol);
				source.set(index, sol);
				
			}
			setAtendiendo(false);
		}
	}
	
	public SolicitudDeDeposito refresh(String id){
		String hql="from SolicitudDeDeposito s left join fetch s.cliente c" +
		" left join fetch s.cuentaDestino c" +
		" left join fetch s.bancoOrigen b" +
		" left join fetch s.pago p " +
		" where s.id=?";
		return (SolicitudDeDeposito)ServiceLocator2.getHibernateTemplate().find(hql,id).get(0);
	}

	public synchronized boolean isAtendiendo() {
		return atendiendo;
	}

	public void setAtendiendo(boolean atendiendo) {
		synchronized (this) {
			this.atendiendo = atendiendo;
		}
	}
	
	public void apagarAtencionAutomatica(){
		getScheduler().stop();
		atencioAutomaticaLabel.setIcon(ResourcesUtils.getIconFromResource("images2/transmit_delete.png"));
		prenderAtencion.setEnabled(true);
		apagarAtencion.setEnabled(false);
	}
	public void prenderAtencionAutomatica(){		
		getScheduler().start();
		atencioAutomaticaLabel.setIcon(ResourcesUtils.getIconFromResource("images2/transmit_add.png"));
		prenderAtencion.setEnabled(false);
		apagarAtencion.setEnabled(true);
	}
	
	private SchedulerFactoryBean getScheduler(){
		return (SchedulerFactoryBean)context.getBean("&scheduler");
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context=applicationContext;
		
	}
	
	private int next=0;
	
	public synchronized int getNext(){
		
		if(next>=getFilteredSource().size()){
			next=0;
			return next;
		}else{
			int current=next++;
			return current;
		}
	}

}
