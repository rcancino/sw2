package com.luxsoft.sw3.contabilidad.polizas.af;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;

/**
 * Controlador para el mantenimiento de polizas de activo fijo
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ControladorDeActivoFijo extends ControladorDinamico{
	
	
	
	public  ControladorDeActivoFijo() {
		setClase("ACTIVO_FIJO");
		getProcesadores().add(new Proc_ActivoFijo());
	}
	
	public void cargar(ModelMap model) {}
	
	public List<Poliza> generar(Date fecha) {
		return generar(Periodo.getPeriodoDelMesActual(fecha));
	}
	@Override
	public List<Poliza> generar(Periodo periodo) {	
		//System.out.println("Generando poliza: "+periodo);
		final List<Poliza> polizas=new ArrayList<Poliza>();
		model=new ModelMap();
		model.addAttribute("periodo",periodo);
		model.addAttribute("jdbcTemplate",getJdbcTemplate());
		model.addAttribute("hibernateTemplate",getHibernateTemplate());		
		Poliza poliza=new Poliza();
		poliza.setClase(getClase());
		poliza.setFecha(periodo.getFechaFinal());
		poliza.setReferencia(new SimpleDateFormat("MM/yyyy").format(periodo.getFechaFinal()));
		poliza.setDescripcion(MessageFormat.format("Activo Fijo al: {0,date,short}",periodo.getFechaFinal()));
		procesar(poliza, model);
		poliza.actualizar();
		polizas.add(poliza);
		return polizas;
	}
	
	public void recargar(Poliza poliza,boolean total){
		poliza.getPartidas().clear();
		poliza.actualizar();
		Periodo periodo=Periodo.getPeriodoDelMesActual(poliza.getFecha());
		final List<Poliza> polizas=generar(periodo);
		Poliza target=polizas.get(0);
		BeanUtils.copyProperties(poliza, target,new String[]{"id","version","partidas"});
		for(PolizaDet det:target.getPartidas()){
			poliza.agregarPartida(det);
		}
		poliza.actualizar();	
	}
	
	
	public static void main(String[] args) {
		ControladorDeActivoFijo c=new ControladorDeActivoFijo();
		c.generar(DateUtil.toDate("20/02/2012"));
		
	}

		
}


