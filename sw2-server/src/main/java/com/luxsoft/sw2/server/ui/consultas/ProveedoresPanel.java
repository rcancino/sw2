package com.luxsoft.sw2.server.ui.consultas;





import java.sql.Types;
import java.util.List;

import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;


import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;
import com.luxsoft.sw3.replica.EntityLog;

/**
 * Panel para generar mensajes JMS de replica en el servidor de la sucursal
 * 
 * @author Ruben Cancino
 *
 */
public class ProveedoresPanel extends DefaultCentralReplicaPanel<ListaDePreciosCliente>{
	
	

	public ProveedoresPanel() {
		super(Proveedor.class);	
		setTitle("Proveedores");
	}
	
	
	public void init(){
			addProperty("id","clave","nombre","userLog.creado","userLog.modificado");
			addLabels("Id","Clave","Nombre","Creado","Modificado");
		
		
	//	installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
	//	installTextComponentMatcherEditor("Proveedor", "clave","nombre");		
	//	installTextComponentMatcherEditor("Folio", "folio");
		setDefaultComparator(GlazedLists.beanPropertyComparator(Proveedor.class, "userLog.modificado"));
		
	}
	
	@Override
	protected void adjustMainGrid(JXTable grid) {		
		//grid.getColumnExt("Importado").setVisible(false);
	}
	
	

	@Override
	protected List<Proveedor> findData() {
		String hql="from Proveedor p " +
				" where  date(p.userLog.modificado) between ? and ?";
		return getHibernateTemplate().find(hql,getDefaultParameters());
	}
	
	
	@Override
	public void replicar() {
		for(Object o:getSelected()){
			Proveedor selected=(Proveedor)o;
			final String clave=selected.getClave();
			Proveedor target=ServiceLocator2.getProveedorManager().buscarInicializado(clave);
			EntityLog log=new EntityLog(target,target.getId(),"OFICINAS",EntityLog.Tipo.CAMBIO);
			String destino="REPLICA.TOPIC";
			Topic topic=new ActiveMQTopic(destino);
			ServiceLocator2.getJmsTemplate().convertAndSend(topic, log);
			logger.info("JMS enviado de replica Proveedor : "+ clave+ " Al destino: "+topic);
		}
		
	}
	

	
	
}
