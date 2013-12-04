package com.luxsoft.siipap.cxc.ui.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.hibernate.validator.NotNull;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.Depositable;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.FichaDet;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.service.FichasFactory;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;

/**
 * Modelo para la forma de generacion de fichas de depositos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class FichasFormModel extends DefaultFormModel{
	
	
	public FichasFormModel() {
		super(Bean.proxy(FichasModel.class));
		getModel("cuenta").addValueChangeListener(new CuentaHandler());
		setValue("sucursal",ServiceLocator2.getConfiguracion().getSucursal());
		setValue("origen",OrigenDeOperacion.CRE);
		
	}
	
	public void generarFichas(final List<Pago> pagos){
		logger.info("Generando fichas para pagos: "+pagos);
		if(this.pagos==null){ 
			this.pagos=GlazedLists.eventList(FichasFactory.getInstance().buscarPendientes(pagos));
			
		}
	}
	
	public FichasModel getFichasModel(){
		return (FichasModel)getBaseBean();
	}
	
	private FunctionList<Pago, FichaDet> partidas;
	
	private EventList<Pago> pagos;
	
	public EventList<FichaDet> getPartidas(){
		if(partidas==null){
			partidas=new FunctionList<Pago, FichaDet>(pagos,new FunctionList.Function<Pago, FichaDet>(){
				public FichaDet evaluate(Pago pago) {
					Depositable dep=(Depositable)pago;
					FichaDet det=new FichaDet();
					det.setPago(pago);
					pago.setDeposito(det);
					det.setBanco(pago.getBanco());
					det.setEfectivo(dep.getEfectivo());
					det.setCheque(dep.getCheque());					
					return det;
				}
			});
		}
		return partidas;
	}
	
	private void actualizarFichas(){
		
		List<Ficha> fichasGeneradas=FichasFactory.getInstance().agrupar(getPartidas(), getFichasModel().getCuenta());
		getFichas().clear();
		getFichas().addAll(fichasGeneradas);
		setValue("depositos", getFichas().size());
		
		BigDecimal efectivo=BigDecimal.ZERO;
		BigDecimal cheque=BigDecimal.ZERO;
		for(FichaDet det:getPartidas()){
			efectivo=efectivo.add(det.getEfectivo());
			cheque=cheque.add(det.getCheque());
			
		}
		setValue("cheque",cheque);
		setValue("efectivo", efectivo);
		setValue("total", cheque.add(efectivo));
		
	}
	
	private EventList<Ficha> fichas;
	
	public EventList<Ficha> getFichas(){
		if(fichas==null){
			fichas=new BasicEventList<Ficha>();
			/*fichas=new FunctionList<FichaDet, Ficha>(getPartidas(),new FunctionList.Function<FichaDet, Ficha>(){
				public Ficha evaluate(FichaDet sourceValue) {
					return sourceValue.getFicha();
				}
			});
			fichas=new UniqueList<Ficha>(fichas,GlazedLists.beanPropertyComparator(Ficha.class, "folio"));*/
		}
		return fichas;
	}
	
	public void comit(){		
		for(Ficha f:getFichas()){
			f.setComentario(getFichasModel().getComentario());
			f.setCuenta(getFichasModel().getCuenta());
			f.setFecha(getFichasModel().getFecha());
			f.setSucursal(getFichasModel().getSucursal());
			f.setOrigen(getFichasModel().getOrigen());
			BigDecimal tot=BigDecimal.ZERO;
			for(FichaDet det:f.getPartidas()){
				det.getPago().setCuenta(getFichasModel().getCuenta());
				tot=tot.add(det.getImporte());
			}
			f.setTotal(tot);
			f.actualizarTotal();
		}
	}
	
	public void cancel(){
		for(Pago p:pagos){
			p.setDeposito(null);
		}
	}

	private class CuentaHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			actualizarFichas();
		}
	}
	
	
	
	public static class FichasModel{
		
		private Date fecha=new Date();		
		@NotNull(message="La cuenta destino es obligatoria")
		private Cuenta cuenta;		
		private Sucursal sucursal;		 
		private String comentario;		 
		private BigDecimal total;
		private BigDecimal cheque;
		private BigDecimal efectivo;
		private int depositos;
		private OrigenDeOperacion origen=OrigenDeOperacion.CRE;
				
		public Date getFecha() {
			return fecha;
		}
		public void setFecha(Date fecha) {
			this.fecha = fecha;
		}		
		public BigDecimal getTotal() {
			return total;
		}
		public void setTotal(BigDecimal total) {
			this.total = total;
		}
		public BigDecimal getCheque() {
			return cheque;
		}
		public void setCheque(BigDecimal cheque) {
			this.cheque = cheque;
		}
		public BigDecimal getEfectivo() {
			return efectivo;
		}
		public void setEfectivo(BigDecimal efectivo) {
			this.efectivo = efectivo;
		}
		public int getDepositos() {
			return depositos;
		}
		public void setDepositos(int depositos) {
			this.depositos = depositos;
		}
		public Cuenta getCuenta() {
			return cuenta;
		}
		public void setCuenta(Cuenta cuenta) {
			this.cuenta = cuenta;
		}		
		public String getComentario() {
			return comentario;
		}
		public void setComentario(String comentario) {
			this.comentario = comentario;
		}
		public Sucursal getSucursal() {
			return sucursal;
		}
		public void setSucursal(Sucursal sucursal) {
			this.sucursal = sucursal;
		}
		public OrigenDeOperacion getOrigen() {
			return origen;
		}
		public void setOrigen(OrigenDeOperacion origen) {
			this.origen = origen;
		}		
		
	}
}
