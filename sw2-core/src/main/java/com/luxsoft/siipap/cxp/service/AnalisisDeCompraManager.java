package com.luxsoft.siipap.cxp.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.model.ContraReciboDet;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.utils.LoggerHelper;

@Service("analisisDeCompraManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class AnalisisDeCompraManager {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	Logger logger=LoggerHelper.getLogger();
	
	@Transactional(propagation=Propagation.REQUIRED)
	public AnalisisDeFactura get(Long id){
		AnalisisDeFactura a=(AnalisisDeFactura)this.hibernateTemplate.get(AnalisisDeFactura.class, id);
		Hibernate.initialize(a.getPartidas());
		Hibernate.initialize(a.getFactura().getAnalisis());
		return a;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public AnalisisDeFactura salvarAnalisis(final AnalisisDeFactura bean){
		bean.actualizarCostos();
		registrarBitacura(bean);
		return (AnalisisDeFactura)this.hibernateTemplate.merge(bean);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void eliminar(final Long analisisId){
		AnalisisDeFactura a=get(analisisId);
		Assert.isTrue(a.getFactura().getRequisitado().abs().doubleValue()==0,"Anális con requisiciones ");
		Assert.isTrue(a.getFactura().getPagos().abs().doubleValue()==0,"Análisis con factura con pagos aplicados no se puede eliminar");
		CXPFactura fac=a.getFactura();
		fac.eliminarAnalisis(a);
		this.hibernateTemplate.delete(a);
		if(fac.getAnalisis().size()==0)
			this.hibernateTemplate.delete(fac);
		
	}
	
	private void registrarBitacura(AnalisisDeFactura bean){
		Date time=new Date();
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

	@Transactional(propagation=Propagation.REQUIRED)
	public List<AnalisisDeFactura> buscarAnalisisPorFactura(List<String> numeros,Proveedor prov){
		//System.out.println("Buscando factruras: "+numeros);
		//System.out.println(" Prov: "+prov);
		List<AnalisisDeFactura> res=new ArrayList<AnalisisDeFactura>();
		for(String numero:numeros){
			//System.out.println("Anexando analisis de factura: "+numero);
			String hql="from AnalisisDeFactura a where a.factura.proveedor.id=? and a.factura.documento=?";
			res.addAll(getHibernateTemplate().find(hql,new Object[]{prov.getId(),numero}));
		}
		return res;
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
	

	public static void main(String[] args) {
		Periodo periodo=Periodo.getPeriodoDelMesActual(new Date());
		List res=ServiceLocator2
		.getHibernateTemplate()
		.find("select a.documento from CXPFactura  a where a.fecha between ? and ?"
				,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
		System.out.println("Res: "+res.size());
	}
	
}
