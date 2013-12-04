package com.luxsoft.siipap.cxc.model;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.AssertTrue;

import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Cuenta por cobrar originada por un cheque devuelto
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@DiscriminatorValue("CHE")
public class ChequeDevuelto extends Cargo{
	
	@ManyToOne (optional=true)
    @JoinColumn (name="CHEQUE_ID")
	private PagoConCheque cheque;
	
	public ChequeDevuelto(){
		setPrecioBruto(false);
	}

	@Override
	public String getTipoDocto() {
		return "CHE";
	}

	public PagoConCheque getCheque() {
		return cheque;
	}

	public void setCheque(PagoConCheque cheque) {
		this.cheque = cheque;
		setCliente(cheque.getCliente());
		setTotal(cheque.getTotal());
		setImporte(MonedasUtils.calcularImporteDelTotal(cheque.getTotal()));
		setImpuesto(MonedasUtils.calcularImpuesto(getImporte()));
		setComentario("Cargo por cheque devuelto :"+cheque.getBanco()+ ": "+cheque.getNumero());
		setCobrador(cheque.getCobrador());
		setOrigen(OrigenDeOperacion.CHE);
		setSucursal(cheque.getSucursal());
		
		
	}
	
	/**
	 * Fecha en que se entrego el cheque por parte del cobrador
	 * @return
	 */
	public Date getFechaDeEntrega(){
		return getCheque().getRecepcionDevolucion();
	}

	@AssertTrue(message="El cheeque origen de la cuenta no puede ser nulo")
	public boolean validar(){
		return cheque!=null;
	}
	

}
