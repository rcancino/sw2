package com.luxsoft.siipap.cxc.ui.form;

import java.util.Date;
import java.util.List;

import org.hibernate.validator.Length;
import org.springframework.util.Assert;



import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.ventas.model.Cobrador;

/**
 * PresentationModel para la revision de cuentas por cobrar
 * 
 * @author Ruben Cancino
 *
 */
public class RevisionDeCargosModel extends DefaultFormModel{
	
	private final List<Cargo> cuentas;
	private final Cliente cliente;
	

	public RevisionDeCargosModel(List<Cargo> cuentas) {
		super((RevisionModel)Bean.proxy(RevisionModel.class));
		this.cuentas = cuentas;
		this.cliente=cuentas.get(0).getCliente();
		Assert.notNull(cliente,"El cliente no puede ser nulo");
		initModel();
	}
	
	protected void initModel(){
		
		RevisionModel model=getRevision();
		model.setCobrador(cliente.getCobrador());
		if(cliente.getCredito()!=null){
			model.setDiaCobro(cliente.getCredito().getDiacobro());
			model.setDiaRevision(cliente.getCredito().getDiarevision());
			model.setVencimientoFechaRevision(!cliente.getCredito().isVencimientoFactura());
			model.setPlazo(cliente.getCredito().getPlazo());
			model.setFechaRevision(cuentas.get(0).getFechaRevisionCxc());
			model.setCobrador(cuentas.get(0).getCobrador());
			model.setComentarioRecepcion(cuentas.get(0).getComentario2());
			model.setComentarioRevision(cuentas.get(0).getComentarioRepPago());
		}
		
	}
	
	public Cliente getCliente(){
		return cliente;
	}
	
	public List<Cargo> getCuentas(){
		return cuentas;
	}
	
	public RevisionModel getRevision(){
		return (RevisionModel)getBaseBean();
	}
	
	public void aplicarCambios(){
		for(Cargo c:cuentas){
			c.setDiaDelPago(getRevision().getDiaCobro());
			c.setDiaRevision(getRevision().getDiaRevision());
			c.setFechaRevisionCxc(getRevision().getFechaRevision());
			c.setPlazo(getRevision().getPlazo());
			
			//if(getRevision().getCobrador()!=null)
			c.setCobrador(getRevision().getCobrador());			
			c.setRevision(getRevision().isVencimientoFechaRevision());
			c.setComentario2(getRevision().getComentarioRecepcion());
			c.setComentarioRepPago(getRevision().getComentarioRevision());
		}
	}
	
	public TableFormat<Cargo> createCXCTableFormat(){
		String props[]={"cobrador.id","diaRevision","diaDelPago","sucursal.clave","documento","numeroFiscal","fecha","vencimiento","total","saldoCalculado","plazo","atraso"};
		String names[]={"Cob","Dia Rev","Dia Cob","Suc","Docto","Fiscal","Fecha","Vto","Total","Saldo","Plazo","Atraso"};
		return GlazedLists.tableFormat(Cargo.class,props, names);
	}
	
	public static class RevisionModel {
		
		private int diaCobro=0;
		private int diaRevision=0;
		private Date fechaRevision;
		private boolean vencimientoFechaRevision;
		
		@Length(max=100)
		private String comentarioRecepcion;
		
		@Length(max=100)
		private String comentarioRevision;
		
		private int plazo=0;
		private Cobrador cobrador;
		
		public int getDiaCobro() {
			return diaCobro;
		}
		public void setDiaCobro(int diaCobro) {
			this.diaCobro = diaCobro;
		}
		
		
		
		public Date getFechaRevision() {
			return fechaRevision;
		}
		public void setFechaRevision(Date fechaRevision) {
			this.fechaRevision = fechaRevision;
		}
		public int getDiaRevision() {
			return diaRevision;
		}
		public void setDiaRevision(int diaRevision) {
			this.diaRevision = diaRevision;
		}
		public boolean isVencimientoFechaRevision() {
			return vencimientoFechaRevision;
		}
		public void setVencimientoFechaRevision(boolean vencimientoFechaRevision) {
			this.vencimientoFechaRevision = vencimientoFechaRevision;
		}
		public int getPlazo() {
			return plazo;
		}
		public void setPlazo(int plazo) {
			this.plazo = plazo;
		}
		public Cobrador getCobrador() {
			return cobrador;
		}
		public void setCobrador(Cobrador cobrador) {
			this.cobrador = cobrador;
		}
		public String getComentarioRecepcion() {
			return comentarioRecepcion;
		}
		public void setComentarioRecepcion(String comentarioRecepcion) {
			this.comentarioRecepcion = comentarioRecepcion;
		}
		public String getComentarioRevision() {
			return comentarioRevision;
		}
		public void setComentarioRevision(String comentarioRevision) {
			this.comentarioRevision = comentarioRevision;
		}
		
		
	}
	

}
