package com.luxsoft.sw3.contabilidad.polizas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;

public class CargaDeSaldosConta {
	
	public static void run()throws Exception{
		List<String> lines=new ArrayList<String>();
		FileReader reader=new FileReader(new File("/pruebas/ContabilidadCargaInicial2012.csv"));
		BufferedReader buf=new BufferedReader(reader);
		String headers=buf.readLine();
		System.out.println("Columnas: "+headers);
		String line=buf.readLine();
		while(line!=null){
			lines.add(line);
			line=buf.readLine();
		}
		buf.close();
		reader.close();
		System.out.println("Registros a cargar: "+lines.size());
		Poliza poliza=new Poliza();
		poliza.setDescripcion("CARGA INICIAL 2012");
		poliza.setFecha(DateUtil.toDate("31/12/2011"));
		poliza.setReferencia("INI_2012");
		poliza.setClase("GENERICA");
		poliza.setTipo(Poliza.Tipo.DIARIO);
		int renglon=1;
		for(String l:lines){
			String[] row=l.split(",");
			String cuenta=row[2];
			String concepto=row[3];
			String conceptoDesc=row[4];
			String ref1=row[5];
			String ref2=row[7];
			boolean cargo=true;
			
			if(StringUtils.isBlank(row[8]))
				row[8]="0.00";
			BigDecimal importe=new BigDecimal(row[8]);
			if(importe.doubleValue()==0){
				importe=new BigDecimal(row[9]);
				cargo=false;
			}
			importe=CantidadMonetaria.pesos(importe).amount();
			String asiento=row[10];
			//String tipo=row[10];
			String desc2="Carga inicial";
			PolizaDet det=PolizaDetFactory.generarPolizaDet(poliza, cuenta
					, concepto
					, cargo, importe, desc2, ref1, ref2, asiento
					);
			det.setRenglon(renglon++);
			if(det.getConcepto()==null){
				ConceptoContable cc=PolizaDetFactory.generarConceptoContable(concepto, conceptoDesc, cuenta);
				det.setConcepto(cc);
			}
			//Assert.notNull(det.getConcepto(),"No existe el concepto: "+concepto+ " Cta: "+cuenta);
			//System.out.println( l );
		}
		poliza.actualizar();
		ServiceLocator2.getPolizasManager().salvarPoliza(poliza);
	}
	
	public static void main(String[] args) throws Exception{
		run();
	}

}
