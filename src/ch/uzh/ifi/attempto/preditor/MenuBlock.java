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

package ch.uzh.ifi.attempto.preditor;

import java.util.List;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionListener;
import ch.uzh.ifi.attempto.echocomp.DelayedComponent;
import ch.uzh.ifi.attempto.echocomp.Label;
import ch.uzh.ifi.attempto.echocomp.Style;
import ch.uzh.ifi.attempto.echocomp.VSpace;
import ch.uzh.ifi.attempto.echocomp.WindowPane;

/**
 * This class represents a menu block of the predictive editor. A menu block consists of a list of
 * menu items.
 * 
 * @author Tobias Kuhn
 */
class MenuBlock extends SplitPane {
	
	private static final long serialVersionUID = -5856577034761259001L;
	
	private ActionListener actionListener;
	private Column menuColumn = new Column();
	private Button label = new Button("...");
	
	/**
	 * Creates a new menu block.
	 * 
	 * @param actionListener The action listener.
	 * @param parent The parent window.
	 */
	public MenuBlock(ActionListener actionListener, WindowPane parent) {
		super(ORIENTATION_VERTICAL_TOP_BOTTOM, new Extent(16));
		this.actionListener = actionListener;
		
		label.setEnabled(false);
		label.setHeight(new Extent(15));
		label.setWidth(new Extent(100));
		label.setDisabledBackground(Color.WHITE);
		label.setDisabledForeground(Color.BLACK);
		label.setDisabledFont(new Font(Style.fontTypeface, Font.ITALIC, new Extent(11)));
		label.setRolloverEnabled(false);
		label.setLineWrap(false);
		label.setAlignment(new Alignment(Alignment.LEFT, Alignment.BOTTOM));
		label.setInsets(new Insets(1, 0, 0, 0));
		add(label);
		
		SplitPane spLeft = new SplitPane(SplitPane.ORIENTATION_HORIZONTAL_LEFT_RIGHT);
		spLeft.setSeparatorColor(Color.BLACK);
		spLeft.setSeparatorWidth(new Extent(1));
		spLeft.setSeparatorPosition(new Extent(0));
		spLeft.add(new Label());
		add(spLeft);
		
		SplitPane spTop = new SplitPane(SplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM);
		spTop.setSeparatorColor(Color.BLACK);
		spTop.setSeparatorHeight(new Extent(1));
		spTop.setSeparatorPosition(new Extent(0));
		spTop.add(new Label());
		spLeft.add(spTop);
		
		SplitPane spRight = new SplitPane(SplitPane.ORIENTATION_HORIZONTAL_RIGHT_LEFT);
		spRight.setSeparatorColor(Color.BLACK);
		spRight.setSeparatorWidth(new Extent(1));
		spRight.setSeparatorPosition(new Extent(0));
		spRight.add(new Label());
		spTop.add(spRight);
		
		SplitPane spBottom = new SplitPane(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP);
		spBottom.setSeparatorColor(Color.BLACK);
		spBottom.setSeparatorHeight(new Extent(1));
		spBottom.setSeparatorPosition(new Extent(0));
		spBottom.add(new Label());
		spBottom.setBackground(Style.lightBackground);
		spRight.add(spBottom);
		
		Column menuBaseColumn = new Column();
		menuBaseColumn.setBackground(Style.mediumBackground);
		menuBaseColumn.add(menuColumn);
		spBottom.add(menuBaseColumn);
	}
	
	/**
	 * Sets the content and the size of this menu block.
	 * 
	 * @param content The content to be shown in this menu block.
	 * @param width The width of this menu block in pixels.
	 * @param pageSize The vertical page size in number of items.
	 */
	public void setContent(final MenuBlockContent content, final int width, final int pageSize) {
		label.setText(content.getName());
		label.setWidth(new Extent(width - 3));
		menuColumn.removeAll();
		
		final List<MenuItem> items = content.getItems();
		
		if (items.size() > pageSize) {
			for (int i=0; i < pageSize; i++) {
				MenuItem m = items.get(i);
				m.setWidth(new Extent(width - 24));
				m.addActionListener(actionListener);
				menuColumn.add(m);
			}
			
			menuColumn.add(new DelayedComponent(new VSpace((items.size()-pageSize)*15)) {
				
				private static final long serialVersionUID = -5737622027016695873L;

				public Component initComponent() {
					Column c = new Column();
					for (int i = pageSize; i < items.size() ; i++) {
						MenuItem m = items.get(i);
						m.setWidth(new Extent(width - 24));
						m.addActionListener(actionListener);
						c.add(m);
					}
					return c;
				}
				
			});
			
		} else {
			for (MenuItem m : items) {
				menuColumn.add(m);
				m.setWidth(new Extent(width - 7));
				m.addActionListener(actionListener);
			}
		}
	}

}
