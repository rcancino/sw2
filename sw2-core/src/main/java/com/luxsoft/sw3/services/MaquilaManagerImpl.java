package com.luxsoft.sw3.services;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.sw3.maquila.dao.AnalisisDeMaterialDao;
import com.luxsoft.sw3.maquila.dao.EntradaDeMaterialDao;
import com.luxsoft.sw3.maquila.dao.OrdenDeCorteDao;
import com.luxsoft.sw3.maquila.dao.RecepcionDeCorteDao;
import com.luxsoft.sw3.maquila.dao.SalidaDeBobinasDao;
import com.luxsoft.sw3.maquila.model.AnalisisDeFlete;
import com.luxsoft.sw3.maquila.model.AnalisisDeHojeo;
import com.luxsoft.sw3.maquila.model.AnalisisDeMaterial;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterial;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;
import com.luxsoft.sw3.maquila.model.OrdenDeCorte;
import com.luxsoft.sw3.maquila.model.OrdenDeCorteDet;
import com.luxsoft.sw3.maquila.model.RecepcionDeCorte;
import com.luxsoft.sw3.maquila.model.RecepcionDeCorteDet;
import com.luxsoft.sw3.maquila.model.SalidaDeBobinas;
import com.luxsoft.sw3.maquila.model.SalidaDeHojasDet;

@Service("maquilaManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class MaquilaManagerImpl  implements MaquilaManager{
	
	@Autowired
	private HibernateTemplate hibernateTemplate;

	@Autowired
	private EntradaDeMaterialDao entradaDeMaterialDao;
	
	@Autowired
	private OrdenDeCorteDao ordenDeCorteDao;
	
	@Autowired
	private RecepcionDeCorteDao recepcionDeCorteDao;
	
	@Autowired
	private SalidaDeBobinasDao salidaDeBobinasDao;
	
	@Autowired
	private AnalisisDeMaterialDao analisisDeMaterialDao;
	
	
	@SuppressWarnings("unused")
	private Logger logger=Logger.getLogger(getClass());
	
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public EntradaDeMaterial getEntrada(Long id) {
		List<EntradaDeMaterial> res=getHibernateTemplate().find("from EntradaDeMaterial e left join fetch e.partidas p where e.id=?",id);
		return res.isEmpty()?null:res.get(0);
	}

	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public EntradaDeMaterial salvarEntrada(EntradaDeMaterial entrada) {
		for(EntradaDeMaterialDet det:entrada.getPartidas()){
			det.setEntradaDeMaquilador(entrada.getEntradaDeMaquilador());
			det.setFecha(entrada.getFecha());
		}
		EntradaDeMaterial res=getEntradaDeMaterialDao().save(entrada);
		for(EntradaDeMaterialDet det:res.getPartidas()){
			actualiarCostos(det);
		}
		return res;
	}

	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public void eliminarEntrada(EntradaDeMaterial entrada) {
		getEntradaDeMaterialDao().remove(entrada.getId());
	}

	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public OrdenDeCorte getOrden(Long id) {
		List<OrdenDeCorte> res=getHibernateTemplate().find("from OrdenDeCorte o left join fetch o.cortes c where o.id=?",id);
		return res.isEmpty()?null:res.get(0);
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public void eliminarOrden(OrdenDeCorte orden) {
		getOrdenDeCorteDao().remove(orden.getId());
	}
	

	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public OrdenDeCorte salvarOrden(OrdenDeCorte orden) {
		for(OrdenDeCorteDet det: orden.getCortes()){
			det.setEntradaDeMaquilador(det.getOrigen().getEntradaDeMaquilador());
			det.setFecha(orden.getFecha());
		}
		return getOrdenDeCorteDao().save(orden);
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public RecepcionDeCorte getRecepcionDeCorte(Long id) {
		List<RecepcionDeCorte> res=getHibernateTemplate()
			.find("from RecepcionDeCorte o left join fetch o.partidas c where o.id=?",id);
		return res.isEmpty()?null:res.get(0);
	}

	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public void eliminarRecepcionDeCorte(RecepcionDeCorte recepcion) {
		getRecepcionDeCorteDao().remove(recepcion.getId());
	}	

	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public RecepcionDeCorte salvarRecepcionDeCorte(RecepcionDeCorte recepcion) {
		for(RecepcionDeCorteDet det:recepcion.getPartidas()){
			if(det.getProducto().getMetros2PorMillar()!=det.getMetros2PorMillar())
				det.getProducto().setMetros2PorMillar(det.getMetros2PorMillar());
			det.getCorte().setKilos(det.getKilos());
			det.getCorte().setMetros2(det.getMetros2());
			det.setFecha(recepcion.getFecha());
		}
		return getRecepcionDeCorteDao().save(recepcion);
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public SalidaDeBobinas getSalidaDeBobina(Long id) {
		String hql="from SalidaDeBobinas s" +
				" where s.id=?";
		List<SalidaDeBobinas> res=getHibernateTemplate().find(hql,id);
		return res.isEmpty()?null:res.get(0);
	}

	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public SalidaDeBobinas salvarSalidaDeBobina(SalidaDeBobinas bean) {
		Date time=new Date();
		
		String user=KernellSecurity.instance().getCurrentUserName();
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();
		
		bean.getLog().setUpdateUser(user);
		bean.getLog().setModificado(time);
		bean.getAddresLog().setUpdatedIp(ip);
		bean.getAddresLog().setUpdatedMac(mac);
		if(bean.getId()!=null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
			bean.getAddresLog().setCreatedIp(ip);
			bean.getAddresLog().setCreatedMac(mac);
		}
		return getSalidaDeBobinasDao().save(bean);
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public void eliminarSalidaDeBobina(SalidaDeBobinas salida) {
		getSalidaDeBobinasDao().remove(salida.getId());
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public AnalisisDeMaterial getAnalisis(final Long id){
		String hql="from AnalisisDeMaterial a left join fetch a.entradas e where a.id=?";
		List<AnalisisDeMaterial> res=getHibernateTemplate().find(hql,id);
		return res.isEmpty()?null:res.get(0);
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public AnalisisDeMaterial salvarAnalisis(final AnalisisDeMaterial bean){
		Date time=new Date();		
		String user=KernellSecurity.instance().getCurrentUserName();
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();		
		bean.getLog().setUpdateUser(user);
		bean.getLog().setModificado(time);
		bean.getAddresLog().setUpdatedIp(ip);
		bean.getAddresLog().setUpdatedMac(mac);
		if(bean.getId()!=null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
			bean.getAddresLog().setCreatedIp(ip);
			bean.getAddresLog().setCreatedMac(mac);
		}		
		AnalisisDeMaterial res=getAnalisisDeMaterialDao().save(bean);
		for(EntradaDeMaterialDet ent:res.getEntradas()){
			actualiarCostos(ent);
		}
		return res;
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public void eliminarAnalisis(final AnalisisDeMaterial a){
		getAnalisisDeMaterialDao().remove(a.getId());
	}

	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public void actualiarCostos(final EntradaDeMaterialDet entrada){	
		
		//Cortado
		String hql="from OrdenDeCorteDet o where o.origen.id=?";
		List<OrdenDeCorteDet> cortes=getHibernateTemplate().find(hql,entrada.getId());
		for(OrdenDeCorteDet corte:cortes){
			double m2=corte.getMetros2().doubleValue();
			double precio=entrada.getPrecioPorM2();
			double costo=precio*m2;
			corte.setCosto(costo);
			getHibernateTemplate().update(corte);
			System.out.println(corte.getCosto());
		}
		
		//Recepcion de corte
		String hql_1="from RecepcionDeCorteDet r where r.origen.id=?";
		List<RecepcionDeCorteDet> recepciones=getHibernateTemplate()
			.find(hql_1,entrada.getId());
		for(RecepcionDeCorteDet r:recepciones){
			double m2=r.getMetros2().doubleValue();
			double precio=entrada.getPrecioPorM2();
			double costo=precio*m2;
			r.setCosto(costo);
		}
		
		
		//Salida de hojeo
		
		for(RecepcionDeCorteDet corte:recepciones){
			String hql1="from SalidaDeHojasDet s where s.origen.id=?";
			
			List<SalidaDeHojasDet> hojeado=getHibernateTemplate()
				.find(hql1,corte.getId());
			
			/*for(SalidaDeHojasDet sal:hojeado){
				double costo=corte.getCostoPorMillar();
				sal.setCosto(costo*sal.getCantidadMillares());
				sal.getDestino().setCostoMateria(BigDecimal.valueOf(costo));
				EntradaDeMaquila maq=sal.getDestino();
				BigDecimal costoTot=maq.getCostoMateria().add(maq.getCostoFlete()).add(maq.getCostoCorte());
				
				maq.setCosto(costoTot);
			}*/
			
			
			for(SalidaDeHojasDet sal:hojeado){			
				
				double costo=corte.getCostoPorMillar();
				sal.setCosto(costo*sal.getCantidadMillares());
				
				//Transferimos el costo al MAQ
				EntradaDeMaquila maq=sal.getDestino();
				//Buscar otras salidas participantes
				List<SalidaDeHojasDet> otras=getHibernateTemplate().find("from SalidaDeHojasDet s where s.destino.id=?",maq.getId());
				double cantidadAnterior=0;
				double costoAnterior=0;
				for(SalidaDeHojasDet otra:otras){
					cantidadAnterior+=otra.getCantidad();
					costoAnterior+=otra.getCosto();
				}
				
				double cantidadTotal=cantidadAnterior;
				double costoTotal=costoAnterior;
				
				if(sal.getId()==null){
					cantidadTotal=cantidadAnterior+sal.getCantidad();
					costoTotal=costoAnterior+sal.getCosto();
				}				
				
				double res=0;
				if(cantidadTotal>0)
					res=(costoTotal/cantidadTotal)*1000;
				
				maq.setCostoMateria(BigDecimal.valueOf(res));
				
				BigDecimal costoTot=maq.getCostoMateria().add(maq.getCostoFlete()).add(maq.getCostoCorte());
				maq.setCosto(costoTot);
			}
		}
		
		// Actualizar las salidas directas
		String hql2="from SalidaDeBobinas s where s.origen.id=?";
		List<SalidaDeBobinas> salidas=getHibernateTemplate().find(hql2,entrada.getId());
		for(SalidaDeBobinas sal:salidas){
			double precio=entrada.getPrecioPorKilo();
			sal.setCosto(precio*sal.getCantidad());
			sal.getDestino().setCostoMateria(BigDecimal.valueOf(entrada.getPrecioPorKilo()));
			
			EntradaDeMaquila maq=sal.getDestino();
			BigDecimal costoTot=maq.getCostoMateria()
				.add(maq.getCostoFlete())
				.add(maq.getCostoCorte());
			
			maq.setCosto(costoTot);
			
		}
		
	}
	
	// Analisis de gastos
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public AnalisisDeFlete getAnalisisDeFlete(final Long id){
		String hql="from AnalisisDeFlete a " +
				" left join fetch a.entradas e " +
				" left join fetch a.coms c " +
				" where a.id=?";
		List<AnalisisDeFlete> res=getHibernateTemplate().find(hql,id);
		if(res.isEmpty())
			return null;
		AnalisisDeFlete a=res.get(0);
		getHibernateTemplate().initialize(a.getTransformaciones());
		getHibernateTemplate().initialize(a.getTraslados());
		return a;
		
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public AnalisisDeFlete salvarAnalisisDeFlete(final AnalisisDeFlete bean){
		Date time=new Date();		
		String user=KernellSecurity.instance().getCurrentUserName();
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();		
		bean.getLog().setUpdateUser(user);
		bean.getLog().setModificado(time);
		bean.getAddresLog().setUpdatedIp(ip);
		bean.getAddresLog().setUpdatedMac(mac);
		if(bean.getId()!=null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
			bean.getAddresLog().setCreatedIp(ip);
			bean.getAddresLog().setCreatedMac(mac);
		}
		bean.actualizarCostos();
		AnalisisDeFlete res=(AnalisisDeFlete)getHibernateTemplate().merge(bean);
		return res;
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public void eliminarAnalisisDeFlete(final AnalisisDeFlete a){
		Assert.isNull(a.getCxpFactura(),"No se puede eliminar una alisis con cuenta por pagar registrada");
		getHibernateTemplate().update(a);
		for(EntradaDeMaquila e:a.getEntradas()){
			e.setCostoFlete(BigDecimal.ZERO);
			e.setCosto(e.getCostoMateria().add(e.getCostoCorte()));
			e.setAnalisisFlete(null);
			getHibernateTemplate().update(e);
		}
		getHibernateTemplate().delete(a);
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public AnalisisDeFlete generarCuentaPorPagar(final AnalisisDeFlete a){
		String user=KernellSecurity.instance().getCurrentUserName();
		CXPFactura cxp=new CXPFactura();
		cxp.setComentario(a.getComentario());
		cxp.setComentarioAnalisis("ANALISIS_FLETE");
		cxp.setCreateUser(user);
		cxp.setDocumento(a.getFactura());
		cxp.setFecha(a.getFechaFactura());
		
		//cxp.setFlete(a.get);
		cxp.setImporte(a.getImporte());
		cxp.setImpuesto(a.getImpuesto());
		cxp.setImpuestoAnalizado(a.getImpuesto());
		//cxp.setImpuestoflete(impuestoflete);
		cxp.setProveedor(a.getProveedor());
		cxp.setRetencionflete(a.getRetencion());
		cxp.setTotal(a.getTotal());
		cxp.setTotalAnalizado(a.getTotal());
		cxp.setUpdateUser(user);
		cxp.actualizarVencimiento();
		a.setCxpFactura(cxp);
		return salvarAnalisisDeFlete(a);
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public AnalisisDeHojeo getAnalisisDeHojeo(final Long id){
		String hql="from AnalisisDeHojeo a left join fetch a.entradas e where a.id=?";
		List<AnalisisDeHojeo> res=getHibernateTemplate().find(hql,id);
		if(res.isEmpty())
			return null;
		AnalisisDeHojeo a=res.get(0);
		getHibernateTemplate().initialize(a.getTransformaciones());
		getHibernateTemplate().initialize(a.getEntradasCompras());
		return a;
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public AnalisisDeHojeo salvarAnalisisDeHojeo(final AnalisisDeHojeo bean){
		Date time=new Date();		
		String user=KernellSecurity.instance().getCurrentUserName();
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();	
		
		bean.getLog().setUpdateUser(user);
		bean.getLog().setModificado(time);
		bean.getAddresLog().setUpdatedIp(ip);
		bean.getAddresLog().setUpdatedMac(mac);
		if(bean.getId()!=null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
			bean.getAddresLog().setCreatedIp(ip);
			bean.getAddresLog().setCreatedMac(mac);
		}
		bean.actualizarCostos(); 
		AnalisisDeHojeo res=(AnalisisDeHojeo)getHibernateTemplate().merge(bean);
		return res;
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public void eliminarAnalisisDeHojeo(final AnalisisDeHojeo a){
		getHibernateTemplate().update(a);
		for(EntradaDeMaquila e:a.getEntradas()){
			e.setCostoCorte(BigDecimal.ZERO);
			e.setCosto(e.getCostoMateria().add(e.getCostoFlete()));
			e.setAnalisisFlete(null);
			getHibernateTemplate().update(e);
		}
		getHibernateTemplate().delete(a);
	}
	
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public AnalisisDeHojeo generarCuentaPorPagar(AnalisisDeHojeo a) {
		String user=KernellSecurity.instance().getCurrentUserName();
		CXPFactura cxp=new CXPFactura();
		cxp.setComentario(a.getComentario());
		cxp.setComentarioAnalisis("ANALISIS_HOJEO");
		cxp.setCreateUser(user);
		cxp.setDocumento(a.getFactura());
		cxp.setFecha(a.getFechaFactura());
		cxp.setImporte(a.getImporte());
		cxp.setImpuesto(a.getImpuesto());
		cxp.setImpuestoAnalizado(a.getImpuesto());
		cxp.setProveedor(a.getProveedor());
		cxp.setTotal(a.getTotal());
		cxp.setTotalAnalizado(a.getTotal());
		cxp.setUpdateUser(user);
		cxp.actualizarVencimiento();
		a.setCxpFactura(cxp);
		return salvarAnalisisDeHojeo(a);
	
	}
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public AnalisisDeMaterial generarCuentaPorPagar(AnalisisDeMaterial a){
		String user=KernellSecurity.instance().getCurrentUserName();
		CXPFactura cxp=new CXPFactura();
		cxp.setComentario(a.getComentario());
		cxp.setComentarioAnalisis("ANALISIS_MATERIA_MAQUILA");
		cxp.setCreateUser(user);
		cxp.setDocumento(a.getFactura());
		cxp.setFecha(a.getFechaFactura());
		cxp.setImporte(a.getImporte());
		cxp.setImpuesto(a.getImpuesto());
		cxp.setImpuestoAnalizado(a.getImpuesto());
		cxp.setProveedor(a.getProveedor());
		cxp.setTotal(a.getTotal());
		cxp.setTotalAnalizado(a.getTotal());
		cxp.setUpdateUser(user);
		cxp.actualizarVencimiento();
		a.setCxpFactura(cxp);
		return salvarAnalisis(a);
	}

	public EntradaDeMaterialDao getEntradaDeMaterialDao() {
		return entradaDeMaterialDao;
	}

	public void setEntradaDeMaterialDao(EntradaDeMaterialDao entradaDeMaterialDao) {
		this.entradaDeMaterialDao = entradaDeMaterialDao;
	}

	public OrdenDeCorteDao getOrdenDeCorteDao() {
		return ordenDeCorteDao;
	}

	public void setOrdenDeCorteDao(OrdenDeCorteDao ordenDeCorteDao) {
		this.ordenDeCorteDao = ordenDeCorteDao;
	}	

	public RecepcionDeCorteDao getRecepcionDeCorteDao() {
		return recepcionDeCorteDao;
	}

	public void setRecepcionDeCorteDao(RecepcionDeCorteDao recepcionDeCorteDao) {
		this.recepcionDeCorteDao = recepcionDeCorteDao;
	}	

	public SalidaDeBobinasDao getSalidaDeBobinasDao() {
		return salidaDeBobinasDao;
	}

	public void setSalidaDeBobinasDao(SalidaDeBobinasDao salidaDeBobinasDao) {
		this.salidaDeBobinasDao = salidaDeBobinasDao;
	}	

	public AnalisisDeMaterialDao getAnalisisDeMaterialDao() {
		return analisisDeMaterialDao;
	}

	public void setAnalisisDeMaterialDao(AnalisisDeMaterialDao analisisDeMaterialDao) {
		this.analisisDeMaterialDao = analisisDeMaterialDao;
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

}
