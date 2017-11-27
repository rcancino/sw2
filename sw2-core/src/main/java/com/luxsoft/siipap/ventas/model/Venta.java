/*
 *  Copyright 2008 Ruben Cancino Ramos.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.luxsoft.siipap.ventas.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.replica.Replicable;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoDet;
import com.luxsoft.sw3.ventas.Pedido.FormaDeEntrega;

/**
 * Bean para representar lo que es una venta
 * Su estructura es simple y limitada. El comportamiento
 * general de la venta esta delineado por reglas de negocios
 * que no es conveniente implementar en este componente
 *  
 * 
 * @author Ruben Cancino Ramos
 */
@Entity
@DiscriminatorValue("FAC")
public class Venta extends Cargo implements Replicable{
	
	@ManyToOne(optional = true,fetch=FetchType.LAZY)			
	@JoinColumn(name = "PEDIDO_ID",nullable=true,insertable=true,updatable=false)
	private Pedido pedido;
   
	//@Embedded
    //@Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
	@Transient
    private Direccion direccion=new Direccion();
   
    @ManyToOne (optional=true,cascade= {CascadeType.MERGE , CascadeType.PERSIST},fetch=FetchType.LAZY)
    @JoinColumn (name="SOCIO_ID")
    private Asociado socio;
    
    @ManyToOne(optional = true,fetch=FetchType.LAZY)			
	@JoinColumn(name = "VENDEDOR_ID",nullable=true)
	private Vendedor vendedor;    
   
    
    @Embedded
    private VentaCredito credito=new VentaCredito();
    
   
    /** Propiedades por compatibilidad con siipap dbf 
     *  solo existen para las ventas importadas**/
    
    
    @Column(name="SERIE_SIP",length=1)
    private String serieSiipap;
    
    @Column(name="TIPO_SIP",length=1)
    private String tipoSiipap;
    
    
    
    @OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch= FetchType.LAZY			
				)
	@JoinColumn(name="VENTA_ID",nullable=false)
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN,org.hibernate.annotations.CascadeType.REPLICATE})
	@IndexColumn(name="IDX",base=1)
	private List<VentaDet> partidas=new ArrayList<VentaDet>();
    
    //@OneToOne(mappedBy="venta",cascade={CascadeType.MERGE,CascadeType.PERSIST})
    //private CancelacionDeVenta cancelada;
    
    @Column(name="SURTIDOR",length=100)
    private String surtidor;
    
    @Column(name="ORIGEN_IMPORTACION",nullable=true,length=6)
    private String importacion;
    
    @Column(name="CANCELACION_DBF")
    private Boolean cancelacionDBF=false;
    
    @Column(name="CANCELACION_DBF_COM" ,length=70)
    private String comentarioCancelacionDBF;
    
    @Column(name = "FLETE", nullable = false,columnDefinition=" DECIMAL(19,2) default 0")
	private BigDecimal flete = BigDecimal.ZERO;
    
    @Column(name="CE",nullable=false,columnDefinition=" bit default false")
    private boolean contraEntrega=false;
    
    @Enumerated(EnumType.STRING)
	@Column(name = "FPAGO", nullable = true, length = 25)
	private FormaDePago formaDePago=FormaDePago.EFECTIVO;
    
    @Column(name="PEDIDO_CREADO",nullable=true)
    //@Transient
    private Date pedidoCreado;
   
    
    
    @ManyToOne(optional = true,fetch=FetchType.LAZY,cascade={CascadeType.MERGE,CascadeType.PERSIST})			
	@JoinColumn(name = "AUT_SIN_EXIS_ID")
    private AutorizacionParaFacturarSinExistencia autorizacionSinExistencia;

    public Venta() {
    }
    
    public Direccion getDireccion() {
		return direccion;
	}
	public void setDireccion(Direccion direccion) {
		this.direccion = direccion;
	}

	public Venta(Pedido pedido) {
    	setPedido(pedido);
    }

    public Venta(Cliente cliente) {
        super(cliente);
    }
    
    public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		//if(getId()!=null)
			//throw new RuntimeException("La factura ya esta persistida por lo que no es posible re asignar el pedido");
		this.pedido = pedido;
		if(pedido==null)
			return;
		setCliente(pedido.getCliente());
		setDireccion(pedido.getCliente().getDireccionFiscal());
		setNombre(pedido.getNombre());
		setSucursal(pedido.getSucursal());
		setCobrador(pedido.getCliente().getCobrador());
		
		setVendedor(pedido.getCliente().getVendedor());
		if(pedido.getSocio()!=null && pedido.getSocio().getVendedor()!=null)
			setVendedor(pedido.getSocio().getVendedor());
		setComentario(pedido.getComentario());
		setComentario2(pedido.getComentario2());		
		setDescuentoGeneral(pedido.getDescuento());
		setFecha(pedido.getFecha());
		setSurtidor(pedido.getSurtidor());
		//setImporte(pedido.getSubTotal());
		//setImpuesto(pedido.getImpuesto());
		//setTotal(pedido.getTotal());
		
		setMoneda(pedido.getMoneda());
		OrigenDeOperacion origen;
		
		if(pedido.isDeCredito()){
			//Cuando el pedido es de credito la unica opcion es Factura de credito
			origen=OrigenDeOperacion.CRE;
			setSerieSiipap("E");
			
		}else{
			if(pedido.getInstruccionDeEntrega()!=null){
				origen=OrigenDeOperacion.CAM;
				setSerieSiipap("C");
			}else{
				origen=OrigenDeOperacion.MOS;
				setSerieSiipap("A");
			}
		}
		
		setOrigen(origen);
		setPrecioBruto(false);
		//Si el pedido es con cheque post fechado
		if(pedido.getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO)){
			setPostFechado(true);
			setOrigen(OrigenDeOperacion.CRE);
		}
		
		setTc(pedido.getTc());
		setTipoSiipap("Q");
		if(pedido.isDeCredito()){
			VentaCredito credito=new VentaCredito();
			credito.setVenta(this);			
			setCredito(credito);
			setPlazo(pedido.getCliente().getPlazo());
			setDiaRevision(pedido.getCliente().getCredito().getDiarevision());
			setDiaDelPago(pedido.getCliente().getCredito().getDiacobro());
			setRevision(!pedido.getCliente().getCredito().isVencimientoFactura());
			//setComentario(rs.getString("BCOMENTARIO"));
			//setComentarioRepPago(rs.getString("BCOMENTARIO2"));
			//setDiaPago(pedido.getCliente().getCredito().getDiacobro());
			//setFechaRecepcionCXC(rs.getDate("FECHARECEPCIONCXC"));
			//setFechaRevision(rs.getDate("FECHAREVISION"));
			//setFechaRevisionCxc(rs.getDate("FECHAREVISIONCXC"));
			//setNumeroFiscal(rs.getInt("NUMEROFISCAL"));
			
			//setReprogramarPago(rs.getDate("REPROGRAMARPAGO"));
			//setRevisada(rs.getBoolean("REVISADA"));
			//setRevision(rs.getBoolean("REVISION"));
		}
		setFormaDePago(pedido.getFormaDePago());
		setSocio(pedido.getSocio());
		setContraEntrega(pedido.isContraEntrega());
		
		/*
		 * parametros.put("ENVIO", 		venta.getPedidoFormaDeEntrega().equals("LOCAL")?"PASAN":"ENVIO");
		parametros.put("PEDIDO", 		venta.getPedidoFolio());
		parametros.put("IP", 			venta.getPedidoCreatedIp());
		parametros.put("ELAB_VTA",		venta.getPedidoCreateUser());
		parametros.put("PUESTO", 		venta.getPuesto()?"**PUESTO**":"");
		parametros.put("DIR_ENTREGA", 	venta.getInstruccionDeEntrega());
		 */
		setPedidoFormaDeEntrega(pedido.getEntrega()!=null?pedido.getEntrega().name():FormaDeEntrega.LOCAL.name());
		setPedidoFolio(pedido.getFolio());
		setPedidoCreatedIp(pedido.getAddresLog().getCreatedIp());
		setPedidoCreateUser(pedido.getLog().getCreateUser());
		setPuesto(pedido.isPuesto());
		if(pedido.getSocio()!=null){
			if(pedido.isMismaDireccion()){
				setInstruccionDeEntrega(StringUtils.abbreviate(pedido.getSocio().getDireccion(),255));
			}else{
				setInstruccionDeEntrega(pedido.getInstruccionDeEntrega()!=null
						?StringUtils.abbreviate(pedido.getInstruccionDeEntrega().oneLineString(),255)
						:null);
			}
		}else{
			setInstruccionDeEntrega(pedido.getInstruccionDeEntrega()!=null
					?StringUtils.abbreviate(pedido.getInstruccionDeEntrega().oneLineString(),255)
					:null);
		}		
		setMisma(pedido.isMismaDireccion());
		setAnticipo(pedido.isAnticipo());
		setMoneda(pedido.getMoneda());
		setPedidoCreado(pedido.getLog().getCreado());
		
	}
    
    public VentaCredito getCredito() {
		return credito;
	}

	public void setCredito(VentaCredito credito) {
		this.credito = credito;
	}	

	
	
    public FormaDePago getFormaDePago() {
		return formaDePago;
	}

	public void setFormaDePago(FormaDePago formaDePago) {
		this.formaDePago = formaDePago;
	}

	public Asociado getSocio() {
		return socio;
	}

	public void setSocio(Asociado socio) {
		this.socio = socio;
	}

	
	public String getSerieSiipap() {
		return serieSiipap;
	}

	public void setSerieSiipap(String serie) {
		this.serieSiipap = serie;
	}

	public String getTipoSiipap() {
		return tipoSiipap;
	}

	public void setTipoSiipap(String tipo) {
		this.tipoSiipap = tipo;
	}

	public String getSurtidor() {
		return surtidor;
	}

	public void setSurtidor(String surtidor) {
		this.surtidor = surtidor;
	}
	
	
	public boolean isContraEntrega() {
		return contraEntrega;
	}

	public void setContraEntrega(boolean contraEntrega) {
		this.contraEntrega = contraEntrega;
	}

	public BigDecimal getFlete() {
		if(flete==null)
			return BigDecimal.ZERO;
		return flete;
	}

	public void setFlete(BigDecimal flete) {
		this.flete = flete;
	}

	/*** Manejo de collection de partidas ***/

    public List<VentaDet> getPartidas() {
        return partidas;
    }
	public void setPartidas(List<VentaDet> partidas) {
		this.partidas = partidas;
	}



	public boolean agregarPartida(final VentaDet det){ 
        Assert.notNull(getMoneda(),"No puede agregar partidas sin antes definir la moneda");
        det.setVenta(this);
        det.setSucursal(getSucursal());
        return partidas.add(det);
        
    }
	
	
    public boolean eliminarPartida(final VentaDet det){
        det.setVenta(null);
        return partidas.remove(det);
    }

	@Override
	public String getTipoDocto() {
		return "FAC";
	}

	public Vendedor getVendedor() {
		return vendedor;
	}

	public void setVendedor(Vendedor vendedor) {
		this.vendedor = vendedor;
	}
	
	/**
	 * Regresa la provision para descuentos.
	 * Esta se calcula a partir de las partidas
	 * 
	 */
	public BigDecimal getProvision(){
		BigDecimal provision=BigDecimal.ZERO;
		for(VentaDet det:partidas){
			provision=provision.add(det.getProvision());
		}
		return provision;
	}
	
	/**
	 * Obtiene el descuento general de la venta segun
	 * lo pactado en las partidas
	 * 
	 */
	public double getDescuento(){
		BigDecimal prov=getProvision();
		BigDecimal tot=getImporte();
		if(tot.doubleValue()>0){
			BigDecimal res=prov.divide(tot,4,RoundingMode.HALF_EVEN);
			return res.doubleValue();
		}
		return 0;
	}
	
	/**
	 * El descuento aplicable en nota de credito
	 * 
	 * Solo util para las ventas creditio de precio bruto
	 * 
	 * @return
	 */
	public double getDescuentoNota(){
		if(isPrecioBruto()||(getDescuentoFinanciero()>0)){
			/*BigDecimal provision=getProvisionable();
			if(provision!=null){
				BigDecimal tot=getImporte();
				BigDecimal res=provision.divide(tot,4,RoundingMode.HALF_EVEN);
				return res.doubleValue()*100;
			}*/
			return getDescuentoGeneral()*100;
		}
		return 0;
	}

	/**
	 * Comodity para saver si una venta tiene posiblidad de ser devuelta
	 * 
	 * 
	 * @return
	 */
	public double getPorDevolver(){
		return getTotal().subtract(getDevoluciones()).doubleValue();
	}

	/**
	 * Indica el origen de esta entidad si es que fue importada 
	 * 
	 * @return
	 */
	public String getImportacion() {
		return importacion;
	}

	public void setImportacion(String importacion) {
		this.importacion = importacion;
	}

	public Boolean getCancelacionDBF() {
		if(cancelacionDBF==null)
			cancelacionDBF=Boolean.FALSE;
		return cancelacionDBF;
	}

	public void setCancelacionDBF(Boolean cancelacionDBF) {
		this.cancelacionDBF = cancelacionDBF;
	}

	public String getComentarioCancelacionDBF() {
		return comentarioCancelacionDBF;
	}

	public void setComentarioCancelacionDBF(String motivoCancelacionDBF) {
		this.comentarioCancelacionDBF = motivoCancelacionDBF;
	}
	
	public BigDecimal getSubTotal(){
		CantidadMonetaria importe=getImporteCM();
		CantidadMonetaria cargos=new CantidadMonetaria(getCargos(),getMoneda());
		return importe.subtract(cargos).amount();
		
	}
	
	public BigDecimal getSubTotal2(){
		CantidadMonetaria importe=getImporteCM();
		CantidadMonetaria cargos=new CantidadMonetaria(getCargos(),getMoneda());
		CantidadMonetaria flete=new CantidadMonetaria(getFlete(),getMoneda());
		CantidadMonetaria cortes=new CantidadMonetaria(0,getMoneda());
		for(VentaDet det:getPartidas()){
			cortes=cortes.add(new CantidadMonetaria(det.getImporteCortes(),getMoneda()));
		}
		
		CantidadMonetaria subtotal2=importe
			.subtract(cargos)
			.subtract(flete)
			.subtract(cortes);
		
		return subtotal2.amount();
		
	}
	
	public BigDecimal getImporteCortes(){
		CantidadMonetaria cortes=new CantidadMonetaria(0,getMoneda());
		for(VentaDet det:getPartidas()){
			cortes=cortes.add(new CantidadMonetaria(det.getImporteCortes(),getMoneda()));
		}
		return cortes.amount();
		
	}
	
	public BigDecimal getImporteDescuento(){
		CantidadMonetaria importe=new CantidadMonetaria(BigDecimal.ZERO,getMoneda());
		for(VentaDet det:getPartidas()){
			importe=importe.add(new CantidadMonetaria(det.getImporteDescuento(),getMoneda()));
		}
		return importe.amount();
		
	}
	
	public BigDecimal getImporteBruto(){
		//CantidadMonetaria importe=CantidadMonetaria.pesos(0);
		CantidadMonetaria importe=new CantidadMonetaria(BigDecimal.ZERO,getMoneda());
		for(VentaDet det:getPartidas()){
			importe=importe.add(new CantidadMonetaria(det.getImporte(),getMoneda()));
		}
		return importe.amount();	
	}
	
	static Date vigenciaDF=DateUtil.endOfDay(DateUtil.toDate("31/03/2009")); 

	/**
	 * 
	 * 
	 */
	public double getDescuentoFinanciero(){
		if(getDescuentos().abs().doubleValue()>0)
			return 0;
		if(getCliente().getCredito()!=null){
			if(getCliente().getCredito().isConNotaAnticipada())
				return 0;
			if(getCliente().getCredito().isVencimientoFactura()){
				if(isPrecioNeto()){
					if(getAtraso()<=0){
						if(getFecha().after(vigenciaDF)){
							if(getCliente().getCredito().getPlazo()<=30){
								Date fecha=DateUtil.truncate(new Date(),Calendar.DATE);
								int tolerancia=-getToleranciaDF(fecha);
								fecha =DateUtils.addDays(fecha, tolerancia);
								Date today=DateUtil.startOfDay(fecha);
								int days=DateUtil.getDaysDiff(today.getTime(), getFecha().getTime());
								if(days<=15)
									return 3.0d;
								else if(days>15 && days<=30)
									return 2.0d;
							}
						}						
					}
				}				
			}
		}
		return 0;
	}
	
	public boolean isCancelado(){
		return getCancelacion()!=null;
	}
	
	public String getFacturista(){
		return getLog()!=null?getLog().getCreateUser():"ND";
	}
	
	public double getDescuentoGlobal(){		
		for(VentaDet det:getPartidas()){
			if(det.getProducto().getModoDeVenta().equals("N") && det.getVenta().getFormaDePago().CHEQUE_POSTFECHADO.equals(true) && !det.getVenta().getOrigen().equals("CRE") )
				return 0.0;
			else
				return det.getDescuento();
		}
		return 0;
	}
	
	public AutorizacionParaFacturarSinExistencia getAutorizacionSinExistencia() {
		return autorizacionSinExistencia;
	}

	public void setAutorizacionSinExistencia(
			AutorizacionParaFacturarSinExistencia autorizacionSinExistencia) {
		this.autorizacionSinExistencia = autorizacionSinExistencia;
	}



	public static int getToleranciaDF(final Date fecha){
		
		int dia=DateUtil.getDayOfWeek(fecha.getTime());
		
		int tolerancia=0;
		
		switch (dia) {
		case Calendar.MONDAY:
			tolerancia=2;
			break;
		case Calendar.TUESDAY:
		case Calendar.WEDNESDAY:
		case Calendar.THURSDAY:
		case Calendar.FRIDAY:
		case Calendar.SATURDAY:
		case Calendar.SUNDAY:
			tolerancia=1;
			break;
		default:
			break;
		}
		
		return tolerancia;
	}
	
	
	 /** Atributos del Pedido que se quieren transferir a la factura**/
	
	
	@Column(name = "PEDIDO_FENTREGA", length = 20)	
	private String pedidoFormaDeEntrega;
	
	@Column(name="PEDIDO_FOLIO")
	private Long pedidoFolio;
	
	@Column(name="PEDIDO_IP_CREATED" ,length=50)
	private String pedidoCreatedIp;
	
	@Column(name="PEDIDO_CREADO_USERID")
	private String pedidoCreateUser;
	
	@Column(name="KILOS",scale=3)
	private Double kilos;
	 
	@Column(name="PUESTO")
	private Boolean puesto=false;
	
	@Column(name="INSTRUCCION_ENTREGA")
	private String instruccionDeEntrega;
	
	@Column(name="MISMA")
	private Boolean misma=false;

	

	public String getPedidoFormaDeEntrega() {
		if(StringUtils.isBlank(pedidoFormaDeEntrega))
			pedidoFormaDeEntrega="LOCAL";
		return pedidoFormaDeEntrega;
	}

	public void setPedidoFormaDeEntrega(String pedidoFormaDeEntrega) {
		this.pedidoFormaDeEntrega = pedidoFormaDeEntrega;
	}

	public Long getPedidoFolio() {
		return pedidoFolio;
	}

	public void setPedidoFolio(Long pedidoFolio) {
		this.pedidoFolio = pedidoFolio;
	}

	public String getPedidoCreatedIp() {
		return pedidoCreatedIp;
	}

	public void setPedidoCreatedIp(String pedidoCreatedIp) {
		this.pedidoCreatedIp = pedidoCreatedIp;
	}

	public String getPedidoCreateUser() {
		return pedidoCreateUser;
	}

	public void setPedidoCreateUser(String pedidoCreateUser) {
		this.pedidoCreateUser = pedidoCreateUser;
	}

	public Double getKilos() {
		double kilos=0;
		for(VentaDet det:partidas){
			if(det!=null)
				kilos+=det.getKilosCalculados()*-1;
		}
		return kilos;
	}

	public void setKilos(Double kilos) {
		this.kilos = kilos;
	}

	public Boolean getPuesto() {
		if(puesto==null)
			puesto=Boolean.FALSE;
		return puesto;
	}

	public void setPuesto(Boolean puesto) {
		this.puesto = puesto;
	}

	public Boolean getMisma() {
		if(misma==null)
			return false;
		return misma;
	}

	public void setMisma(Boolean misma) {
		this.misma = misma;
	}
	
	public String getInstruccionDeEntrega() {
		return instruccionDeEntrega;
	}

	public void setInstruccionDeEntrega(String instruccionDeEntrega) {
		this.instruccionDeEntrega = instruccionDeEntrega;
	}

	
	public CantidadMonetaria getCargosPorInteresesMoratorios(){
		if(getOrigen().equals(OrigenDeOperacion.CAM)){
			double car=getCargosPorAplicarCamioneta()/100;
			CantidadMonetaria imp=new CantidadMonetaria(getTotal().subtract(getDevoluciones()),getMoneda());
			return imp.multiply(car);
		}
		return CantidadMonetaria.pesos(0);
	}
	
	private double getCargosPorAplicarCamioneta(){
		double pena=1.0;
		int atraso=0;
		int PLAZO_TOLERADO=4;
		Date today=new Date();		
		long res=today.getTime()-getFecha().getTime();
		if(res>0){			
			long dias=(res/(86400*1000));			
			atraso=((int)dias-PLAZO_TOLERADO);
		}
		if(atraso<=0 || (getCargosAplicados().doubleValue()>0))
			return 0;
		int semanas=atraso/7;
		return ((double)semanas)*pena;
	}
	
	
	@Column(name="ANTICIPO")
	private boolean anticipo=false;


	public boolean isAnticipo() {
		return anticipo;
	}

	public void setAnticipo(boolean anticipo) {
		this.anticipo = anticipo;
	}
    
	@Formula("(select ifnull(sum(X.APLICADO),0) FROM SX_ANTICIPOS_APLICADOS X where X.ORIGEN_ID=CARGO_ID)")
	private BigDecimal anticiposAplicados=BigDecimal.ZERO;
	
	public BigDecimal getDisponibleDeAnticipo(){
		if(isAnticipo()){
			CantidadMonetaria aplic=new CantidadMonetaria(anticiposAplicados,getMoneda());
			return getTotalCM().subtract(aplic).amount();
		}else
			return BigDecimal.ZERO;
	}
	
	@Column(name="ANTICIPO_APLICADO")
	private BigDecimal anticipoAplicado=BigDecimal.ZERO;

	public BigDecimal getAnticipoAplicado() {
		if(anticipoAplicado==null)
			anticipoAplicado=BigDecimal.ZERO;
		return anticipoAplicado;
	}

	public void setAnticipoAplicado(BigDecimal anticipoAplicado) {
		this.anticipoAplicado = anticipoAplicado;
	}
	

	public BigDecimal getTotalConAnticipo(){
		return getTotal().add(getAnticipoAplicado());
	}

	
	
	 @Formula("(select ifnull(X.IMPORTE * X.TC,0) FROM SX_VENTAS X where X.CARGO_ID=CARGO_ID)")
	private BigDecimal importeporTc;
	 
	 @Formula("(select ifnull(X.IMPUESTO * X.TC,0) FROM SX_VENTAS X where X.CARGO_ID=CARGO_ID)")
	private BigDecimal impuestoporTc;
	 
	 @Formula("(select ifnull(X.TOTAL * X.TC,0) FROM SX_VENTAS X where X.CARGO_ID=CARGO_ID)")
	private BigDecimal totalporTc;
	 
	 @Formula("(select ifnull(X.ANTICIPO_APLICADO * X.TC,0) FROM SX_VENTAS X where X.CARGO_ID=CARGO_ID)")
	private BigDecimal antAplicporTc;
	 
	 @Formula("(select MAX(X.CREADO) FROM SX_CXC_APLICACIONES X  where X.CARGO_ID=CARGO_ID  )")  // se cambio select MAX(X.CAR_ORIGEN) FROM SX_CXC_APLICACIONES X where X.ABONO_ID=ABONO_ID
	private Date ultimoPago;

	public Date getPedidoCreado() {
		return pedidoCreado;
	}

	public void setPedidoCreado(Date pedidoCreado) {
		this.pedidoCreado = pedidoCreado;
	}

	public Date getUltimoPago() {
		return ultimoPago;
	}

	public void setUltimoPago(Date ultimoPago) {
		this.ultimoPago = ultimoPago;
	}
	
	public double getKilosCalculados(){
		double res=0;
		for(VentaDet det:partidas){
			res+=det.getKilos();
		}
		return Math.abs(res);
	}
	
	//@Column(name="CFDI_TIMBRADO")
	@Formula("(select X.TIMBRADO FROM SX_CFDI X where X.ORIGEN_ID=CARGO_ID)")
	//@Transient
	private Date timbrado;
	
	//@Column(name="CFDI")
	@Formula("(select X.CFD_ID FROM SX_CFDI X where X.ORIGEN_ID=CARGO_ID)")
	//@Transient
	private String cfdi;

	public Date getTimbrado() {
		return timbrado;
	}

	public void setTimbrado(Date timbrado) {
		this.timbrado = timbrado;
	}

	public String getCfdi() {
		return cfdi;
	}

	public void setCfdi(String cfdi) {
		this.cfdi = cfdi;
	}
     
	
}
