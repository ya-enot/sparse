/**
 * Copyright © 2017 Anton Filatov (ya-enot@mail.ru)
 *
 * This file is part of SParse.
 *
 * SParse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SParse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SParse.  If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package one.utopic.sparse.ebml;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class EBMLType {

	private final Context context;
	private final EBMLCode code;
	private final String name;

	private EBMLType(String name, EBMLCode code, Context context) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(code);
		Objects.requireNonNull(context);
		this.name = name;
		this.code = code;
		this.context = context;
	}

	public String getName() {
		return name;
	}

	public EBMLCode getEBMLCode() {
		return code;
	}

	public Context getContext() {
		return context;
	}

	public static interface Context {

		EBMLType getType(EBMLCode code);

	}

	public static class RootContext implements Context {

		private final Map<EBMLCode, EBMLType> typeMap;

		public RootContext() {
			this.typeMap = new ConcurrentHashMap<EBMLCode, EBMLType>();
		}

		public synchronized EBMLType newType(String name, EBMLCode code) {
			Objects.requireNonNull(name);
			Objects.requireNonNull(code);
			if (typeMap.containsKey(code)) {
				throw new IllegalArgumentException("Type is already registered for " + code);
			}
			code = code.intern();
			EBMLType type = new EBMLType(name, code, this);
			typeMap.put(code, type);
			return type;
		}

		public synchronized EBMLType addType(EBMLType type) {
			Objects.requireNonNull(type);
			if (typeMap.containsKey(type.code)) {
				throw new IllegalArgumentException("Type is already registered for " + type.code);
			}
			typeMap.put(type.code, type);
			return type;
		}

		public EBMLType getType(EBMLCode code) {
			return typeMap.get(code);
		}

		@Override
		public String toString() {
			return "Context[0x" + Integer.toHexString(hashCode()) + "]";
		}

	}

	public static class SubContext extends RootContext {

		private final Context parent;

		public SubContext(Context parentContext) {
			super();
			this.parent = parentContext;
		}

		public EBMLType getType(EBMLCode code) {
			EBMLType type = super.getType(code);
			return type != null ? type : parent != null ? parent.getType(code) : null;
		}

		@Override
		public String toString() {
			return "SubContext[0x" + Integer.toHexString(hashCode()) + ", parent=" + parent + "]";
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EBMLType other = (EBMLType) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EBMLType[name=\"" + name + "\", code=" + code + ", context=" + context + "]";
	}

}
