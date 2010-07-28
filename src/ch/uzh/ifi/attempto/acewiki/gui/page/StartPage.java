// This file is part of AceWiki.
// Copyright 2008-2010, Tobias Kuhn.
// 
// AceWiki is free software: you can redistribute it and/or modify it under the terms of the GNU
// Lesser General Public License as published by the Free Software Foundation, either version 3 of
// the License, or (at your option) any later version.
// 
// AceWiki is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
// even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with AceWiki. If
// not, see http://www.gnu.org/licenses/.

package ch.uzh.ifi.attempto.acewiki.gui.page;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import ch.uzh.ifi.attempto.acewiki.Wiki;
import ch.uzh.ifi.attempto.acewiki.core.ontology.OntologyElement;
import ch.uzh.ifi.attempto.acewiki.gui.ListItem;
import ch.uzh.ifi.attempto.acewiki.gui.Title;
import ch.uzh.ifi.attempto.acewiki.gui.WikiLink;
import ch.uzh.ifi.attempto.echocomp.Label;
import ch.uzh.ifi.attempto.echocomp.VSpace;

/**
 * This class represents the start page of a wiki.
 * 
 * @author Tobias Kuhn
 */
public class StartPage extends WikiPage implements ActionListener {
	
	private static final long serialVersionUID = -1528040616289818728L;
	
	private Column linksColumn = new Column();
	
	/**
	 * Creates a new start page.
	 * 
	 * @param wiki The wiki instance.
	 * @param title The title of the wiki.
	 * @param description The description of the wiki.
	 */
	public StartPage(Wiki wiki, String title, String description) {
		super(wiki, new Title(
				( title == null || title.length() == 0 ? "Untitled Wiki" : title ),
				true
			));
		
		addSelectedTab("Main Page");
		addTab("Index", this);
		addTab("Search", this);
		addTab("About", this);
		
		add(new VSpace(10));
		
		if (description != null && description.length() > 0) {
			addHeadline("Description");
			Row desc = new Row();
			desc.setInsets(new Insets(10, 10, 10, 15));
			desc.add(new Label(description, Font.ITALIC));
			add(desc);
		}
		
		addHeadline("Largest Articles");
		linksColumn.setInsets(new Insets(10, 10, 10, 15));
		add(linksColumn);

		add(new VSpace(20));
	}
	
	protected void doUpdate() {
		linksColumn.removeAll();
		List<OntologyElement> elements = getWiki().getOntologyElements();
		Collections.sort(elements,
			new Comparator<OntologyElement>() {
				public int compare(OntologyElement oe1, OntologyElement oe2) {
					return oe2.getStatements().size() - oe1.getStatements().size();
				}
			}
		);
		int max = (elements.size() > 10 ? 10 : elements.size());
		for (int i = 0; i < max; i++) {
			Row r = new Row();
			r.add(new ListItem(new WikiLink(elements.get(i), getWiki())));
			linksColumn.add(r);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if ("Index".equals(e.getActionCommand())) {
			getWiki().showIndexPage();
		} else if ("Search".equals(e.getActionCommand())) {
			getWiki().showSearchPage();
		} else if ("About".equals(e.getActionCommand())) {
			getWiki().showAboutPage();
		}
	}

	public boolean equals(Object obj) {
		return obj instanceof StartPage;
	}
	
	public String toString() {
		return "-MAIN-";
	}

}
