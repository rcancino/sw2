package com.luxsoft.siipap.pos.ui.consultas;

import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jdesktop.swingx.calendar.DateUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.util.Worker;
import com.luxsoft.siipap.cxc.model.AutorizacionDeAbono;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.services.PagosManager;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.services.AutorizacionesFactory;
import com.luxsoft.sw3.ventas.Pedido;


/**
 * Panel para el mantenimiento y administracion de pagos mediante depositos y/o transferencias
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class DepositosAutorizadosPanel extends FilteredBrowserPanel<PagoConDeposito>{

	public DepositosAutorizadosPanel() {
		super(PagoConDeposito.class);
	}
	
	protected void init(){		
		addProperty(
				"sucursal.nombre"
				,"origen"
				,"nombre"
				,"tipo"
				,"folio"
				,"fecha"
				,"total"
				,"fechaDeposito"
				,"banco"
				,"cuenta.banco"
				,"salvoBuenCobro"
				,"autorizacionInfo"
				,"comentario"
				,"solicito"
				);
		addLabels(
				"Sucursal"
				,"Origen"
				,"Cliente"
				,"Tipo"
				,"Folio"
				,"Fecha"
				,"Total"
				,"Fecha Solicitud"
				,"Banco Ori"
				,"Banco Dest"
				,"SBC"
				,"Autorizado"
				,"Comentario"
				,"Solicita"
				);
		
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Cliente", "cliente.clave","cliente.nombre");		
		installTextComponentMatcherEditor("Forma de Pago", "tipo");
		
		manejarPeriodo();
		//periodo=Periodo.hoy();
		
		
	}
	
	protected void manejarPeriodo(){
		//periodo=Periodo.getPeriodoDelMesActual();
		Date hoy=Services.getInstance().obtenerFechaDelSistema();
		long l=DateUtils.addDays(hoy.getTime(), -15);
		Date inicio=new Date(l);
		periodo=new Periodo(inicio,hoy);
	}
	
	@Override
	protected void installEditors(EventList editors) {
		super.installEditors(editors);
		Matcher<PagoConDeposito> m=new Matcher<PagoConDeposito>(){
			public boolean matches(PagoConDeposito item) {
				return item.getAutorizacion()!=null;
			}
		};
		MatcherEditor editor=GlazedLists.fixedMatcherEditor(m);
		editors.add(editor);
	}
	
	
	/*@Override
	protected void installEditors(EventList editors) {
		super.installEditors(editors);
		Matcher<PagoConDeposito> m1=new Matcher<PagoConDeposito>(){
			public boolean matches(PagoConDeposito item) {
				return item.getAutorizacion()==null;
			}
		};
		MatcherEditor editor1=GlazedLists.fixedMatcherEditor(m1);		
		editors.add(editor1);
		
	}*/
	
	@Override
	protected void afterGridCreated() {
		selectionModel.setSelectionMode(ListSelection.SINGLE_INTERVAL_SELECTION);
	}

	@Override
	protected List<PagoConDeposito> findData() {
		String hql="from PagoConDeposito p where p.origen in(\'CAM\',\'MOS\') " 
			+"and p.fecha between ? and ?"
			;
		Date fechaInicial=periodo.getFechaInicial();
		Date limite=DateUtil.toDate("15/12/2009");
		if(fechaInicial.before(limite))
			fechaInicial=limite;
		Object[] params={fechaInicial,periodo.getFechaFinal()};
		return Services.getInstance().getHibernateTemplate().find(hql,params);
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,addRoleBasedContextAction(new NotNullSelectionPredicate(), POSRoles.AUTORIZADOR_DE_DEPOSITOS.name(), this, "cancelarAutorizacion", "Cancelar (aut)")
				
				
				};
		return actions;
	}

	
	
	
	public void cancelarAutorizacion(){
		if(getSelectedObject()!=null){			
			PagoConDeposito deposito=(PagoConDeposito)getSelectedObject();
			try {
				if(deposito.getAutorizacion()!=null){
					if(getManager().isModificable(deposito)){
						if(MessageUtils.showConfirmationMessage("Cancelar autorizacion para depósito  :\n"+deposito.toString()
								, "Cancelación de documentos")){
							
							//Services.getInstance().getUniversalDao().remove(AutorizacionDeAbono.class, deposito.getAutorizacion().getId());
							AutorizacionDeAbono aut=deposito.getAutorizacion();
							deposito.setAutorizacion(null);
							persistir(deposito);
							refrescarAbono(deposito.getId());
							Services.getInstance().getUniversalDao().remove(AutorizacionDeAbono.class, aut.getId());
						}
					}
				}
				
			} catch (Exception e) {
				String err=ExceptionUtils.getRootCauseMessage(e);
				JOptionPane.showMessageDialog(getControl(), err,"Error de generación",JOptionPane.ERROR_MESSAGE);
			}
		}	
	}
	
	
	
	
	/**
	 * Refresca desde la base de datos el abono indicado
	 * 
	 * @param id
	 */
	public void refrescarAbono(String id){
		try {
			PagoConDeposito a=getDeposito(id);
			for(int index=0;index<source.size();index++){
				Pago other=(Pago)source.get(index);
				if(other.getId().equals(a.getId()))
					source.set(index, a);
			}			
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}		
	}
	
	/**
	 * Persiste un PagoConDeposito
	 * 
	 * TODO Mover a PagosManager
	 *  
	 * @param deposito
	 * @return
	 */
	private PagoConDeposito persistir(PagoConDeposito deposito){
		return (PagoConDeposito) getManager().salvar(deposito);
	}
	
	/**
	 * Regresa una copia fresca del deposito
	 * 
	 * @param id
	 * @return
	 */
	private PagoConDeposito getDeposito(String id){
		return(PagoConDeposito) getManager().getAbono(id);
	}
	
	protected PagosManager getManager(){
		return Services.getInstance().getPagosManager();
	}
	
	
	
	
	Timer task;
	
	public void startWatch(){
		if(task==null){
			ActionListener handler=EventHandler.create(ActionListener.class, this, "buscarNuevos");
			task=new Timer(2000,handler);
		}
		if(!task.isRunning())
			task.start();
	}
	
	public void close(){
		if(task!=null )
			if(task.isRunning() ){
				task.stop();
				task=null;
			}
		super.close();
	}
	
	
	public void buscarNuevos(){
		System.out.println("buscando .....");
		logger.info("Buscando nuevos depositos");
	}
	

	@Override
	protected void afterLoad() {
		startWatch();
	}
	
	//private SwingWorker<, V>

}
