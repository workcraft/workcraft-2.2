/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.workcraft.parsers.breeze.javacc;

import java.util.List;

import org.workcraft.parsers.breeze.Expression;
import org.workcraft.parsers.breeze.ParameterScope;

public class CaseExpression<T1, T2> implements Expression<T2> {

	private final Expression<T1> toCheck;
	private final List<Expression<T1>> conditions;
	private final List<Expression<T2>> values;
	private final Expression<T2> elseValue;

	public CaseExpression(Expression<T1> toCheck,
			List<Expression<T1>> conditions,
			List<Expression<T2>> values, Expression<T2> elseValue) {
		if(toCheck == null)
			throw new NullPointerException("toCheck");
		if(conditions == null)
			throw new NullPointerException("conditions");
		if(values == null)
			throw new NullPointerException("values");
		if(elseValue == null)
			throw new NullPointerException("elseValue");
		
		if(conditions.size() != values.size())
			throw new RuntimeException("'conditions' and 'values' sizes don't match");
		
		this.toCheck = toCheck;
		this.conditions = conditions;
		this.values = values;
		this.elseValue = elseValue;
	}

	@Override
	public T2 evaluate(ParameterScope parameters) {
		T1 checkValue = toCheck.evaluate(parameters);
		for(int i=0;i<conditions.size();i++)
			if(conditions.get(i).evaluate(parameters).equals(checkValue))
				return values.get(i).evaluate(parameters);
		return elseValue.evaluate(parameters);
	}

}
