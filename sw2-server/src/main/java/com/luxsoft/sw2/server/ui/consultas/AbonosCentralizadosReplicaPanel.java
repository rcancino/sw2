package com.luxsoft.sw2.server.ui.consultas;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jdesktop.swingx.JXTable;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.replica.EntityLog;



/**
 * Panel para el mantenimiento de replicas de Compras
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AbonosCentralizadosReplicaPanel extends DefaultCentralReplicaPanel<Map<String, Object>> {
	

	public AbonosCentralizadosReplicaPanel() {
		super(Abono.class);
	}
	
	public void init(){
		addProperty(
				"sucursal.nombre"
				,"tipo"
				,"clave"
				,"nombre"
				,"fecha"
				,"total"
				,"liberado"
				,"log.creado"
				,"log.modificado"
				,"id"
				);
		addLabels(
				"Sucursal"
				,"Tipo"
				,"Cliente"
				,"Nombre"
				,"Fecha"
				,"Total"
				,"Liberado"
				,"Creado"
				,"Modificado"
				,"Id"
				);	
		installTextComponentMatcherEditor("Cliente", "clave","nombre");
		installTextComponentMatcherEditor("Tipo", "tipo");
		installTextComponentMatcherEditor("Info", "info");
		installTextComponentMatcherEditor("Total", "total");
		
	}
	
	@Override
	protected void adjustMainGrid(JXTable grid) {		
		//grid.getColumnExt("Importado").setVisible(false);
	}
	
	

	@Override
	protected List<Abono> findData() {
		String hql="from Abono s " +
				" where  date(s.fecha) between ? and ?";
		return getHibernateTemplate().find(hql,getDefaultParameters());
	}
	
	private Serializable getId(Object bean){
		try {
			return (Serializable) PropertyUtils.getProperty(bean, "id");
		} catch (Exception e) {
			return null;
		}
	}
	
	
	@Override
	public void replicar() {
		for(Object o:getSelected()){
			final Serializable id=getId(o);
			getHibernateTemplate().execute(new HibernateCallback() {				
				public Object doInHibernate(Session session) throws HibernateException,SQLException {
					Object bean=session.load(beanClazz, id);
					doReplicar(bean);
					return null;
				}
			});
		}
	}
	
	protected void doReplicar(Object bean){
		Abono abono=(Abono)bean;
		EntityLog log=new EntityLog(abono,abono.getId(),abono.getSucursal().getNombre(),EntityLog.Tipo.CAMBIO);
		ServiceLocator2.getReplicaMessageCreator().enviar(log);
	}
	

}
