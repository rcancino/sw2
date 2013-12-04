package com.luxsoft.sw2.server.task;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;

/**
 * Tarea para mandar en forma periodica los archivos XML y PDF correspondientes a CFDs 
 * 
 * @author Ruben Cancino
 *
 */
public class EnvioAutomaticoDeCFDs {
	
	public void enviar(Date fecha){
		String hql="from ComprobanteFiscal c where date(c.creado)=?";
		List<ComprobanteFiscal> cfds=getHibernateTemplate().find(hql, fecha);
		
	}
	
	
	
	
	private HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}

}
