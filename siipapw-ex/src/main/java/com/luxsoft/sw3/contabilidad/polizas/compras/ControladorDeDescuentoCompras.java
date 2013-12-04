package com.luxsoft.sw3.contabilidad.polizas.compras;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;

/**
 * Controlador para el mantenimiento de polizas de pago de gastos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ControladorDeDescuentoCompras extends ControladorDinamico{
	
	Proc_DescuentosEnCompras proc;
	
	public  ControladorDeDescuentoCompras() {
		setClase("COMPRAS_DESCUENTOS");
		proc=new Proc_DescuentosEnCompras();
	}
	
	@Override
	public List<Poliza> generar(Periodo periodo) {
		List<Date> dias=periodo.getListaDeDias();
		List<Poliza> polizas=new ArrayList<Poliza>();
		for(Date dia:dias){
			Poliza p=generar(dia,new SimpleDateFormat("dd/MM/yyyy").format(dia));
			if(p.getDebe().doubleValue()==0 && p.getHaber().doubleValue()==0)
				continue;
			polizas.add(p);
		}
		return polizas;
	}
	
	@Override
	public Poliza generar(Date fecha, String referencia) {
		Poliza poliza=super.generar(fecha, referencia);
		poliza.setTipo(Poliza.Tipo.DIARIO);
		poliza.setDescripcion(MessageFormat
				.format("Notas de descuento: {0,date,short}",fecha));
		poliza.setClase(getClase());
		poliza.setReferencia(referencia);
		proc.procesarPoliza(poliza);
		poliza.actualizar();
		return poliza;
	}
	
	public static void main(String[] args) {
		ControladorDeDescuentoCompras c=new ControladorDeDescuentoCompras();
		c.generar(DateUtil.toDate("17/08/2011"));
		
	}

		
}


