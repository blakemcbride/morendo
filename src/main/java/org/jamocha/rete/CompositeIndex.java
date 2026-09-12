/*
 * Copyright 2002-2008 Peter Lin
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.jamocha.rete;

import java.util.Objects;

/**
 * The key under which alpha nodes are shared: a slot name, the comparison operator
 * and the literal value of a constraint.
 */
public record CompositeIndex(String name, Operator operator, Object value) {

	@Override
	public boolean equals(Object other) {
		return this == other || (other instanceof CompositeIndex ci && ci.name.equals(name)
				&& ci.operator == operator && Objects.equals(ci.value, value));
	}

	@Override
	public int hashCode() {
		return name.hashCode() + operator.ordinal() + (value == null ? 0 : value.hashCode());
	}

	public String toPPString() {
		return name + ":" + operator + ":" + String.valueOf(value);
	}
}
