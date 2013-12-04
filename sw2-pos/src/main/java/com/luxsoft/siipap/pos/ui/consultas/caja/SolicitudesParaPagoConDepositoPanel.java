package com.luxsoft.siipap.pos.ui.consultas.caja;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.cxc.model.AutorizacionDeAbono;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.services.SolicitudDeDepositosManager;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;
import com.luxsoft.sw3.ui.services.AutorizacionesFactory;


/**
 * Panel para la autorizacion de depositos y transferencias
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SolicitudesParaPagoConDepositoPanel extends FilteredBrowserPanel<SolicitudDeDeposito> {
	
	
	
	

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
		//manejarPeriodo();
		
		
	}
	/*
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodo(-5);
	}*/

	@Override
	protected void adjustMainGrid(JXTable grid) {		
		grid.getColumnExt("Importado").setVisible(false);
	}
	
	public void open(){
		load();
	}
	
	
	@Override
	public Action[] getActions() {
		if(actions==null){
			actions=new Action[]{
				getLoadAction()
				,addAction(null,"atenderSeleccion", "Autorizar")
				,addAction(null,"cancelar", "Cancelar")
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
				" where s.cancelacion is null and date(s.fecha)= ?";
		return Services.getInstance().getHibernateTemplate().find(hql
				,new Object[]{
					new Date()
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
		SolicitudDeDeposito sol=(SolicitudDeDeposito)getSelectedObject();
		if(sol!=null)
			doAtender(sol);
	}
		
	private void doAtender(SolicitudDeDeposito sol){
		
		if(sol.getSalvoBuenCobro())
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
					SolicitudDeDeposito duplicada=getSolicitudDeDepositosManager().buscarDuplicada(sol);
					if(duplicada!=null){
						String pattern="Solicitud duplicada folio:{0} en la sucursal:{1}" +
								"\n solicitó:{2} " +
								"\n{3}" +
								"\n NO SE PUEDE AUTORIZAR?";
						String msg=MessageFormat.format(
								pattern, duplicada.getDocumento(),duplicada.getSucursal().getNombre()
								,duplicada.getSolicita()
								,duplicada.getPago()!=null?"AUTORIZADA :"+duplicada.getPagoInfo():"SIN AUTORIZAR");
						sol.setDuplicado(true);
						MessageUtils.showMessage(msg, "Autorización de depositos");
						sol=getSolicitudDeDepositosManager().save(sol);
						
						
						
					}else{
						AutorizacionDeAbono aut= AutorizacionesFactory.getAutorizacionParaDeposito();
						if(aut!=null){							
							sol=getSolicitudDeDepositosManager().autorizar(sol,aut);
						}else{
							return;
						}
					}					
					
				}else{
					sol=getSolicitudDeDepositosManager().save(sol);
				}
				source.set(index, sol);
			}			
		}
	}
	
	private SolicitudDeDepositosManager getSolicitudDeDepositosManager(){
		return Services.getInstance().getSolicitudDeDepositosManager();
	}
	
	public void cancelar(){
		SolicitudDeDeposito sol=(SolicitudDeDeposito)getSelectedObject();
		if(sol!=null){
			
			if(sol.getPago()!=null){
				MessageUtils.showMessage("Solicitud con abono, no se puede cancelar", "Cancelación de solicitudes");
				return;
			}
			if(MessageUtils.showConfirmationMessage("Cancelar la solicitud:\n "+sol, "Cancelación de solicitudes")){
				
				String comentario=JOptionPane.showInputDialog("Comentario de cancelación", "DATOS INCORRECTOS");
				sol.setCancelacion(new Date());
				sol.setComentarioCancelacion(comentario);
				int index=source.indexOf(sol);
				sol=getSolicitudDeDepositosManager().save(sol);
				source.set(index, sol);
				
			}
		}
	}
	
	public SolicitudDeDeposito refresh(String id){
		String hql="from SolicitudDeDeposito s left join fetch s.cliente c" +
		" left join fetch s.cuentaDestino c" +
		" left join fetch s.bancoOrigen b" +
		" left join fetch s.pago p " +
		" where s.id=?";
		return (SolicitudDeDeposito)Services.getInstance().getHibernateTemplate().find(hql,id).get(0);
	}

	

}
