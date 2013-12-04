package com.luxsoft.sw3.tesoreria.ui.consultas;

import java.util.List;

import javax.swing.Action;

import org.jdesktop.swingx.JXTable;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;
import com.luxsoft.sw3.tesoreria.ui.forms.SolicitudDeDepositoForm;

/**
 * Panel para el mantenimiento de solicitudes de depositos en transito
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SolicitudesEnTransitoPanel extends FilteredBrowserPanel<SolicitudDeDeposito> implements ApplicationContextAware{
	
	private ApplicationContext context;
	
	public SolicitudesEnTransitoPanel() {
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
				,"pagoInfo"
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
				,"Pago"
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
		
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {		
		//grid.getColumnExt("Comentario").setVisible(false);
		grid.getColumnExt("Cancelacion").setVisible(false);
		grid.getColumnExt("Comentario (Cancel)").setVisible(false);
		grid.getColumnExt("Ultima Mod").setVisible(false);
		grid.getColumnExt("Importado").setVisible(false);
		grid.getColumnExt("Cliente").setVisible(false);
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null){			
			actions=new Action[]{
				getLoadAction()
				
				};
		}
		return actions;
	}

	@Override
	protected List<SolicitudDeDeposito> findData() {
		String hql="from SolicitudDeDeposito s left join fetch s.cliente c" +
				" left join fetch s.cuentaDestino c" +
				" left join fetch s.bancoOrigen b" +
				" where s.cancelacion is null " +
				" and s.pago is null" +
				" and s.comentario is not null" +
				" and date(s.fecha) between ? and ?";
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
	
	
	
	
	
	public SolicitudDeDeposito refresh(String id){
		String hql="from SolicitudDeDeposito s left join fetch s.cliente c" +
		" left join fetch s.cuentaDestino c" +
		" left join fetch s.bancoOrigen b" +
		" left join fetch s.pago p " +
		" where s.id=?";
		return (SolicitudDeDeposito)ServiceLocator2.getHibernateTemplate().find(hql,id).get(0);
	}

	
	
	
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context=applicationContext;
		
	}
	
	

}
