package com.luxsoft.siipap.swing.utils;

import java.awt.Color;
import java.awt.Component;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.renderer.StringValue;

import com.lowagie.text.Font;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.swing.controls.BooleanCellRenderer;

public class Renderers {
	
	public static TableCellRenderer buildBoldDecimalRenderer(int decimales){
		return new BoldDecimalRenderer(decimales);
	}
	
	public static TableCellRenderer buildIntegerRenderer(){
		return new IntegerRenderer();
	}
	
	public static TableCellRenderer buildDefaultNumberRenderer(){
		return new DefaultNumberRenderer();
	}
	public static TableCellRenderer buildRightAllignRenderer(){
		return new RightAlligmentRenderer();
	}
	
	public static TableCellRenderer getCantidadMonetariaTableCellRenderer(){
		return new CantidadMonetariaRenderer();
	}
	public static TableCellRenderer getCantidadNormalTableCellRenderer(){
		return new CantidadMonetariaStdRenderer();
	}
	
	public static TableCellRenderer getUtilidadPorcRenderer(){
		return new UtilidadPorcentageRenderer();
	}
	
	public static TableCellRenderer getPorcentageRenderer(int multiplier){
		return new PorcentageRenderer(multiplier);
	}
	
	public static TableCellRenderer getPorcentageRenderer(){
		return new PorcentageRenderer();
	}
	public static TableCellRenderer getBooleanRenderer(){
		return new BooleanCellRenderer();
	}
	
	public static TableCellRenderer getTipoDeCambioRenderer(){
		return new TipoDeCambioRenderer();
	}
	
	public static  class IntegerRenderer extends DefaultTableCellRenderer{
		
		private NumberFormat nf;

		public IntegerRenderer() {
			super();
			nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(0);
			nf.setGroupingUsed(false);
		}

		@Override
		protected void setValue(Object value) {			
			if(value instanceof CantidadMonetaria){
				CantidadMonetaria monto=(CantidadMonetaria)value;
				long  vv=monto.getAmount().longValue();
				long nv=vv;
				setValue(nf.format(nv));
				setHorizontalAlignment(JLabel.RIGHT);
			}else if(value instanceof Number){
				Number val=(Number)value;
				long  vv=val.longValue();
				long nv=vv;
				setValue(nf.format(nv));
				setHorizontalAlignment(JLabel.RIGHT);
				
			}else{
				setHorizontalAlignment(JLabel.LEFT);
				super.setValue(value);
			}
				
		}
		
		
	}
	
	public static  class DefaultNumberRenderer extends DefaultTableCellRenderer{
		
		private NumberFormat nf;

		public DefaultNumberRenderer() {
			super();
			nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(0);
		}

		@Override
		protected void setValue(Object value) {			
			if(value instanceof CantidadMonetaria){
				CantidadMonetaria monto=(CantidadMonetaria)value;
				long  vv=monto.getAmount().longValue();
				long nv=vv/1000;
				setValue(nf.format(nv));
				setHorizontalAlignment(JLabel.RIGHT);
			}else if(value instanceof Number){
				Number val=(Number)value;
				long  vv=val.longValue();
				long nv=vv/1000;
				setValue(nf.format(nv));
				setHorizontalAlignment(JLabel.RIGHT);
				
			}else{
				setHorizontalAlignment(JLabel.LEFT);
				super.setValue(value);
			}
				
		}
		
		
	}
	
	public static  class CantidadMonetariaRenderer extends DefaultTableCellRenderer{
		
		private NumberFormat nf;

		public CantidadMonetariaRenderer() {
			super();
			nf=NumberFormat.getCurrencyInstance();
			nf.setMaximumFractionDigits(2);
		}

		@Override
		protected void setValue(Object value) {			
			if(value instanceof CantidadMonetaria){
				CantidadMonetaria monto=(CantidadMonetaria)value;				
				setValue(nf.format(monto.amount().doubleValue()));
				setHorizontalAlignment(JLabel.RIGHT);
			}else{
				setHorizontalAlignment(JLabel.LEFT);
				super.setValue(value);
			}
				
		}
		
		
	}
	
	private static class RightAlligmentRenderer extends DefaultTableCellRenderer{

		public RightAlligmentRenderer() {
			super();
			setHorizontalAlignment(JLabel.RIGHT);
		}		
		
		
	}
	
	public static class UtilidadPorcentageRenderer extends DefaultTableCellRenderer{
		
		private NumberFormat nf;

		public UtilidadPorcentageRenderer() {
			super();
			setHorizontalAlignment(JLabel.RIGHT);
			nf=NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(2);
			
		}

		@Override
		protected void setValue(Object value) {
			if(value instanceof Number){
				Number nn=(Number)value;
				double dd=nn.doubleValue();
				if(dd<0)
					setForeground(Color.red);
				else
					setForeground(Color.black);
				setValue(nf.format(dd));
			}else
				super.setValue(value);
		}
		
		
		
	}
	
	public static class PorcentageRenderer extends DefaultTableCellRenderer{
		
		private NumberFormat nf;
		

		public PorcentageRenderer(int multiplier){
			super();
			setHorizontalAlignment(JLabel.RIGHT);
			//nf=NumberFormat.getNumberInstance();
			nf=NumberFormat.getPercentInstance();
			DecimalFormat df=(DecimalFormat)nf;
			df.setMultiplier(multiplier);
			nf.setMaximumFractionDigits(2);
			nf.setMinimumFractionDigits(2);
		}
		
		public PorcentageRenderer() {
			this(1);
		}

		@Override
		protected void setValue(Object value) {
			if(value instanceof Number){
				Number nn=(Number)value;
				double dd=nn.doubleValue();				
				setValue(nf.format(dd));
			}else
				super.setValue(value);
		}				
		
		
	}
	
		
	public static  class CantidadMonetariaStdRenderer extends DefaultTableCellRenderer{
		
		private NumberFormat nf;

		public CantidadMonetariaStdRenderer() {
			super();
			nf=NumberFormat.getCurrencyInstance();
			nf.setMaximumFractionDigits(2);
		}

		@Override
		protected void setValue(Object value) {			
			if(value instanceof Number){
				double monto=((Number)value).doubleValue();				
				setValue(nf.format(monto));
				setHorizontalAlignment(JLabel.RIGHT);
			}else{
				setHorizontalAlignment(JLabel.LEFT);
				super.setValue(value);
			}
			setFont(getFont().deriveFont(Font.BOLD));	
		}
		
		
	}
	
	public static class TipoDeCambioRenderer extends DefaultTableCellRenderer{
		
		private NumberFormat nf;
		

		public TipoDeCambioRenderer(){
			super();
			setHorizontalAlignment(JLabel.RIGHT);
			nf=NumberFormat.getNumberInstance();
			//nf=NumberFormat.getPercentInstance();
			//DecimalFormat df=(DecimalFormat)nf;
			//df.setGroupingUsed(newValue)
			nf.setMaximumFractionDigits(5);
			nf.setMinimumFractionDigits(2);
		}		

		@Override
		protected void setValue(Object value) {
			if(value instanceof Number){
				Number nn=(Number)value;
				double dd=nn.doubleValue();				
				setValue(nf.format(dd));
			}else
				super.setValue(value);
		}				
		
		
	}
	
	public static class ToHourConverter implements StringValue{
		
		final DateFormat df=new SimpleDateFormat("dd/MM/yyyy :hh:mm");

		public String getString(Object value) {
			try {
				return df.format(value);
			} catch (Exception e) {				
				if(value!=null)
					return value.toString();
				else
					return "";
			}
			
		}
		
	}
	
	/*public static class TipoDeOperacionRenderer extends DefaultListCellRenderer{
		
		static Map<OrigenDeOperacion, String> map=new HashMap<OrigenDeOperacion, String>();
		static{
			for(OrigenDeOperacion o:OrigenDeOperacion.values()){
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			
			super.getListCellRendererComponent(list, value, index, isSelected,cellHasFocus);
			OrigenDeOperacion o=(OrigenDeOperacion)value;
			if(o!=null)
				setText(o.name());
			return this;
		}
		
	}
*/
	public static class BoldDecimalRenderer extends DefaultTableCellRenderer{
		
		private NumberFormat nf;
		 

		public BoldDecimalRenderer(int decimales){
			super();
			setHorizontalAlignment(JLabel.RIGHT);
			nf=NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(decimales);
			//nf.setMinimumFractionDigits(2);
			
		}
		
		public BoldDecimalRenderer() {
			this(1);
		}
		
		

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {			
			Component res=super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			setFont(getFont().deriveFont(Font.BOLD));
			return res;
		}

		@Override
		protected void setValue(Object value) {
			if(value instanceof Number){
				Number nn=(Number)value;
				double dd=nn.doubleValue();				
				setValue(nf.format(dd));
			}else
				super.setValue(value);
		}
		
	}
}
