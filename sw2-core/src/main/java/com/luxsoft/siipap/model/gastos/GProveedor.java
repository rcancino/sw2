package com.luxsoft.siipap.model.gastos;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Collections;
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Type;
//import org.hibernate.validator.AssertTrue;
//import org.hibernate.validator.Email;
//import org.hibernate.validator.Length;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.UserLog;

@Entity
@Table (name="SW_GPROVEEDOR")
public class GProveedor extends BaseBean{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="PROVEEDOR_ID")
	private Long id;
	
	
	@Column (name="NOMBRE",nullable=true)
	//@Length (max=100)
	private String nombre;
	
	@ManyToOne (optional=true) @JoinColumn (name="GTIPO_ID")	
	private GTipoProveedor tipo;
	
	//@Length (max=14)
	//@Patterns({@Pattern(regex="[A-Z]{3,4}[0-9]{6}[A-Z]{4}[0-9]")}) 
	private String Rfc;
	
	@Embedded
	private Direccion direccion=new Direccion();
	
	//@Length (max=25)
	private String telefono1;
	
	//@Length (max=25)
	private String telefono2;
	
	//@Length (max=25)
	private String telefono3;
	
	@Column(name="CUENTACONTABLE",nullable=true,unique=true,length=30)	
	//@Length (max=30,message="El rango maximo es de 30 caracteres")
	private String cuentaContable;
	
	//@Email
	//@Length (max=100)
	private String email1;
	
	//@Email
	//@Length (max=100)
	private String email2;
	
	//@Length (max=150)
	private String www;
	
	//@Length (max=100)
	private String contacto1;
	
	//@Length (max=100)
	private String contacto2;
	
	private Boolean nacional=true;
	
	private BigDecimal credito=BigDecimal.ZERO;
	
	private int plazo=0;
	
	@Column(name="TIEMPOE" )
	private int tiempoDeEntrega=0;
	
	@CollectionOfElements (fetch=FetchType.LAZY)
	@JoinTable(
			name="SW_GPROVEEDOR_NOTAS",
			joinColumns=@JoinColumn(name="PROVEEDOR_ID")	
	)
	@Column(name="COMENTARIO",nullable=false)
	@AccessType (value="field")
	private Set<String> comentarios=new HashSet<String>();
	
	@CollectionOfElements (fetch=FetchType.LAZY)
	@JoinTable(name="SW_GPROVEEDOR_PRODS",joinColumns=@JoinColumn(name="PROVEEDOR_ID"))
	@CollectionId(
			columns=@Column(name="GPROV_PROD_ID"),
			type=@Type(type="long"),
			generator="auto"
	)	
	private Set<GProductoPorProveedor> productos=new HashSet<GProductoPorProveedor>();
	
	@Version
	private int version;
	
	@Embedded
	private UserLog userLog=new UserLog();
	
	
	public GProveedor() {}
	
	public GProveedor(String nombre) {
		this.nombre = nombre;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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

	public GTipoProveedor getTipo() {
		return tipo;
	}
	public void setTipo(GTipoProveedor tipo) {
		this.tipo = tipo;
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
	
	/** Manejo de collecciones  colaboradores **/

	public Set<String> getComentarios() {
		return Collections.unmodifiableSet(comentarios);
	}
	
	public boolean agregarComentario(final String s) {		
		Assert.notNull(s, "El comentario no puede ser nulo");
		Assert.isTrue(!StringUtils.isEmpty(s),"El comentario no puede estar vacio");
		return comentarios.add(s);
	}
	
	public boolean  removeComentario(final String s){
		return  comentarios.remove(s);
	}
	
	public Set<GProductoPorProveedor> getProductos() {
		return Collections.unmodifiableSet(productos);
	}
	
	public boolean agregarProducto(final GProductoServicio s) {		
		Assert.notNull(s, "El producto - servicio  no puede ser nulo");
		final GProductoPorProveedor p=new GProductoPorProveedor(this,s);
		return productos.add(p);
	}
	
	public boolean agregarProducto(final GProductoPorProveedor p){
		Assert.notNull(p, "El producto - servicio por proveedor no puede ser nulo");
		return productos.add(p);
	}
	
	public boolean removeProducto(final GProductoServicio s){
		Assert.notNull(s, "El producto - servicio  no puede ser nulo");
		final GProductoPorProveedor p=new GProductoPorProveedor(this,s);
		return productos.remove(p);
	}
	
	public boolean removerProducto(final GProductoPorProveedor p){
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
		GProveedor otro=(GProveedor)o;		
		return new EqualsBuilder()
		.append(getNombre(),otro.getNombre())
		.append(getApellidoP(), otro.getApellidoP())
		.append(getApellidoM(), otro.getApellidoM())
		.append(getNombres(), otro.getNombres())
		.append(getRfc(), otro.getRfc())
		.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(getNombre())
		.append(getApellidoP())
		.append(getApellidoM())
		.append(getNombres())
		.append(getRfc())
		.toHashCode();
	}
	
	@Override
	public String toString() {
		return getNombreRazon();			
	}

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

}
