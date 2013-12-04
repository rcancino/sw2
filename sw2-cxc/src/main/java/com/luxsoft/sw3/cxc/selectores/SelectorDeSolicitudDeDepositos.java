package com.luxsoft.sw3.cxc.selectores;

import java.awt.Dimension;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.cxc.forms.SolicitudDeDepositoForm;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

/**
 * Version especial de Selector de solicitudes de autorizaciones de depositos para
 * el modulo de CxC
 * 
 * @author Ruben Cancino
 *
 */
public class SelectorDeSolicitudDeDepositos extends AbstractSelector<SolicitudDeDeposito>{
	
	private Cliente cliente;	
	private Date fechaSistema;
	private OrigenDeOperacion origen=OrigenDeOperacion.CRE;
	private final DateFormat df=new SimpleDateFormat("yyyy/MM/dd ");

	public SelectorDeSolicitudDeDepositos() {
		super(SolicitudDeDeposito.class,"Solicitudes de autorizacion de depositos");
		this.fechaSistema=ServiceLocator2.obtenerFechaDelSistema();
	}

	protected void addButton(ToolBarBuilder builder){
		builder.add(CommandUtils.createEditAction(this, "modificarSolicitud"));
	}

	@Override
	protected TextFilterator<SolicitudDeDeposito> getBasicTextFilter() {
		return GlazedLists.textFilterator("nombre","documento");
	}

	@Override
	protected void installEditors(EventList<MatcherEditor<SolicitudDeDeposito>> editors) {
		super.installEditors(editors);
		Matcher<SolicitudDeDeposito> m1=new Matcher<SolicitudDeDeposito>(){
			public boolean matches(SolicitudDeDeposito item) {
				if(item.getPago()!=null){
					return DateUtils.isSameDay(fechaSistema, item.getPago().getLiberado());
				}
				return true;
			}
		};
		MatcherEditor<SolicitudDeDeposito> editor=GlazedLists.fixedMatcherEditor(m1);
		editors.add(editor);
	}



	@Override
	protected List<SolicitudDeDeposito> getData() {
		
		long diferenciaEnDias = 30;
		Date fechaActual = Calendar.getInstance().getTime();
		long tiempoActual = fechaActual.getTime();
		long treintaDias = diferenciaEnDias * 24 * 60 * 60 * 1000;
		Date fechaCorte = (new Date(tiempoActual - treintaDias));
		System.out.println(df.format(fechaCorte));
		System.out.println(df.format(fechaActual));
	
		
		if(getCliente()!=null){
			
			String hql="from SolicitudDeDeposito p  " +
					" left join fetch p.cliente cc" +
					" left join fetch p.cuentaDestino cd" +
					" left join fetch p.bancoOrigen b " +
					" where (p.cliente.clave=?" +
					"  and p.origen=\'@ORIGEN\' "+
					" and p.cancelacion is null" +
					" and p.pago is null" +
					" and p.fecha >= \'@FECHA_INI\'" +
					" and p.fecha >=\'@FECHA_INI 00:00:00\'" +
					" and p.fecha<=\'@FECHA_FIN 23:59:00\')" 
					+ " or (p.origen=\'@ORIGEN\' and p.pago.liberado=\'@FECHA_FIN\')"
					+")"		
					;
		
			hql=hql.replaceAll("@ORIGEN", origen.name());
			hql=hql.replaceAll("@FECHA_INI", df.format(fechaCorte).toString());
			hql=hql.replaceAll("@FECHA_FIN", df.format(fechaActual).toString());
			
			List<SolicitudDeDeposito> pendientes=ServiceLocator2
				.getHibernateTemplate().find(hql,getCliente().getClave());
			return pendientes;
		}else{
			String hql="from SolicitudDeDeposito p  " +
					" left join fetch p.cliente cc" +
					" left join fetch p.cuentaDestino cd" +
					" left join fetch p.bancoOrigen b " +
					" where p.origen=\'@ORIGEN\'"+
					" and p.cancelacion is null" +
					" and ( p.pago is null and p.fecha >=\'@FECHA_INI 00:00:00\' and p.fecha<=\'@FECHA_FIN 23:59:00\'  )"
					;
			
			
			hql=hql.replaceAll("@ORIGEN", origen.name());
			hql=hql.replaceAll("@FECHA_INI", df.format(fechaCorte).toString());
			hql=hql.replaceAll("@FECHA_FIN", df.format(fechaActual).toString());
			List<SolicitudDeDeposito> pendientes=ServiceLocator2
			.getHibernateTemplate().find(hql);
			
			hql="from SolicitudDeDeposito p  " +
					" left join fetch p.cliente cc" +
					" left join fetch p.cuentaDestino cd" +
					" left join fetch p.bancoOrigen b " +
					" where p.origen=\'@ORIGEN\'"+
					" and p.cancelacion is null" +
					" and p.pago.liberado=\'@FECHA_FIN\'" 
					;
			
			
			hql=hql.replaceAll("@ORIGEN", origen.name());
			hql=hql.replaceAll("@FECHA_INI", df.format(fechaCorte).toString());
			hql=hql.replaceAll("@FECHA_FIN", df.format(fechaActual).toString());
			List<SolicitudDeDeposito> liberados=ServiceLocator2
					.getHibernateTemplate().find(hql);
			pendientes.addAll(liberados);
		return pendientes;
		}
		
	}

	@Override
	protected TableFormat<SolicitudDeDeposito> getTableFormat() {
		String props[]={"origen","nombre","tipo","documento","fecha","fechaDeposito","total","solicita","comentario","salvoBuenCobro","pago.info","pago.liberado"};
		String labels[]={"Origen","Cliente","Tipo","Folio","Fecha","Fecha (Dep)","Total","Solicita","Comentario","SBC","Pago","Liberado"};
		return GlazedLists.tableFormat(SolicitudDeDeposito.class,props,labels);
	}
	
	@Override
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(850,400));
	}
	
	@Override
	protected void onWindowOpened() {
		load();
	}
	
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	

	public OrigenDeOperacion getOrigen() {
		return origen;
	}

	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
	}

	public void modificarSolicitud(){
		if(getSelected()!=null){
			SolicitudDeDeposito target=getSelected();
			target=(SolicitudDeDeposito)ServiceLocator2.getSolicitudDeDepositosManager().get(target.getId());
			if( (target.getPago()!=null) || StringUtils.isBlank(target.getComentario())){
				SolicitudDeDepositoForm.consultar(target);
				return;
			}			
			target=SolicitudDeDepositoForm.modificar(target);
			if(target!=null){
				int index=source.indexOf(target);
				if(index!=-1){					
					target.setComentario("");
					target=ServiceLocator2.getSolicitudDeDepositosManager().save(target);
					source.set(index, target);
				}
			}
		}
	}
	
	public static SolicitudDeDeposito buscar(OrigenDeOperacion origen){
		final SelectorDeSolicitudDeDepositos selector=new SelectorDeSolicitudDeDepositos();
		selector.setOrigen(origen);
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelected();
		}
		return null;
	}

	public static SolicitudDeDeposito buscar(){
		final SelectorDeSolicitudDeDepositos selector=new SelectorDeSolicitudDeDepositos();
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelected();
		}
		return null;
	}
	
	public static SolicitudDeDeposito buscar(final Cliente cliente){
		final SelectorDeSolicitudDeDepositos selector=new SelectorDeSolicitudDeDepositos();
		selector.setCliente(cliente);
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelected();
		}
		return null;
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
				buscar();
				System.exit(0);
			}

		});
	}

}
