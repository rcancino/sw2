package com.luxsoft.sw3.tasks;

import java.math.BigDecimal;

import com.luxsoft.siipap.model.core.Corte;
import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Actualiza e importa la informacion en las tablas de corte
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ActualizarTablasDeCorte {
	
	
	public void importarTiposDecorte(){
		
		Corte.Tipo tipos[]={Corte.Tipo.SENCILLO,Corte.Tipo.DOBLE,Corte.Tipo.HOJAS};
		BigDecimal precios[]={BigDecimal.valueOf(10.00),BigDecimal.valueOf(20.00),BigDecimal.valueOf(2.00)};
		int[] maximos={1000,1000,1};
		
		for(int i=0;i<tipos.length;i++){
			Corte corte=new Corte();
			corte.setTipo(tipos[i]);
			corte.setMaximoDeHojas(maximos[i]);
			corte.setPrecioContado(precios[i]);
			corte.setPrecioCredito(precios[i]);
			switch (corte.getTipo()) {
			case SENCILLO:
				corte.agregarMedida(4, 0, 10);
				corte.agregarMedida(2, 10.1, 15);
				corte.agregarMedida(1,15.1,1000);
				break;
			case DOBLE:
				corte.agregarMedida(8, 0, 10);
				corte.agregarMedida(2, 10.1, 15);
				corte.agregarMedida(1,15.1,1000);
				break;
			case HOJAS:
				corte.agregarMedida(8, 0, 10);
				corte.agregarMedida(4, 10.1, 15);
				corte.agregarMedida(2,15.1,30);
				corte.agregarMedida(1,30.1,1000);
			default:
				break;
			}
			Services.getInstance().getUniversalDao().save(corte);
		}
		
	}
	
	public void configurarCortesOpalina(){
		Linea linea=(Linea)Services.getInstance().getUniversalDao().get(Linea.class, 113L);
		Corte corte=new Corte();
		corte.setTipo(Corte.Tipo.ESPECIAL);
		corte.setMaximoDeHojas(1000);
		corte.setPrecioContado(BigDecimal.valueOf(10.00));
		corte.setPrecioCredito(BigDecimal.valueOf(10.00));
		corte.agregarMedida(8, 0, 10);
		corte.agregarMedida(2, 10.1, 15);
		corte.agregarMedida(1,15.1,1000);
		linea.setCorte(corte);
		Services.getInstance().getUniversalDao().save(linea);
		
	}
	
	public void configurarCortesCapleSBS(){
		
		Linea linea=(Linea)Services.getInstance().getUniversalDao().get(Linea.class, 104L);
		
		Corte corte=new Corte();
		corte.setTipo(Corte.Tipo.ESPECIAL);
		corte.setMaximoDeHojas(1000);
		corte.setPrecioContado(BigDecimal.valueOf(20.00));
		corte.setPrecioCredito(BigDecimal.valueOf(20.00));
		corte.agregarMedida(25, 0, 10);
		corte.agregarMedida(2, 10.1, 15);
		corte.agregarMedida(1,15.1,1000);
		linea.setCorte(corte);
		Services.getInstance().getUniversalDao().save(linea);
		
		linea=(Linea)Services.getInstance().getUniversalDao().get(Linea.class, 120L);
		linea.setCorte(corte);
		Services.getInstance().getUniversalDao().save(linea);
		
	}
	
	
	
	public static void main(String[] args) {
		POSDBUtils.whereWeAre();
		ActualizarTablasDeCorte task=new ActualizarTablasDeCorte();
		task.importarTiposDecorte();
		task.configurarCortesOpalina();
		task.configurarCortesCapleSBS();
	}

}
