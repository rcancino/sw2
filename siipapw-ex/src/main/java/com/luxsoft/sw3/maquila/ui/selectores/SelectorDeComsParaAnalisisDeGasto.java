package com.luxsoft.sw3.maquila.ui.selectores;

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
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
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
public class SelectorDeComsParaAnalisisDeGasto extends AbstractSelector<EntradaPorCompra>{
	
	Periodo periodo;
	
	public SelectorDeComsParaAnalisisDeGasto() {
		super(EntradaPorCompra.class, "Entradas  por compra (COM) ");
		periodo=Periodo.periodoDeloquevaDelMes();
	}

	@Override
	protected List<EntradaPorCompra> getData() {
		String hql="from EntradaPorCompra e left join fetch e.recepcion r " +
		"  where e.fecha between ? and ? " +
		"    and e.analisisGasto is null " +
		"    and e.recepcion is not null";
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
	protected TableFormat<EntradaPorCompra> getTableFormat() {
		
		String[] props={
				"sucursal.nombre"
				,"recepcion.documento"
				,"recepcion.remision"
				,"renglon"
				,"clave"
				,"descripcion"
				,"cantidad"
				,"kilosCalculados"
				,"costoFlete"
				//,"costoCorte"
				//,"costoMateria"
				,"comentario"
				};
		String[] names={
				"Sucursal"
				,"Maq"
				,"Remisión"
				,"Rngl"
				,"Producto"
				,"Descripción"
				,"Cantidad"
				,"Kilos"
				,"Flete"
				//,"Hojeo","Costo M.P.","Comentario"
				};
		return GlazedLists.tableFormat(EntradaPorCompra.class, props,names);
	}

	@Override
	protected TextFilterator<EntradaPorCompra> getBasicTextFilter() {
		return GlazedLists.textFilterator("recepcion.remision","documento","producto.clave","producto.descripcion");
	}
	
	
	public static List<EntradaPorCompra> seleccionar(){
		SelectorDeComsParaAnalisisDeGasto selector=new SelectorDeComsParaAnalisisDeGasto();
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();		
		if(!selector.hasBeenCanceled()){
			return new ArrayList<EntradaPorCompra>(selector.getSelectedList());
		}
		return ListUtils.EMPTY_LIST;
	}

	public static EntradaPorCompra find(){
		SelectorDeComsParaAnalisisDeGasto selector=new SelectorDeComsParaAnalisisDeGasto();		
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();		
		if(!selector.hasBeenCanceled()){
			EntradaPorCompra selected=selector.getSelected();
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
				System.out.println(ToStringBuilder.reflectionToString(seleccionar()));
				System.exit(0);
			}
		});
	}

}
