package com.luxsoft.sw2.server.ui.consultas;

import java.util.List;

import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.replica.EntityLog;



/**
 * Panel para el mantenimiento de replicas de Compras
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ComprasReplicaPanel extends DefaultCentralReplicaPanel<Compra2> {
	

	public ComprasReplicaPanel() {
		super(Compra2.class);
	}
	
	public void init(){
		addProperty("folio","sucursal.nombre","clave","nombre","fecha","moneda","tc","total","descuentoEspecial","depuracion","consolidada","comentario");
		addLabels("Folio","Sucursal","Prov","Nombre","Fecha","Mon","TC","Total","Dscto","Depuracion","Con","Comentario");
		
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Proveedor", "clave","nombre");		
		installTextComponentMatcherEditor("Folio", "folio");
		setDefaultComparator(GlazedLists.beanPropertyComparator(Compra2.class, "log.modificado"));
		
	}
	
	@Override
	protected void adjustMainGrid(JXTable grid) {		
		//grid.getColumnExt("Importado").setVisible(false);
	}
	
	

	@Override
	protected List<Compra2> findData() {
		String hql="from Compra2 s " +
				" where  date(s.fecha) between ? and ?";
		return getHibernateTemplate().find(hql,getDefaultParameters());
	}
	
	
	@Override
	public void replicar() {
		for(Object o:getSelected()){
			Compra2 selected=(Compra2)o;
			final String id=selected.getId();
			Compra2 target=ServiceLocator2.getComprasManager().buscarInicializada(id);
			EntityLog log=new EntityLog(target,target.getId(),target.getSucursal().getNombre(),EntityLog.Tipo.CAMBIO);
			if(target.getProveedor().getId().equals(60L) && target.getSucursal().getId().equals(1l)){
				String destino="REPLICA.TOPIC";
				Topic topic=new ActiveMQTopic(destino);
				ServiceLocator2.getJmsTemplate().convertAndSend(topic, log);
				logger.info("JMS enviado de replica Compra Importaciones: "+id+ " Al destino: "+topic);
			}else{
				
				String destino="REPLICA.QUEUE."+target.getSucursal().getNombre();
				Queue queue=new ActiveMQQueue(destino);
				ServiceLocator2.getJmsTemplate().convertAndSend(queue, log);
				logger.info("JMS enviado de replica Compra: "+id+ " Al destino: "+queue);
			}
		}
		
	}
	

}
