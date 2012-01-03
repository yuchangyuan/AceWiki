// This file is part of AceWiki.
// Copyright 2008-2012, AceWiki developers.
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

package ch.uzh.ifi.attempto.acewiki.core;

/**
 * This is a partial implementation of a monolingual AceWiki engine.
 * 
 * @author Tobias Kuhn
 */
public abstract class MonolingualEngine extends AbstractAceWikiEngine {

	/**
	 * Returns the language handler.
	 * 
	 * @return The language handler.
	 */
	public abstract LanguageHandler getLanguageHandler();
	
	public final LanguageHandler getLanguageHandler(String language) {
		return getLanguageHandler();
	}

	public final String[] getLanguages() {
		return new String[] {"Default"};
	}

}
