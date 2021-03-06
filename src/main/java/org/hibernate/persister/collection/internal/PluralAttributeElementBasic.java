/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.collection.internal;

import org.hibernate.persister.collection.spi.ImprovedCollectionPersister;
import org.hibernate.persister.collection.spi.PluralAttributeElement;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.DomainReferenceImplementor;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.type.BasicType;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeElementBasic implements PluralAttributeElement<BasicType>, DomainReferenceImplementor {
	private final ImprovedCollectionPersister collectionPersister;
	private final BasicType type;
	private final Column[] columns;

	public PluralAttributeElementBasic(ImprovedCollectionPersister collectionPersister, BasicType type, Column[] columns) {
		this.collectionPersister = collectionPersister;
		this.type = type;
		this.columns = columns;
	}

	@Override
	public ElementClassification getClassification() {
		return ElementClassification.BASIC;
	}

	@Override
	public DomainReference getType() {
		return this;
	}

	@Override
	public BasicType getOrmType() {
		return type;
	}

	public Column[] getColumns() {
		return columns;
	}

	@Override
	public String asLoggableText() {
		return "PluralAttributeElement(" + collectionPersister.getPersister().getRole() + " [" + getOrmType().getName() + "])" ;
	}
}
