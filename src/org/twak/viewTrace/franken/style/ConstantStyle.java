package org.twak.viewTrace.franken.style;

import java.util.Random;

import javax.swing.JPanel;

import org.twak.utils.ui.ListDownLayout;
import org.twak.viewTrace.franken.App;
import org.twak.viewTrace.franken.SelectedApps;
import org.twak.viewTrace.franken.style.ui.UIVector;

public class ConstantStyle implements StyleSource {
	
	double[] mean;
	App app;
	
	public ConstantStyle(App app) {
		this.mean = new double[app.sizeZ];
		this.app = app;
	}
	
	@Override
	public double[] draw( Random random, App app ) {
		return mean;
	}
	
	@Override
	public JPanel getUI( Runnable update ) {

		JPanel out = new JPanel(new ListDownLayout() );
		
		out.add( new UIVector (mean, app, true, update ) );
		
		return out;
	}
	
	public boolean install( SelectedApps next ) {
		return false;
	}
}
