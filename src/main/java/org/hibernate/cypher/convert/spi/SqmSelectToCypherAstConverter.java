/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.cypher.convert.spi;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.proposed.QueryOptions;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.convert.spi.Callback;
import org.hibernate.sqm.BaseSemanticQueryWalker;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.query.SqmSelectStatement;

/**
 * @author Davide D'Alto
 */
public class SqmSelectToCypherAstConverter extends BaseSemanticQueryWalker {

	public static SelectQuery interpret(SqmSelectStatement statement, SessionFactoryImplementor sessionFactory, DomainMetamodel domainMetamodel,
			QueryOptions queryOptions, Callback callBack) {
		return null;
	}
}
