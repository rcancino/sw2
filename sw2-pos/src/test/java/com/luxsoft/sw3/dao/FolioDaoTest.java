package com.luxsoft.sw3.dao;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.dao.SucursalDao;
import com.luxsoft.siipap.dao.core.FolioDao;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Folio;
import com.luxsoft.siipap.model.core.FolioId;
import com.luxsoft.sw3.ventas.dao.VentasBaseDaoTest2;

import static org.junit.Assert.*;


public class FolioDaoTest extends VentasBaseDaoTest2{
	
	@Autowired
	FolioDao folioDao;
	
	@Autowired
	protected SucursalDao sucursalDao;
	
	Sucursal sucursal;
	
	@Before
	public void setUp(){
		sucursal=sucursalDao.buscarPorClave(5);
	}

	@Test
	@NotTransactional
	public void testAdd(){
		Folio f=new Folio();
		FolioId id=new FolioId(sucursal.getId(),"FAC_CRE");
		f.setId(id);
		f.next();
		f=folioDao.save(f);
		flush();
		assertNotNull(f.getId());
		logger.info("Folio salvado:"+f.getId());
	}

}
