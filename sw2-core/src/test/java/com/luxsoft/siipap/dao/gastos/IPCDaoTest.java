package com.luxsoft.siipap.dao.gastos;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.gastos.INPC;

public class IPCDaoTest extends BaseDaoTestCase{
	
	public void testAddRemove(){
		INPC ipc=new INPC(2007,1,8.85);
		ipc=(INPC)universalDao.save(ipc);
		assertNotNull(ipc);
		flush();
		setComplete();
		
		universalDao.remove(INPC.class, ipc.getId());
		try {
			ipc=(INPC)universalDao.get(INPC.class, ipc.getId());
			fail("Debiio mandar error");
		} catch (ObjectRetrievalFailureException ex) {
			assertNotNull(ex);
			logger.debug("OK no encontro el bean");
		}
	}

}
