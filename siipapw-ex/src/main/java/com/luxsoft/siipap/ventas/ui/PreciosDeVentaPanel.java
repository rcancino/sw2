package com.luxsoft.siipap.ventas.ui;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.security.SeleccionDeUsuario;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.matchers.RangoMatcherEditor;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.ventas.model.ListaDePreciosVenta;
import com.luxsoft.siipap.ventas.model.ListaDePreciosVentaDet;
import com.luxsoft.siipap.ventas.service.ListaDePreciosVentaManager;
import com.luxsoft.sw3.ventas.VentasRoles;


public class PreciosDeVentaPanel extends AbstractMasterDatailFilteredBrowserPanel<ListaDePreciosVenta, ListaDePreciosVentaDet>{

	public PreciosDeVentaPanel() {
		super(ListaDePreciosVenta.class);
	}

	@Override
	protected void init() {
		super.init();
		addProperty("id","aplicada","comentario","autorizada","tcDolares","tcEuros");
		addLabels("Id","Aplicada","Comentario","Autorizada","T.C. (Dolares)","T.C. (Euros");
		installTextComponentMatcherEditor("Id", "id");		
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={
				"lista.id"
				,"producto.linea.nombre"
				,"producto.marca.nombre"
				,"producto.clase.nombre"
				,"producto.clave"
				,"producto.descripcion"
				,"descripcion"
				,"kilos"
				,"gramos"
				,"precioAnterior"
				,"precio"
				,"precioAnteriorCredito"
				,"precioCredito"
				,"costo"
				,"factor"
				};
		String[] names={
				"Lista"
				,"Linea"
				,"Marcha"
				,"Clase"
				,"Producto"
				,"Prod (Desc)"
				,"Descripción"
				,"Kg"
				,"g"
				,"Precio Ant(CON)"
				,"Precio (CON)"
				,"Precio Ant(CRE)"
				,"Precio (CRE)"
				,"Costo"
				,"Factor (CON)"
				};
		return GlazedLists.tableFormat(ListaDePreciosVentaDet.class, props,names);
	}

	@Override
	protected Model<ListaDePreciosVenta, ListaDePreciosVentaDet> createPartidasModel() {
		return new Model<ListaDePreciosVenta, ListaDePreciosVentaDet>(){
			public List<ListaDePreciosVentaDet> getChildren(ListaDePreciosVenta parent) {
				return getManager().buscarPartidas(parent);
			}
		};
	}
	
	@Override
	protected EventList getSourceEventList() {
		Comparator<ListaDePreciosVenta> c=GlazedLists.beanPropertyComparator(ListaDePreciosVenta.class, "id");
		UniqueList<ListaDePreciosVenta> data=new UniqueList<ListaDePreciosVenta>(super.getSourceEventList(),c);
		return data;
	}	
	
	
	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction(VentasRoles.DIRECCION_DE_VENTAS.getId(), "autorizar", "Autorización "));
		procesos.add(addAction(VentasRoles.GERENCIA_DE_VENTAS_CORPORATIVO.getId(), "aplicar", "Aplicar"));
		procesos.add(addAction(VentasRoles.LISTAS_DE_PRECIOS_VENTA.getId(), "copiar", "Copiar"));
		return procesos;
		
	}
	
	private JTextField productoField=new JTextField(5);
	private JTextField gramosField=new JTextField(5);
	private JTextField kilosField=new JTextField(5);
	private RangoMatcherEditor<ListaDePreciosVentaDet> factorMayorEditor;
	private RangoMatcherEditor<ListaDePreciosVentaDet> factorMenorEditor;
	
	protected void installDetailFilterComponents(DefaultFormBuilder builder){
		builder.appendSeparator("Detalle");
		builder.append("Producto",productoField);
		builder.append("Gramos",gramosField);
		builder.append("Kilos",kilosField);
		builder.appendSeparator("Factor (CON");
		builder.append(">=",factorMayorEditor.getField());
		builder.append("<",factorMenorEditor.getField());
	}
	/*
	protected DefaultFormBuilder getFilterPanelBuilder(){
		if(filterPanelBuilder==null){
			FormLayout layout=new FormLayout(
					" p,2dlu,30dlu, 1dlu" +
					",p,2dlu,30dlu","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.getPanel().setOpaque(false);
			filterPanelBuilder=builder;
		}
		return filterPanelBuilder;
	}
	*/
	@Override
	protected EventList decorateDetailList( EventList data){
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		
		TextFilterator productoFilterator=GlazedLists.textFilterator("producto.clave","producto.descripcion");
		TextComponentMatcherEditor productoEditor=new TextComponentMatcherEditor(productoField,productoFilterator);
		editors.add(productoEditor);
		
		TextFilterator gramosFilterator=GlazedLists.textFilterator("gramos");
		TextComponentMatcherEditor gramosEditor=new TextComponentMatcherEditor(gramosField,gramosFilterator);
		editors.add(gramosEditor);
		
		
		TextFilterator kilosFilterator=GlazedLists.textFilterator("kilos");
		TextComponentMatcherEditor kilosEditor=new TextComponentMatcherEditor(kilosField,kilosFilterator);
		editors.add(kilosEditor);
		
		factorMayorEditor=new RangoMatcherEditor<ListaDePreciosVentaDet>(){
			@Override
			public boolean evaluar(ListaDePreciosVentaDet item) {
				return item.getFactor()>=getDoubleValue();
			}
			
		};
		editors.add(factorMayorEditor);
		
		factorMenorEditor=new RangoMatcherEditor<ListaDePreciosVentaDet>(){
			@Override
			public boolean evaluar(ListaDePreciosVentaDet item) {
				return item.getFactor()<getDoubleValue();
			}
			
		};
		editors.add(factorMenorEditor);
		
		CompositeMatcherEditor matcherEditor=new CompositeMatcherEditor(editors);
		FilterList detailFilter=new FilterList(data,matcherEditor);
		return detailFilter;
	}

	@Override
	protected ListaDePreciosVenta doInsert() {
		ListaDePreciosVenta lp=PreciosDeVentaForm.showForm();
		logger.info("Mandando salvar lista: "+lp.getId()+ " Precios: "+lp.getPrecios().size());
		if(lp!=null){			
			lp=getManager().salvar(lp);			
			return lp;
		}
		return null;
	}
	
	
	
	@Override
	public boolean doDelete(ListaDePreciosVenta bean) {
		getManager().eliminar(bean);
		return true;
	}

	@Override
	protected ListaDePreciosVenta doEdit(ListaDePreciosVenta bean) {
		ListaDePreciosVenta target=getManager().get(bean.getId());
		target=PreciosDeVentaForm.showForm(target,false);
		if(target!=null){
			target=getManager().salvar(target);
			return target;
		}
		return null;
	}
	
	public void autorizar(){		
		this.executeSigleSelection(new SingleSelectionHandler<ListaDePreciosVenta>(){
			public ListaDePreciosVenta execute(ListaDePreciosVenta selected) {
				if(StringUtils.isNotBlank(selected.getAutorizada())){
					MessageUtils.showMessage("Lista ya autorizada", "Autorización de listas");
					return selected;
				}
				Assert.isNull(selected.getAplicada(),"La lista ya se ha aplicado");
				User user=SeleccionDeUsuario.findUser(ServiceLocator2.getHibernateTemplate());
				if(user==null)
					return selected;
				if(user.hasRole(VentasRoles.DIRECCION_DE_VENTAS.getId())){
					Date fecha=ServiceLocator2.obtenerFechaDelSistema();
					String pattern="AUTORIZO: {0} Fecha :{1,date,long} IP:{2}";
					String aut=MessageFormat.format(pattern
							, user.getUsername()
							,fecha
							,KernellSecurity.getIPAdress()
							);
					selected.setAutorizada(aut);
					return getManager().salvar(selected);
					
				}else{
					MessageUtils.showMessage("Procedimiento exclusivo de: "+VentasRoles.DIRECCION_DE_VENTAS, "Autorización de listas");
					return null;
				}
				
			}			
		});
	}
	
	public void aplicar(){
		this.executeSigleSelection(new SingleSelectionHandler<ListaDePreciosVenta>(){
			public ListaDePreciosVenta execute(ListaDePreciosVenta selected) {
				if(StringUtils.isBlank(selected.getAutorizada())){
					MessageUtils.showMessage("Lista no autorizada", "Aplicación de listas");
					return selected;
				}
				if(selected.getAplicada()!=null){
					MessageUtils.showMessage("Lista ya aplicada", "Aplicación  de listas");
					return selected;
				}
				User user=SeleccionDeUsuario.findUser(ServiceLocator2.getHibernateTemplate());
				if(user==null)
					return selected;
				if(user.hasRole(VentasRoles.GERENCIA_DE_VENTAS_CORPORATIVO.getId())){
					return getManager().aplicar(selected,user);
					
				}else{
					MessageUtils.showMessage("Procedimiento exclusivo de: "+VentasRoles.DIRECCION_DE_VENTAS, "Autorización de listas");
					return null;
				}
				
			}			
		});
	}
	
	public void copiar(){
		if(selectionModel.isSelectionEmpty())
			return;
		ListaDePreciosVenta selected=(ListaDePreciosVenta)selectionModel.getSelected().get(0);
		if(MessageUtils.showConfirmationMessage("Copiar la lista: "+selected.getId(), "Precios de venta")){
			ListaDePreciosVenta nueva=getManager().copiar(selected);
			source.add(nueva);
		}
			
	}

	public ListaDePreciosVentaManager getManager(){
		return ServiceLocator2.getListaDePreciosVentaManager();
	}
	

	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

}