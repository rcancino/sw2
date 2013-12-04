package com.luxsoft.siipap.cxp.service;

import java.util.List;

import com.luxsoft.siipap.cxp.model.CXPAbono;
import com.luxsoft.siipap.cxp.model.CXPAnticipo;
import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPNota;
import com.luxsoft.siipap.cxp.model.CXPPago;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.Requisicion;

public interface CXPAbonosManager {
	
	public CXPNota buscarNota(final Long id);
	
	public CXPAnticipo buscarAnticipo(final Long id);
	
	public CXPPago aplicarPago(final Requisicion r);
	
	public CXPAnticipo aplicarAnticipo(final Requisicion r);
	
	public  CXPPago salvarPago(final CXPPago pago);	
	
	public  CXPAnticipo salvarAnticipo(final CXPAnticipo anticipo);
	
	public void eliminarPago(final Long pagoId);
	
	public void eliminarNota(final Long notaId);
	
	public List<CXPAbono> buscarAbonos(final Periodo p);
	
	public  CXPNota salvarNota(final CXPNota nota);
	
	public List<CXPAplicacion> buscarAplicaciones(final CXPAbono id);
	
	public CXPAbono registrarDiferencia(final CXPAbono abono);

}
