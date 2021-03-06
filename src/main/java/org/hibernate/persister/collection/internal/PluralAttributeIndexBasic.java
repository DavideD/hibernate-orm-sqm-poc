/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.collection.internal;

import org.hibernate.persister.common.spi.AbstractPluralAttributeIndex;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.type.BasicType;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeIndexBasic extends AbstractPluralAttributeIndex<BasicType> {
	public PluralAttributeIndexBasic(ImprovedCollectionPersisterImpl persister, BasicType ormType, Column[] columns) {
		super( persister, ormType, columns );
	}

	@Override
	public IndexClassification getClassification() {
		return IndexClassification.BASIC;
	}

	@Override
	public DomainReference getType() {
		return this;
	}
}
