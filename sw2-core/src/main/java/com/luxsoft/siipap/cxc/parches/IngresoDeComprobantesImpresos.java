package com.luxsoft.siipap.cxc.parches;

import java.text.MessageFormat;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;

public class IngresoDeComprobantesImpresos {
	
	public static void run(){
		
		String INSERT="INSERT INTO sx_comprobantes_impresos (" +
				"ORIGEN_ID" +
				",ESTADO" +
				",TIPO" +
				",TIPO_CFD" +
				",SERIE" +
				",FOLIO" +
				",FECHA" +
				",RECEPTOR" +
				",RFC" +
				",IMPUESTO" +
				",TOTAL" +
				",ANO_APROBACION" +
				",NO_APROBACION,EMISOR)" +
				" VALUES (" +
				"?" + //ORIGEN_ID
				",?" + //ESTADO
				",?" +//TIPO
				",'I'" +//TIPO_CFD
				",?" +//SERIE
				",?" +//FOLIO
				",?" +//FECHA
				",'IMPRETEI,S.A. DE C.V'" +
				",'IMP960607EY3'" +
				",0" +
				",0" +
				",2010" +
				",?" + //numoer de aprobacion
				",'PAPEL S.A. de C.V.'" +
				")";
		//VALUES ('QUFACCRE11024',1,'QUFACCRE','I','R',11024,{d '2010-12-22'},'MOSTRADOR','XAXX010101000',0,0,2010,20365801,'PAPEL S.A. de C.V.');
		Long[] FOLIOS={27603l,27604l,27605l,27606l,27607l,27608l,27609l,27610l,27611l,27612l,27613l
					,174606L,174607L,174608L,174609L,174610L,174611L,174612L,174613L,174614L};
		int[] ESTADOS={0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
		String TIPOS[]={"TAFACCRE","TAFACCRE","TAFACCRE","TAFACCRE","TAFACCRE","TAFACCRE","TAFACCRE","TAFACCRE","TAFACCRE","TAFACCRE","TAFACCRE"
				,"ANFACCRE","ANFACCRE","ANFACCRE","ANFACCRE","ANFACCRE","ANFACCRE","ANFACCRE","ANFACCRE","ANFACCRE"
				};
		String FECHAS[]={"05/07/2011","05/07/2011","05/07/2011","05/07/2011","08/07/2011","13/07/2011","13/07/2011","13/07/2011","13/07/2011","15/07/2011","22/07/2011"
				,"21/07/2011","21/07/2011","30/07/2011","30/07/2011","30/07/2011","30/07/2011","30/07/2011","30/07/2011","30/07/2011"
				};
		 
		
		for(int index=0;index<20;index++){
			Long folio=FOLIOS[index];
			int estado=ESTADOS[index];
			String fecha=FECHAS[index];
			String tipo=TIPOS[index];
			Object[] args={
					MessageFormat.format("{0}{1}", tipo,folio)
					,estado
					,tipo
					,tipo.equalsIgnoreCase("TAFACCRE")?"X":"H"
					,folio
					,DateUtil.toDate(fecha)
					,tipo.equalsIgnoreCase("TAFACCRE")?20041571:20041554
			};
			
			int res=ServiceLocator2.getJdbcTemplate().update(INSERT, args);
		}

	}
	public static void main(String[] args) {
		run();
	}

}
