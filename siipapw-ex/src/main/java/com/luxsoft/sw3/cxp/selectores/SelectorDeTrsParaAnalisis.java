package com.luxsoft.sw3.cxp.selectores;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JToolBar;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.util.ActionLabel;

import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.util.DBUtils;

/**
 * Selector de entradas por compra para analizar flete
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SelectorDeTrsParaAnalisis extends AbstractSelector<TransformacionDet>{
	
	Periodo periodo;
	
	public SelectorDeTrsParaAnalisis() {
		super(TransformacionDet.class, "TRS por sin analizar");
		periodo=Periodo.periodoDeloquevaDelMes();
	}

	@Override
	protected List<TransformacionDet> getData() {
		String hql="from TransformacionDet e  " +
		"  where e.fecha between ? and ? " +
		"    and e.analisis is null ";
		return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}
	
	public void adjustGrid(final JXTable grid){
	}
	
	protected ActionLabel periodoLabel;
	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);		
		//builder.add(CommandUtils.createLoadAction(this, "load"));
		builder.add(getPeriodoLabel());
		builder.add(buildFilterPanel());
		addButton(builder);
		return builder.getToolBar();
	}
	
	
	public ActionLabel getPeriodoLabel(){
		if(periodoLabel==null){
			periodoLabel=new ActionLabel("Periodo: "+periodo.toString());
			periodoLabel.addActionListener(EventHandler.create(ActionListener.class, this, "cambiarPeriodo"));
		}
		return periodoLabel;
	}
	
	public void cambiarPeriodo(){
		ValueHolder holder=new ValueHolder(periodo);
		AbstractDialog dialog=Binder.createPeriodoSelector(holder);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			periodo=(Periodo)holder.getValue();
			periodoLabel.setText("Per:" +periodo.toString());
			load();
		}
	}
	
	
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(750,400));
	}

	@Override
	protected TableFormat<TransformacionDet> getTableFormat() {
		
		String[] props={
				"sucursal.nombre"
				,"conceptoOrigen"
				,"documento"
				,"renglon"
				,"clave"
				,"descripcion"
				,"cantidad"
				,"kilosCalculados"
				,"analisis"
				,"comentario"
				};
		String[] names={
				"Sucursal"
				,"Tipo"
				,"Docto"
				,"Rngl"
				,"Producto"
				,"Descripcion"
				,"Cantidad"
				,"Kilos"
				,"Analisis"
				,"Comentario"
				};
		return GlazedLists.tableFormat(TransformacionDet.class, props,names);
	}

	@Override
	protected TextFilterator<TransformacionDet> getBasicTextFilter() {
		return GlazedLists.textFilterator("documento","producto.clave","producto.descripcion");
	}
	
	
	public static List<TransformacionDet> pendientesDeAnalizar(){
		SelectorDeTrsParaAnalisis selector=new SelectorDeTrsParaAnalisis(){
			@Override
			protected List<TransformacionDet> getData() {
				String hql="from TransformacionDet e  " +
				"  where e.fecha between ? and ? " +
				"   and e.cantidad>0" +
				"   and e.analisis is null" +
				"   and e.conceptoOrigen in(\'MET\') ";
				return ServiceLocator2.getHibernateTemplate().find(hql
						,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
			}
		};
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();		
		if(!selector.hasBeenCanceled()){
			return new ArrayList<TransformacionDet>(selector.getSelectedList());
		}
		return ListUtils.EMPTY_LIST;
	}
	
	public static List<TransformacionDet> seleccionarEntradasParaFlete(){
		SelectorDeTrsParaAnalisis selector=new SelectorDeTrsParaAnalisis(){
			@Override
			protected List<TransformacionDet> getData() {
				String hql="from TransformacionDet e  " +
				"  where e.fecha between ? and ? " +
				"   and e.cantidad>0" +
				"   and e.analisisFlete is null" +
				"   and e.conceptoOrigen in(\'TRS\',\'MET\') ";
				return ServiceLocator2.getHibernateTemplate().find(hql
						,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
			}
		};
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();		
		if(!selector.hasBeenCanceled()){
			return new ArrayList<TransformacionDet>(selector.getSelectedList());
		}
		return ListUtils.EMPTY_LIST;
	}
	
	public static List<TransformacionDet> seleccionarEntradasParaAnalisisHojeo(){
		SelectorDeTrsParaAnalisis selector=new SelectorDeTrsParaAnalisis(){
			@Override
			protected List<TransformacionDet> getData() {
				String hql="from TransformacionDet e  " +
				"  where e.fecha between ? and ? " +
				"   and e.cantidad>0" +
				"   and e.analisisHojeo is null "+
				"   and e.conceptoOrigen in(\'TRS\',\'MET\') ";
				return ServiceLocator2.getHibernateTemplate().find(hql
						,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
			}
		};
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();		
		if(!selector.hasBeenCanceled()){
			return new ArrayList<TransformacionDet>(selector.getSelectedList());
		}
		return ListUtils.EMPTY_LIST;
	}

	public static TransformacionDet find(){
		SelectorDeTrsParaAnalisis selector=new SelectorDeTrsParaAnalisis();		
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();		
		if(!selector.hasBeenCanceled()){
			TransformacionDet selected=selector.getSelected();
			return selected;
		}
		return null;
	}
	
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				DBUtils.whereWeAre();
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();								
				System.out.println(ToStringBuilder.reflectionToString(pendientesDeAnalizar()));
				System.exit(0);
			}
		});
	}

}
