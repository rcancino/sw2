package com.luxsoft.siipap.cxp.ui.consultas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;


import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.contabilidad.Poliza;
import com.luxsoft.siipap.service.contabilidad.ExportadorGenericoDePolizas;
import com.luxsoft.siipap.swing.binding.Binder;


public class PolizaAlmacenPorProveedorPanel extends PolizaAlmacenPanel implements PropertyChangeListener{
	
	private PolizaDeAlmacenPorProveedorModel model;
	
	protected void init(){
		addProperty(new String[]{"fecha","tipo","concepto","sucursalNombre","debe","haber","cuadre","year","mes"});
		addLabels(  new String[]{"Fecha","Tipo","Concepto","Sucursal","Debe","Haber","Cuadre","year","mes"});
		model=new PolizaDeAlmacenPorProveedorModel();
		Date fecha=new Date();
		yearModel=new ValueHolder(Periodo.obtenerYear(fecha));
		yearModel.addValueChangeListener(this);
		mesModel=new ValueHolder(Periodo.obtenerMes(fecha)+1);
		mesModel.addValueChangeListener(this);
		manejarPeriodo();
		model.setProveedor("I001");
		
	}
	
	private HeaderPanel header;
	
	@Override
	protected JComponent buildHeader(){
		if(header==null){
			header=new HeaderPanel("Póliza de Almacén IMPAP ","Año:  Mes:");
			updateHeader();
		}
		return header;
	}
	
	private void updateHeader(){
		if(header!=null)
			header.setDescription("Periodo: "+getYear()+"/"+getMes());
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		updateHeader();
	}
	
	public void cambiarPeriodo(){
		AbstractDialog dialog=Binder.createSelectorMesYear(yearModel, mesModel);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			periodo=Periodo.getPeriodoEnUnMes(getMes()-1, getYear());
			nuevoPeriodo(periodo);
			updatePeriodoLabel();
		}
		
	}
		
	public List<Poliza> findData(){
		return model.generarPoliza(periodo);
	}
	
	private DateFormat df=new SimpleDateFormat("ddMM");
	
	public void generarPoliza(){
		if(!getSelected().isEmpty()){
			for(Object o:getSelected()){
				Poliza poliza=(Poliza)o;
				String suc=StringUtils.leftPad(String.valueOf(poliza.getSucursalId()), 2,'0');
				poliza.setExportName("C"+suc+df.format(poliza.getFecha())+".POL");
				poliza.ordenarPartidas("descripcion2", "descripcion3");
				File res=ExportadorGenericoDePolizas.exportarACoi(poliza, null,"META-INF/templates/Poliza_Compras.ftl");
				JOptionPane.showMessageDialog(getControl(), "Poliza generada en: \n"+res.getPath(),"Exportador a COI",JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	
	
	private ValueModel yearModel;
	
	private ValueModel mesModel;

	
	public Integer getMes(){
		return (Integer)mesModel.getValue();
	}
	public Integer getYear(){
		return (Integer)yearModel.getValue();
	}
		
}
