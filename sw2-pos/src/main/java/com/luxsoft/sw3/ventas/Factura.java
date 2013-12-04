package com.luxsoft.sw3.ventas;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaCredito;

/**
 * Adaptacion menor a Venta para vincuarla correctamente a los pedidos
 * 
 * @author Ruben Cancino
 *
 */
//@Entity
//@DiscriminatorValue("FAC2")
public class Factura extends Venta{
	
	@ManyToOne(optional = true,fetch=FetchType.LAZY)			
	@JoinColumn(name = "PEDIDO_ID",nullable=true,insertable=true,updatable=false)
	private Pedido pedido;
	
	@ManyToOne(optional = true,cascade={CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REMOVE})			
	@JoinColumn(name = "FOLIO_AUTO",nullable=true)
	private FacturaFolio folioAutomatico;
	
	

	public FacturaFolio getFolioAutomatico() {
		return folioAutomatico;
	}
	

	public void setFolioAutomatico(FacturaFolio folioAutomatico) {
		this.folioAutomatico = folioAutomatico;
		
	}


	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		if(getId()!=null)
			throw new RuntimeException("La factura ya esta persistida por lo que no es posible re asignar el pedido");
		this.pedido = pedido;
		
		setCliente(pedido.getCliente());
		setSucursal(pedido.getSucursal());
		setCobrador(pedido.getCliente().getCobrador());
		setComentario(pedido.getComentario());
		setComentario2(pedido.getComentario2());
		
		setDescuentoGeneral(pedido.getDescuento());
		setFecha(pedido.getFecha());
		setImporte(pedido.getSubTotal());
		setImpuesto(pedido.getImpuesto());
		setMoneda(pedido.getMoneda());
		final OrigenDeOperacion origen;
		if(pedido.getInstruccionDeEntrega()!=null){
			origen=OrigenDeOperacion.CAM;
			setSerieSiipap("A");
		}
		else if(pedido.isDeCredito()){
			origen=OrigenDeOperacion.CRE;
			setSerieSiipap("E");
		}
		else{
			origen=OrigenDeOperacion.MOS;
			setSerieSiipap("C");
		}
		setOrigen(origen);
		setPrecioBruto(false);
		setPostFechado(pedido.getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO));
		setTc(pedido.getTc());
	
		setTotal(pedido.getTotal());
		setTipoSiipap("Q");
		if(pedido.isDeCredito()){
			VentaCredito credito=new VentaCredito();
			credito.setVenta(this);			
			setCredito(credito);	
			//setComentario(rs.getString("BCOMENTARIO"));
			//setComentarioRepPago(rs.getString("BCOMENTARIO2"));
			//setDiaPago(pedido.getCliente().getCredito().getDiacobro());
			//setFechaRecepcionCXC(rs.getDate("FECHARECEPCIONCXC"));
			//setFechaRevision(rs.getDate("FECHAREVISION"));
			//setFechaRevisionCxc(rs.getDate("FECHAREVISIONCXC"));
			//setNumeroFiscal(rs.getInt("NUMEROFISCAL"));
			setPlazo(pedido.getCliente().getPlazo());
			//setReprogramarPago(rs.getDate("REPROGRAMARPAGO"));
			//setRevisada(rs.getBoolean("REVISADA"));
			//setRevision(rs.getBoolean("REVISION"));
			
			
		}
			
		
	}
	
	

}
