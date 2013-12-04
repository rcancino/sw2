package com.luxsoft.siipap.pos.ui.selectores;

import java.awt.Dimension;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.apache.commons.collections.ListUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.inventarios.model.ExistenciaConteo;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.services.Services;

public class SelectorDeExistenciasParaConteo extends AbstractSelector<ExistenciaConteo>{
	
	private Long sucursalId;

	public SelectorDeExistenciasParaConteo() {
		super(ExistenciaConteo.class, "Eixstencias para inventario físico");
	}

	@Override
	protected List<ExistenciaConteo> getData() {		
		String hql="from ExistenciaConteo e where e.sucursal.id=?";
		return Services.getInstance().getHibernateTemplate().find(hql,getSucursalId());
	}

	@Override
	protected TableFormat<ExistenciaConteo> getTableFormat() {
		String[] props={"sucursal.nombre","fecha","clave","descripcion","producto.kilos","producto.unidad","existencia","conteo","diferencia","ajuste","existenciaFinal","sectores"};
		String[] names={"Suc","Fecha","Producto","Descripción","Kg","U","Exis","Conteo","Dif","Ajuste","Exis final","Sectores"};
		return GlazedLists.tableFormat(ExistenciaConteo.class, props,names);
	}
	
	private JTextField claveField=new JTextField(7);
	private JTextField descField=new JTextField(20);
	
	@Override
	protected void installEditors(
		EventList<MatcherEditor<ExistenciaConteo>> editors) {
		TextFilterator textFilterator=GlazedLists.textFilterator("clave");
		TextComponentMatcherEditor e1=new TextComponentMatcherEditor(claveField,textFilterator);
		editors.add(e1);
		editors.add(new TextComponentMatcherEditor(descField,GlazedLists.textFilterator("descripcion")));
	}

	@Override
	protected void setPreferedDimension(JComponent gridComponent) {
		gridComponent.setPreferredSize(new Dimension(750,400));
	}

	
	protected JComponent buildFilterPanel(){
		FormLayout layout=new FormLayout("p,2dlu,p,2dlu p,2dlu,p,70dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Clave ",claveField);
		builder.append("Descripción ",descField);
		return builder.getPanel();
	}
	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		Action a=CommandUtils.createViewAction(this, "cambiarPeriodo");
		a.putValue(Action.NAME, "Periodo");
		a.putValue(Action.SHORT_DESCRIPTION, "Cambiar fecha");
		builder.add(a);
		builder.add(CommandUtils.createLoadAction(this, "load"));
		builder.add(buildFilterPanel());
		
		return builder.getToolBar();
	}
	
	
	
	public Long getSucursalId() {
		return sucursalId;
	}

	public void setSucursalId(Long sucursalId) {
		this.sucursalId = sucursalId;
	}

	public static List<ExistenciaConteo> seleccionar(Long sucursalId){
		SelectorDeExistenciasParaConteo selector=new SelectorDeExistenciasParaConteo();
		selector.setSucursalId(sucursalId);
		selector.open();
		if(!selector.hasBeenCanceled()){
			return selector.getSelectedList();
		}
		return ListUtils.EMPTY_LIST;
	
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
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				seleccionar(5L);
				System.exit(0);
			}

		});
	}

}
