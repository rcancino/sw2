package com.luxsoft.siipap.cxc.ui.model;

import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;

import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.model.core.Cliente;

/**
 * PresentationLayer bean para la generacion de aplicaciones de abonos * 
 * 
 * 
 * @author Ruben Cancino
 *
 */
public class AplicacionModel {
	
	@NotNull(message="El cliente es mandatorio")
	private Cliente cliente;
	
	@NotNull(message="El Abono es mandatorio")
	private Abono abono;
	
	@NotNull(message="La fecha es mandatoria")
	private Date fecha=new Date();
	
	@NotNull
	private BigDecimal disponible=BigDecimal.ZERO;
	
	@NotNull
	private BigDecimal total=BigDecimal.ZERO;

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Abono getAbono() {
		return abono;
	}

	public void setAbono(Abono abono) {
		this.abono = abono;
		if(abono!=null)
			setDisponible(abono.getDisponible());
	}

	public BigDecimal getDisponible() {
		return disponible;
	}

	public void setDisponible(BigDecimal disponible) {
		this.disponible = disponible;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {		
		this.total = total;
	}
	
	
	
	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	
	@AssertTrue(message="Disponible insuficiente")
	public boolean  validarDisponible(){
		return getDisponible().doubleValue()>=0;
	}
	
	@AssertTrue(message="No hay importe por aplicar")
	public boolean validarTotal(){
		return getTotal().doubleValue()>0;
	}

	/**
	 * FactorMethod para regresar una instancia de este modelo
	 *  
	 * @return
	 */
	public static AplicacionModel getModel(){
		return (AplicacionModel)Bean.proxy(AplicacionModel.class);
	}

}
