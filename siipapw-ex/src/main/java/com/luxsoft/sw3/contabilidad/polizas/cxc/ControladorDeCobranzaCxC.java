package com.luxsoft.sw3.contabilidad.polizas.cxc;

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
public class ControladorDeCobranzaCxC extends ControladorDinamico{
	
	InicializadorCobranzaCxC inicializador;
	
	public  ControladorDeCobranzaCxC() {
		inicializador=new InicializadorCobranzaCxC(getHibernateTemplate(),getJdbcTemplate());
		getProcesadores().add(new Proc_AnticiposCxC());
		getProcesadores().add(new Proc_DepositosPorIdentificarCxC());
		getProcesadores().add(new Proc_PagoNormalConDepositoCxC());
		getProcesadores().add(new Proc_NotaDeCreditoBonificacionCxC());
		getProcesadores().add(new Proc_NotaDeCreditoDevolucionCxC());
		getProcesadores().add(new Proc_CobranzaFichasCxC());
		getProcesadores().add(new Proc_CobranzaPagosConDiferenciasCxC());
		getProcesadores().add(new Proc_AplicacionDePagoTarjetaCxC());
		getProcesadores().add(new Proc_AplicacionDeAnticiposCxC());
		getProcesadores().add(new Proc_AplicacionDeDisponibleCxC());
		getProcesadores().add(new Proc_NotasDeCargo());
		//getProcesadores().add(new Proc_IETU_cxc());
		getProcesadores().add(new Proc_IETU("CRE","03"));		
	//	getProcesadores().add(new Proc_PagosSAFCxC());
	}
	
	@Override
	public void cargar(ModelMap model) {
		inicializador.inicializar(model);		
	}
	
	@Override
	public Poliza generar(Date fecha, String referencia) {
		Poliza res=super.generar(fecha, referencia);
		res.setDescripcion("Cobranza CxC");
		res.setTipo(Poliza.Tipo.INGRESO);
		return res;
	}
	
	public static void main(String[] args) {
		ControladorDeCobranzaCxC c=new ControladorDeCobranzaCxC();
		c.generar(DateUtil.toDate("16/11/2011"));
		
	}

		
}


