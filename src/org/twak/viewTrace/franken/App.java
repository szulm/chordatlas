package org.twak.viewTrace.franken;


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.twak.tweed.gen.skel.MiniRoof;
import org.twak.utils.collections.MultiMap;
import org.twak.utils.geom.DRectangle;
import org.twak.utils.ui.AutoCheckbox;
import org.twak.viewTrace.facades.FRect;
import org.twak.viewTrace.facades.HasApp;
import org.twak.viewTrace.facades.MiniFacade;
import org.twak.viewTrace.franken.style.ConstantStyle;
import org.twak.viewTrace.franken.style.GaussStyle;
import org.twak.viewTrace.franken.style.StyleSource;

public abstract class App /*earance*/ implements Cloneable {
	
	public enum TextureUVs {
		SQUARE, ZERO_ONE, Rectangle;
	}
	
	public enum AppMode {
		Off, Bitmap, Parent, Net
	}
	
	public AppMode appMode = AppMode.Off;
	public TextureUVs textureUVs = TextureUVs.SQUARE;
	public Color color = Color.gray;
	
	public String texture;
	
	public double[] styleZ;
	public StyleSource styleSource;
	
	public HasApp hasA;
	String name;
	
	// marks as needing geometry recreation
	public boolean isDirty = false; 
	
	// GAN optoins
//	public String netName;
//	public int sizeZ = -1;
//	public int resolution;
	public DRectangle textureRect;
	
	public App( App a ) {
		this.hasA = a.hasA;
		this.appMode = a.appMode;
		this.textureUVs = a.textureUVs;
		this.color = a.color;
		this.texture = a.texture;
		this.styleZ = a.styleZ;
		this.name = a.name;
		this.styleSource = a.styleSource;
	}
	
	public App( HasApp ha ) {
		
		NetInfo ni = NetInfo.index.get(this.getClass());
		
		this.name = ni.name;
		this.hasA = ha; 
		this.styleZ = new double[ni.sizeZ];
		this.styleSource = new GaussStyle(NetInfo.get(this));
	}

	public static App createFor(HasApp ha) {
		
		if (ha.getClass() == MiniRoof.class) {
			return new RoofTexApp(ha);
		} else if (ha.getClass() == FRect.class) {
			return new PanesLabelApp(ha);
		}		
		
		throw new Error("unkown to factory " + ha.getClass().getSimpleName());
	}
	
	public JComponent createUI( Runnable globalUpdate, SelectedApps apps ) {
		return new JPanel();
	}

	static Random randy = new Random();
	static final int Batch_Size = 16;
	
	public static void computeWithChildren (int first, int stage, MultiMap<Integer, App> todo, Runnable globalUpdate ) {
		
		if (todo.get( stage ).isEmpty())
			return;
		
		if (first >= todo.size()) {
			System.out.println( "finishing "+ todo.get( 0 ).getClass().getSimpleName() );
			
			globalUpdate.run();
			
			for (App a : new ArrayList<> ( todo.get( stage )) )
				todo.put( NetInfo.evaluationOrder.indexOf( a.getClass() ), a );
			
			App.computeWithChildren( 0, stage+1, todo, globalUpdate );
			
		} else {
		
			List<App> all = todo.get( stage );
			List<App> batch = new ArrayList<>();
			
			for ( int i = first; i < Math.min( all.size(), first + Batch_Size ); i++ ) {
				App app = all.get( i );
				if (app.appMode == AppMode.Net) {
					
					if (app.styleSource != null)
						app.styleZ = app.styleSource.draw( randy, app );
					
					batch.add( app );
				}
			}

			if (!batch.isEmpty()) {
				System.out.println( "batch " + first +"/"+ all.size() + " "+ todo.get( 0 ).getClass().getSimpleName() );
				batch.get( 0 ).computeBatch ( () -> 
				App.computeWithChildren( first + Batch_Size, stage, todo, globalUpdate ), 
					batch );
			}
			else
				App.computeWithChildren( 0, stage+1, todo, globalUpdate );
		}
		
	}

	public void markDirty() {
		isDirty = true;
		App up = getUp();
		if (up != null)
			up.markDirty();
	}
	
	public String zAsString() {
		String zs = "";
		for ( double d : styleZ )
			zs += "_" + d;
		return zs;
	}
	
	public abstract App copy();
	public abstract App getUp();
	public abstract MultiMap<String, App> getDown();
	public abstract void computeBatch(Runnable whenDone, List<App> batch);

	public Enum[] getValidAppModes() {
		return new Enum[] {AppMode.Off, AppMode.Net};
	}
	
	
}
