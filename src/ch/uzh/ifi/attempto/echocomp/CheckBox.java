// This file is part of the Attempto Java Packages.
// Copyright 2008-2009, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
//
// The Attempto Java Packages is free software: you can redistribute it and/or modify it under the
// terms of the GNU Lesser General Public License as published by the Free Software Foundation,
// either version 3 of the License, or (at your option) any later version.
//
// The Attempto Java Packages is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE. See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License along with the Attempto
// Java Packages. If not, see http://www.gnu.org/licenses/.

package ch.uzh.ifi.attempto.echocomp;

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.ResourceImageReference;

/**
 * This class represents a check box in blue style.
 * 
 * @author Tobias Kuhn
 */
public class CheckBox extends nextapp.echo2.app.CheckBox {

	private static final long serialVersionUID = -8160475963811004744L;

	/**
	 * Creates a new check box having a text and an icon.
	 * 
	 * @param text The text.
	 * @param icon The icon.
	 */
	public CheckBox(String text, ImageReference icon) {
		super(text, icon);
		setStateIcon(new ResourceImageReference("ch/uzh/ifi/attempto/echocomp/style/notchecked.png"));
		setSelectedStateIcon(new ResourceImageReference("ch/uzh/ifi/attempto/echocomp/style/checked.png"));
		setFont(new Font(Style.fontTypeface, Font.PLAIN, new Extent(13)));
	}
	
	/**
	 * Creates a new check box having only a text.
	 * 
	 * @param text The text.
	 */
	public CheckBox(String text) {
		this(text, null);
	}
	
	/**
	 * Creates a new check box having only an icon.
	 * 
	 * @param icon The icon.
	 */
	public CheckBox(ImageReference icon) {
		this(null, icon);
	}
	
	/**
	 * Creates a new check box having neither a text nor an icon.
	 */
	public CheckBox() {
		this(null, null);
	}

}
