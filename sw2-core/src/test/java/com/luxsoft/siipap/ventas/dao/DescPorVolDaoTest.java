package com.luxsoft.siipap.ventas.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.DescPorVol;
import com.luxsoft.siipap.ventas.model.DescPorVol.Tipo;


public class DescPorVolDaoTest extends BaseDaoTestCase{
	
	private DescPorVolDao descPorVolDao;
	
	public void testAddCredito(){
		DescPorVol d=new DescPorVol();
		d.setActivo(true);
		d.setDescuento(.45);
		d.setImporte(BigDecimal.valueOf(140));
		d.setTipo(Tipo.CREDITO);
		d.setVigencia(new Date());
		
		d=descPorVolDao.save(d);
		flush();
		assertNotNull(d.getId());
		
	}
	
	public void testAddContado(){
		DescPorVol d=new DescPorVol();
		d.setActivo(true);
		d.setDescuento(.45);
		d.setImporte(BigDecimal.valueOf(140));
		d.setTipo(Tipo.CONTADO);
		d.setVigencia(new Date());
		
		d=descPorVolDao.save(d);
		assertNotNull(d.getId());		
	}
	
	public void testBuscarDescuentoPorVolumen(){
		cargarDescuentosDePrueba();
		double[] vols=    {450.45,750.45,35350.50,125000,25,6350,18625};
		double[] expected={45    ,46    ,49       ,51   ,0 ,47   ,48    };
		for (int i = 0; i < expected.length; i++) {
			double d = expected[i];
			double vol=vols[i];
			double actual=descPorVolDao.buscarDescuentoContado(vol);			
			assertEquals(d, actual);
			logger.info("Volumen: "+vol+ " Desc: "+actual);
			
		}
	}

	public void setDescPorVolDao(DescPorVolDao descPorVolDao) {
		this.descPorVolDao = descPorVolDao;
	}
	
	private void cargarDescuentosDePrueba(){
		DBUtils.whereWeAre();
		String path="classpath:dbunit/descuentosVolumen.xml";
		try {
			insertDataSet(path);
		} catch (SQLException se) {
			//se.printStackTrace();
			DataAccessException dae=jdbcTemplate.getExceptionTranslator().translate("", "", se);
			if(dae instanceof DataIntegrityViolationException){
				//OK ya existe
				logger.info("Ya existen los descuentos");
			}else{
				se.printStackTrace();
				fail("No pudo importar descuentos de prueba: "+path);
			}
			//dae.printStackTrace();
			
		}catch (Exception e) {
			e.printStackTrace();
			fail("No pudo importar descuentos de prueba: "+path);
		}
	}
	
	protected void saveDemoData(){
		double[] rangos={139.99,700,3500,9800,22400.00,39100,84700,84700.01};
		double[] descuentos={0,45,46,47,48,49,50,51};
		for (int i=0;i<rangos.length;i++){
			DescPorVol d=new DescPorVol();
			d.setActivo(true);
			d.setDescuento(descuentos[i]);
			d.setImporte(BigDecimal.valueOf(rangos[i]));
			d.setTipo(Tipo.CONTADO);
			d.setVigencia(DateUtil.toDate("31/12/2009"));
			descPorVolDao.save(d);
		}
		setComplete();
	}
	

}
