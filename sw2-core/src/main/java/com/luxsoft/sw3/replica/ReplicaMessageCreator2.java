package com.luxsoft.sw3.replica;

import java.util.HashSet;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.compras.model.ListaDePrecios;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.CargoPorTesoreria;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Clase;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.model.core.Marca;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.model.core.Socio;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;
import com.luxsoft.sw3.embarque.Chofer;
import com.luxsoft.sw3.embarque.ChoferFacturista;
import com.luxsoft.sw3.embarque.Transporte;
import com.luxsoft.sw3.replica.converters.ReplicaMessageConverter;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;
import com.luxsoft.utils.LoggerHelper;

/**
 * Entidad para controla la generacion  de mensajes de replica desde las oficinas centrales
 * 
 * @author Ruben Cancino
 *
 */
public class ReplicaMessageCreator2 {
	
	
	
	Logger logger=LoggerHelper.getLogger();
	
	private Set<Class> clases=new HashSet<Class>();
	
	Topic replicaTopic=new ActiveMQTopic("REPLICA.TOPIC");
	
	
	public ReplicaMessageCreator2() {
		
		init();
		
	}
	
	private void init(){
		clases.add(Cliente.class);
		clases.add(Producto.class);
		clases.add(Linea.class);
		clases.add(Marca.class);
		clases.add(Clase.class);
		clases.add(Existencia.class);
		clases.add(ListaDePrecios.class);
		clases.add(Proveedor.class);
		clases.add(Chofer.class);
		clases.add(ChoferFacturista.class);
		clases.add(Transporte.class);
		clases.add(Socio.class);
		clases.add(ListaDePreciosCliente.class);
	}
	
	public void enviar(EntityLog entityLog){
		Object bean=entityLog.getBean();
		if(Abono.class.isAssignableFrom(bean.getClass()) ){
			enviarAbono(entityLog);			
		}else if(bean instanceof CargoPorTesoreria){
			CargoPorTesoreria c=(CargoPorTesoreria)bean;
			Queue queue=new ActiveMQQueue("REPLICA.QUEUE."+c.getSucursal().getNombre());
			doEnviar(entityLog,queue);
		}else if (bean instanceof SolicitudDeDeposito){
			SolicitudDeDeposito sol=(SolicitudDeDeposito)bean;
			Queue queue=new ActiveMQQueue("REPLICA.QUEUE."+sol.getSucursal().getNombre());
			doEnviar(entityLog,queue);
		}else if(bean instanceof Compra2){
			Compra2 compra=(Compra2)bean;
			enviarCompra(compra);
		}else if(getClases().contains(entityLog.getBean().getClass())){
			doEnviar(entityLog, replicaTopic);
		}
	}
	
	private void enviarAbono(final EntityLog entityLog){
		Abono abono=(Abono)entityLog.getBean();
		Sucursal sucursal=abono.getSucursal();
		if(sucursal.getId().equals(new Long(1))){
			return;
		}
		final EntityLog log=new EntityLog(abono,abono.getId(),sucursal.getNombre(),entityLog.getTipo());
		Queue queue=new ActiveMQQueue("REPLICA.QUEUE."+abono.getSucursal().getNombre());
		doEnviar(log, queue);
	}
	
	private void enviarCompra(Compra2 compra){
		Set<String> sucursales=new HashSet<String>();
		try {
			for(CompraUnitaria cu:compra.getPartidas()){
				sucursales.add(cu.getSucursal().getNombre());
			}
			EntityLog entity=new EntityLog(compra,compra.getId(),compra.getSucursal().getNombre(),EntityLog.Tipo.CAMBIO);
			for(String suc:sucursales){
				//Destination destino=new ActiveMQTopic();
				String destino="REPLICA.QUEUE."+suc;
				jmsTemplate.convertAndSend(destino, entity);
				logger.info("Enviando compra a: "+destino);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	void doEnviar(final EntityLog entityLog,Destination destino){
		try {				
			getJmsTemplate().convertAndSend(destino, entityLog.getBean(),new MessagePostProcessor() {
				public Message postProcessMessage(Message message) throws JMSException {
					message.setStringProperty("SUCURSAL_ORIGEN", entityLog.getSucursalOrigen());
					message.setStringProperty("TIPO", entityLog.getTipo().name());					
					return message;
				}
			});
			logger.info("Entidad enviada al JMS Broker. Desc: "+entityLog);
		} catch (Exception e) {
			logger.error("Error enviando mensaje de replica...",e);
		}	
	}
	
	private JmsTemplate jmsTemplate;
	
	public JmsTemplate getJmsTemplate() {
		if(jmsTemplate==null){
			jmsTemplate=new JmsTemplate(connectionFactory);
			jmsTemplate.setMessageConverter(new ReplicaMessageConverter());
		}
		return jmsTemplate;
	}
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}
	
	public Set<Class> getClases() {
		return clases;
	}

	public void setClases(Set<Class> clases) {
		this.clases = clases;
	}
	
	private ConnectionFactory connectionFactory;
	
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

}
