package com.luxsoft.sw3.cfd.dao;

import java.util.Map;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.sw3.cfd.model.CertificadoDeSelloDigital;

/**
 * DAO para el certificado de sello digital
 * 
 * @author Ruben Cancino Ramos
 *
 */
public interface CertificadoDeSelloDigitalDao extends GenericDao<CertificadoDeSelloDigital, Long>{
	
	

	/**
	 * Regresa un certificado de sello digital apropiado para las operaicones
	 * 
	 * @param contexto map de posibles parametros para localizar el certificado
	 * @return
	 */
	public CertificadoDeSelloDigital buscarCertificadoVigente(Map contexto);
}
