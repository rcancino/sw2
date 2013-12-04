package com.luxsoft.sw3.contabilidad.polizas.cobranzacam;

import java.util.Date;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;
import com.luxsoft.sw3.contabilidad.polizas.cobranza.Proc_AjustesAutomaticosDeDisponibles;
import com.luxsoft.sw3.contabilidad.polizas.cobranza.Proc_IETU;
import com.luxsoft.sw3.contabilidad.polizas.cobranza.Proc_SobrantesFaltantes;

public class CobranzaCamionetaController extends ControladorDinamico{
	
	InicializadorCobranzaCamioneta inicializador;
	
	public CobranzaCamionetaController() {
		inicializador=new InicializadorCobranzaCamioneta(getHibernateTemplate(),getJdbcTemplate());
		getProcesadores().add(new Proc_AnticiposCamioneta());
		getProcesadores().add(new Proc_DepositosPorIdentificarCamioneta());
		getProcesadores().add(new Proc_PagoNormalConDepositoCamioneta());
		getProcesadores().add(new Proc_PagoAnticipoFacturadoCam());
		getProcesadores().add(new Proc_CobranzaFichasCamioneta());
		getProcesadores().add(new Proc_CobranzaPagosConDiferenciasCamioneta());
		getProcesadores().add(new Proc_AjustesAutomaticosDeDisponibles());
		getProcesadores().add(new Proc_AplicacionDeAnticiposCamioneta());
		getProcesadores().add(new Proc_AplicacionDeDisponiblesCamioneta());
		getProcesadores().add(new Proc_NotaDeCreditoBonificacionCamioneta());
		getProcesadores().add(new Proc_NotaDeCreditoDevolucionCamioneta());
		getProcesadores().add(new Proc_SobrantesFaltantes("COBRANZA CAM"));
		getProcesadores().add(new Proc_IETU("CAM","02"));
		getProcesadores().add(new Proc_NotasDeCargoCamioneta());
	}
	
	@Override
	public void cargar(ModelMap model) {
		inicializador.inicializar(model);		
	}
	
	@Override
	public Poliza generar(Date fecha, String referencia) {		
		Poliza pol=super.generar(fecha, referencia);
		pol.setDescripcion("Cobranza camioneta");
		pol.setTipo(Poliza.Tipo.INGRESO);
		return pol;
	}
	
	

	
	public static void main(String[] args) {
		CobranzaCamionetaController controller=new CobranzaCamionetaController();
		controller.generar(DateUtil.toDate("16/11/2011"));
		
	}
	

}
