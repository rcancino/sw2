package com.luxsoft.siipap.ventas.model;

import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.model.AdressLog;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Lista de precios especial para un cliente
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_LP_CLIENTE")
public class ListaDePreciosCliente extends BaseBean implements Replicable{
	
	//@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@TableGenerator(
            name="idsGen", 
            table="SX_TABLE_IDS", 
            pkColumnName="GEN_KEY", 
            valueColumnName="GEN_VALUE", 
            pkColumnValue="LP_CLIENTES_ID", 
            initialValue=50,
            allocationSize=1)
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="idsGen")
	@Column(name="LISTA_ID")
	private Long id;
	
	@Version
	private int version;
	
	@ManyToOne(optional=false,fetch=FetchType.EAGER)
	@JoinColumn(name="CLIENTE_ID",nullable=false,updatable=false,unique=true)
	private Cliente cliente;
	
	@Column(name="FECHA_INI",nullable=false)
	private Date fechaInicial;
	
	@Column(name="FECHA_FIN",nullable=false)
	private Date fechaFinal;
	
	@Column(name="VIGENTE",nullable=false)
	private boolean activo=false;
	
	@Column(name="DESCUENTO_FIJO")
	private double descuento=0;
	
	@Column(name="COMENTARIO")
	@Length(max=255)
	private String comentario;
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.EAGER,mappedBy="lista")
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN,org.hibernate.annotations.CascadeType.REPLICATE})
	private Set<ListaDePreciosClienteDet> precios=new HashSet<ListaDePreciosClienteDet>();
	
	public ListaDePreciosCliente(){
		Periodo p=Periodo.getPeriodoDelMesActual();
		fechaInicial=p.getFechaInicial();
		fechaFinal=p.getFechaFinal();
	}
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log=new UserLog();
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createdIp",	column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedIp",	column=@Column(nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedMac",column=@Column(nullable=true,insertable=true,updatable=true))
	   })
	private AdressLog addresLog=new AdressLog();
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;
	
	
	public Long getId() {
		return id;
	}
	
	public int getVersion() {
		return version;
	}	
	

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		Object old=this.cliente;
		this.cliente = cliente;
		firePropertyChange("cliente", old, cliente);
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
	
	public boolean isActivo() {
		return activo;
	}
	public void setActivo(boolean vigente) {
		this.activo = vigente;
	}
	
	
	public double getDescuento() {
		return descuento;
	}

	public void setDescuento(double descuento) {
		double old=this.descuento;
		this.descuento = descuento;
		firePropertyChange("descuento", old, descuento);
	}

	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	

	public Set<ListaDePreciosClienteDet> getPrecios() {
		return precios;
	}
	
	
	public Date getImportado() {
		return importado;
	}

	public void setImportado(Date importado) {
		this.importado = importado;
	}

	public AdressLog getAddresLog() {
		if(addresLog==null){
			addresLog=new AdressLog();
		}
		return addresLog;
	}

	public void setAddresLog(AdressLog addresLog) {
		this.addresLog = addresLog;
	}

	public UserLog getLog() {
		if(log==null)
			log=new UserLog();
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	public Date getReplicado() {
		return replicado;
	}

	public void setReplicado(Date replicado) {
		this.replicado = replicado;
	}

	public boolean agregarPrecio(final ListaDePreciosClienteDet det){
		det.setLista(this);
		return precios.add(det);
	}
	public boolean removerPrecio(final ListaDePreciosClienteDet det){
		det.setLista(null);
		return precios.remove(det);
	}
	public void eliminarPrecios(){
		precios.clear();
	}
	
	
	
	
	public ListaDePreciosClienteDet getPrecio(Producto prod){
		return getPrecio(prod, MonedasUtils.PESOS);
	}
	
	/**
	 * Busca el bean {@link ListaDePreciosClienteDet}  del producto para la moneda indicada
	 * 
	 * @param prod
	 * @param moneda
	 * @return
	 */
	public ListaDePreciosClienteDet getPrecio(Producto prod, Currency moneda){
		return null;
	}
	
	@AssertTrue(message="El periodo es incorrecto Fecha inicial deve ser <= Fecha final")
	public boolean validarPeriodo(){
		return (fechaInicial.compareTo(fechaFinal)<=0);
	}

	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,79)
		.append(getId())
		.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj==null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListaDePreciosCliente other = (ListaDePreciosCliente) obj;
		return new EqualsBuilder()
		.append(getId(), other.getId())
		.isEquals();
	}

	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE)
		.append(id)		
		.append(cliente)
		.append(activo)
		.append(fechaInicial)
		.append(fechaInicial)
		.append(comentario)
		.toString();
	}

}
