package com.luxsoft.sw3.contabilidad.polizas.tesoreria;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;

/**
 * Controlador para la poliza de transito de depositos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ControladorDeTransitoDeDepositos extends ControladorDinamico{
	
	InicializadorParaDepositosEnTransito inicializador;
	Proc_CortesDeTarjetaEnTransitoTraspaso transitoTraspaso;
	Proc_CortesDeTarjetaEnTransitoCancelacion transitoCancelacion;
	
	public  ControladorDeTransitoDeDepositos() {
		setClase("DEPOSITOS_EN_TRANSITO");
		inicializador=new InicializadorParaDepositosEnTransito(getHibernateTemplate(), getJdbcTemplate());
		transitoTraspaso=new Proc_CortesDeTarjetaEnTransitoTraspaso();
		transitoCancelacion=new Proc_CortesDeTarjetaEnTransitoCancelacion();
	}
	
	@Override
	public void cargar(ModelMap model) {		
		inicializador.inicializar(model);		
	}

	@Override
	public List<Poliza> generar(Date fecha) {
		List<Poliza> res=new ArrayList<Poliza>();
		
		model=new ModelMap();
		model.addAttribute("fecha",fecha);
		model.addAttribute("jdbcTemplate",getJdbcTemplate());
		model.addAttribute("hibernateTemplate",getHibernateTemplate());
		
		
		cargar(model);
		Periodo per=Periodo.getPeriodoAnterior(new Periodo(fecha));
		String referencia=new SimpleDateFormat("dd/MM/yyyy").format(per.getFechaFinal());
		Poliza polizaCancelaciion=generarTraspaso(fecha,referencia+" (Cancelacion)",this.transitoCancelacion);
		polizaCancelaciion.setFecha(per.getFechaFinal());
		polizaCancelaciion.actualizar();
		res.add(polizaCancelaciion);
		
		referencia=new SimpleDateFormat("dd/MM/yyyy").format(fecha);
		Poliza polizaTransito=generarTraspaso(fecha,referencia+" (Traspaso)",this.transitoTraspaso);
		polizaTransito.actualizar();
		res.add(polizaTransito);
		
		
		
		return res;
	}

	
	public Poliza generarTraspaso(Date fecha, String referencia,IProcesador proc) {
		Poliza poliza=new Poliza();
		poliza.setDescripcion("Depositos en transito al : "+referencia);
		poliza.setTipo(Poliza.Tipo.DIARIO);		
		poliza.setClase(getClase());
		poliza.setReferencia(referencia);
		poliza.setFecha(fecha);
		poliza.setDescripcion(getClase()+ " - "+referencia);
		proc.procesar(poliza, model);		
		return poliza;
		
	}
	
	
	public static void main(String[] args) {
		ControladorDeTransitoDeDepositos c=new ControladorDeTransitoDeDepositos();
		c.generar(DateUtil.toDate("24/02/2012"));
		
	}

		
}


