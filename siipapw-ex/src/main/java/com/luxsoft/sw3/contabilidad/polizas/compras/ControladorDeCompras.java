package com.luxsoft.sw3.contabilidad.polizas.compras;

import java.util.Date;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;

/**
 * Controlador para el mantenimiento de polizas de pago de gastos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ControladorDeCompras extends ControladorDinamico{
	
	InicializadorCompras inicializador; 
	
	public  ControladorDeCompras() {
		inicializador=new InicializadorCompras(getHibernateTemplate(),getJdbcTemplate());
	//  getProcesadores().add(new Proc_TransitoComp());
		getProcesadores().add(new Proc_TransitoXDifComp());	
		getProcesadores().add(new Proc_FleteProveedorComp());
		getProcesadores().add(new Proc_AnticipoComp());
		getProcesadores().add(new Proc_EntradaAnticipoComp());
	//	getProcesadores().add(new Proc_AnalisisDeTransformaciones());
	//	getProcesadores().add(new Proc_AnalisisDeHojeo());
	//	getProcesadores().add(new Proc_AnalisisDeFlete());
		getProcesadores().add(new Proc_DiferenciasFinancieras());   
	//	getProcesadores().add(new Proc_EntradaNormalCompras());
		
	//	getProcesadores().add(new Proc_DesctoNotaComp());
		
	}
	
	@Override
	public void cargar(ModelMap model) {
		inicializador.inicializar(model);		
	}
	
	@Override
	public Poliza generar(Date fecha, String referencia) {
		Poliza res=super.generar(fecha, referencia);
		res.setDescripcion("Compras");
		res.setTipo(Poliza.Tipo.DIARIO);
		return res;
	}
	
	public static void main(String[] args) {
		ControladorDeCompras c=new ControladorDeCompras();
		c.generar(DateUtil.toDate("02/01/2012"));
		
	}

		
}


