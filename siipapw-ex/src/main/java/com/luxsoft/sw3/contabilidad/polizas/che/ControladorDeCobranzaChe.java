package com.luxsoft.sw3.contabilidad.polizas.che;

import java.util.Date;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;
import com.luxsoft.sw3.contabilidad.polizas.cobranza.Proc_IETU;

/**
 * Controlador para el mantenimiento de polizas de pago de gastos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ControladorDeCobranzaChe extends ControladorDinamico{
	
	InicializadorCobranzaChe inicializador;
	
	public  ControladorDeCobranzaChe() {
		inicializador=new InicializadorCobranzaChe(getHibernateTemplate(),getJdbcTemplate());
		getProcesadores().add(new Proc_DepositosPorIdentificarChe());
		getProcesadores().add(new Proc_PagoNormalConDepositoChe());
		getProcesadores().add(new Proc_NotaDeCreditoBonificacionChe());
		getProcesadores().add(new Proc_CobranzaFichasChe());
		getProcesadores().add(new Proc_CobranzaPagosConDiferenciasChe());
		getProcesadores().add(new Proc_AplicacionDePagoTarjetaChe());
		getProcesadores().add(new Proc_AplicacionDeAnticiposChe());
		getProcesadores().add(new Proc_AplicacionDeDisponibleChe());
		getProcesadores().add(new Proc_NotasDeCargo_Che());
		getProcesadores().add(new Proc_Cargo_Che_Dev());
	//	getProcesadores().add(new Proc_IETU("CHE","03"));
	}
	
	@Override
	public void cargar(ModelMap model) {
		inicializador.inicializar(model);		
	}
	
	public static void main(String[] args) {
		ControladorDeCobranzaChe c=new ControladorDeCobranzaChe();
		c.generar(DateUtil.toDate("16/11/2011"));
		
	}
	
	
	@Override
	public Poliza generar(Date fecha, String referencia) {
		Poliza res=super.generar(fecha, referencia);
		res.setDescripcion("Cheques Devueltos");
		res.setTipo(Poliza.Tipo.INGRESO);
		return res;
	}
	

		
}


