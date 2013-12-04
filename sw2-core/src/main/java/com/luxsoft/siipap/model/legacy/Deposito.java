package com.luxsoft.siipap.model.legacy;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;


public class Deposito extends BaseBean{
	
	private Long id;	
	
	private Integer sucursal;
	
	private int sucursalId;
	
	private String origen;
	
	private String formaDePago;
	
	private Date fecha;
	
	private Date fechaCobranza;
	
	private CantidadMonetaria importe;
	
	private String cuentaDestino;
	
	private String banco;
	
	private String cuenta;
	
	private boolean revisada=false;
	
	private int folio;
	
	private Set<DepositoUnitario> partidas=new HashSet<DepositoUnitario>();
	

	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		Object oldValue=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", oldValue, fecha);
	}
	

	public String getOrigen() {
		return origen;
	}
	public void setOrigen(String origen) {
		this.origen = origen;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CantidadMonetaria getImporte() {
		return importe;
	}

	public void setImporte(CantidadMonetaria importe) {
		Object oldValue=this.importe;
		this.importe = importe;
		firePropertyChange("importe", oldValue, importe);
	}	
	
	public String getFormaDePago() {
		return formaDePago;
	}
	public void setFormaDePago(String formaDePago) {
		this.formaDePago = formaDePago;
	}
	
	public Collection<DepositoUnitario> getPartidas() {
		return Collections.unmodifiableCollection(partidas);
	}
	
	public boolean agregarPartida(final DepositoUnitario du){
		du.setDeposito(this);
		return partidas.add(du);
	}
	
	public boolean eliminarPartida(final DepositoUnitario du){
		boolean res=partidas.remove(du);
		if(res)
			du.setDeposito(null);
		return res;
	}
	
	public Integer getSucursal() {
		return sucursal;
	}
	public void setSucursal(Integer sucursal) {
		this.sucursal = sucursal;
	}
	
	public String getCuentaDestino() {
		return cuentaDestino;
	}
	public void setCuentaDestino(String cuentaDestino) {
		Object oldValue=this.cuentaDestino;
		this.cuentaDestino = cuentaDestino;
		firePropertyChange("cuentaDestino", oldValue, cuentaDestino);
	}
	
	public void actualizarImporte(){
		BigDecimal imp=BigDecimal.ZERO;
		for(DepositoUnitario d:getPartidas()){
			imp=imp.add(d.getImporte());
		}
		setImporte(CantidadMonetaria.pesos(imp.doubleValue()));
	}
	
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Deposito other = (Deposito) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;	
	}	
	
	
	
	public String toString(){		
		//return ToStringBuilder
		//.reflectionToString(this,ToStringStyle.SHORT_PREFIX_STYLE);
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)		
		.append(getFecha())
		.append(getBanco())
		.append("Cuenta:",getCuenta())
		.append("Importe:",getImporteAsDouble())
		.toString();
	}
	
	public String getCuenta() {
		return cuenta;
	}
	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}
	
	
	public String getBanco() {
		return banco;
	}
	public void setBanco(String banco) {
		this.banco = banco;
	}
	
	public void resolverCuenta(){
		if(!StringUtils.isEmpty(getCuentaDestino())){
			String cta=getCuentaDestino();
			int index=cta.indexOf('(');
			String banco=cta.substring(0, index-1).trim();
			int index2=cta.indexOf(')');
			String cuenta=cta.substring(index+1,index2).trim();
			setBanco(banco);
			setCuenta(cuenta);
		}
		
	}
	
	public static final int MAXIMO_CHEQUES=5;
	
	@SuppressWarnings("unchecked")
	public void agrupar(){
		final Set<DepositoUnitario> registros=new HashSet<DepositoUnitario>();
		registros.addAll(getPartidas());
		
		final Collection<DepositoUnitario> locales=CollectionUtils.select(registros, new Predicate(){
			public boolean evaluate(Object object) {
				DepositoUnitario du=(DepositoUnitario)object;
				return du.getBanco().equalsIgnoreCase(getBanco());
			}			
		});
		final Collection<DepositoUnitario> foraneos=CollectionUtils.select(registros, new Predicate(){
			public boolean evaluate(Object object) {				
				DepositoUnitario du=(DepositoUnitario)object;
				return !du.getBanco().equalsIgnoreCase(getBanco());
			}			
		});
		int last=agrupar(locales,0);
		agrupar(foraneos,last);
	}
	
	public int agrupar(final Collection<DepositoUnitario> registros,int start){
		int buff=0;
		int grupo=start;
		for(DepositoUnitario d:registros){
			if(buff++%5==0){
				grupo++;
			}
			d.setGrupo(grupo);
		}
		return grupo;
	}
	
	public int getSucursalId() {
		return sucursalId;
	}
	public void setSucursalId(int sucursalId) {
		this.sucursalId = sucursalId;
	}
	
	public String getCuentaContable(){
		String cta=getBanco();
		if(cta.startsWith("BANCOMER")){
			return "102-0001-000";
		}else if(cta.startsWith("BANAMEX")){
			return "102-0002-000";
		}else if(cta.startsWith("HSBC")){
			return "102-0004-000";
		}else if(cta.startsWith("SCOTTIA")){
			return "102-0005-000";
		}else if(cta.startsWith("SANTANDER")){
			return "102-0008-000";
		}else
			return "ERRROR";
	}
	
	public String getConcepto(){
		String pattern="{0}{1} {2} ";
		return MessageFormat.format(pattern, ""
				,getBanco()
				,getId()!=null?"Dep:"+getId():"F.P: "+StringUtils.substring(getFormaDePago().toString(), 0,7));
	}
	
	public CantidadMonetaria getImporteMN(){
		return CantidadMonetaria.pesos(getImporte().amount().doubleValue());
	}
	
	public double getImporteAsDouble(){
		return getImporte().amount().doubleValue();
	}
	public int getFolio() {
		return folio;
	}
	public void setFolio(int folio) {
		this.folio = folio;
	}
	public Date getFechaCobranza() {
		/*if(!origen.equals("CON")){
			getFecha();
		}}
		*/
		return fechaCobranza;
	}
	public void setFechaCobranza(Date fechaCobranza) {
		this.fechaCobranza = fechaCobranza;
	}
	public boolean isRevisada() {
		return revisada;
	}
	public void setRevisada(boolean revisada) {
		boolean old=this.revisada;
		this.revisada = revisada;
		firePropertyChange("revisada", old, revisada);
	}
	
	

}
