package com.luxsoft.sw3.contabilidad.polizas.ventas;

import java.util.Date;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;
import com.luxsoft.sw3.contabilidad.polizas.cobranza.Proc_AjustesAutomaticosDeDisponibles;
import com.luxsoft.sw3.contabilidad.polizas.cobranza.Proc_IETU;
import com.luxsoft.sw3.contabilidad.polizas.cobranza.Proc_SobrantesFaltantes;

public class VentasController extends ControladorDinamico{
	
	InicializadorParaVentas inicializador;
	
	public VentasController() {
		inicializador=new InicializadorParaVentas(getHibernateTemplate(),getJdbcTemplate());
		getProcesadores().add(new Proc_Ventas());
		getProcesadores().add(new Proc_DepositosPorIdentificarMostrador());
		getProcesadores().add(new Proc_PagosNormalesConDepositosMostrador());
		getProcesadores().add(new Proc_PagosChequeEfectivoMostrador());
		getProcesadores().add(new Proc_CobranzaPagosConDiferenciasMostrador());
		getProcesadores().add(new Proc_AjustesAutomaticosDeDisponibles());
		getProcesadores().add(new Proc_AplicacionDeAnticiposMostrador());
		getProcesadores().add(new Proc_AplicacionDeDisponibleMostrador());
		getProcesadores().add(new Proc_NotaDeCreditoDevolucionMostrador());
		getProcesadores().add(new Proc_NotaDeCreditoBonificacionMostrador());
		getProcesadores().add(new Proc_PagosTarjetaMostrador());
		getProcesadores().add(new Proc_AnticiposTarjeta());
		getProcesadores().add(new Proc_AnticiposMostrador());
		getProcesadores().add(new Proc_IETU("MOS", "01"));
		getProcesadores().add(new Proc_SobrantesFaltantes("COBRANZA MOS"));
	}
	
	@Override
	public void cargar(ModelMap model) {
		inicializador.inicializarVentas(model)
		.inicializarVentas(model)
		.inicializarAbonos(model)
		.inicializarFichas(model)
		.inicializarCortes(model)
		.inicializarCorreccionesDeFichas(model);
		;		
	}
	
	@Override
	public Poliza generar(Date fecha, String referencia) {		
		Poliza pol=super.generar(fecha, referencia);
		pol.setDescripcion("Ventas");
		pol.setTipo(Poliza.Tipo.INGRESO);
		return pol;
	}
	
	public static void main(String[] args) {
		VentasController controller=new VentasController();
		controller.generar(DateUtil.toDate("29/10/2011"));
		
	}

}
