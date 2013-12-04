package com.luxsoft.sw2.server.ui.consultas;





import java.sql.SQLException;
import java.util.List;

import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQTopic;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Producto;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.sw3.replica.ReplicaMessageCreator;
import com.luxsoft.sw3.replica.converters.ReplicaMessageConverter;

/**
 * Panel para generar mensajes JMS de replica en el servidor de la sucursal
 * 
 * @author Ruben Cancino
 *
 */
public class ProductosCentralizadosReplicaPanel extends DefaultCentralReplicaPanel<Producto>{
	
	

	public ProductosCentralizadosReplicaPanel() {
		super(Producto.class);	
		setTitle("Productos");
	}
	
	
	
	@Override
	protected void init() {
		
		addProperty(
				"id"
				,"clave"
				,"descripcion"				
				,"userLog.modificado"
				,"activo"
				,"inventariable"
				,"servicio"
				,"deLinea"
				,"nacional"
				,"kilos"
				,"gramos"
				,"precioContado"
				,"precioCredito"
				,"linea.nombre"
				,"marca.nombre"
				,"userLog.creado"
				
				);
		addLabels(
				"id"
				,"Clave"
				,"Descripcion"
				,"Modificado"
				,"Activo"
				,"Inventariable"
				,"Servicio"
				,"DeLinea"
				,"Nal"
				,"Kilos"
				,"Gramos"
				,"PrecioContado"
				,"PrecioCredito"
				,"Linea"
				,"Marca"
				,"Creado"
				
				);
		installTextComponentMatcherEditor("Producto", "clave","descripcion");
		installTextComponentMatcherEditor("Linea", "linea.nombre");
	}
	
	@Override
	protected void manejarPeriodo() {
		periodo=Periodo.periodoDeloquevaDelYear();
	}
	@Override
	protected void adjustMainGrid(JXTable grid) {
		grid.getColumnExt("Creado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		//grid.getColumnExt("Importado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Modificado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
	}
	
	@Override
	protected List findData() {
		String sql="from Producto p where date(p.userLog.modificado) between ? and ? order by p.userLog.modificado desc";
		return ServiceLocator2.getHibernateTemplate().find(sql,getDefaultParameters());
	}

	protected Object[] getDefaultParameters(){
		return new Object[]{				
				periodo.getFechaInicial()
				,periodo.getFechaFinal()
		};
	}
	
	
	
	@Override
	public void replicar() {		
		if(!getSelected().isEmpty()){
			List list=getSelected();
			if(MessageUtils.showConfirmationMessage("Replicar "+list.size()+ " productos", "Replicación de productos")){
				for(Object o:list){
					Producto selected=(Producto)o;
					Producto target=ServiceLocator2.getProductoManager().buscarPorClave(selected.getClave());
					EntityLog log=new EntityLog(target,target.getId(),"OFICINAS",EntityLog.Tipo.CAMBIO);
					ServiceLocator2.getReplicaMessageCreator().enviar(log);
					//logger.info("Producto replicado: "+target);
					
				}
			}
		}
		
	}

	
	
}
