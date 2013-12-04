package com.luxsoft.sw3.cfd.dao;

import java.util.Date;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.sw3.cfd.model.FolioFiscal;
import com.luxsoft.sw3.cfd.model.FolioFiscalId;

public class FolioFiscalDaoTest extends BaseDaoTestCase{
	
	
	
	private FolioFiscalDao folioFiscalDao;
	


	public void testAddRemove(){
		
		FolioFiscalId id=new FolioFiscalId(3L,"TA-FAC-CRE");
		FolioFiscal folio=new FolioFiscal();
		
		folio.setId(id);
		folio.setAnoAprobacion(2010);
		folio.setNoAprobacion(45256);
		folio.setAsignacion(new Date());
		folio.setFolioInicial(1L);
		folio.setFolioFinal(25000L);
		folio.next();
		folio=folioFiscalDao.save(folio);
		flush();
		folio=folioFiscalDao.get(id);
		assertNotNull(folio);
		assertEquals(25000L, folio.getFolioFinal().longValue());
		setComplete();
	}
	
	

	public FolioFiscalDao getFolioFiscalDao() {
		return folioFiscalDao;
	}

	public void setFolioFiscalDao(FolioFiscalDao folioFiscalDao) {
		this.folioFiscalDao = folioFiscalDao;
	}

	
	

}
