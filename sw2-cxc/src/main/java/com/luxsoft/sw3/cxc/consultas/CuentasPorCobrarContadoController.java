package com.luxsoft.sw3.cxc.consultas;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.cfdi.CFDIPrintUI;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.old.ImpresionUtils;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.cxc.ui.form.NotaDevolucionFormModel;
import com.luxsoft.siipap.cxc.ui.form.NotaDevoucionForm;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeDevoluciones;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorNotasDevolucionPorImprimir;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.sw3.cfd.CFDPrintServicesCxC;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.cfdi.model.CFDI;

public class CuentasPorCobrarContadoController {
	

	
	public List<CuentaPorCobrar> buscarCuentas(){
		String sql=SQLUtils.loadSQLQueryFromResource("sql/cuentas_x_cobrar_contado.sql");
		return ServiceLocator2.getJdbcTemplate().query(sql, new BeanPropertyRowMapper(CuentaPorCobrar.class));
	}
	
	public void generarNotasDeDevolucion(){
		Devolucion devolucion=SelectorDeDevoluciones.seleccionar(OrigenDeOperacion.CAM);
		if(devolucion!=null){
			
			NotaDevolucionFormModel fmodel=new NotaDevolucionFormModel(devolucion);
			fmodel.asignarFolio();
			NotaDevoucionForm form=new NotaDevoucionForm(fmodel);
			form.open();
			
			if(!form.hasBeenCanceled()){
				NotaDeCreditoDevolucion nota=fmodel.procesarNota();
				nota=(NotaDeCreditoDevolucion)ServiceLocator2.getCXCManager().salvarNota(nota);
				MessageUtils.showMessage("Nota de credito por devolución generada folio:"+nota.getFolio()+"\ngenerando Comprobante fiscal digital...."
						, "Generación de Notas");
				//ComprobanteFiscal cf=ServiceLocator2.getCFDManager().generarComprobante(nota);
				//MessageUtils.showMessage("Comprobante generado: "+cf.getXmlPath()+ " Folio CFD: "+cf.getFolio(), "Generación de comprobantes");
				//nota.setFolio(Integer.valueOf(cf.getFolio()));
				//ServiceLocator2.getHibernateTemplate().merge(nota);
				//ServiceLocator2.getCXCManager().salvarNota(nota);
				CXCUIServiceFacade.timbrar(nota);
				//CFDPrintServicesCxC.imprimirNotaDeCreditoElectronica(nota.getId());
				//ImpresionUtils.imprimirNotaDevolucion(nres.getId());
			}
		}		
	}	
	/*
	public void imprimirNotaDeMostrador(){
		NotaDeCreditoDevolucion nota=SelectorNotasDevolucionPorImprimir.seleccionar();
		if(nota!=null){
			nota=buscarNotaDeCreditoInicializada(nota.getId());
			ComprobanteFiscal cf=ServiceLocator2.getCFDManager().cargarComprobante(nota);
			if(cf==null){
				cf=ServiceLocator2.getCFDManager().generarComprobante(nota);
				nota.setFolio(Integer.valueOf(cf.getFolio()));
				nota.setReplicado(null);
				ServiceLocator2.getHibernateTemplate().merge(nota);
				MessageUtils.showMessage("Comprobante generado: "+cf.getXmlPath(), "Generación de comprobantes");
			}else{
				CFDPrintServicesCxC.imprimirNotaDeCreditoElectronica(nota.getId());
			}
		}		
	}*/
	
	public void generarCfdiMostrador(){
		NotaDeCreditoDevolucion nota=SelectorNotasDevolucionPorImprimir.seleccionar();
		if(nota!=null){
			nota=buscarNotaDeCreditoInicializada(nota.getId());
			CFDI cfdi=ServiceLocator2.getCFDINotaDeCredito().generar(nota);
			try {
				cfdi=ServiceLocator2.getCFDIManager().timbrar(cfdi);
				String message=MessageFormat.format("Nota generada: {0} a favor de:{1} UUID:{2}"
						,cfdi.getFolio()
						,nota.getCliente().getNombreRazon()
						,cfdi.getUUID());
				MessageUtils.showMessage(message, "CFDI");
				CFDIPrintUI.impripirComprobante(nota, cfdi,"", new Date(), true);
			} catch (Exception e) {
				String msg=MessageFormat.format("Error timbrando o imprimiendo CFDI: {0} Causa:{1}"
						,cfdi.getFolio()
						,ExceptionUtils.getRootCauseMessage(e));
				MessageUtils.showMessage(msg, "Timbrando CFDI");
			}
			
		}		
	}
	
	private static NotaDeCreditoDevolucion buscarNotaDeCreditoInicializada(final String id){
		
		return (NotaDeCreditoDevolucion)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				NotaDeCredito res=(NotaDeCredito)session.get(NotaDeCredito.class, id);
				res.getCliente().getTelefonosRow();
				if(!res.getAplicaciones().isEmpty()){
					res.getAplicaciones().iterator().next();
				}
				if(res instanceof NotaDeCreditoDevolucion){
					NotaDeCreditoDevolucion ndev=(NotaDeCreditoDevolucion)res;
					ndev.getDevolucion().getPartidas().iterator().next();
					ndev.getDevolucion().isTotal();
				}
				return res;
			}
			
		});
		
	}
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		//CuentasPorCobrarContadoController controller=new CuentasPorCobrarContadoController();
		//controller.generarNotaDeDevolucion(179L, 6L);
		ImpresionUtils.imprimirNotaDevolucion("8a8a81c7-2bfe3859-012b-fe3a6552-0003");
	}

}
