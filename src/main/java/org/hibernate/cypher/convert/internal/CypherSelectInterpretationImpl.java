/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.cypher.convert.internal;

import java.util.List;

import org.hibernate.cypher.convert.spi.CypherSelectInterpretation;
import org.hibernate.sql.exec.results.spi.ResolvedReturn;
import org.hibernate.sql.spi.ParameterBinder;

/**
 * @author Davide D'Alto
 */
public class CypherSelectInterpretationImpl implements CypherSelectInterpretation {

	private final String cypher;
	private final List<ParameterBinder> parameterBinders;
	private final List<ResolvedReturn> returns;

	public CypherSelectInterpretationImpl(String cypher, List<ParameterBinder> parameterBinders, List<ResolvedReturn> returns) {
		this.cypher = cypher;
		this.parameterBinders = parameterBinders;
		this.returns = returns;
	}

	@Override
	public String getCypher() {
		return cypher;
	}

	public List<ParameterBinder> getParameterBinders() {
		return parameterBinders;
	}

	public List<ResolvedReturn> getReturns() {
		return returns;
	}
}
