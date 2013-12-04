package com.luxsoft.sw3.contabilidad.polizas.jur;

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
public class ControladorDeCobranzaJur extends ControladorDinamico{
	
	InicializadorCobranzaJur inicializador;
	
	public  ControladorDeCobranzaJur() {
		inicializador=new InicializadorCobranzaJur(getHibernateTemplate(),getJdbcTemplate());

		getProcesadores().add(new Proc_DepositosPorIdentificarJur());
		getProcesadores().add(new Proc_PagoNormalConDepositoJur());
		getProcesadores().add(new Proc_NotaDeCreditoBonificacionJur());
		getProcesadores().add(new Proc_CobranzaFichasJur());
		getProcesadores().add(new Proc_CobranzaPagosConDiferenciasJur());
		getProcesadores().add(new Proc_AplicacionDePagoTarjetaJur());
		getProcesadores().add(new Proc_AplicacionDeAnticiposJur());
		getProcesadores().add(new Proc_AplicacionDeDisponibleJur());
		getProcesadores().add(new Proc_NotasDeCargo_Jur());
		getProcesadores().add(new Proc_Traspaso_Jur());
	//	getProcesadores().add(new Proc_IETU("JUR","03"));
		getProcesadores().add(new Proc_PagoEnEspecieJur());
	}
	
	@Override
	public void cargar(ModelMap model) {
		inicializador.inicializar(model);		
	}
	
	
	@Override
	public Poliza generar(Date fecha, String referencia) {
		Poliza res=super.generar(fecha, referencia);
		res.setDescripcion("Juridico");
		res.setTipo(Poliza.Tipo.INGRESO);
		return res;
	}
	
	public static void main(String[] args) {
		ControladorDeCobranzaJur c=new ControladorDeCobranzaJur();
		c.generar(DateUtil.toDate("31/10/2011"));
		
	}

		
}


