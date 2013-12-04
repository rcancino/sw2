package com.luxsoft.sw3.replica;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;

/**
 * Facade para ejecutar las tareas de replica de manera mas simple
 * 
 * @author ruben
 *
 */
public class ReplicadorManager {
	
	private ReplicadorDeVentas replicadorDeVentas=new ReplicadorDeVentas();
	private ReplicadorDeCompras replicadorDeCompras=new ReplicadorDeCompras();
	private ReplicadorDeCOMS replicadorDeCOMS=new ReplicadorDeCOMS();
	private ReplicadorDeMovimientos replicadorDeMovimientos=new ReplicadorDeMovimientos();
	private ReplicadorDeDevoluciones replicadorDeDevoluciones=new ReplicadorDeDevoluciones();
	private ReplicadorDeTransformaciones replicadorDeTransformaciones=new ReplicadorDeTransformaciones();
	private ReplicadorDeDevolucionesDeCompra replicadorDevolucionesCompra=new ReplicadorDeDevolucionesDeCompra();
	private ReplicadorDeEmbarques replicadorDeEmbarques=new ReplicadorDeEmbarques();
	private ReplicadorDeEntregas replicadorDeEntregas=new ReplicadorDeEntregas();
	private ReplicadorDeAbonos replicadorDeAbonos = new ReplicadorDeAbonos();
	private ReplicadorDeNotasDeDevolucion replicadorDeNotasDevolucion=new ReplicadorDeNotasDeDevolucion();
	private ReplicadorDeClientes replicadorDeClientes=new ReplicadorDeClientes();
	
	public void importar(String dia){		
		importar(dia,dia);
	}
	
	public void importar(String diaInicial,String diaFinal){
		Periodo per=new Periodo(diaInicial,diaFinal);
		importar(per);
	}
	
	public void importar(Periodo periodo){
		List<Date> dias=periodo.getListaDeDias();
		importar(dias.toArray(new Date[0]));
	}
	
	public void importar(String... sdias){
		for(String dia:sdias){
			importar(dia);
		}
	}
	
	public void importar(Date...dias){		
		for(Date dia:dias){
			
			for(Long sucursalId:getSucursales()){
				this.replicadorDeClientes.importarPendientes(sucursalId);
				this.replicadorDeVentas.importarFaltantes(sucursalId, dia);
				//this.replicadorDeCompras.importar(dia, sucursalId);
				
				this.replicadorDeMovimientos.importar(dia, sucursalId);
				this.replicadorDeTransformaciones.importar(dia, sucursalId);
				this.replicadorDeDevoluciones.importar(dia, sucursalId);
				this.replicadorDevolucionesCompra.importar(dia, sucursalId);
				this.replicadorDeAbonos.importar(sucursalId, dia);
				this.replicadorDeNotasDevolucion.importar();
				this.replicadorDeNotasDevolucion.replicar();
			}
		}
		
		this.replicadorDeEmbarques.importar();
		this.replicadorDeEntregas.importar();
	}
	
	
	public static void importarPorDia(){
		Date dia=SelectorDeFecha.seleccionar();
		ReplicadorManager manager=new ReplicadorManager();
		manager.importar(dia);
	}
	
	private Collection<Long> sucursales;

	public Collection<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Collection<Long> sucursales) {
		this.sucursales = sucursales;
	}

	public ReplicadorManager setSucursales(Long...sucs){
		this.sucursales=Arrays.asList(sucs);
		return this;
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				importarPorDia();
				System.exit(0);
			}

		});
	}

}
