/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression.domain;

import java.util.List;

import org.hibernate.persister.collection.spi.ImprovedCollectionPersister;
import org.hibernate.persister.collection.spi.PluralAttributeElement;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.from.TableGroup;
import org.hibernate.sql.convert.results.spi.Return;
import org.hibernate.sql.convert.spi.NotYetImplementedException;
import org.hibernate.sql.exec.spi.SqlAstSelectInterpreter;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeElementReferenceExpression implements DomainReferenceExpression {
	private final ImprovedCollectionPersister collectionPersister;
	private final ColumnBindingSource columnBindingSource;

	public PluralAttributeElementReferenceExpression(
			ImprovedCollectionPersister collectionPersister,
			TableGroup columnBindingSource) {
		this.collectionPersister = collectionPersister;
		this.columnBindingSource = columnBindingSource;

	}

	@Override
	public Type getType() {
		return null;
	}

	@Override
	public void accept(SqlAstSelectInterpreter walker, boolean shallow) {
		walker.visitPluralAttributeElement( this, shallow );
	}

	@Override
	public Return toQueryReturn(String resultVariable) {
		throw new NotYetImplementedException(  );
	}

	@Override
	public List<ColumnBinding> resolveColumnBindings(boolean shallow) {
		return columnBindingSource.resolveColumnBindings( this, shallow );
	}

	@Override
	public PluralAttributeElement getDomainReference() {
		return collectionPersister.getElementReference();
	}


}