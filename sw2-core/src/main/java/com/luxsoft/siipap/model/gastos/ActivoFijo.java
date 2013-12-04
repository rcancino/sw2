package com.luxsoft.siipap.model.gastos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

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
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * 
 * @author Ruben Cancino
 * @TODO  Propiedades nuevas: 
 * 
 *  -Descripcion ampla del activo en descripcionAf
 *  -Serie
 *  -Modelo
 *  -rubo (Como en GCompraDet) ya no se usara la clasificacion actual.
 *  - Un posible mantenimiento para asociar gastos a activos fijos existentes como por ejemplo gastos de mantenimiento a una guillotina
 * 
 */
@Entity
@Table(name = "SW_ACTIVO_FIJO")
public class ActivoFijo extends BaseBean {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ACTIVO_ID")
	private Long id;

	@Column(name = "MOI", nullable = false)
	private BigDecimal moi;

	@ManyToOne(optional = true)
	@JoinColumn(name = "SUCURSAL_ID", nullable = true)
	private Sucursal sucursal;

	@ManyToOne(optional = true)
	@JoinColumn(name = "DEPARTAMENTO_ID", nullable = true)
	private Departamento departamento;

	@ManyToOne(optional = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE })
	@JoinColumn(name = "PROVEEDOR_ID", nullable = true)
	private GProveedor proveedor;

	@ManyToOne(optional = false, cascade = { CascadeType.MERGE,
			CascadeType.PERSIST })
	@JoinColumn(name = "PRODUCTO_ID", nullable = false)
	@NotNull
	private GProductoServicio producto;

	@Column(name = "DOCUMENTO", length = 20)
	@Length(max = 20)
	private String documento;

	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	@NotNull
	private Date fechaDeAdquisicion;

	@Column(name = "INPC", nullable = false)
	private double inpc;

	@ManyToOne(optional = false, cascade = { CascadeType.MERGE,
			CascadeType.PERSIST })
	@JoinColumn(name = "ORIGEN_INPC_ID", nullable = false)
	@NotNull
	private INPC inpcOriginal;

	@Column(name = "TASA_DEP")
	private double tasaDepreciacion;

	@ManyToOne(optional = true, cascade = { CascadeType.MERGE,
			CascadeType.PERSIST })
	@JoinColumn(name = "ACTIVOTIPO_ID", nullable = true)
	private ClasificacionDeActivo clasificacion;
	
	@ManyToOne (optional=true) 
	@JoinColumn (name="CLASE_ID")
	private ConceptoDeGasto rubro;

	/**
	 * @Column (name="DEP_ACU_ANT") private BigDecimal
	 *         depreciacionAcumuladaAnterior=BigDecimal.ZERO;
	 */

	@ManyToOne(optional = true, cascade = { CascadeType.MERGE,
			CascadeType.PERSIST })
	@JoinColumn(name = "ULTIMO_INPC_ID", nullable = true)
	private INPC ultimoINPC;

	@Type(type = "date")
	private Date fechaActualizacion ;
	
	

	@ManyToOne(optional = true, cascade = { CascadeType.MERGE,
			CascadeType.PERSIST })
	@JoinColumn(name = "CONSIGNATARIO_ID", nullable = true)
	private Consignatario consignatario;

	@Column(name = "COMENTARIO", length = 150)
	@Length(max = 150)
	private String comentario;
	
	@Column(name = "SERIE", length = 50)
	@Length(max = 50)
	private String serie;
	
	@Column(name = "MODELO", length = 50)
	@Length(max = 50)
	private String modelo;
	
	@ManyToOne(optional = true,fetch=FetchType.LAZY)
	@JoinColumn(name = "GCOMPRADET_ID", nullable = true)
	private GCompraDet compraDeGastoDet;
	
	@Column(name = "DPRECIACION_TOTAL")
	@Type(type = "date")
	private Date depreciacionTotal;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "ESTADO", nullable = false,length=20)
	private Estado estado=Estado.ACTIVO;
	
	@Column (name="COSTO_ACTUALIZADO",nullable=false,scale=2,precision=16)
	private BigDecimal costoActualizado=BigDecimal.ZERO;
	
	@Column (name="DEPRECIACION_MENSUAL",nullable=false,scale=2,precision=16)
	private BigDecimal depreciacionMensual=BigDecimal.ZERO;
	
	@ManyToOne(optional = true,fetch=FetchType.LAZY)
	@JoinColumn(name = "VENTA_ID")
	private Venta venta;
	
	@Column(name = "VENTA_FACTURA")
	private Long facturaDeVenta;
	
	@Column(name = "FACTURA_FECHA")
	@Type(type = "date")
	private Date facturaFecha;
	
	@Column (name="IMPORTE_FACTURADO",nullable=false,scale=2,precision=16)
	private BigDecimal importeFacturado=BigDecimal.ZERO;

	@Embedded
	private UserLog log = new UserLog();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public GProductoServicio getProducto() {
		return producto;
	}

	public void setProducto(GProductoServicio producto) {
		Object old = this.producto;
		this.producto = producto;
		firePropertyChange("producto", old, producto);
	}

	public Departamento getDepartamento() {
		return departamento;
	}

	public void setDepartamento(Departamento departamento) {
		Object old = this.departamento;
		this.departamento = departamento;
		firePropertyChange("departamento", old, departamento);
	}

	public BigDecimal getMoi() {
		return moi;
	}

	public void setMoi(BigDecimal moi) {
		Object old = this.moi;
		this.moi = moi;
		firePropertyChange("moi", old, moi);
	}

	public GProveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(GProveedor proveedor) {
		Object old = this.proveedor;
		this.proveedor = proveedor;
		firePropertyChange("proveedor", old, proveedor);
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		Object old = this.sucursal;
		this.sucursal = sucursal;
		firePropertyChange("sucursal", old, sucursal);
	}

	public Date getFechaDeAdquisicion() {
		return fechaDeAdquisicion;
	}

	public void setFechaDeAdquisicion(Date fechaDeAdquisicion) {
		Object old = this.fechaActualizacion;
		this.fechaDeAdquisicion = fechaDeAdquisicion;
		firePropertyChange("fechaDeAdquisicion", old, fechaDeAdquisicion);
	}

	public ClasificacionDeActivo getClasificacion() {
		return clasificacion;
	}

	public void setClasificacion(ClasificacionDeActivo clasificacion) {
		Object old = this.clasificacion;
		this.clasificacion = clasificacion;
		firePropertyChange("clasificacion", old, clasificacion);
		if(clasificacion!=null){
			setTasaDepreciacion(clasificacion.getTasa());
		}else{
			setTasaDepreciacion(0);
		}
	}

	/**
	 * TODO Documentar
	 * 
	 * @return
	 * 
	 * public BigDecimal getDepreciacionAcumuladaAnterior() { return
	 * depreciacionAcumuladaAnterior; } public void
	 * setDepreciacionAcumuladaAnterior(BigDecimal depreciacionAcumulada) {
	 * Object old=this.depreciacionAcumuladaAnterior;
	 * this.depreciacionAcumuladaAnterior = depreciacionAcumulada;
	 * firePropertyChange("depreciacionAcumulada", old, depreciacionAcumulada); }
	 */

	/**
	 * Referencia al INPC original al momento de la adquisicion
	 * 
	 * @return
	 */
	public INPC getInpcOriginal() {
		return inpcOriginal;
	}

	public void setInpcOriginal(INPC inpcOriginal) {
		Object old = this.inpcOriginal;
		this.inpcOriginal = inpcOriginal;
		firePropertyChange("inpcOriginal", old, inpcOriginal);
		if(inpcOriginal!=null)
			setInpc(inpcOriginal.getIndice());
	}

	/**
	 * Valor del INPC al momento de la adquisición
	 * 
	 * @return
	 */
	public double getInpc() {
		return inpc;
	}

	public void setInpc(double inpc) {
		double old = this.inpc;
		this.inpc = inpc;
		firePropertyChange("inpc", old, inpc);
	}

	/**
	 * Tasa de depreciación al momento de la adquisición
	 * 
	 * @return
	 */
	public double getTasaDepreciacion() {
		return tasaDepreciacion;
	}

	public void setTasaDepreciacion(double tasaDepreciacion) {
		double old=this.tasaDepreciacion;
		this.tasaDepreciacion = tasaDepreciacion;
		firePropertyChange("tasaDepreciacion", old, tasaDepreciacion);
	}

	/**
	 * Regresa la fecha asignada para la ultima actualizacion
	 * 
	 * @return
	 */
	public Date getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) {
		Object old = this.fechaActualizacion;
		this.fechaActualizacion = fechaActualizacion;
		firePropertyChange("fechaActualizacion", old, fechaActualizacion);
	}

	/**
	 * Referencia al INPC de la fecha de actualización
	 * 
	 * 
	 * @return
	 */
	public INPC getUltimoINPC() {
		return ultimoINPC;
	}

	public void setUltimoINPC(INPC ultimoINPC) {
		Object old = this.ultimoINPC;
		this.ultimoINPC = ultimoINPC;
		firePropertyChange("ultimoINPC", old, ultimoINPC);
	}

	public Consignatario getConsignatario() {
		return consignatario;
	}

	public void setConsignatario(Consignatario consignatario) {
		this.consignatario = consignatario;
	}

	/**
	 * Comentario de uso opcional relacionado con el activo fijo
	 * 
	 * @return
	 */
	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old = this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}

	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
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
		final ActivoFijo other = (ActivoFijo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String toString() {
		String pattern = "{0}  MOI:{1}";
		return MessageFormat.format(pattern, getProducto(), getMoi());
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		Object old=this.documento;
		this.documento = documento;
		firePropertyChange("documento", old, documento);
	}

	/**
	 * Importe pendiente de depreciar
	 * 
	 */
	public BigDecimal getRemanenteInicial() {
		return getMoi().subtract(getDepreciacionInicial());
	}

	public BigDecimal getRemanente() {
		return getMoi().subtract(getDepreciacionAcumulada());
	}

	public BigDecimal getDepreciacionDelEjercicio() {
		return getDepreciacionAcumulada().subtract(getDepreciacionInicial());
	}

	/**
	 * Calcula la depreciacion acumulada hasta el ejercicio anterior. Regresa
	 * cero si el activo se ha depreciado totalmente durante este preiodo
	 * 
	 * @return
	 */
	public BigDecimal getDepreciacionInicial() {
		Date fechaFin = DateUtils.truncate(getFechaActualizacion(),
				Calendar.MONTH);
		Calendar c = Calendar.getInstance();
		c.setTime(fechaFin);
		c.add(Calendar.YEAR, -1);
		c.set(Calendar.MONTH, 11);
		fechaFin = c.getTime();
		return getDepreciacionHistorica(fechaFin);
	}

	/**
	 * Depreciacion acumulada hasta la fecha de actualizacion
	 * 
	 * @return
	 */
	public BigDecimal getDepreciacionAcumulada() {
		if (getFechaActualizacion() == null)
			return BigDecimal.ZERO;
		Calendar c = Calendar.getInstance();
		c.setTime(getFechaActualizacion());
		c.add(Calendar.MONTH, -1);
		// setFechaActualizacion(c.getTime());
		return getDepreciacionHistorica(c.getTime());
	}

	public BigDecimal getDepreciacionHistorica(final Date fechaFin) {

		if (!fechaFin.after(getFechaDeAdquisicion()))
			return BigDecimal.ZERO;

		double tm = (getTasaDepreciacion() / 12) / 100;

		Date fechaIni = DateUtils.truncate(getFechaDeAdquisicion(),
				Calendar.MONTH);
		BigDecimal monto = BigDecimal.ZERO;
		BigDecimal depMensual = getMoi().multiply(BigDecimal.valueOf(tm))
				.setScale(2, RoundingMode.HALF_EVEN);

		Calendar c = Calendar.getInstance();
		c.setTime(fechaIni);
		fechaIni = c.getTime();

		BigDecimal remantente = BigDecimal.ZERO;

		while (fechaIni.before(fechaFin)) {
			monto = monto.add(depMensual);
			c.add(Calendar.MONTH, 1);
			fechaIni = c.getTime();
			remantente = getMoi().subtract(monto);
			if (remantente.doubleValue() <= 0) {
				return getMoi();
			}

			if (remantente.compareTo(depMensual) < 0) {
				return remantente;
			}

		}
		return monto;
	}

	public BigDecimal getFactorDeActualizacion() {
		BigDecimal val = BigDecimal.ZERO;
		if (getUltimoINPC() != null && getInpc() > 0) {
			val = BigDecimal.valueOf(getUltimoINPC().getIndice());
			BigDecimal ultimo = BigDecimal.valueOf(getInpc());
			val = val.divide(ultimo, 4, RoundingMode.HALF_EVEN);
		}
		return val;

	}

	public BigDecimal getDepreciacionActualizada() {
		return getDepreciacionDelEjercicio().multiply(
				getFactorDeActualizacion());
	}

	public Date getUltimaFechaActualizable() {
		double tm = (getTasaDepreciacion() / 12) / 100;

		Date fechaIni = DateUtils.truncate(getFechaDeAdquisicion(),
				Calendar.MONTH);
		BigDecimal monto = BigDecimal.ZERO;
		BigDecimal depMensual = getMoi().multiply(BigDecimal.valueOf(tm))
				.setScale(2, RoundingMode.HALF_EVEN);

		Calendar c = Calendar.getInstance();
		c.setTime(fechaIni);
		fechaIni = c.getTime();

		BigDecimal remantente = BigDecimal.ZERO;

		while (fechaIni.before(getFechaActualizacion())) {
			monto = monto.add(depMensual);
			c.add(Calendar.MONTH, 1);
			fechaIni = c.getTime();
			remantente = getMoi().subtract(monto);
			if (remantente.doubleValue() <= 0) {
				return fechaIni;
			}

		}
		return getFechaActualizacion();
	}

	/**
	 * Meses de uso del activo
	 * 
	 * @return
	 */
	public int getMesesUso() {
		if (getFechaActualizacion() != null) {
			Date d1 = DateUtils.round(getFechaDeAdquisicion(), Calendar.MONTH);
			Date d2 = DateUtils.round(getFechaActualizacion(), Calendar.MONTH);
			Calendar c = Calendar.getInstance();
			c.setTime(d1);
			int meses = 0;
			while (d1.before(d2)) {
				meses++;
				c.add(Calendar.MONTH, 1);
				d1 = c.getTime();
				//System.out.println(DateUtil.convertDateToString(d1) + "mes: "+ meses);
			}
			return meses;

		}
		return 0;
	}

	public GCompraDet getCompraDeGastoDet() {
		return compraDeGastoDet;
	}

	public void setCompraDeGastoDet(GCompraDet compraDeGastoDet) {
		this.compraDeGastoDet = compraDeGastoDet;
		if(compraDeGastoDet!=null){
			//setRubro(compraDeGastoDet.getRubro());
			setProveedor(compraDeGastoDet.getCompra().getProveedor());
			setDepartamento(compraDeGastoDet.getCompra().getDepartamento());
			GFacturaPorCompra factura=compraDeGastoDet.getCompra().getFacturas().iterator().next();
			setDocumento(factura.getDocumento());
			setFechaDeAdquisicion(factura.getFecha());
			setMoi(compraDeGastoDet.getImporteMN().amount());
			setProducto(compraDeGastoDet.getProducto());
			setSucursal(compraDeGastoDet.getSucursal());
			setSerie(compraDeGastoDet.getSerie());
			setModelo(compraDeGastoDet.getModelo());
		}
	}
	
	

	public Date getDepreciacionTotal() {
		return depreciacionTotal;
	}

	public void setDepreciacionTotal(Date depreciacionTotal) {
		this.depreciacionTotal = depreciacionTotal;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	public BigDecimal getCostoActualizado() {
		if(costoActualizado==null)
			costoActualizado=BigDecimal.ZERO;
		return costoActualizado;
	}

	public void setCostoActualizado(BigDecimal costoActualizado) {
		this.costoActualizado = costoActualizado;
	}

	public Venta getVenta() {
		return venta;
	}

	public void setVenta(Venta venta) {
		this.venta = venta;
	}

	

	public Date getFacturaFecha() {
		return facturaFecha;
	}

	public void setFacturaFecha(Date facturaFecha) {
		this.facturaFecha = facturaFecha;
	}

	public Long getFacturaDeVenta() {
		return facturaDeVenta;
	}

	public void setFacturaDeVenta(Long facturaDeVenta) {
		this.facturaDeVenta = facturaDeVenta;
	}

	public BigDecimal getImporteFacturado() {
		return importeFacturado;
	}

	public void setImporteFacturado(BigDecimal importeFacturado) {
		this.importeFacturado = importeFacturado;
	}

	

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		Object old=this.serie;
		this.serie = serie;
		firePropertyChange("serie", old, serie);
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		Object old=this.modelo;
		this.modelo = modelo;
		firePropertyChange("modelo", old, modelo);
	}


	public void actualizar(){
		setCostoActualizado(getRemanente());
		setDepreciacionMensual(getDepreciacionDelEjercicio());
		actualizarDepreciacionTotal();
		setEstado(getDepreciacionDelEjercicio().doubleValue()==0?Estado.DEPRECIADO:Estado.ACTIVO);
	}
	
	private void actualizarDepreciacionTotal(){
		if(this.depreciacionTotal==null){
			setDepreciacionTotal(getUltimaFechaActualizable());
		}
	}

	

	public ConceptoDeGasto getRubro() {
		return rubro;
	}

	public void setRubro(ConceptoDeGasto rubro) {
		Object old=this.rubro;
		this.rubro = rubro;
		firePropertyChange("rubro", old, rubro);
		if(rubro!=null){
			setTasaDepreciacion(rubro.getTasa());
		}else
			setTasaDepreciacion(0);
	}

	
	

	public BigDecimal getDepreciacionMensual() {
		return depreciacionMensual;
	}

	public void setDepreciacionMensual(BigDecimal depreciacionMensual) {
		this.depreciacionMensual = depreciacionMensual;
	}




	public static enum Estado{
		ACTIVO,DEPRECIADO
	}

}
