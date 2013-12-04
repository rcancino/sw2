/**
 * 
 */
package com.luxsoft.siipap.gastos.consultas;

import java.math.BigDecimal;
import java.util.Date;

import com.luxsoft.siipap.model.Periodo;

public class Analisis2{
	private BigDecimal CLASE_ID;
	private String DESCRIP_RUBRO;
	private String NOMBRE;
	private BigDecimal COMPRA_ID;
	private Date F_COMPRA;
	private BigDecimal GCOMPRADET_ID;
	private String DESCRIPCION;
	private BigDecimal IMPORTE;
	private BigDecimal IMPUESTO_IMP;
	private BigDecimal RET1_IMPP;
	private BigDecimal RET2_IMP;
	private BigDecimal TOTAL;
	private BigDecimal TOT_COMP;
	private BigDecimal IETU;
	private BigDecimal INVERSION;
	private String DOCUMENTO;
	private Date F_DOCTO;
	private BigDecimal TOT_FACT;
	private String ORIGEN;
	private BigDecimal REQUISICION_ID;
	private Date F_REQ;
	private BigDecimal TOT_REQ;
	private Date F_PAGO;
	private String FORMADP;
	private String REFERENCIA;
	private String BANCO;
	private String TOT_PAG;
	private String SUCURSAL;
	private String REF_CONTABLE;
	
	
	public Analisis2() {
	}
	
	public BigDecimal getCLASE_ID() {
		return CLASE_ID;
	}
	public void setCLASE_ID(BigDecimal clase_id) {
		CLASE_ID = clase_id;
	}
	public String getDESCRIP_RUBRO() {
		return DESCRIP_RUBRO;
	}
	public void setDESCRIP_RUBRO(String descrip_rubro) {
		DESCRIP_RUBRO = descrip_rubro;
	}
	public String getNOMBRE() {
		return NOMBRE;
	}
	public void setNOMBRE(String nombre) {
		NOMBRE = nombre;
	}
	public BigDecimal getCOMPRA_ID() {
		return COMPRA_ID;
	}
	public void setCOMPRA_ID(BigDecimal compra_id) {
		COMPRA_ID = compra_id;
	}
	public Date getF_COMPRA() {
		return F_COMPRA;
	}
	public void setF_COMPRA(Date f_compra) {
		F_COMPRA = f_compra;
	}
	public BigDecimal getGCOMPRADET_ID() {
		return GCOMPRADET_ID;
	}
	public void setGCOMPRADET_ID(BigDecimal gcompradet_id) {
		GCOMPRADET_ID = gcompradet_id;
	}
	public String getDESCRIPCION() {
		return DESCRIPCION;
	}
	public void setDESCRIPCION(String descripcion) {
		DESCRIPCION = descripcion;
	}
	public BigDecimal getIMPORTE() {
		return IMPORTE;
	}
	public void setIMPORTE(BigDecimal importe) {
		IMPORTE = importe;
	}
	public BigDecimal getIMPUESTO_IMP() {
		return IMPUESTO_IMP;
	}
	public void setIMPUESTO_IMP(BigDecimal impuesto_imp) {
		IMPUESTO_IMP = impuesto_imp;
	}
	public BigDecimal getRET1_IMPP() {
		return RET1_IMPP;
	}
	public void setRET1_IMPP(BigDecimal ret1_impp) {
		RET1_IMPP = ret1_impp;
	}
	public BigDecimal getRET2_IMP() {
		return RET2_IMP;
	}
	public void setRET2_IMP(BigDecimal ret2_imp) {
		RET2_IMP = ret2_imp;
	}
	public BigDecimal getTOTAL() {
		return TOTAL;
	}
	public void setTOTAL(BigDecimal total) {
		TOTAL = total;
	}
	public BigDecimal getTOT_COMP() {
		return TOT_COMP;
	}
	public void setTOT_COMP(BigDecimal tot_comp) {
		TOT_COMP = tot_comp;
	}
	public BigDecimal getIETU() {
		return IETU;
	}
	public void setIETU(BigDecimal ietu) {
		IETU = ietu;
	}
	public BigDecimal getINVERSION() {
		return INVERSION;
	}
	public void setINVERSION(BigDecimal inversion) {
		INVERSION = inversion;
	}
	public String getDOCUMENTO() {
		return DOCUMENTO;
	}
	public void setDOCUMENTO(String documento) {
		DOCUMENTO = documento;
	}
	public Date getF_DOCTO() {
		return F_DOCTO;
	}
	public void setF_DOCTO(Date f_docto) {
		F_DOCTO = f_docto;
	}
	public BigDecimal getTOT_FACT() {
		return TOT_FACT;
	}
	public void setTOT_FACT(BigDecimal tot_fact) {
		TOT_FACT = tot_fact;
	}
	public String getORIGEN() {
		return ORIGEN;
	}
	public void setORIGEN(String origen) {
		ORIGEN = origen;
	}
	public BigDecimal getREQUISICION_ID() {
		return REQUISICION_ID;
	}
	public void setREQUISICION_ID(BigDecimal requisicion_id) {
		REQUISICION_ID = requisicion_id;
	}
	public Date getF_REQ() {
		return F_REQ;
	}
	public void setF_REQ(Date f_req) {
		F_REQ = f_req;
	}
	public BigDecimal getTOT_REQ() {
		return TOT_REQ;
	}
	public void setTOT_REQ(BigDecimal tot_req) {
		TOT_REQ = tot_req;
	}
	public Date getF_PAGO() {
		return F_PAGO;
	}
	public void setF_PAGO(Date f_pago) {
		F_PAGO = f_pago;
	}
	public String getFORMADP() {
		return FORMADP;
	}
	public void setFORMADP(String formadp) {
		FORMADP = formadp;
	}
	public String getREFERENCIA() {
		return REFERENCIA;
	}
	public void setREFERENCIA(String referencia) {
		REFERENCIA = referencia;
	}
	public String getBANCO() {
		return BANCO;
	}
	public void setBANCO(String banco) {
		BANCO = banco;
	}
	public String getTOT_PAG() {
		return TOT_PAG;
	}
	public void setTOT_PAG(String tot_pag) {
		TOT_PAG = tot_pag;
	}
	
	public int getMes(){
		if(F_DOCTO!=null)
			return Periodo.obtenerMes(F_DOCTO)+1;
		return 0;
	}

	public String getSUCURSAL() {
		return SUCURSAL;
	}

	public void setSUCURSAL(String sucursal) {
		SUCURSAL = sucursal;
	}

	public String getREF_CONTABLE() {
		return REF_CONTABLE;
	}

	public void setREF_CONTABLE(String cta_contable) {
		REF_CONTABLE = cta_contable;
	}
	
	
	
	
}