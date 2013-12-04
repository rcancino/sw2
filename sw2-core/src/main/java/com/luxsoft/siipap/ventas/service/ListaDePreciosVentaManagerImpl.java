package com.luxsoft.siipap.ventas.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.ventas.dao.ListaDePreciosVentaDao;
import com.luxsoft.siipap.ventas.model.ListaDePreciosVenta;
import com.luxsoft.siipap.ventas.model.ListaDePreciosVentaDet;
import com.luxsoft.utils.LoggerHelper;


@Service("listaDePreciosVentaManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class ListaDePreciosVentaManagerImpl implements ListaDePreciosVentaManager{
	
	Logger logger=LoggerHelper.getLogger();
	
	@Autowired
	private ListaDePreciosVentaDao lisDePreciosVentaDao;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;	

	@Transactional(propagation=Propagation.REQUIRED)
	public ListaDePreciosVenta get(Long  listaId) {
		ListaDePreciosVenta lista=this.lisDePreciosVentaDao.get(listaId);
		getHibernateTemplate().initialize(lista.getPrecios());
		return lista;
	}

	public ListaDePreciosVenta salvar(ListaDePreciosVenta lista){
		registrarBitacora(lista);		
		return doSalvar(lista);
	}	

	@Transactional(propagation=Propagation.REQUIRED)
	private ListaDePreciosVenta doSalvar(ListaDePreciosVenta lista){
		lista=this.lisDePreciosVentaDao.save(lista);
		logger.info("Lista de precios salvada: "+lista.getId()+ " Partdas: "+lista.getPrecios().size());
		return lista;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void eliminar(ListaDePreciosVenta lista){
		this.lisDePreciosVentaDao.remove(lista.getId());
	}
	
	public List<ListaDePreciosVentaDet> buscarPartidas(ListaDePreciosVenta lista){
		return this.hibernateTemplate.find("from ListaDePreciosVentaDet d where d.lista.id=?",lista.getId()); 
	}	
	
	
	public ListaDePreciosVenta aplicar(ListaDePreciosVenta lista,User user) {
		return doAplicar(lista, user, obtenerFechaDelSistema());
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	private ListaDePreciosVenta doAplicar(ListaDePreciosVenta lista,User user,Date time) {
		Assert.isNull(lista.getAplicada(),"La lista ya esta aplicada");
		Assert.isTrue(StringUtils.isNotBlank(lista.getAutorizada()),"La lista no ha sido autorizada");
		ListaDePreciosVenta target=get(lista.getId());
		for(ListaDePreciosVentaDet det:target.getPrecios()){
			Producto prod=det.getProducto();
			prod.setPrecioCredito(det.getPrecioCredito().doubleValue());
			prod.setPrecioContado(det.getPrecio().doubleValue());
			
			prod.getUserLog().setModificado(time);
			prod.getUserLog().setUpdateUser(user.getUsername());
			prod.setFechaDeAplicacionListaDePrecios(time);
		}		
		target.setAplicada(time);
		return target;
	}
	
	
	public ListaDePreciosVenta copiar(ListaDePreciosVenta lista){
		ListaDePreciosVenta source=get(lista.getId());
		ListaDePreciosVenta target=new ListaDePreciosVenta();
		for(ListaDePreciosVentaDet det:source.getPrecios()){
			ListaDePreciosVentaDet dtarget=new ListaDePreciosVentaDet();
			BeanUtils.copyProperties(det, dtarget);
			target.agregarPrecio(dtarget);
		}
		target.setComentario("Copia de lista: "+source.getId());
		return salvar(target);
		
	}

	private void registrarBitacora(ListaDePreciosVenta bean){
		Date time=obtenerFechaDelSistema();
		String user=KernellSecurity.instance().getCurrentUserName();	
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();
		
		bean.getLog().setModificado(time);
		bean.getLog().setUpdateUser(user);
		bean.getAddresLog().setUpdatedIp(ip);
		bean.getAddresLog().setUpdatedMac(mac);
		
		
		if(bean.getId()==null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
			bean.getAddresLog().setCreatedIp(ip);
			bean.getAddresLog().setCreatedMac(mac);
		}
	}
	
	public boolean exists(Long id) {
		return this.lisDePreciosVentaDao.exists(id);
	}
	
	public synchronized Date obtenerFechaDelSistema(){
		return (Date)getJdbcTemplate().queryForObject("select now()", Date.class);
	}

	public ListaDePreciosVentaDao getLisDePreciosVentaDao() {
		return lisDePreciosVentaDao;
	}

	public void setLisDePreciosVentaDao(ListaDePreciosVentaDao lisDePreciosVentaDao) {
		this.lisDePreciosVentaDao = lisDePreciosVentaDao;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	
	

}
