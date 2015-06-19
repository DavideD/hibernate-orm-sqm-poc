/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.select;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.hql.parser.ParsingContext;

/**
 * @author Steve Ebersole
 */
public class SelectList implements Selection {
	private final ParsingContext parsingContext;

	private List<SelectListItem> selectListItems;

	public SelectList(ParsingContext parsingContext) {
		this.parsingContext = parsingContext;
	}

	public SelectList(ParsingContext parsingContext, SelectListItem... items) {
		this( parsingContext );
		if ( items != null ) {
			for ( SelectListItem item : items ) {
				addSelectListItem( item );
			}

		}
	}

	public void addSelectListItem(SelectListItem item) {
		if ( selectListItems == null ) {
			selectListItems = new ArrayList<SelectListItem>();
		}
		selectListItems.add( item );
	}
}
