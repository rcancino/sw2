/**
 * 
 */
package com.luxsoft.sw3.ventas;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.springframework.util.Assert.notNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

/**
 * Pruebas unitarias para el comportamiento y estado de la
 * entidad {@link Pedido}
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PedidoTest {
	
	public  void setProperty(Object bean,String property,Object value) throws Exception{
		boolean readable=PropertyUtils.isWriteable(bean, property);
		if(readable){
			PropertyUtils.setProperty(bean, property, value);
		}else{
			Field field=ReflectionUtils.findField(bean.getClass(), property, value.getClass());
			ReflectionUtils.makeAccessible(field);
			ReflectionUtils.setField(field, bean, value);
		}
	}

	
	/**
	 * Test method for {@link com.luxsoft.sw3.ventas.Pedido#hashCode()}.
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws NoSuchMethodException 
	 */
	@Test
	public void testHashCode() throws Exception{
		
		final Pedido p1=new Pedido();
		
		setProperty(p1, "id", new Long(1));
		notNull(p1.getId());
		
		final Pedido p2=new Pedido();
		setProperty(p2, "id", new Long(2));
		notNull(p2.getId());
		System.out.println("hashCode P1: "+p1.hashCode());
		System.out.println("hashCode P2: "+p2.hashCode());
		
		assertFalse(p1.hashCode()==p2.hashCode());
		
		setProperty(p2, "id", new Long(1));
		assertEquals(p1.hashCode(), p2.hashCode());
		
	}

	/**
	 * Test method for {@link com.luxsoft.sw3.ventas.Pedido#equals(java.lang.Object)}.
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	@Test
	public void testEqualsObject() throws Exception {
		final Pedido p1=new Pedido();
		setProperty(p1, "id", new Long(1));
		notNull(p1.getId());
		
		final Pedido p2=new Pedido();
		setProperty(p2, "id", new Long(2));
		notNull(p2.getId());
		
		
		System.out.println("Pedido 1: "+p1.getId());
		System.out.println("Pedido 2: "+p2.getId());
		assertFalse("No deben ser iguales",p1.equals(p2));
		BeanUtils.setProperty(p2, "id", new Long(1));
		//assertEquals(p1, p2);
	}
	
	@Test
	public void testActualizarImportes(){
		
		Pedido p=new Pedido();
		crearPartidasDePrueba(p);
		p.setFlete(BigDecimal.valueOf(500.00));
		p.setComisionTarjeta(2d);
		p.actualizarImportes();
		
		
		BigDecimal importeBruto=BigDecimal.valueOf(10200.00);
		assertEquals(importeBruto.doubleValue(), p.getImporteBruto().doubleValue(),.0001d);
		System.out.println("Importe bruto OK: "+p.getImporteBruto());
		
		BigDecimal importeDescuento=BigDecimal.valueOf(4896.00);
		assertEquals(importeDescuento.doubleValue(), p.getImporteDescuento().doubleValue(),.0001d);
		System.out.println("Importe Descuento OK: "+p.getImporteDescuento());
		
		BigDecimal importeCortes=BigDecimal.valueOf(50.00);
		assertEquals(importeCortes.doubleValue(), p.getImporteCorte().doubleValue(),.0001d);
		System.out.println("Importe Cortes OK: "+p.getImporteCorte());
		
		BigDecimal importeFlete=BigDecimal.valueOf(500.00);
		assertEquals(importeFlete.doubleValue(), p.getFlete().doubleValue(),.0001d);
		
		BigDecimal importeComisionTarjeta=BigDecimal.valueOf(204.00);
		assertEquals(importeComisionTarjeta.doubleValue(), p.getComisionTarjetaImporte().doubleValue(),.0001d);
		System.out.println("Comision Tarjeta OK: "+p.getComisionTarjetaImporte());
		
		BigDecimal subTotal=BigDecimal.valueOf(6058.00);
		assertEquals(subTotal.doubleValue(), p.getSubTotal().doubleValue(),.0001d);
		System.out.println("Sub Total OK: "+p.getSubTotal());
		
	}
	
	private void crearPartidasDePrueba(Pedido p){
		
		double[] cantidades={3000d,1000d,8000d,5000d};
		int[] cortes={5,0,0,0};
		for(int i=0;i<cantidades.length;i++){
			PedidoDet partida=PedidoDet.getPedidoDet();
			partida.setPrecio(BigDecimal.valueOf(600.00));
			partida.setCantidad(cantidades[i]);
			partida.setFactor(1000.00);
			partida.setKilos(50.00);
			partida.setDescuento(48d);
			partida.setCortes(cortes[i]);
			partida.setPrecioCorte(BigDecimal.valueOf(10));
			p.agregarPartida(partida);
		}		
	}

}
