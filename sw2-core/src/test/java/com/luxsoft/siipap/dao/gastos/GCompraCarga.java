package com.luxsoft.siipap.dao.gastos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;

import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.EstadoDeCompra;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.model.gastos.TipoDeCompra;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Clase de prueba que carga informacion de pruebas
 * 
 * @author Ruben Cancino
 *
 */
public class GCompraCarga {
	
	private List<GProveedor> proveedores;
	private boolean isNew=false;
	
	protected GProveedor nextProveedor(){
		if(proveedores==null){
			if(isNew){
				proveedores=new ArrayList<GProveedor>();
				for(int i=1;i<50;i++){
					GProveedor p=new GProveedor("XXPROV_"+i);
					ServiceLocator2.getUniversalDao().save(p);
					proveedores.add(p);
				}
			}else
				proveedores=ServiceLocator2.getLookupManager().getProveedores();
			
			
		}
		int next=RandomUtils.nextInt(proveedores.size()-1);
		return proveedores.get(next);
	}
	
	private List<GProductoServicio> productos;
	
	protected GProductoServicio nextProducto(){
		if(productos==null){
			if(isNew){
				productos=new ArrayList<GProductoServicio>();
				int buf=0;
				for(int i=1;i<=50;i++){
					GProductoServicio p=new GProductoServicio();
					p.setClave("PROD_"+i);
					p.setDescripcion("DESC PROD_"+i);
					p.setRubro(nextConcepto());				
					if(++buf%5==0){
						p.setServicio(true);
					}if(buf%2==0){
						p.setIetu(true);					
					}
					ServiceLocator2.getUniversalDao().save(p);
					productos.add(p);
					
				}
			}else
				productos=ServiceLocator2.getLookupManager().getProductos();
			
		}
		int next=RandomUtils.nextInt(50);
		return productos.get(next);
	}
	
	protected Double nextDescuento(){
		double next=RandomUtils.nextDouble();
		BigDecimal val=BigDecimal.valueOf(next).setScale(2, RoundingMode.HALF_EVEN);
		return val.doubleValue();
	}
	
	protected BigDecimal nextImporte(){
		Double[] importes={150d,223d,264d,285d,1222d,6455d,9000d,122d,460d,670d};
		int next=RandomUtils.nextInt(importes.length-1);
		double nextD=RandomUtils.nextDouble();
		return BigDecimal.valueOf(importes[next]*nextD).setScale(2, RoundingMode.HALF_EVEN);
	}
	
	public void execute(){
		Calendar ca=Calendar.getInstance();
		ca.set(Calendar.YEAR, 2007);
		for(int i=1;i<=1000;i++){
			GCompra c=new GCompra();
			c.setProveedor(nextProveedor());
			c.setDepartamento(nextDepartamento());
			c.setSucursal(nextSucursal());
			c.setComentario("Compra test #"+i);
			c.setEstado(EstadoDeCompra.PAGADA);
			c.setTipo(TipoDeCompra.CARGAINICIAL);
			addPartidas(c);
			
			int mes=RandomUtils.nextInt(12);
			ca.set(Calendar.MONTH, mes);
			c.setFecha(ca.getTime());
			c.setYear(2007);
			c.setMes(mes+1);
			c.actualizarTotal();
			try {
				c=ServiceLocator2.getGCompraDao().save(c);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			
			System.out.println("Comra: "+c+ " Partidas: "+c.getPartidas().size());
		}
	}
	
	protected void addPartidas(GCompra c){
		int partidas=RandomUtils.nextInt(9);
		int offset=0;
		for(int i=0;i<=partidas;i++){
			GCompraDet det=new GCompraDet();
			det.setProducto(nextProducto());
			BigDecimal imp=nextImporte();
			det.setPrecio(imp);
			det.setCantidad(BigDecimal.valueOf(RandomUtils.nextInt(50)+1));
			if(offset%3==0){
				det.setDescuento1(nextDescuento());
				offset++;
			}			
			det.actualizar();
			c.agregarPartida(det);			
		}
	}
	
	private List<Sucursal> sucursales;
	
	private Sucursal nextSucursal(){
		if(sucursales==null){
			sucursales=ServiceLocator2.getLookupManager().getSucursales();
		}
		return sucursales.get(RandomUtils.nextInt(sucursales.size()));
	}
	
	private List<Departamento> departamentos;
	
	private Departamento nextDepartamento(){
		if(departamentos==null){
			departamentos=ServiceLocator2.getLookupManager().getDepartamentos();
		}
		return departamentos.get(RandomUtils.nextInt(departamentos.size()));
	}
	
	private List<ConceptoDeGasto> conceptos;
	
	private ConceptoDeGasto nextConcepto(){
		if(conceptos==null){
			conceptos=ServiceLocator2.getLookupManager().getClasificaciones();
		}
		return conceptos.get(RandomUtils.nextInt(conceptos.size()));
	}
	
	public static void main(String[] args) {
		GCompraCarga carga=new GCompraCarga();
		carga.execute();
	}

}
