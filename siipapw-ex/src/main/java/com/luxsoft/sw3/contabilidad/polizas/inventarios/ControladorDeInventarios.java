package com.luxsoft.sw3.contabilidad.polizas.inventarios;

import java.text.MessageFormat;
import java.util.Date;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;

/**
 * Controlador para el mantenimiento de polizas de pago de inventarios
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ControladorDeInventarios extends ControladorDinamico{
	
	
	//Proc_Inventario proc;
	
	public  ControladorDeInventarios() {
		getProcesadores().add(new Proc_Compras());
		getProcesadores().add(new Proc_CostoDeVentas());
		getProcesadores().add(new Proc_DevolucionDeCompras());
		getProcesadores().add(new Proc_Transformaciones());
		getProcesadores().add(new Proc_Movimientos());
		getProcesadores().add(new Proc_Redondeo());
		getProcesadores().add(new Proc_Maquila());
		getProcesadores().add(new Proc_FletesProveedor());
		
	}	
	
	@Override
	public Poliza generar(Date fecha, String referencia) {
		Poliza  poliza=super.generar(fecha, referencia);
		Periodo periodo=Periodo.getPeriodoEnUnMes(fecha);		
		poliza.setTipo(Poliza.Tipo.DIARIO);
		poliza.setDescripcion(MessageFormat.format("Inventarios : ",periodo.toString2()));
		poliza.setClase("INVENTARIOS");
		poliza.setReferencia(periodo.toString());
		return poliza;
	}
	
	public static void main(String[] args) {
		ControladorDeInventarios c=new ControladorDeInventarios();
		c.generar(DateUtil.toDate("31/01/2012"));
		
	}

		
}


