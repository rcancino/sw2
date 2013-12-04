package com.luxsoft.siipap.cxp.dao;

import com.luxsoft.siipap.cxp.model.CXPAnticipo;
import com.luxsoft.siipap.cxp.model.CXPPago;
import com.luxsoft.siipap.dao.GenericDao;

public interface CXPPAgoDao extends GenericDao<CXPPago, Long>{
	
	public CXPAnticipo salvarAnticipo(CXPAnticipo anticipo);

}
