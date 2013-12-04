package com.luxsoft.sw2.server.ui.consultas;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.jms.Queue;

import org.apache.activemq.command.ActiveMQQueue;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jdesktop.swingx.JXTable;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.sw3.replica.ReplicaMessageCreator;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

/**
 * Panel para el mantenimiento de replicas de solicituedes de deposito
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SolicitudesParaPagoConDepositoPanel extends DefaultCentralReplicaPanel<SolicitudDeDeposito> {

	ReplicaMessageCreator messageCreator;

	public SolicitudesParaPagoConDepositoPanel() {
		super(SolicitudDeDeposito.class);
		messageCreator=new ReplicaMessageCreator();
	}
	
	public void init(){
		addProperty(
				"sucursal.nombre","origen","pago.id","clave","nombre","documento","fecha","fechaDeposito","comentario","referenciaBancaria","total","cuentaDestino.descripcion","bancoOrigen.clave","solicita","salvoBuenCobro","comentario","cancelacion","comentarioCancelacion","log.modificado");
		addLabels("Sucursal"				,"Tipo"			,"Abono"	,"Cliente"				,"Nombre"				,"Folio"				,"Fecha"				,"Fecha (Dep)"				,"Comentario"				,"Referencia"				,"Total"				,"Cuenta Dest"				,"Banco"				,"Solicita"				,"SBC"				,"Comentario"				,"Cancelacion"				,"Comentario (Cancel)"				,"Ultima Mod");
		setDefaultComparator(GlazedLists.beanPropertyComparator(SolicitudDeDeposito.class, "log.modificado"));
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Documento", "documento");
	}
	@Override
	protected void manejarPeriodo() {
		periodo=new Periodo(new Date());
	}
	
	
	@Override
	protected void adjustMainGrid(JXTable grid) {		
		//grid.getColumnExt("Importado").setVisible(false);
	}
	
	

	@Override
	protected List<SolicitudDeDeposito> findData() {
		String hql="from SolicitudDeDeposito s left join fetch s.cliente c" +
				" left join fetch s.cuentaDestino c" +
				" left join fetch s.bancoOrigen b" +
				" left join fetch s.pago p " +
				" where date(s.fecha) between ? and ?";
		return getHibernateTemplate().find(hql,getDefaultParameters());
	}
	
	
	@Override
	public void replicar() {
		SolicitudDeDeposito selected=(SolicitudDeDeposito)getSelectedObject();
		if(selected!=null){
			final String id=selected.getId();
			
			getHibernateTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException,SQLException {					
					SolicitudDeDeposito sol=(SolicitudDeDeposito)session.load(beanClazz, id);
					EntityLog log=new EntityLog(sol,sol.getId(),sol.getSucursal().getNombre(),EntityLog.Tipo.CAMBIO);
					ServiceLocator2.getReplicaMessageCreator().enviar(log);
					return null;
				}
			});
			/*
			SolicitudDeDeposito sol=ServiceLocator2.getSolicitudDeDepositosManager().get(id);
			
			String destino="REPLICA.QUEUE."+sol.getSucursal().getNombre();
			Queue queue=new ActiveMQQueue(destino);
			ServiceLocator2.getJmsTemplate().convertAndSend(queue, log);
			logger.info("JMS enviado de replica del deposito: "+id+ " Al destino: "+queue);
			if(sol.getPago()!=null){
				logger.info("        Abono: "+sol.getPagoInfo()+ "    Fecha: "+sol.getPago().getFecha()+ " Liberado: "+sol.getPago().getLiberado());
			}	*/		
		}
		
	}
	
	
	

}
