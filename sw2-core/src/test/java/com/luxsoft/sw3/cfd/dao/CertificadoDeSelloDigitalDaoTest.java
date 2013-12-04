package com.luxsoft.sw3.cfd.dao;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.sw3.cfd.model.CertificadoDeSelloDigital;

public class CertificadoDeSelloDigitalDaoTest extends BaseDaoTestCase{
	
	private CertificadoDeSelloDigitalDao certificadoDeSelloDigitalDao;
	
	
	public void testAddRemove()throws Exception{
		CertificadoDeSelloDigital cer=new CertificadoDeSelloDigital();		
		String cerPath="Z:\\CFD\\CERT\\00001000000102129215.cer";
		cer.setCertificadoPath(cerPath);
		cer.setPrivateKeyPath("Z:\\CFD\\CERT\\TACUBA_CFD.KEY");
		cer.setAlgoritmo(CertificadoDeSelloDigital.ALGORITMOS[0]);
		
		cer=certificadoDeSelloDigitalDao.save(cer);
		flush();
		
		cer=certificadoDeSelloDigitalDao.get(cer.getId());
		flush();
		assertEquals(cerPath, cer.getCertificadoPath());
		assertEquals("00001000000102129215", cer.getNumeroDeCertificado());
		setComplete();
		/*
		certificadoDeSelloDigitalDao.remove(cer.getId());
		try {
			cer=certificadoDeSelloDigitalDao.get(cer.getId());
			fail("Debio mandar error al no encontrar el registro");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
			log.info("Eliminacion OK");
		}
		*/
	}



	public void setCertificadoDeSelloDigitalDao(
			CertificadoDeSelloDigitalDao certificadoDeSelloDigitalDao) {
		this.certificadoDeSelloDigitalDao = certificadoDeSelloDigitalDao;
	}
	
	
	
}
