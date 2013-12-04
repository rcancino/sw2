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
public class AutorizacionDepositosPanel extends FilteredBrowserPanel<PagoConDeposito>{

	public AutorizacionDepositosPanel() {
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
				,"Fecha Solicitud"
				,"Total"
				,"Fecha"
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
		installTextComponentMatcherEditor("Total", "total");
		
		manejarPeriodo();
		//periodo=Periodo.hoy();
		
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodo(-15);
	}
	
	@Override
	protected void installEditors(EventList editors) {
		super.installEditors(editors);
		Matcher<PagoConDeposito> m=new Matcher<PagoConDeposito>(){
			public boolean matches(PagoConDeposito item) {
				return item.getAutorizacion()==null;
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
				,addRoleBasedContextAction(new AutorizablePredicate(), POSRoles.AUTORIZADOR_DE_DEPOSITOS.name(), this, "autorizar", "Autorizar")
				//,addRoleBasedContextAction(new ModificablePredicate(), POSRoles.AUTORIZADOR_DE_DEPOSITOS.name(), this, "modificar", "Anexar comentario")
				,addAction("", "modificar", "Anexar comentario")
				,addRoleBasedContextAction(new NotNullSelectionPredicate(), POSRoles.AUTORIZADOR_DE_DEPOSITOS.name(), this, "cancelarAutorizacion", "Cancelar (aut)")
				,addRoleBasedContextAction(new AutorizablePredicate(), POSRoles.AUTORIZADOR_DE_DEPOSITOS.name(), this, "cancelarDeposito", "Cancelar (depósito)")
				
				};
		return actions;
	}

	public void autorizar(){
		final PagoConDeposito source=(PagoConDeposito)getSelectedObject();
		if(source==null)
			return;
		if(source.getAutorizacion()!=null)
			return;
		String ref=JOptionPane.showInputDialog(getControl(), "Referencia Bancaria:");
		if(!StringUtils.isBlank(ref)){			
			boolean proceed=KernellSecurity.instance().hasRole(POSRoles.AUTORIZADOR_DE_DEPOSITOS.name());
			if(proceed){
				PagoConDeposito target=getDeposito(source.getId());
				AutorizacionDeAbono aut=AutorizacionesFactory.getAutorizacionParaDeposito();
				target.setAutorizacion(aut);
				persistir(target);
				refrescarAbono(source.getId());
			}else{
				JOptionPane.showMessageDialog(getControl(), "Derechos insuficientes para esta operación");
			}
		}else{
			MessageUtils.showMessage("La referencia no puede ser nula", "Depositos/Transferencias");
		}
		
	}
	
	public void modificar(){
		if(getSelectedObject()!=null){
			PagoConDeposito source=(PagoConDeposito)getSelectedObject();
			PagoConDeposito target=getDeposito(source.getId());
			
			ModificacionesPanel form=new ModificacionesPanel(target);
			form.open();
			if(!form.hasBeenCanceled()){
				persistir(target);
				refrescarAbono(target.getId());
			}
			if(getManager().isModificable(target) && (target.getAutorizacion()==null)){
				
			}
		}
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
	
	public void cancelarDeposito(){
		if(getSelectedObject()!=null){			
			PagoConDeposito deposito=(PagoConDeposito)getSelectedObject();
			try {
				if(deposito.getAutorizacion()==null){
					if(getManager().isModificable(deposito)){
						if(MessageUtils.showConfirmationMessage("Cancelar deposito  :\n"+deposito.toString()
								, "Cancelación de documentos")){
							AutorizacionDeAbono aut=AutorizacionesFactory.getCancelacionDeDeposito();
							Date fecha=Services.getInstance().obtenerFechaDelSistema();
							//PagoConDeposito target=(PagoConDeposito)
							getManager().cancelarPago(deposito.getId(), aut, fecha);
							refrescarAbono(deposito.getId());
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
	
	private class ModificablePredicate implements Predicate{
		public boolean evaluate(Object bean) {
			PagoConDeposito pago=(PagoConDeposito)bean;
			if(pago!=null){
				if( getManager().isModificable(pago)){
					return (pago.getAutorizacion()==null) ;
				}
			}
			return false;
		}
	}
	
	private class AutorizablePredicate implements Predicate{
		public boolean evaluate(Object bean) {
			PagoConDeposito pago=(PagoConDeposito)bean;
			if(pago!=null){
				return ((pago.getAutorizacion()==null) && !pago.isCancelado());
			}
			return false;
		}
	}
	
	/**
	 * Forma para el mantenimiento de propiedades modificables de un deposito
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	private static class ModificacionesPanel extends AbstractForm{
		
		public ModificacionesPanel(PagoConDeposito pago) {
			super(new DefaultFormModel(pago));
			setTitle("Mantenimiento de abonos por autorizar");
		}

		@Override
		protected JComponent buildFormPanel() {
			FormLayout layout=new FormLayout("p,2dlu,p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Comentario",getControl("comentario"));
			builder.append("SBC",getControl("salvoBuenCobro"));
			return builder.getPanel();
		}
		
	}
	
	
	
	

	
}
