package com.luxsoft.siipap.cxc.util;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import com.luxsoft.siipap.model.contabilidad.Poliza;
import com.luxsoft.siipap.service.contabilidad.ExportadorGenericoDePolizas;
import com.luxsoft.siipap.swing.utils.MessageUtils;

public class PolizaUtils {
	
	public static void exportarPoliza(final Poliza poliza,String prefijoDeArchivo){
		final ExportadorGenericoDePolizas manager=new ExportadorGenericoDePolizas();
		String res=manager.validar(poliza);
		if(!StringUtils.isBlank(res)){
			MessageUtils.showMessage(res, "Error al generar póliza");
			return;
		}
		File file=manager.exportar(poliza,"META-INF/templates/Poliza_CxC.ftl",prefijoDeArchivo);
		if(file!=null){
			MessageUtils.showMessage("Poliza generada:\n"+file.getAbsolutePath(), "Poliza de gastos");
		}
	}

}
