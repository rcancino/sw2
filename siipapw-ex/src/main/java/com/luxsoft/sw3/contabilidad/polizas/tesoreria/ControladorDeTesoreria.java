package com.luxsoft.sw3.contabilidad.polizas.tesoreria;

import java.util.Date;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;

/**
 * Controlador para el mantenimiento de polizas de pago de inventarios
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ControladorDeTesoreria extends ControladorDinamico{
	
	InicializadorDeControladorDeTesoreria inicializador;
	
	public  ControladorDeTesoreria() {
		inicializador=new InicializadorDeControladorDeTesoreria(getHibernateTemplate(), getJdbcTemplate());
		getProcesadores().add(new Proc_Inversiones());
		getProcesadores().add(new Proc_InversionesRetorno());
		getProcesadores().add(new Proc_Transferencias());
		getProcesadores().add(new Proc_Comisiones());
		getProcesadores().add(new Proc_Morralla());
		getProcesadores().add(new Proc_CompraMoneda());
		//getProcesadores().add(new Proc_PagosConRequisicionTesoreria());
		
	}
	
	@Override
	public void cargar(ModelMap model) {		
		inicializador.inicializar(model);		
	}
	
	
	

	@Override
	public Poliza generar(Date fecha, String referencia) {
		Poliza res=super.generar(fecha, referencia);
		res.setDescripcion("Tesoreria: "+referencia);
		res.setTipo(Poliza.Tipo.DIARIO);
		//res.setReferencia(referencia);
		return res;
	}
	
	
	
	public static void main(String[] args) {
		ControladorDeTesoreria c=new ControladorDeTesoreria();
		c.generar(DateUtil.toDate("24/02/2012"));
		
	}

		
}


