package com.luxsoft.sw3.ventas;

import java.util.Date;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Entidad para generar un analisis global de las opreciones en el punto de venta
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnalisisDeVentas implements ListEventListener {
	
	private Date creado;
	
	private Date fecha;
	
	private Long id;
	
	private CantidadMonetaria ventaTotal=CantidadMonetaria.pesos(0);
	
	private CantidadMonetaria ventaCredito=CantidadMonetaria.pesos(0);
	
	private CantidadMonetaria ventaCamioneta=CantidadMonetaria.pesos(0);
	
	private CantidadMonetaria ventaMostrador=CantidadMonetaria.pesos(0);
	
	private EventList<Venta> ventas=new BasicEventList<Venta>(0);
	
	

	public Date getCreado() {
		return creado;
	}

	public void setCreado(Date creado) {
		this.creado = creado;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CantidadMonetaria getVentaTotal() {
		return ventaTotal;
	}

	public void setVentaTotal(CantidadMonetaria ventaTotal) {
		this.ventaTotal = ventaTotal;
	}

	public CantidadMonetaria getVentaCredito() {
		return ventaCredito;
	}

	public void setVentaCredito(CantidadMonetaria ventaCredito) {
		this.ventaCredito = ventaCredito;
	}

	public CantidadMonetaria getVentaCamioneta() {
		return ventaCamioneta;
	}

	public void setVentaCamioneta(CantidadMonetaria ventaCamioneta) {
		this.ventaCamioneta = ventaCamioneta;
	}

	public CantidadMonetaria getVentaMostrador() {
		return ventaMostrador;
	}

	public void setVentaMostrador(CantidadMonetaria ventaMostrador) {
		this.ventaMostrador = ventaMostrador;
	}

	public EventList<Venta> getVentas() {
		return ventas;
	}

	public void setVentas(EventList<Venta> ventas) {
		this.ventas = ventas;
	}

	public void listChanged(ListEvent listChanges) {
		while(listChanges.next()){
		
		}
		actualizarTotales();
		
	}

	/**
	 * Actualiza los totales al detectar algun cambio en las listas
	 * 
	 */
	private void actualizarTotales() {
		CantidadMonetaria ventaTotal=CantidadMonetaria.pesos(0);		
		CantidadMonetaria ventaCredito=CantidadMonetaria.pesos(0);		
		CantidadMonetaria ventaCamioneta=CantidadMonetaria.pesos(0);		
		CantidadMonetaria ventaMostrador=CantidadMonetaria.pesos(0);
		for(Venta v:this.ventas){
			ventaTotal=ventaTotal.add(v.getTotalCM());
			switch (v.getOrigen()) {
			case CRE:
				ventaCredito=ventaCredito.add(v.getTotalCM());
				break;
			case MOS:
				ventaMostrador=ventaMostrador.add(v.getTotalCM());
				break;
			case CAM:
				ventaCamioneta=ventaCamioneta.add(v.getTotalCM());
				break;
			default:
				break;
			}
		}
		setVentaTotal(ventaTotal);
		setVentaCredito(ventaCredito);
		setVentaCamioneta(ventaCamioneta);
		setVentaMostrador(ventaMostrador);
	}
	
	
	
	
	

}
