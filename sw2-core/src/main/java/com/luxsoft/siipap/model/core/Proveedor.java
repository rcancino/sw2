package com.luxsoft.siipap.model.core;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.ventas.service.DescuentosManagerTest;
import com.luxsoft.sw3.replica.Replicable;

@Entity
@Table (name="SX_PROVEEDORES")
public class Proveedor extends BaseBean implements Replicable{
	
	@TableGenerator(
            name="idsGen", 
            table="SX_TABLE_IDS", 
            pkColumnName="GEN_KEY", 
            valueColumnName="GEN_VALUE", 
            pkColumnValue="PROVEEDOR_ID", 
            allocationSize=1)
	@Id 
	@GeneratedValue(strategy=GenerationType.TABLE, generator="idsGen")
	@Column (name="PROVEEDOR_ID")
	private Long id;
	
	@Column(name="CLAVE",nullable=false,length=4)
	private String clave;
	
	@Column (name="NOMBRE",nullable=true)
	@Length (max=250) @NotNull @NotEmpty(message="El nombre es incorrecto")
	private String nombre;
        
    @Column(name="FISICA")
	private boolean personaFisica=false;
	
	@Column(name="CURP",length=18)
	private String curp;
	
	@Column(name="APELLIDOP",length=50)
	private String apellidoP;
	
	@Column(length=50)
	private String apellidoM;
	
	@Column(length=150)
	private String nombres;
	
	@Length (max=14)
	//@Patterns({@Pattern(regex="[A-Z]{3,4}[0-9]{6}[A-Z]{4}[0-9]")}) 
	private String Rfc;
	
	@Embedded
	private Direccion direccion=new Direccion();
	
	@Length (max=25)
	private String telefono1;
	
	@Length (max=25)
	private String telefono2;
	
	@Length (max=25)
	private String telefono3;
	
	@Column(name="CUENTACONTABLE",nullable=true,unique=false,length=30)	
	@Length (max=30,message="El rango maximo es de 30 caracteres")
	private String cuentaContable;
	
	@Email
	@Length (max=100)
	private String email1;
	
	@Email
	@Length (max=100)
	private String email2;
	
	@Length (max=150)
	private String www;
	
	@Length (max=100)
	private String contacto1;
	
	@Length (max=100)
	private String contacto2;
	
	private Boolean nacional=true;
	
	private BigDecimal credito=BigDecimal.ZERO;
	
	private int plazo=0;
	
	@Column(name="TIEMPOE" )
	private int tiempoDeEntrega=0;
	
	@CollectionOfElements (fetch=FetchType.LAZY)
	@JoinTable(
			name="SX_PROVEEDOR_NOTAS",
			joinColumns=@JoinColumn(name="PROVEEDOR_ID")	
	)
	@Fetch(value=FetchMode.SUBSELECT)
	@AccessType (value="field")
	private Set<Comentario> comentarios=new HashSet<Comentario>();
	
		
	@CollectionOfElements (fetch=FetchType.LAZY)
	@JoinTable(
			name="SX_PROVEEDOR_PRODUCTOS",
			joinColumns=@JoinColumn(name="PROVEEDOR_ID")	
	)
	@AccessType (value="field")
	@Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
	private Set<ProductoPorProveedor> productos=new HashSet<ProductoPorProveedor>();
	
	@Column(name="ACTIVO",nullable=false)
	private boolean activo=true;
	
	@Column(name="DESCUENTOF",nullable=false)
	private double descuentoFinanciero=0;
	
	@Column(name="DIASDF",nullable=false)
	private int diasDescuentoF=0;
	
	@Column(name="FLETE")
	private Boolean cobraFlete=Boolean.FALSE;

	@Column(name="VTO_FECHAREV")
	private Boolean vtoFechaRevision=Boolean.TRUE;
         
	@Version
	private int version;
	
	@Embedded
	private UserLog userLog=new UserLog();
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;
	
	@Column(name="IMPORTADOR")
	private boolean importador=false;
	
	@Column(name="IMPRIMIR_COSTO")
	private boolean imprimirCosto=false;
	
	@Column(name="MAQUILADOR")
	private boolean maquilador=false;
	
	@Column(name = "DESCTO_NOTA", nullable = false)
	private boolean descuentoNota=false;
	
	public Proveedor() {}
	
	public Proveedor(String nombre) {
		this.nombre = nombre;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getClave() {
		return clave;
	}
	
	public void setClave(String clave) {
		this.clave = clave;
	}

	/**
	 * 
	 * @return
	 */
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		Object old=this.nombre;
		this.nombre = nombre;
		firePropertyChange("nombre", old, nombre);
	}
        
        
	public String getRfc() {
		return Rfc;
	}
	public void setRfc(String rfc) {
		Rfc = rfc;
	}

	public Direccion getDireccion() {
		return direccion;
	}
	public void setDireccion(Direccion direccion) {
		this.direccion = direccion;
	}

	public String getTelefono1() {
		return telefono1;
	}
	public void setTelefono1(String telefono1) {
		this.telefono1 = telefono1;
	}

	public String getTelefono2() {
		return telefono2;
	}
	public void setTelefono2(String telefono2) {
		this.telefono2 = telefono2;
	}

	public String getTelefono3() {
		return telefono3;
	}
	public void setTelefono3(String telefono3) {
		this.telefono3 = telefono3;
	}

	public String getCuentaContable() {
		return cuentaContable;
	}
	public void setCuentaContable(String cuentaContable) {
		this.cuentaContable = cuentaContable;
	}
	
	

	public double getDescuentoFinanciero() {
		return descuentoFinanciero;
	}

	public void setDescuentoFinanciero(double descuentoFinanciero) {
		double old=this.descuentoFinanciero;
		this.descuentoFinanciero = descuentoFinanciero;
		firePropertyChange("descuentoFinanciero", old, descuentoFinanciero);
	}

	public int getDiasDescuentoF() {
		return diasDescuentoF;
	}

	public void setDiasDescuentoF(int diasDescuentoF) {
		int old=this.diasDescuentoF;
		this.diasDescuentoF = diasDescuentoF;
		firePropertyChange("diasDescuentoF", old, diasDescuentoF);
	}

	public String getEmail1() {
		return email1;
	}
	public void setEmail1(String email1) {
		this.email1 = email1;
	}

	public String getEmail2() {
		return email2;
	}
	public void setEmail2(String email2) {
		this.email2 = email2;
	}

	public String getWww() {
		return www;
	}
	public void setWww(String www) {
		this.www = www;
	}

	public String getContacto1() {
		return contacto1;
	}
	public void setContacto1(String contacto1) {
		this.contacto1 = contacto1;
	}

	public String getContacto2() {
		return contacto2;
	}
	public void setContacto2(String contacto2) {
		this.contacto2 = contacto2;
	}

	public Boolean getNacional() {
		return nacional;
	}
	public void setNacional(Boolean nacional) {
		this.nacional = nacional;
	}

	public BigDecimal getCredito() {
		return credito;
	}
	public void setCredito(BigDecimal credito) {
		this.credito = credito;
	}

	public int getPlazo() {
		return plazo;
	}
	public void setPlazo(int plazo) {
		this.plazo = plazo;
	}
	
	public Boolean getVtoFechaRevision() {
		if(this.vtoFechaRevision==null)
			this.vtoFechaRevision=Boolean.TRUE;
		return vtoFechaRevision;
	}

	public void setVtoFechaRevision(Boolean vtoFechaRevision) {
		Object old = this.vtoFechaRevision;
		this.vtoFechaRevision = vtoFechaRevision;
		firePropertyChange("vtoFechaRevision", old, vtoFechaRevision);
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		boolean old=this.activo;
		this.activo = activo;
		firePropertyChange("activo", old, activo);
	}

	/** Manejo de collecciones  colaboradores **/

	public Set<Comentario> getComentarios() {
		return Collections.unmodifiableSet(comentarios);
	}
	
	public boolean agregarComentario(Comentario c){
		return comentarios.add(c);
	}
	
	public boolean agregarComentario(final String s) {		
		Assert.notNull(s, "El comentario no puede ser nulo");
		Assert.isTrue(!StringUtils.isEmpty(s),"El comentario no puede estar vacio");
		Comentario c=new Comentario("GENERAL",s);
		return agregarComentario(c);
	}
	
	public boolean  removeComentario(final Comentario c){
		return  comentarios.remove(c);
	}
	
	public Set<ProductoPorProveedor> getProductos() {
		return productos;
	}
	
	public boolean agregarProducto(final Producto s) {		
		Assert.notNull(s, "El producto   no puede ser nulo");
		final ProductoPorProveedor p=new ProductoPorProveedor(this,s);
		return productos.add(p);
	}
	
	public boolean agregarProducto(final ProductoPorProveedor p){
		Assert.notNull(p, "El producto  por proveedor no puede ser nulo");
		return productos.add(p);
	}
	
	public boolean removeProducto(final Producto s){
		Assert.notNull(s, "El producto - servicio  no puede ser nulo");
		final ProductoPorProveedor p=new ProductoPorProveedor(this,s);
		return productos.remove(p);
	}
	
	public boolean removerProducto(final ProductoPorProveedor p){
		Assert.notNull(p, "El producto - servicio por proveedor no puede ser nulo");
		return productos.remove(p);
	}
	
	/** Fin del manejo de collecciones ***/
	
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	public int getTiempoDeEntrega() {
		return tiempoDeEntrega;
	}
	public void setTiempoDeEntrega(int tiempoDeEntrega) {
		this.tiempoDeEntrega = tiempoDeEntrega;
	}	
	
	public UserLog getUserLog() {
		return userLog;
	}
	

	@Override
	public boolean equals(Object o) {
		if(o==null) return false;
		if(o==this) return true;
		if(!getClass().isAssignableFrom(o.getClass())) return false;
		Proveedor otro=(Proveedor)o;		
		return new EqualsBuilder()
		.append(getId(), otro.getId())
		.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(getId())
		.toHashCode();
	}
	
	@Override
	public String toString() {
		return getNombreRazon()+" ("+clave+")";			
	}

	public String getApellidoM() {
		return apellidoM;
	}
	public void setApellidoM(String apellidoM) {
		Object old=this.apellidoM;
		this.apellidoM = apellidoM;
		firePropertyChange("apellidoM", old, apellidoM);
	}

	public String getApellidoP() {
		return apellidoP;
	}
	public void setApellidoP(String apellidoP) {
		Object old=this.apellidoP;
		this.apellidoP = apellidoP;
		firePropertyChange("apellidoP", old, apellidoP);
	}

	public String getCurp() {
		return curp;
	}
	public void setCurp(String curp) {
		Object old = this.curp;
		this.curp = curp;
		firePropertyChange("curp", old, curp);
	}

	public String getNombres() {
		return nombres;
	}
	public void setNombres(String nombres) {
		Object old=this.nombres;
		this.nombres = nombres;
		firePropertyChange("nombres", old, nombres);
	}

	public boolean isPersonaFisica() {
		return personaFisica;
	}
	public void setPersonaFisica(boolean personaFisica) {
		Object old=this.personaFisica;
		this.personaFisica = personaFisica;
		firePropertyChange("personaFisica", old, personaFisica);
	}
	
	
	public String getNombreRazon(){		
		if(isPersonaFisica()){
			String pattern="{0} {1} {2}";
			return MessageFormat.format(pattern, getApellidoP(),getApellidoM(),getNombres());
		}else
			return getNombre();
	}
	
	//@AssertTrue  (message="El Nombre de la persona moral es obligatorio")
	public boolean validarNombre(){
		if(!isPersonaFisica()){
			return StringUtils.isNotBlank(getNombre());
		}
		return true;
	}
	
	//@AssertTrue  (message="El Nombre de la persona fisica es obligatorio")
	public boolean validarNombrePF(){
		if(isPersonaFisica()){
			return (StringUtils.isNotBlank(getApellidoP()) &&
					StringUtils.isNotBlank(getApellidoM()) &&
					StringUtils.isNotBlank(getNombres())
					);
		}
		return true;
	}
	
	public void actualizarNombre(){
		if(isPersonaFisica() ){
			setNombre(getNombreRazon());
		}
	}

	public Boolean getCobraFlete() {
		return cobraFlete;
	}

	public void setCobraFlete(Boolean cobraFlete) {
		this.cobraFlete = cobraFlete;
	}
	
	public String getClaveEspceialConta(){
		String cta=getCuentaContable();
		String res="";
		if(!StringUtils.isBlank(cta)){
			String s1=StringUtils.substring(cta, 4,5);
			String s2=StringUtils.substring(cta, 6,8);
			res=s1+s2;
		}
		return res;
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

	public boolean isImportador() {
		return importador;
	}

	public void setImportador(boolean importador) {
		boolean old=this.importador;
		this.importador = importador;
		firePropertyChange("importador", old, importador);
	}

	public boolean isImprimirCosto() {
		return imprimirCosto;
	}

	public void setImprimirCosto(boolean imprimirCosto) {
		boolean old=this.imprimirCosto;
		this.imprimirCosto = imprimirCosto;
		firePropertyChange("imprimirCosto", old, imprimirCosto);
	}

	public boolean isMaquilador() {
		return maquilador;
	}

	public void setMaquilador(boolean maquilador) {
		boolean old=this.maquilador;
		this.maquilador = maquilador;
		firePropertyChange("maquilador", old, maquilador);
	}

	
	public boolean isDescuentoNota() {
		return descuentoNota;
	}
	
	public void setDescuentoNota(boolean descuentoNota) {
		this.descuentoNota = descuentoNota;
	}
	
	

}
