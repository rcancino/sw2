package com.luxsoft.siipap.compras.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.replica.Replicable;

@Entity
@Table(name="SX_LP_PROVS")
public class ListaDePrecios extends BaseBean implements Replicable{
	
	@TableGenerator(
            name="idsGen", 
            table="SX_TABLE_IDS", 
            pkColumnName="GEN_KEY", 
            valueColumnName="GEN_VALUE", 
            pkColumnValue="LP_PROVEEDORES_ID", 
            initialValue=50,
            allocationSize=1)
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="idsGen")
	private Long id;
	
	@Version
	private int version;
	
	@ManyToOne(optional=false,fetch=FetchType.EAGER)
	@JoinColumn(name="PROVEEDOR_ID",nullable=false,updatable=false)
	@NotNull(message="El proveedor es mandatorio")
	private Proveedor proveedor;
	
	
	@Column(name="FECHA_INI",nullable=false)
	@NotNull
	private Date fechaInicial=new Date();
	
	@Column(name="FECHA_FIN",nullable=false)
	@NotNull
	private Date fechaFinal=new Date();
	
	@Column(name="DESCRIPCION")
	@Length(max=200)
	private String descripcion;
	
	@Column(name="VIGENTE",nullable=false)
	@NotNull
	private boolean vigente=true;
	
	@Column(name="DESC_F",nullable=false,scale=6,precision=4)
	@NotNull
	private double descuentoFinanciero=0;
	
	@Column(name="CARGO1",nullable=false,scale=6,precision=4)
	@NotNull
	private Double cargo1=0d;
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY,mappedBy="lista")
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN
			,org.hibernate.annotations.CascadeType.REPLICATE})
	private Set<ListaDePreciosDet> precios =new HashSet<ListaDePreciosDet>();
	
	private Long oldId;
	
	@Embedded
	private UserLog log=new UserLog();
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;
	
	public ListaDePrecios(){}
	
	public ListaDePrecios(Date fechaInicial,Date fechaFinal) {
		this.fechaInicial = fechaInicial;
		this.fechaFinal = fechaFinal;
		Assert.isTrue(validarPeriodo(),"Periodo incorrecto");
	}
	
	public ListaDePrecios(Periodo p){
		this(p.getFechaInicial(),p.getFechaFinal());
	}

	public static ListaDePrecios createLista(){
		return new ListaDePrecios(Periodo.getPeriodoDelMesActual());
	}


	public Long getId() {
		return id;
	}
	
	

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		Object old=this.proveedor;
		this.proveedor = proveedor;
		firePropertyChange("proveedor", old, proveedor);
		if((proveedor!=null) && (proveedor.getDescuentoFinanciero()>0))
			setDescuentoFinanciero(proveedor.getDescuentoFinanciero());
	}

	public Date getFechaInicial() {
		return fechaInicial;
	}
	public void setFechaInicial(Date fechaInicial) {
		Object old=this.fechaInicial;
		this.fechaInicial = fechaInicial;
		firePropertyChange("fechaInicial", old, fechaInicial);
	}

	public Date getFechaFinal() {
		return fechaFinal;
	}

	public void setFechaFinal(Date fechaFinal) {
		Object old=this.fechaFinal;
		this.fechaFinal = fechaFinal;
		firePropertyChange("fechaFinal", old, fechaFinal);
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		Object old=this.descripcion;
		this.descripcion = descripcion;
		firePropertyChange("descripcion", old, descripcion);
	}

	public boolean isVigente() {
		return vigente;
	}

	public void setVigente(boolean vigente) {
		boolean old=this.vigente;
		this.vigente = vigente;
		firePropertyChange("vigente", old, vigente);
	}
	
	
	
	public Double getCargo1() {
		return cargo1;
	}

	public void setCargo1(Double cargo1) {
		Object old=this.cargo1;
		this.cargo1 = cargo1;
		firePropertyChange("cargo1", old, cargo1);
		if(!precios.isEmpty()){
			for(ListaDePreciosDet det:precios){
				det.setCargo1(cargo1);
			}
		}
	}

	public UserLog getLog() {
		return log;
	}
	public void setLog(UserLog log) {
		this.log = log;
	}

	/*** ****/
	public ListaDePreciosDet buscarPrecio(final Producto p){
		ListaDePreciosDet res= (ListaDePreciosDet)CollectionUtils.find(precios, new Predicate(){
			public boolean evaluate(Object object) {
				ListaDePreciosDet o=(ListaDePreciosDet)object;
				return o.getProducto().equals(p);
			}
			
		});
		return res;
	}
	
	public double getDescuentoFinanciero() {
		return descuentoFinanciero;
	}

	public void setDescuentoFinanciero(double descuentoFinanciero) {
		double old=this.descuentoFinanciero;
		this.descuentoFinanciero = descuentoFinanciero;
		firePropertyChange("descuentoFinanciero", old, descuentoFinanciero);
		if(!precios.isEmpty()){
			for(ListaDePreciosDet det:precios){
				det.setDescuentoFinanciero(descuentoFinanciero);
			}
		}
	}

	/***** Coleccion de precios ******/
	public Set<ListaDePreciosDet> getPrecios() {
		return precios;
	}
	
	public boolean agregarPrecio(final ListaDePreciosDet det){
		det.setLista(this);
		return precios.add(det);
	}
	public boolean eliminarPrecio(final ListaDePreciosDet det){
		det.setLista(null);
		return precios.remove(det);
	}
	
	/**
	 * Elimina todos los precios de la lista
	 */
	public void eliminarPrecios(){
		precios.clear();
	}
	
	@AssertTrue(message="Periodo incorrecto")
	public boolean validarPeriodo(){
		return fechaInicial.compareTo(fechaFinal)<=0;
	}
	
	/*** toString, hashCode equals *******/
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append(getId())
		.append(getProveedor())
		.append(DateUtil.convertDateToString(getFechaInicial()))
		.append(DateUtil.convertDateToString(getFechaFinal()))
		.append(isVigente())
		.append(getDescripcion())
		.toString();
	}
 
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(id)
		.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(obj==this) return true;
		if (getClass() != obj.getClass())
			return false;
		ListaDePrecios other=(ListaDePrecios)obj;
		return new EqualsBuilder()
		.append(id, other.getId())		
		.isEquals();
	}

	public Long getOldId() {
		return oldId;
	}

	public void setOldId(Long oldId) {
		this.oldId = oldId;
	}

	
	public Periodo periodo(){
		return new Periodo(fechaInicial,fechaFinal);
	}

	public Date getImportado() {
		return importado;
	}

	public void setImportado(Date importado) {
		this.importado = importado;
	}

	public Date getReplicado() {
		return replicado;
	}

	public void setReplicado(Date replicado) {
		this.replicado = replicado;
	}
	
	

}
