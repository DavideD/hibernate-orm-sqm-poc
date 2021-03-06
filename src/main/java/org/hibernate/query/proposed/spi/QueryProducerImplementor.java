/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.proposed.spi;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.proposed.QueryProducer;

/**
 * The internal contract for QueryProducer implementations.  Acts as the value passed to
 * produced queries and provides them with access to functionality needed for performing
 * the query.
 *
 * @author Steve Ebersole
 */
public interface QueryProducerImplementor extends QueryProducer {
	SessionFactoryImplementor getFactory();


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// covariant overrides

	@Override
	QueryImplementor createQuery(String queryString);

	@Override
	<R> QueryImplementor<R> createQuery(String queryString, Class<R> resultClass);

	@Override
	NativeQueryImplementor createNativeQuery(String sqlString);

	@Override
	NativeQueryImplementor createNativeQuery(String sqlString, Class resultClass);

	@Override
	NativeQueryImplementor createNativeQuery(String sqlString, String resultSetMapping);

	@Override
	QueryImplementor getNamedQuery(String queryName);

	@Override
	QueryImplementor createNamedQuery(String name);

	@Override
	<R> QueryImplementor<R> createNamedQuery(String name, Class<R> resultClass);

	@Override
	NativeQueryImplementor getNamedNativeQuery(String name);
}
