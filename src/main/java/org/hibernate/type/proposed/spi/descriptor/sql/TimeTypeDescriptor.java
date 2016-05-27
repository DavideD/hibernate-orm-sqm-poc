/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.proposed.spi.descriptor.sql;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.Calendar;

import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;

/**
 * Descriptor for {@link Types#TIME TIME} handling.
 *
 * @author Steve Ebersole
 */
public class TimeTypeDescriptor implements SqlTypeDescriptor {
	public static final TimeTypeDescriptor INSTANCE = new TimeTypeDescriptor();

	public TimeTypeDescriptor() {
	}

	@Override
	public int getSqlType() {
		return Types.TIME;
	}

	@Override
	public boolean canBeRemapped() {
		return true;
	}

	@Override
	public org.hibernate.type.proposed.spi.descriptor.java.JavaTypeDescriptor getJdbcRecommendedJavaTypeMapping() {
		return org.hibernate.type.proposed.spi.descriptor.java.JavaTypeDescriptorRegistry.INSTANCE.getDescriptor( Time.class );
	}

	@Override
	public <X> ValueBinder<X> getBinder(final org.hibernate.type.proposed.spi.descriptor.java.JavaTypeDescriptor<X> javaTypeDescriptor) {
		return new BasicBinder<X>( javaTypeDescriptor, this ) {
			@Override
			protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
				final Time time = javaTypeDescriptor.unwrap( value, Time.class, options );
				if ( value instanceof Calendar ) {
					st.setTime( index, time, (Calendar) value );
				}
				else {
					st.setTime( index, time );
				}
			}

			@Override
			protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
					throws SQLException {
				final Time time = javaTypeDescriptor.unwrap( value, Time.class, options );
				if ( value instanceof Calendar ) {
					st.setTime( name, time, (Calendar) value );
				}
				else {
					st.setTime( name, time );
				}
			}
		};
	}

	@Override
	public <X> ValueExtractor<X> getExtractor(final org.hibernate.type.proposed.spi.descriptor.java.JavaTypeDescriptor<X> javaTypeDescriptor) {
		return new BasicExtractor<X>( javaTypeDescriptor, this ) {
			@Override
			protected X doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap( rs.getTime( name ), options );
			}

			@Override
			protected X doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap( statement.getTime( index ), options );
			}

			@Override
			protected X doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap( statement.getTime( name ), options );
			}
		};
	}
}