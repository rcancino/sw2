package com.luxsoft.siipap.service.core;



import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.service.ClienteServices;
import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.dao.SucursalDao;
import com.luxsoft.siipap.dao.core.ClienteDao;
import com.luxsoft.siipap.dao.core.FolioDao;
import com.luxsoft.siipap.model.Configuracion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.ClienteCredito;
import com.luxsoft.siipap.model.core.ClienteRow;
import com.luxsoft.siipap.model.core.Folio;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.impl.GenericManagerImpl;
import com.luxsoft.siipap.ventas.model.Cobrador;
import com.luxsoft.siipap.ventas.model.Vendedor;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.replica.EntityLog;


public class ClienteManagerImpl extends GenericManagerImpl<Cliente, Long> implements ClienteManager{

	private Logger logger=Logger.getLogger(getClass());
	
	private FolioDao folioDao;
	
	private SucursalDao sucursalDao;
	
	private HibernateTemplate hibernateTemplate;
	
	private JmsTemplate jmsTemplate;
	
	public ClienteManagerImpl(GenericDao<Cliente, Long> genericDao) {
		super(genericDao);		
	}
	
	public ClienteDao getClienteDao(){
		return (ClienteDao)genericDao;
	}
	
	

	@Transactional (propagation=Propagation.REQUIRED)
	public Cliente save(Cliente cliente) {
		Cliente res=null;
		if(StringUtils.isBlank(cliente.getClave())){
			Sucursal suc=getSucursalDao().get(Configuracion.getSucursalLocalId());
			Folio folio=getFolioDao().buscarNextFolio(suc,"CLIENTES");
			String sucPart=StringUtils.substring(suc.getNombre(), 0, 2);
			String numero=StringUtils.leftPad(folio.getFolio().toString(),5,'0');
			cliente.setClave(sucPart+numero);
			getFolioDao().save(folio);
			String rfc=cliente.getRfc();
			cliente.setRfc(StringUtils.deleteWhitespace(rfc));
			registrarBitacora(cliente);
			registrarCobradorVendedor(cliente);
			res=super.save(cliente);
		}else{
			String rfc=cliente.getRfc();
			cliente.setRfc(StringUtils.deleteWhitespace(rfc));
			registrarBitacora(cliente);
			registrarCobradorVendedor(cliente);
			res= super.save(cliente);
		}
		//enviarJms(res);
		return res;
		
	}
	/*
	private void enviarJms(Cliente res){
		try {
			Sucursal suc=getSucursalDao().get(Configuracion.getSucursalLocalId());
			if(suc.getId().equals(1L)){
				EntityLog log=new EntityLog(res,res.getId(),suc.getNombre(),EntityLog.Tipo.CAMBIO);
				Topic topic=new ActiveMQTopic("REPLICA.TOPIC");
				jmsTemplate.convertAndSend(topic, log);				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
	private void registrarCobradorVendedor(Cliente cliente){
		if(cliente.getCobrador()==null){
			Cobrador c=(Cobrador)getClienteServices().getHibernateTemplate().load(Cobrador.class, new Long(1));
			cliente.setCobrador(c);
		}
		if(cliente.getVendedor()==null){
			Vendedor v=(Vendedor)getClienteServices().getHibernateTemplate().load(Vendedor.class, new Long(1));
			cliente.setVendedor(v);
		}
	}

	public List<Cliente> buscarClientesCredito() {
		return getClienteDao().buscarClientesCredito();
	}

	public Cliente buscarPorNombre(String clave) {
		return getClienteDao().buscarPorClave(clave);
	}

	public Cliente buscarPorRfc(String rfc) {
        List<Cliente> clientes=getHibernateTemplate().find(
        		"from Cliente c where c.rfc=?"
        		, rfc);
        if(clientes.isEmpty()){
        	return null;
        }else{
        	return get(clientes.get(0).getId());
        }
        
    }
	
	public List<ClienteRow> buscarClientes() {
		final List<ClienteRow> clientes=new ArrayList<ClienteRow>();
		getClienteDao().buscarClientes(clientes);
		return clientes;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Cliente get(Long id) {		
		return super.get(id);
	}

	@Transactional(propagation=Propagation.SUPPORTS)
	public Cliente buscarPorClave(String clave) {
		return getClienteDao().buscarPorClave(clave);
	}

	public List<Cliente> buscarClientePorClave(String clave) {
		return getClienteDao().buscarClientePorClave(clave);
	}
	
	public List<Cliente> buscarClientePorNombre(String nombre) {
		return getClienteDao().buscarClientePorNombre(nombre);
	}
	
	
	public void agregarCuenta(String cliente, String cuenta) {		
		Cliente c=buscarPorClave(cliente);
		boolean res=c.getCuentas().add(cuenta);
		if(res){
			getClienteDao().save(c);
		}
		
	}

	/**
	 * Exporta la informacion del cliente al sistema SIIPAP -DBF
	 */
	public void exportarCliente(final Cliente c){
		Runnable runner=new Runnable(){
			public void run() {
				try {
					getClienteServices().exportarCliente(c);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		};
		Thread task=Executors.defaultThreadFactory().newThread(runner);
		task.start();
		
	}
	
	//@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	private void registrarBitacora(Cliente cliente){
		Date time=new Date();
		
		String user=KernellSecurity.instance().getCurrentUserName();	
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();
		
		cliente.getLog().setModificado(time);
		cliente.getLog().setUpdateUser(user);
		cliente.getAddresLog().setUpdatedIp(ip);
		cliente.getAddresLog().setUpdatedMac(mac);
		
		
		if(cliente.getId()==null){
			cliente.getLog().setCreado(time);
			cliente.getLog().setCreateUser(user);
			cliente.getAddresLog().setCreatedIp(ip);
			cliente.getAddresLog().setUpdatedMac(mac);
		}
		
		if(cliente.getCredito()!=null){
			ClienteCredito credito=cliente.getCredito();
			credito.getLog().setModificado(time);
			credito.getLog().setUpdateUser(user);
			credito.getAddresLog().setUpdatedIp(ip);
			credito.getAddresLog().setUpdatedMac(mac);
			
			
			if(credito.getId()==null){
				credito.getLog().setCreado(time);
				credito.getLog().setCreateUser(user);
				credito.getAddresLog().setCreatedIp(ip);
				credito.getAddresLog().setUpdatedMac(mac);
			}
		}
		
	}
	/*
	private synchronized Date obtenerFechaDelSistema(){
		return (Date)jdbcTemplate.queryForObject("select now()", Date.class);
	}*/
	
	private ClienteServices clienteServices;
	
	private JdbcTemplate jdbcTemplate;
	

	public ClienteServices getClienteServices() {
		return clienteServices;
	}

	public void setClienteServices(ClienteServices clienteServices) {
		this.clienteServices = clienteServices;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public FolioDao getFolioDao() {
		return folioDao;
	}

	public void setFolioDao(FolioDao folioDao) {
		this.folioDao = folioDao;
	}

	public SucursalDao getSucursalDao() {
		return sucursalDao;
	}

	public void setSucursalDao(SucursalDao sucursalDao) {
		this.sucursalDao = sucursalDao;
	}
	
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	

}
