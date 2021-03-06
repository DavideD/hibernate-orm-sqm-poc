/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.internal;

import org.hibernate.persister.common.spi.AbstractTable;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.Table;

/**
 * @author Steve Ebersole
 */
public class PhysicalColumn implements Column {
	private final AbstractTable table;
	private final String name;
	private final int jdbcType;

	public PhysicalColumn(AbstractTable table, String name, int jdbcType) {
		this.table = table;
		this.name = name;
		this.jdbcType = jdbcType;
	}

	public String getName() {
		return name;
	}

	@Override
	public Table getSourceTable() {
		return table;
	}

	@Override
	public int getJdbcType() {
		return jdbcType;
	}

	@Override
	public String render(String identificationVariable) {
		return identificationVariable + '.' + name;
	}

	@Override
	public String toLoggableString() {
		return "PhysicalColumn(" + name + ");";
	}
}
