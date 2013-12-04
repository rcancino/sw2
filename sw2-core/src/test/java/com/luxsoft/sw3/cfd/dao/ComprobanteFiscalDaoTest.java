package com.luxsoft.sw3.cfd.dao;

import java.util.List;


import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.cfd.model.FolioFiscal;

public class ComprobanteFiscalDaoTest extends BaseDaoTestCase{
	
	
	private FolioFiscal folio;
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		int rows=countRowsInTable("SX_CFD_FOLIOS");
		if(rows==0){
			insertDataSet("dbunit/cfd-data.xml");
		}
	}
	
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		List<FolioFiscal> data=universalDao.getAll(FolioFiscal.class);
		folio=data.get(0);
		//folio.getCertificado().getNumeroDeCertificado(); //Inicializando proxy
		assertNotNull(folio);
	}


	public void testAddRemove(){
		ComprobanteFiscal cfd=new ComprobanteFiscal();
		cfd.setAnoAprobacion(folio.getAnoAprobacion());
		cfd.setEmisor("PAPEL S.A. de C.V");
		cfd.setNoAprobacion(folio.getNoAprobacion());
		cfd.setNumeroDeCertificado("asdflaksjdflkjsd");
		String origen="asdfasdfasdfoiausdfo";
		cfd.setOrigen(origen);
		cfd.setReceptor("CLIENTE DE PRUEBA");
		cfd.setTipo("FAC");
		cfd.setXmlPath("c:\\pruebas\\cfd-test.xml");
		cfd.setXsdVersion("2.0");
		cfd.setFolio(folio.next().toString());
		cfd=(ComprobanteFiscal)universalDao.save(cfd);
		flush();
		cfd=(ComprobanteFiscal)universalDao.get(ComprobanteFiscal.class, cfd.getId());
		assertEquals(origen, cfd.getOrigen());
		setComplete();
	}

	

	

}
