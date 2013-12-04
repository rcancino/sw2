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
package com.luxsoft.siipap.model.core;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.MapKey;
import org.hibernate.validator.AssertFalse;
import org.hibernate.validator.Length;

import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.ventas.model.Cobrador;
import com.luxsoft.siipap.ventas.model.Vendedor;
import com.luxsoft.sw3.model.AdressLog;
import com.luxsoft.sw3.replica.Replicable;

/**
 *
 * @author Ruben Cancino
 */
@Entity
@Table(name="SX_CLIENTES")
public class Cliente extends BaseBean implements Replicable{

    //@Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    @TableGenerator(
            name="idsGen", 
            table="SX_TABLE_IDS", 
            pkColumnName="GEN_KEY", 
            valueColumnName="GEN_VALUE", 
            pkColumnValue="CLIENTE_ID", 
            allocationSize=1)
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="idsGen")
    @Column(name = "CLIENTE_ID")    
    private Long id;
    
    @Version
    @Column(name="VERSION")
    private int version;
  
    @Column(name = "CLAVE",length=7,unique=true)
    private String clave;
    
    @Column(name = "NOMBRE", nullable = true,length=100)
    @Length (max=100)
    private String nombre;
    
    @Column(name = "FISICA")
    private boolean personaFisica = false;
    
    @Column(name = "CURP", length = 18)
    private String curp;
    
    @Column(name = "APELLIDOP", length = 50)
    private String apellidoP;
    
    @Column(name="APELLIDOM",length = 50)
    private String apellidoM;
    
    @Column(name="NOMBRES",length = 150)
    private String nombres;
    
    @Column(name="RFC",length=20)
    private String rfc;
    
    @ManyToOne (optional=true)
    @JoinColumn (name="TIPO_ID", nullable=true)
    private TipoDeCliente tipo;
    
    @Embedded
    @Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
    private Direccion direccionFiscal=new Direccion();
    
    //Direcciones de entrega
    @CollectionOfElements(fetch= FetchType.EAGER)
    @JoinTable(name="SX_CLIENTES_DIRECCIONES",joinColumns=@JoinColumn( name="CLIENTE_ID"))
    @MapKey(columns=@Column(name="TIPO"))  
    @Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
    private Map<String,Direccion> direcciones=new HashMap<String, Direccion>();
    
    @CollectionOfElements
    (fetch=FetchType.LAZY)
    @JoinTable(name="SX_CLIENTES_COMENT"
    	,joinColumns=@JoinColumn( name="CLIENTE_ID"))
    @MapKey(columns=@Column(name="TIPO",length=20))    
    @Fetch(value=FetchMode.SUBSELECT)
    @Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
    @Column(name="COMENTARIO",length=200)
    private Map<String,String> comentarios=new HashMap<String, String>();
    
    
    @CollectionOfElements
    @JoinTable(name="SX_CLIENTES_TELS",joinColumns=@JoinColumn( name="CLIENTE_ID"))
    @Fetch(value=FetchMode.SUBSELECT)
    @MapKey(columns=@Column(name="TIPO",length=50))
    @Column(name="TELEFONO",length=30)
    @Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
    private Map<String,String> telefonos=new HashMap<String, String>();
    
    @Column(name = "CUENTACONTABLE", nullable = true, unique = false, length = 30)
    @Length (max=30,message="El rango maximo es de 30 caracteres")
    private String cuentaContable;
    
    @CollectionOfElements (fetch=FetchType.LAZY)
    @JoinTable(name="SX_CLIENTES_CONTACTOS",joinColumns=@JoinColumn( name="CLIENTE_ID"))    
    @Fetch(value=FetchMode.SUBSELECT)
    @Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
    private Set<Contacto> contactos=new HashSet<Contacto>();
    
    @CollectionOfElements
    @JoinTable(name="SX_CLIENTES_CUENTAS",joinColumns=@JoinColumn(name="CLIENTE_ID"))
    @JoinColumn(name="CUENTA",nullable=false)
    private Set<String> cuentas=new HashSet<String>();
    
    @OneToOne(cascade={CascadeType.ALL})   
    @JoinColumn(name="CREDITO_ID")
    @Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
    private ClienteCredito credito;
    
    @OneToOne(optional=true,mappedBy="cliente"
    	,cascade={CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE}
    	,fetch=FetchType.EAGER
    )
    private AutorizacionClientePCE autorizacionPagoContraEntrega;
    
    @Column(name="EMAIL1")
    @Length (max=100)
    private String email;
    
    @Column(name="EMAIL2")
    @Length (max=100)
    private String email2;
    
    @Column(name="EMAIL3")
    @Length (max=100)
    private String emai3;
    
    @Column(name="WWW")
    @Length (max=250)    
    private String www;
    
    @Column(name="SUSPENDIDO",nullable=false)
    private boolean suspendido=false;
    
    @Column(name="PNETO",nullable=false)
    private boolean precioNeto=false;
    
    @Embedded
    private UserLog userLog = new UserLog();
    
    @Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createdIp",	column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedIp",	column=@Column(nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedMac",column=@Column(nullable=true,insertable=true,updatable=true))
	   })
	private AdressLog addresLog=new AdressLog();
    
    @ManyToOne(optional = true)			
	@JoinColumn(name = "COBRADOR_ID",nullable=true)
	private Cobrador cobrador;
    
    @ManyToOne(optional = true)			
	@JoinColumn(name = "VENDEDOR_ID",nullable=true)
	private Vendedor vendedor;
    
    @Column(name="FRECUENTE")
    private boolean frecuente=true;
    
    @Column(name="TX_IMPORTADO",nullable=true)
	protected Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	protected Date replicado;
	
	@Column(name="CHEQUES_DEVUELTOS")
	private BigDecimal chequesDevueltos=BigDecimal.ZERO;
	
	@Column(name="JURIDICO")
	private boolean juridico=false;
	
	@Column(name="CEDULA")
	private boolean cedula=false;
	
	@Column(name="FOLIO_RFC")
	private int folioRFC=0;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "FPAGO", nullable = false, length = 25)
	private FormaDePago formaDePago=FormaDePago.EFECTIVO;
	
	@Column(name="CUOTAMENSUAL_COMISION")
	private BigDecimal cuotaMensualComision=BigDecimal.ZERO;
	
    
    public static final String DIRECCION_FISCAL="FISCAL";
    
    public static final String DIRECCION_ENTREGA1="ENTREGA1";

    public Cliente() {
    }

    public Cliente(String clave, String nombre) {
		super();
		this.clave = clave;
		this.nombre = nombre;
	}

	public Cliente(String nombre) {
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
        Object old = this.nombre;
        this.nombre = nombre;
        firePropertyChange("nombre", old, nombre);
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
    	Object old=this.rfc; 
        this.rfc = rfc;
        firePropertyChange("rfc", old, rfc);
    }    

    public TipoDeCliente getTipo() {
        return tipo;
    }
    public void setTipo(TipoDeCliente tipo) {
        this.tipo = tipo;
    }    

    public String getCuentaContable() {
        return cuentaContable;
    }
    public void setCuentaContable(String cuentaContable) {
    	Object old=this.cuentaContable;
        this.cuentaContable = cuentaContable;
        firePropertyChange("cuentaContable", old, cuentaContable);
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }    

    public String getEmail2() {
		return email2;
	}

	public void setEmail2(String email2) {
		this.email2 = email2;
	}

	public String getEmai3() {
		return emai3;
	}

	public void setEmai3(String emai3) {
		this.emai3 = emai3;
	}

	public String getWww() {
        return www;
    }
    public void setWww(String www) {
        this.www = www;
    }  
   
    public String getApellidoM() {
        return apellidoM;
    }
    public void setApellidoM(String apellidoM) {
        Object old = this.apellidoM;
        this.apellidoM = apellidoM;
        firePropertyChange("apellidoM", old, apellidoM);
    }

    public String getApellidoP() {
        return apellidoP;
    }

    public void setApellidoP(String apellidoP) {
        Object old = this.apellidoP;
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
        Object old = this.nombres;
        this.nombres = nombres;
        firePropertyChange("nombres", old, nombres);
    }

    public boolean isPersonaFisica() {
        return personaFisica;
    }
    public void setPersonaFisica(boolean personaFisica) {
        Object old = this.personaFisica;
        this.personaFisica = personaFisica;
        firePropertyChange("personaFisica", old, personaFisica);
    }    
    
    /**
     * Si el cliente esta suspendido no es posible venderle
     * 
     * @return
     */
    public boolean isSuspendido() {
		return suspendido;
	}
	public void setSuspendido(boolean suspendido) {
		boolean old=this.suspendido;
		this.suspendido = suspendido;
		firePropertyChange("suspendido", old, suspendido);
	}
	
	public boolean isCreditoSuspendido(){
		if(isDeCredito()){
			return getCredito().isSuspendido();
		}
		return true;
	}
	
	/**
	 * @return the direccionFiscal
	 */
	public Direccion getDireccionFiscal() {
		return direccionFiscal;
	}

	/**
	 * @param direccionFiscal the direccionFiscal to set
	 */
	public void setDireccionFiscal(Direccion direccionFiscal) {
		this.direccionFiscal = direccionFiscal;
	}	

	public ClienteCredito getCredito() {
		return credito;
	}
	public void setCredito(ClienteCredito credito) {
		Object old=this.credito;
		this.credito = credito;
		firePropertyChange("credito", old, credito);
	}

	public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }

    
    

    public String getNombreRazon() {
        if (isPersonaFisica()) {
            String pattern = "{0} {1} {2} ";
            return MessageFormat.format(pattern, getApellidoP(), getApellidoM(), getNombres());
        } else {
            return MessageFormat.format("{0}", nombre);
        }
    }
    
    /**
     * Estandariza el nombre en el caso de personas fisicas
     * 
     */
    public void fijarNombre(){
        if(isPersonaFisica()){
            setNombre(getNombreRazon());
        }
    }
    

    /**
     * Lista no modificable de las firecciones del cliente
     * @return
     */
    public Map<String, Direccion> getDirecciones() {
        return direcciones;
    }
    
    public void agregarDireccion(Direccion d){
        agregarDireccion(d,DIRECCION_FISCAL);
    }
    
    public void agregarDireccion(Direccion d,String tipo){
        direcciones.put(tipo, d);
    }
    
    /**
     * Elimina la direccion principal del cliente (Fiscal)
     */
    public void eliminarDireccion(){
        eliminarDireccion(DIRECCION_FISCAL);
    }
    
    public void eliminarDireccion(String key){
        direcciones.remove(key);
    }
    
    /**
     * Comentarios relacionados con el producto
     * 
     * @return
     */
    public Map<String, String> getComentarios() {
        //return Collections.unmodifiableMap(comentarios);
        return comentarios;
    }
    
    public void agregarComentario(final String estandar,String valor){
    	String key=StringUtils.left(estandar, 20);
    	String val=StringUtils.left(valor, 200);
    	if(!StringUtils.isBlank(key))
    		comentarios.put(key, val);
    }
    public String eliminarComentario(final String estandar){
        return comentarios.remove(estandar);
    }

   /* *//**
     * Regresa un Set  de solo lectura con los descuentos
     * para este cliente
     * 
     * @return
     *//*
    public SortedSet<Descuento> getDescuentos() {
        return Collections.unmodifiableSortedSet(descuentos);
    }
    
    *//**
     * Agrega un descuento al final de la lista de descuentos
     * 
     * @param valor
     *//*
    public void agregarDescuento(final double valor){        
        int orden=descuentos.size()==0?1:descuentos.size()+1;
        agregarDescuento(valor,orden,"NORMAL");
    }
    
    *//**
     * 
     * Agrega un descuento al final de la lista de descuentos
     * con la descripcion definida
     * 
     * @param valor
     * @param desc
     *//*
    public Descuento agregarDescuento(double valor, String desc) {
        int orden=descuentos.size()==0?1:descuentos.size()+1;
        return agregarDescuento(valor,orden,desc);
    }
   */
    
   /* *//**
     * Forma ordenada de asignar descuentos con una descripción asociada
     * 
     * @param valor
     * @param orden
     * @param descripcion
     * @return El Descuento generado si este no existia, nulo de lo contrario
     *//*
    public Descuento agregarDescuento(final double valor,int orden,String descripcion){
        
        Descuento d=new Descuento(valor,descripcion.toUpperCase());
        d.setOrden(orden);
        boolean res=agregarDescuento(d);
        return res?d:null;
    }
    */
    /**
     * Agrega un descuento fijos para este cliente. 
     * 
     * @param d
     * @return verdadero si el descuento no existia, nulo de lo contrario
     * 
     *//*
    public boolean agregarDescuento(final Descuento d){
        return descuentos.add(d);
    }
    
    *//**
     * Elimina el descuento de este cliente
     * 
     * @param desc
     * @return verdadero si el descuento fue eliminado con exito, nulo de lo contrario
     *//*
    public boolean eliminarDescuento(Descuento desc){
        return descuentos.remove(desc);
    }
    
    public boolean eliminarDescuento(final int orden){
        Descuento d=(Descuento)CollectionUtils.find(descuentos, new Predicate() {

            public boolean evaluate(Object object) {
                Descuento dd=(Descuento)object;
                return dd.getOrden()==orden;
            }
        });
        return eliminarDescuento(d);
    }
    
    public Descuento buscarDescuento(final String key){
    	return (Descuento)CollectionUtils.find(descuentos, new Predicate(){

			public boolean evaluate(Object object) {
				Descuento d=(Descuento)object;
				return d.getDescripcion().equalsIgnoreCase(key);
			}
    		
    	});
    }
    */
    /**
     * Telefonos del cliente
     * 
     * @return
     */
    public Map<String, String> getTelefonos() {
        return telefonos;
    }
    
    public void agregarTelefono(final String estandar,String valor){
    	String key=StringUtils.left(estandar, 30);
    	String val=StringUtils.left(valor, 50);
    	val=StringUtils.deleteWhitespace(val);
    	if(!StringUtils.isBlank(key))
    		telefonos.put(key, val);
    			
        
    }
    public void eliminarTelefono(final String estandar){
        telefonos.remove(estandar);
    }
    
    /**
     * Contactos del cliente
     * 
     * @return
     */
    public Set<Contacto> getContactos() {
        return contactos;
    }
    
    public void agregarContacto(final Contacto contacto){
        contactos.add(contacto);
    }
    public void eliminarContacto(final Contacto contacto){
        contactos.remove(contacto);
    }
    
    public String getContactoPrincipal(){
    	if(getContactos().isEmpty())
    		return "";
    	return getContactos().iterator().next().getNombre();
    }
    
    /**
     * Calcula el vencimiento para una opracion de venta
     * con este cliente
     * 
     * @param ventaFecha
     * @return
     */
    public Date calcularVencimiento(Date ventaFecha){
    	if(isSuspendido())
    		throw new RuntimeException("El cliente esta suspendido para este tipo de operaciones");
    	if(getCredito()!=null){
    		return getCredito().calcularVencimiento(ventaFecha); //Delegamos el calculo
    	}else
    		return ventaFecha;
    }
    
    @AssertFalse (message="Digite el nombre o razón social")
    public boolean validarNombreRazon(){
    	return StringUtils.isBlank(getNombreRazon());
    }
    
    /**
     * Lista de posibles conceptos de comentarios
     */
    public static String[] COMENTARIOS={"VENTAS 1","VENTAS 2","CREDITO 1","CREDITO 2","SUSPENCION"};


	/**
	 * @return the deCredito
	 */
	public boolean isDeCredito() {
		return getCredito()!=null;
	}

	public void habilitarCredito(){
		habilitarCredito(new ClienteCredito());
	}
	
	public void habilitarCredito(final ClienteCredito cred){
		if(this.credito==null){
			cred.setCliente(this);
			setCredito(cred);
		}
	}
	
    public Direccion getDireccionDeEntrega(){
    	return direcciones.get(DIRECCION_ENTREGA1);
    }
    public void setDireccionDeEntrega(final Direccion d){
    	direcciones.put(DIRECCION_ENTREGA1, d);
    }

	public boolean isPrecioNeto() {
		return precioNeto;
	}

	public void setPrecioNeto(boolean precioNeto) {
		this.precioNeto = precioNeto;
	}

	public Cobrador getCobrador() {
		return cobrador;
	}
	public void setCobrador(Cobrador cobrador) {
		Object old=this.cobrador;
		this.cobrador = cobrador;
		firePropertyChange("cobrador", old, cobrador);
	}

	public Vendedor getVendedor() {
		return vendedor;
	}

	public void setVendedor(Vendedor vendedor) {
		Object old=this.vendedor;
		this.vendedor = vendedor;
		firePropertyChange("vendedor", old, vendedor);
	}  
	

	public Set<String> getCuentas() {
		return cuentas;
	}
	
	public String getLabel(){
		return getNombreRazon()+" ("+clave+")";
	}

	public boolean isFrecuente() {
		return frecuente;
	}

	public void setFrecuente(boolean frecuente) {
		this.frecuente = frecuente;
	}
	
	
    
  /*  // Manejo de descuento fijo
    public void setDescuentoFijo(Descuento descuento){
    	if(!descuentos.isEmpty())
    		descuentos.clear();
    	agregarDescuento(descuento);
    }
    
    public Descuento getDescuentoFijo(){
    	if(descuentos.isEmpty())
    		return null;
    	return descuentos.iterator().next();
    }
    */
    
    public String getContacto(){
    	if(this.contactos.isEmpty())
    		return "";
    	else
    		return contactos.iterator().next().getNombre();
    }
    
    public int getPlazo(){
    	if(isDeCredito())
    		return credito.getPlazo();
    	return 0;
    }
    
    @Column(name="PERMITIR_CHEQUE",nullable=false)
    private boolean permitirCheque=false;

	public boolean isPermitirCheque() {
		return permitirCheque;
	}
	
	public boolean isChequePostfechado(){
		if(isDeCredito())
			return getCredito().isChequePostfechado();
		return false;
	}
	
	public boolean isChequeplus(){
		if(isDeCredito())
			return getCredito().isCheckplus();
		return false;
	}

	public void setPermitirCheque(boolean permitirCheque) {
		boolean old=this.permitirCheque;
		this.permitirCheque = permitirCheque;
		firePropertyChange("permitirCheque", old, permitirCheque);
	}	
    
    

	public String getTelefono1() {
		return telefonos.get("TEL1");
	}

	public void setTelefono1(String telefono1) {
		Object old=getTelefono1();
		telefonos.put("TEL1", telefono1);
		firePropertyChange("tel1", old, telefono1);
	}

	public String getTelefono2() {
		return telefonos.get("TEL2");
	}

	public void setTelefono2(String telefono2) {
		Object old=getTelefono2();
		telefonos.put("TEL2", telefono2);
		firePropertyChange("tel2", old, telefono2);
	}	

	public String getFax() {
		return telefonos.get("FAX");
	}

	public void setFax(String fax) {
		Object old=getFax();
		telefonos.put("FAX", fax);
		firePropertyChange("FAX", old, fax);
	}

	public AutorizacionClientePCE getAutorizacionPagoContraEntrega() {
		return autorizacionPagoContraEntrega;
	}

	public void setAutorizacionPagoContraEntrega(AutorizacionClientePCE autorizacionPagoContraEntrega) {
		this.autorizacionPagoContraEntrega = autorizacionPagoContraEntrega;
		
	}
	
	public boolean isContraEntrega(){
		return getAutorizacionPagoContraEntrega()!=null;
	}
    
	
	public String getDireccionAsString(){
		if(getDireccionFiscal()!=null){
			return getDireccionFiscal().toString();
		}return "";
		
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
	
	public BigDecimal getChequesDevueltos() {
		return chequesDevueltos;
	}

	public void setChequesDevueltos(BigDecimal chequesDevueltos) {
		this.chequesDevueltos = chequesDevueltos;
	}

	public boolean isJuridico() {
		return juridico;
	}

	public void setJuridico(boolean juridico) {
		boolean old=this.juridico;
		this.juridico = juridico;
		firePropertyChange("juridico", old, juridico);
	}
	
	

	public boolean isCedula() {
		return cedula;
	}

	public void setCedula(boolean cedula) {
		Object old=this.cedula;
		this.cedula = cedula;
		firePropertyChange("cedula", old, cedula);
	}

	@Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!getClass().isAssignableFrom(o.getClass())) {
            return false;
        }
        Cliente otro = (Cliente) o;
        return new EqualsBuilder()
                .append(clave, otro.getClave())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 35)
                .append(this.clave)
                .toHashCode();
    }

    @Override
    public String toString() {
        return getNombreRazon();
    }  
    
    public String getTelefonosRow(){
    	StringBuffer buf=new StringBuffer();
    	for(String tel:getTelefonos().values()){
    		buf.append(tel);
    		buf.append(" / ");
    	}
    	return buf.toString();
    }
    
    public UserLog getUserLog() {
        return userLog;
    }    
    public UserLog getLog(){
    	if(userLog==null)
    		userLog=new UserLog();
    	return userLog;
    }    
	public void setUserLog(UserLog userLog) {
		this.userLog = userLog;
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

	public int getFolioRFC() {
		return folioRFC;
	}

	public void setFolioRFC(int folioRFC) {
		this.folioRFC = folioRFC;
	}
	
	
    public void setFormaDePago(FormaDePago formaDePago) {
    	Object old=this.formaDePago;
		this.formaDePago = formaDePago;
		firePropertyChange("formaDePago", old, formaDePago);
	}
    public FormaDePago getFormaDePago() {
		return formaDePago;
	}
    
    
	@Column(name = "CUENTA_PAGO", nullable = true, length = 4)
    private String cuentaDePago;
	
	@Column(name = "DESTINATARIO_CFD", nullable = true, length = 100)
	private String destinatarioCFD;
    
    public void setCuentaDePago(String cuentaDePago) {
    	Object old=this.cuentaDePago;
		this.cuentaDePago = cuentaDePago;
		firePropertyChange("cuentaDePago", old,cuentaDePago);
	}
    public String getCuentaDePago() {
		return cuentaDePago;
	}
    
    public void setDestinatarioCFD(String destinatarioCFD) {
    	Object old=this.destinatarioCFD;
		this.destinatarioCFD = destinatarioCFD;
		firePropertyChange("destinatarioCFD", old, destinatarioCFD);
	}
    public String getDestinatarioCFD() {
		return destinatarioCFD;
	}

	public BigDecimal getCuotaMensualComision() {
		return cuotaMensualComision;
	}

	public void setCuotaMensualComision(BigDecimal cuotaMensualComision) {
		Object old=this.cuotaMensualComision;
		this.cuotaMensualComision = cuotaMensualComision;
		firePropertyChange("cuotaMensualComision", old, cuotaMensualComision);
	}
    

    
    
    
}
