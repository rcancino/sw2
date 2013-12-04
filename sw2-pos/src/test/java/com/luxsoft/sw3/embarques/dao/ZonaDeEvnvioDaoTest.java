package com.luxsoft.sw3.embarques.dao;

import java.math.BigDecimal;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;

import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.sw3.embarque.ZonaDeEnvio;
import com.luxsoft.sw3.ventas.dao.VentasBaseDaoTest2;

import static junit.framework.Assert.assertNotNull;

public class ZonaDeEvnvioDaoTest extends VentasBaseDaoTest2{
	
	@Autowired
	protected UniversalDao universalDao;
	
	
	@Test
	@NotTransactional
	public void addAll(){
		String[] ciudades={"Texcoco","Pachuca","Toluca","Cuernavaca","Cuautla","Puebla"};
		String[] estados={"México","Hidalgo","Mexico","Morelos","Morelos","Puebal"};
		Double[] tarifas={250.00,700.00,1000.00,1000.00,1000.00,1800.00};
		for(int i=0;i<ciudades.length;i++){
			ZonaDeEnvio z=new ZonaDeEnvio();
			z.setCiudad(ciudades[i]);
			z.setEstado(estados[i]);
			z.setTarifa(BigDecimal.valueOf(tarifas[i]));
			z.setUnidad("TONELADA");
			z.setMultiplo(3);
			z=(ZonaDeEnvio)universalDao.save(z);
			flush();
			assertNotNull(z.getId());
			
		}
			
		
	}

}
