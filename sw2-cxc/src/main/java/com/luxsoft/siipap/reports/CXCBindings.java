package com.luxsoft.siipap.reports;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
//import com.luxsoft.siipap.cxc.domain.Abogado;
//import com.luxsoft.siipap.cxc.domain.ConceptoDeBonificacion;
//import com.luxsoft.siipap.cxc.domain.FormaDePago;
//import com.luxsoft.siipap.cxc.domain.TiposDeNotas;
//import com.luxsoft.siipap.cxc.model.Cobradores;
import com.luxsoft.siipap.cxc.model.Abogado;
import com.luxsoft.siipap.cxc.model.Cobradores;
import com.luxsoft.siipap.cxc.model.TiposDeNotas;

public final class CXCBindings {
	
	public static JComboBox createBindingDeNotasGenerales(final ValueModel vm){
		//List<String> tipos=TiposDeNotas.getStringIds();
		String[] si={"L","T","M"};
		SelectionInList sl=new SelectionInList(si,vm);		
		JComboBox box=BasicComponentFactory.createComboBox(sl,new TipoToStringCellRenderer());		
		return box;
	
	}
	
	public static JComboBox createTipoDeNotaBindingParaDescuentos(final ValueModel vm){		
		String[] si={"U","V"};
		SelectionInList sl=new SelectionInList(si,vm);		
		JComboBox box=BasicComponentFactory.createComboBox(sl,new TipoToStringCellRenderer());		
		return box;
	
	}
	
	public static JComboBox createBindingDeTipoDeNotasPorDescuentoParaImpresion(final ValueModel vm){
		SelectionInList sl=new SelectionInList(new Object[]{TiposDeNotas.U,TiposDeNotas.V},vm);
		JComboBox tipo=BasicComponentFactory.createComboBox(sl,new TipoCellRenderer());
		return tipo;
	}
	
	public static JComboBox createBindingParaAltas(final ValueModel vm){
		SelectionInList sl=new SelectionInList(TiposDeNotas.values(),vm);
		return BasicComponentFactory.createComboBox(sl,new TipoCellRenderer()); 
	}
	
	public static JComboBox createEjecutivosBinding(final ValueModel vm){
		String[] names={
				"José Sánchez"
				,"Rafael Sánchez"
				,"José Antonio Sánchez"};
		SelectionInList sl=new SelectionInList(names,vm);
		return BasicComponentFactory.createComboBox(sl);
	}
	
/*	public static JComboBox createFormaDePagoBinding(final ValueModel vm){
		List<FormaDePago> formas=new ArrayList<FormaDePago>();
		formas.add(FormaDePago.E);
		formas.add(FormaDePago.H);
		formas.add(FormaDePago.C);
		formas.add(FormaDePago.B);
		formas.add(FormaDePago.Q);
		formas.add(FormaDePago.N);
		formas.add(FormaDePago.X);
		formas.add(FormaDePago.O);
		formas.add(FormaDePago.Y);
		final SelectionInList sl=new SelectionInList(formas,vm);		
		return BasicComponentFactory.createComboBox(sl);
	}*/
	
/*	public static JComboBox createFormaDePagoCompletaBinding(final ValueModel vm){
		List<FormaDePago> formas=Arrays.asList(FormaDePago.values());
		final SelectionInList sl=new SelectionInList(formas,vm);		
		return BasicComponentFactory.createComboBox(sl);
	}*/
	/*
	public static JComboBox createConceptosDeBonificacionBinding(final ValueModel vm){
		List<ConceptoDeBonificacion> list=new ArrayList<ConceptoDeBonificacion>();
		list.add(ConceptoDeBonificacion.BONIFICACION);
		list.add(ConceptoDeBonificacion.RECLAMACION);
		final SelectionInList sl=new SelectionInList(list,vm);		
		return BasicComponentFactory.createComboBox(sl);
	}
	
	public static JComboBox createConceptosDeFinancierosBinding(final ValueModel vm){
		List<ConceptoDeBonificacion> list=new ArrayList<ConceptoDeBonificacion>();
		list.add(ConceptoDeBonificacion.ADICIONAL);
		list.add(ConceptoDeBonificacion.FINANCIERO);
		final SelectionInList sl=new SelectionInList(list,vm);		
		return BasicComponentFactory.createComboBox(sl);
	}*/
	
	public static JComboBox createBancosBinding(final ValueModel vm){
		final SelectionInList sl=new SelectionInList(new String[]{
				"BANCOMER","BANAMEX","BAM","HSBC","SERFIN","BANORTE","SANTANDER","INBURSA","AFIRME","SCOTIA BANK","AMEX","MULTIVA","IXE"},vm);
		return BasicComponentFactory.createComboBox(sl);
	}
	
	public static JComboBox createCuentasDeposito(final ValueModel vm){
		final SelectionInList sl=new SelectionInList(
				new String[]{
						 "BANCOMER   (116228100)"
						,"BANAMEX   (1858193)"
						,"HSBC	    (4019118074)"
						,"SCOTTIA   (1691945)"
						,"SANTANDER (92000395043)"
						,"SANTANDER (65502194067)"
						,"UBS AG-NY (406546)"
						}
				,vm);
		return BasicComponentFactory.createComboBox(sl);
	}
	
	public static JComboBox createAbogadoBinding(final ValueModel vm){
		final List<Abogado> list =new ArrayList<Abogado>();
		list.add(new Abogado(1,"Abogado 1"));
		final SelectionInList sl=new SelectionInList(list);
		return BasicComponentFactory.createComboBox(sl);
	}
	
	public static JComboBox createOperadorBinding(final ValueModel vm){		
		final SelectionInList sl=new SelectionInList(new Integer[]{1,2,3},vm);
		return BasicComponentFactory.createComboBox(sl,new TransformCellRenderer(){
			@Override
			protected String transform(Object value) {
				return value!=null?value.toString():"";				
			}			
		});
	}
	
	public static JComboBox createCobradorBinding(final ValueModel vm){		
		final SelectionInList sl=new SelectionInList(Cobradores.todos(),vm);
		return BasicComponentFactory.createComboBox(sl,new TransformCellRenderer(){
			@Override
			protected String transform(Object value) {
				if(value==null) return "NA";
				return Cobradores.getCobrador(Integer.valueOf(value.toString())).toString();				
			}			
		});
	}
	
	public static abstract class TransformCellRenderer extends DefaultListCellRenderer{
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			
			JLabel l=(JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			l.setText(transform(value));
			return l;
		}
		
		protected abstract String transform(final Object value);
	}
	
	public static class TipoToStringCellRenderer extends DefaultListCellRenderer{

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if(value!=null){
				
				String s=value.toString();
				TiposDeNotas t=TiposDeNotas.valueOf(s);
				if(t!=null)
					value=t.getDesc();
				else
					System.out.println("No encotre: "+value);
				
			}
			return super.getListCellRendererComponent(list, value, index, isSelected,cellHasFocus);
		
		}	
	}
	
	public static class TipoCellRenderer extends DefaultListCellRenderer{

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			
			if(value!=null){				
				TiposDeNotas t=(TiposDeNotas)value;
				if(t!=null)
					value=t.getDesc();
				
				
			}
			return super.getListCellRendererComponent(list, value, index, isSelected,cellHasFocus);
		
		}	
	}

}
