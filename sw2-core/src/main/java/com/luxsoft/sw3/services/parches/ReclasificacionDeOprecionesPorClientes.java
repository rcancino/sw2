package com.luxsoft.sw3.services.parches;

import java.sql.SQLException;
import java.text.MessageFormat;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;
import com.luxsoft.sw3.ventas.Pedido;


public class ReclasificacionDeOprecionesPorClientes {
	
	private final HibernateTemplate template;
	
	public ReclasificacionDeOprecionesPorClientes(final HibernateTemplate template){
		this.template=template;
	}
	
	public void execute(final Long clienteOrigen,final Long clienteDestino){		
		template.execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				
				Cliente origen=(Cliente)session.load(Cliente.class, clienteOrigen);
				Cliente destino=(Cliente)session.load(Cliente.class, clienteDestino);
				String pattern="Reclasificando: {0} Id:{1} en {2} Id:{3}";
				System.out.println( MessageFormat.format(pattern
						, origen.getLabel() ,origen.getId()
						, destino.getLabel() ,destino.getId()
						));
				
				//Reclasificando pedidos
				ScrollableResults rs=session.createQuery("from Pedido p where p.cliente.id=?")
				.setLong(0, clienteOrigen)
				.scroll();
				while(rs.next()){
					Pedido pedido=(Pedido)rs.get()[0];
					pedido.setCliente(destino);
					System.out.println("Reasignando pedido: "+pedido.getFolio());
				}
				session.flush();
				session.clear();
				//Reclasificar Cargos
				rs=session.createQuery("from Cargo c where c.cliente.id=?")
				.setLong(0, clienteOrigen)
				.scroll();
				while(rs.next()){
					Cargo cargo=(Cargo)rs.get()[0];
					cargo.setCliente(destino);
					System.out.println("Reasignando cargo: "+cargo.getDocumento());
				}
				session.flush();
				session.clear();

				//Reclasificar Abonos
				rs=session.createQuery("from Abono a where a.cliente.id=?")
				.setLong(0, clienteOrigen)
				.scroll();
				while(rs.next()){
					Abono abono=(Abono)rs.get()[0];
					abono.setCliente(destino);
					for(Aplicacion a:abono.getAplicaciones()){
						a.getDetalle().setClave(destino.getClave());
						a.getDetalle().setNombre(destino.getNombre());
					}
					System.out.println("Reasignando abono: "+abono.getFolio());
				}
				session.flush();
				session.clear();
				
				//Reclasificar Entregas por chofer				
				rs=session.createQuery("from Entrega c where c.cliente.id=?")
				.setLong(0, clienteOrigen)
				.scroll();
				while(rs.next()){
					Entrega entrega=(Entrega)rs.get()[0];
					entrega.setCliente(destino);
					System.out.println("Reasignando entrega: "+entrega.getDocumento());
				}
				
				//Reclasificar Solicitudes de deposito
				rs=session.createQuery("from SolicitudDeDeposito a where a.cliente.id=?")
				.setLong(0, clienteOrigen)
				.scroll();
				while(rs.next()){
					SolicitudDeDeposito sol=(SolicitudDeDeposito)rs.get()[0];
					sol.setCliente(destino);
					System.out.println("Reasignando solicitud de deposito: "+sol.getDocumento());
				}
				session.flush();
				session.clear();
				
				
				// Limpiando el cliente origen
				origen=(Cliente)session.load(Cliente.class, clienteOrigen);
				System.out.println("Limpiando colecciones del cliente: "+origen.getLabel());
				origen.getTelefonos().clear();
				origen.getComentarios().clear();
				origen.getContactos().clear();
				origen.getCuentas().clear();
				origen.getDirecciones().clear();
				
				if(origen.getCredito()!=null){
					origen.setCredito(null);
					session.delete(origen.getCredito());
				}
				
				session.delete(origen);
				
				session.flush();
				session.clear();
				return null;
			}
			
		});
	}
	
	
	public static void main(String[] args) {
		ReclasificacionDeOprecionesPorClientes task=new ReclasificacionDeOprecionesPorClientes(ServiceLocator2.getHibernateTemplate());
		task.execute(700328L,14471L);
		
		/*
		task.execute(500906L,601100L);
			GOCE8011013H1
			GOCE8011013H1
			
		task.execute(12462L,13053L);	
			SEL820908TG7
			SEL820908TG7 
			
		task.execute(700005L,14868L);	
			GAVJ570916RM6
			GAVJ570916RM6
			
		task.execute(300778L,300653L);
			HOLM6501304B1
			HOLM6501304B1
			
		task.execute(1544L, 300496L);	
			RHI971218M5A
			RHI971218M5A
			
		task.execute(12900L,14854L);	
			SABV650120LC9
			SABV650120LC9
			
		task.execute(900005L,600064L);	
			SIFA7805167SA
			SIFA7805167SA
			
		task.execute(600017L,500008L);	
			TOVY6208128Y0
			TOVY6208128Y0
		*/
		
		
		
	}

}
