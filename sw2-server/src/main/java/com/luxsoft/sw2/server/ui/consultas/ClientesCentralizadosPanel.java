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


import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.ClienteRow2;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.sw3.replica.EntityLog;

/**
 * Panel para generar mensajes JMS de replica en el servidor de la sucursal
 * 
 * @author Ruben Cancino
 *
 */
public class ClientesCentralizadosPanel extends DefaultCentralReplicaPanel<ClienteRow2>{
	
	

	public ClientesCentralizadosPanel() {
		super(ClienteRow2.class);	
		setTitle("Clientes");
	}
	
	@Override
	protected void init() {
		addProperty("clave","nombre","creado","modificado","importado","cliente_id","credito");
		addLabels("Clave","Nombre","Creado","Modificado","Importado","Id","Credito_id");
	}
	
	@Override
	protected void manejarPeriodo() {
		periodo=Periodo.periodoDeloquevaDelYear();
	}
	@Override
	protected void adjustMainGrid(JXTable grid) {
		grid.getColumnExt("Creado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Importado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Modificado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
	}
	
	@Override
	protected List findData() {
		String sql="select clave,cliente_id,nombre,creado,modificado,credito_id as credito from sx_clientes   " 
	            +" where date(modificado) between ? and ?" 
				+"order by modificado desc"
				;
		return ServiceLocator2.getJdbcTemplate().query(sql
				,new Object[]{
					new SqlParameterValue(Types.DATE, periodo.getFechaInicial())
					,new SqlParameterValue(Types.DATE, periodo.getFechaFinal())
				}
				, new BeanPropertyRowMapper(ClienteRow2.class));
	}

	
	
	@Override
	public void replicar() {		
		if(!getSelected().isEmpty()){
			List list=getSelected();
			if(MessageUtils.showConfirmationMessage("Replicar "+list.size()+ " Clientes", "Replicación de clientes")){
				for(Object o:list){
					ClienteRow2 selected=(ClienteRow2)o;
					final Long id=selected.getCliente_id();
					Cliente target=ServiceLocator2.getClienteManager().get(id);
					EntityLog log=new EntityLog(target,target.getId(),"OFICINAS",EntityLog.Tipo.CAMBIO);
					String destino="REPLICA.TOPIC";
					Topic topic=new ActiveMQTopic(destino);
					ServiceLocator2.getJmsTemplate().convertAndSend(topic, log);
					logger.info("JMS enviado de replica Cliente: "+id+ " Al destino: "+topic);
				}
			}
		}
	}

	
	
}
