package com.luxsoft.sw3.contabilidad.ui.reportes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class DIOTPanel extends FilteredBrowserPanel<DIOTPanel.Diot> {

	public DIOTPanel() {
		super(DIOTPanel.Diot.class);
	}

	@Override
	protected void init() {
		addProperty("Proveedor", "rfc","ivaNota", "baseTotal", "ivaAcreditable","ivaRet","baseExcenta");
		addLabels("Proveedor", "Rfc","ivaNota", "baseTotal", "ivaAcreditable","ivaRet","baseExcenta");		
	}

	protected void actualizar(
			EventList<AnalisisDeDIOTPanel.AnalisisDIOT> analisis) {
		source.clear();

		Diot generico = new Diot();
		generico.setProveedor("PROVEEDOR GENERICO");
		generico.setRfc("XX");
		
		for (AnalisisDeDIOTPanel.AnalisisDIOT a : analisis) {
			if (a.getBASE_CALCULADA().doubleValue() >= 50000d) {
				Diot d = new Diot(a);
				d.setRfc(buscarRfc(d.getProveedor(), a.getORIGEN()));
				source.add(d);

			} else {
				generico.addBase(a.getBASE_CALCULADA());
				generico.addIva(a.getIVA_ACRED());
				generico.addNota(a.getIVA_NOTA());
				generico.addRetencion(a.getIVA_RET());
				
			}			
			generico.setBaseExcenta(generico.getBaseExcenta().add(a.getEXENTO()));
		}
		source.add(generico);
		BigDecimal val=BigDecimal.ZERO;
		for (AnalisisDeDIOTPanel.AnalisisDIOT a : analisis) {
			val=val.add(a.getEXENTO());			
		}
		System.out.println("Excento: "+val+ " Partidas: "+analisis.size());		
	}
	
	public void generarCargaBatch(){
		List<String> lineas=new ArrayList<String>();
		String pattern = "|04|85|{0}|||||{1}|||||||||||{2}|{3}|{4}|{5}";
		for (Object row : source) {
			DIOTPanel.Diot d=(DIOTPanel.Diot)row;
			
			
			String line = MessageFormat.format(pattern,
					StringUtils.substring(d.getRfc(), 0, 13)
					,d.getBaseTotal().setScale(0, RoundingMode.HALF_EVEN).equals(new BigDecimal(0)) ? "" :d.getBaseTotal().setScale(0, RoundingMode.HALF_EVEN).toString()
					,d.getBaseExcenta().setScale(0, RoundingMode.HALF_EVEN).equals(new BigDecimal(0)) ? "" : d.getBaseExcenta().setScale(0, RoundingMode.HALF_EVEN).toString()
					,d.getBaseExcenta().setScale(0, RoundingMode.HALF_EVEN).equals(new BigDecimal(0)) ? "" : d.getBaseExcenta().setScale(0, RoundingMode.HALF_EVEN).toString()
					,d.getIvaRet().setScale(0, RoundingMode.HALF_EVEN).equals(new BigDecimal(0)) ? "" :d.getIvaRet().setScale(0, RoundingMode.HALF_EVEN).toString()
					,d.getIvaNota().setScale(0, RoundingMode.HALF_EVEN).equals(new BigDecimal(0)) ? "" :d.getIvaNota().setScale(0, RoundingMode.HALF_EVEN).toString()		
					// ,new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(cfd.getFecha())
					);
			lineas.add(line);
		}
		
		try {
			InformesUtils.salvarEnArchivoDeTexto(lineas);
		} catch (Exception e) {			
			throw new RuntimeException(e);
		}		
	}

	public String buscarRfc(String proveedor, String tipo) {
		if ("COMPRAS".equalsIgnoreCase(tipo)) {
			List<String> res = ServiceLocator2.getHibernateTemplate().find(
					"select p.Rfc from Proveedor p where p.nombre like ?",
					proveedor);
			if (!res.isEmpty()) {
				return res.get(0);
			} else
				return "NA";
		} else {
			List<String> res = ServiceLocator2.getHibernateTemplate().find(
					"select p.Rfc from GProveedor p where p.nombre like ?",
					proveedor);
			if (!res.isEmpty()) {
				return res.get(0);
			} else
				return "NA";
		}
	}

	public static class Diot {

		private String proveedor;
		private String rfc;
		private BigDecimal ivaNota = BigDecimal.ZERO;
		private BigDecimal baseTotal = BigDecimal.ZERO;
		private BigDecimal ivaAcreditable = BigDecimal.ZERO;
		private BigDecimal ivaRet = BigDecimal.ZERO;
		private BigDecimal baseExcenta=BigDecimal.ZERO;

		public Diot() {
		}

		public Diot(AnalisisDeDIOTPanel.AnalisisDIOT a) {
			setProveedor(a.getPROVEEDOR());
			setIvaNota(a.getIVA_NOTA());
			setIvaRet(a.getIVA_RET());
			setBaseTotal(a.getBASE_CALCULADA());
			setIvaAcreditable(a.getIVA_ACRED());
		}

		public String getProveedor() {
			return proveedor;
		}

		public void setProveedor(String proveedor) {
			this.proveedor = proveedor;
		}

		public String getRfc() {
			return rfc;
		}

		public void setRfc(String rfc) {
			this.rfc = rfc;
		}
		public BigDecimal getIvaNota() {
			return ivaNota;
		}

		public void setIvaNota(BigDecimal ivaNota) {
			this.ivaNota = ivaNota;
		}
		public BigDecimal getIvaRet() {
			return ivaRet;
		}

		public void setIvaRet(BigDecimal ivaRet) {
			this.ivaRet = ivaRet;
		}

		public BigDecimal getBaseTotal() {
			return baseTotal;
		}

		public void setBaseTotal(BigDecimal baseTotal) {
			this.baseTotal = baseTotal;
		}

		public BigDecimal getIvaAcreditable() {
			return ivaAcreditable;
		}

		public void setIvaAcreditable(BigDecimal ivaAcreditable) {
			this.ivaAcreditable = ivaAcreditable;
		}

		public void addBase(BigDecimal valor) {
			BigDecimal vv = PolizaUtils.redondear(valor);
			setBaseTotal(getBaseTotal().add(vv));
		}
		
		public void addNota(BigDecimal valor) {
			BigDecimal vv = PolizaUtils.redondear(valor);
			setIvaNota(getIvaNota().add(vv));
		}
		public void addRetencion(BigDecimal valor) {
			BigDecimal vv = PolizaUtils.redondear(valor);
			setIvaRet(getIvaRet().add(vv));
		}
		

		public BigDecimal getBaseExcenta() {
			if(baseExcenta==null)
				baseExcenta=BigDecimal.ZERO;
			return baseExcenta;
		}

		public void setBaseExcenta(BigDecimal baseExcenta) {
			this.baseExcenta = baseExcenta;
		}

		public void addIva(BigDecimal valor) {
			BigDecimal vv = PolizaUtils.redondear(valor);
			setIvaAcreditable(getIvaAcreditable().add(vv));
		}
	}

	

}
