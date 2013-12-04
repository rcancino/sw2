package com.luxsoft.siipap.gastos.consultas;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;

public class AnalisisDeGasto {
	
	private Long proveedorId;
	private String proveedor;
	private Long sucursalId;
	private String sucursal;	
	private Long rubroId;
	private String rubro;
	private Long compraId;
	private Date f_compra;
	private Date f_contable;
	
	// Compra Det
	private Long compraDetId;
	private Long productoId;
	private String descripcion;	
	private BigDecimal importe;
	private BigDecimal impuesto_imp;	
	private BigDecimal ret1_impp;	
	private BigDecimal ret2_imp;
	private BigDecimal total;
	private BigDecimal ietu;
	private boolean inversion;
	
	//Datos de factura
	private String documento;
	private Date f_docto;
	private BigDecimal totalDoc;

	/*Datos de requisicion
	private Long requisicion;	
	private BigDecimal aPagar;
	*/
	/* Datos del pago
	private Date fechaPago;
	private String formaDePago;
	private String referencia;
	private String banco;
	private BigDecimal pago;
	*/
	
	public AnalisisDeGasto(){}
	
	
	public AnalisisDeGasto(GCompraDet det){
		
		//Compra
		
		proveedorId=det.getCompra().getProveedor().getId();
		proveedor=det.getCompra().getProveedor().getNombreRazon();
		if(det.getSucursal()!=null){
			sucursalId=det.getSucursal().getId();
			sucursal=det.getSucursal().getNombre();
		}
		if(det.getRubro().getRubroCuentaOrigen()!=null){
			rubroId=det.getRubro().getRubroOperativo().getId();
			rubro=det.getRubro().getRubroOperativo().getDescripcion();
		}else{
			rubroId=det.getRubro().getId();
			rubro=det.getRubro().getDescripcion();
		}
		
		compraId=det.getCompra().getId();
		f_compra=det.getCompra().getFecha();
		
		// Compra Det
		compraDetId=det.getId();
		productoId=det.getProducto().getId();		
		descripcion=det.getProducto().getDescripcion();	
		importe=det.getImporte();
		impuesto_imp=det.getImpuestoImp();	
		ret1_impp=det.getRetencion1Imp();	
		ret2_imp=det.getRetencion2Imp();
		total=det.getTotal();
		ietu=det.getIetu().amount();
		inversion=det.getProducto().getInversion();
		
		//De la factura
		//documento=det.getFactura();
		
		if(!det.getCompra().getFacturas().isEmpty()){
			GFacturaPorCompra fac=det.getCompra().getFacturas().iterator().next();
			totalDoc=fac.getTotalMN().amount();
			f_docto=fac.getFecha();
			documento=fac.getDocumento();
			f_contable=fac.getFechaContable();
		}
		
		
	}
	
	public Long getProveedorId() {
		return proveedorId;
	}
	public void setProveedorId(Long proveedorId) {
		this.proveedorId = proveedorId;
	}
	public String getProveedor() {
		return proveedor;
	}
	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}
	public Long getSucursalId() {
		return sucursalId;
	}
	public void setSucursalId(Long sucursalId) {
		this.sucursalId = sucursalId;
	}
	public String getSucursal() {
		return sucursal;
	}
	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}
	public Long getRubroId() {
		return rubroId;
	}
	public void setRubroId(Long rubroId) {
		this.rubroId = rubroId;
	}
	public String getRubro() {
		return rubro;
	}
	public void setRubro(String rubro) {
		this.rubro = rubro;
	}
	public Long getCompraId() {
		return compraId;
	}
	public void setCompraId(Long compraId) {
		this.compraId = compraId;
	}
	public Date getF_compra() {
		return f_compra;
	}
	public void setF_compra(Date f_compra) {
		this.f_compra = f_compra;
	}
	public Long getCompraDetId() {
		return compraDetId;
	}
	public void setCompraDetId(Long compraDetId) {
		this.compraDetId = compraDetId;
	}
	public Long getProductoId() {
		return productoId;
	}
	public void setProductoId(Long productoId) {
		this.productoId = productoId;
	}
	
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public BigDecimal getImporte() {
		return importe;
	}
	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}
	public BigDecimal getImpuesto_imp() {
		return impuesto_imp;
	}
	public void setImpuesto_imp(BigDecimal impuesto_imp) {
		this.impuesto_imp = impuesto_imp;
	}
	public BigDecimal getRet1_impp() {
		return ret1_impp;
	}
	public void setRet1_impp(BigDecimal ret1_impp) {
		this.ret1_impp = ret1_impp;
	}
	public BigDecimal getRet2_imp() {
		return ret2_imp;
	}
	public void setRet2_imp(BigDecimal ret2_imp) {
		this.ret2_imp = ret2_imp;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	public BigDecimal getIetu() {
		return ietu;
	}
	public void setIetu(BigDecimal ietu) {
		this.ietu = ietu;
	}
	public boolean isInversion() {
		return inversion;
	}
	public void setInversion(boolean inversion) {
		this.inversion = inversion;
	}
	public String getDocumento() {
		return documento;
	}
	public void setDocumento(String documento) {
		this.documento = documento;
	}
	public Date getF_docto() {
		return f_docto;
	}
	public void setF_docto(Date f_docto) {
		this.f_docto = f_docto;
	}
	public BigDecimal getTotalDoc() {
		return totalDoc;
	}
	public void setTotalDoc(BigDecimal totalDoc) {
		this.totalDoc = totalDoc;
	}
	
	private SimpleDateFormat df=new SimpleDateFormat("MMM-yyyy");
	
	public String getPeriodoFac(){
		if(getF_docto()==null)
			return "SIN FECHA";
		return df.format(getF_docto());
	}
	
	public int getMes(){
		if(getF_docto()==null)
			return 0;
		return Periodo.obtenerMes(getF_docto())+1;
	}
	
	public int getYear(){
		if(getF_docto()==null)
			return 0;
		return Periodo.obtenerYear(getF_docto());
	}


	public Date getF_contable() {
		return f_contable;
	}
	
	
}
