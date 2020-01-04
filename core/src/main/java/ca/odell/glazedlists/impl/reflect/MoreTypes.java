/**
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.odell.glazedlists.impl.reflect;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Static methods for working with types that we aren't publishing in the
 * public {@code Types} API.
 *
 * <p>This class contains modifications by James Lemieux to include it in the
 * Glazed Lists project. The original, unmodified version of this class can be
 * found <a href="http://code.google.com/p/google-guice/source/browse/trunk/src/com/google/inject/internal/MoreTypes.java">here</a>.
 *
 * @author jessewilson@google.com (Jesse Wilson)
 * @author James Lemieux
 */
class MoreTypes {
    private MoreTypes() {
    }

    private static final Map<TypeLiteral<?>, TypeLiteral<?>> PRIMITIVE_TO_WRAPPER;
    static {
        final Map<TypeLiteral<?>, TypeLiteral<?>> primitiveToWrapper = new HashMap<>();
        primitiveToWrapper.put(TypeLiteral.get(boolean.class), TypeLiteral.get(Boolean.class));
        primitiveToWrapper.put(TypeLiteral.get(byte.class), TypeLiteral.get(Byte.class));
        primitiveToWrapper.put(TypeLiteral.get(short.class), TypeLiteral.get(Short.class));
        primitiveToWrapper.put(TypeLiteral.get(int.class), TypeLiteral.get(Integer.class));
        primitiveToWrapper.put(TypeLiteral.get(long.class), TypeLiteral.get(Long.class));
        primitiveToWrapper.put(TypeLiteral.get(float.class), TypeLiteral.get(Float.class));
        primitiveToWrapper.put(TypeLiteral.get(double.class), TypeLiteral.get(Double.class));
        primitiveToWrapper.put(TypeLiteral.get(char.class), TypeLiteral.get(Character.class));
        primitiveToWrapper.put(TypeLiteral.get(void.class), TypeLiteral.get(Void.class));

        PRIMITIVE_TO_WRAPPER = Collections.unmodifiableMap(primitiveToWrapper);
    }

    /**
     * Returns an equivalent (but not necessarily equal) type literal that is
     * free of primitive types. Type literals of primitives will return the
     * corresponding wrapper types.
     */
    public static <T> TypeLiteral<T> wrapPrimitives(TypeLiteral<T> typeLiteral) {
        @SuppressWarnings("unchecked")
        TypeLiteral<T> wrappedPrimitives = (TypeLiteral<T>) PRIMITIVE_TO_WRAPPER.get(typeLiteral);
        return wrappedPrimitives != null
                ? wrappedPrimitives
                : typeLiteral;
    }

    /**
     * Returns a type that is functionally equal but not necessarily equal
     * according to {@link Object#equals(Object) Object.equals()}. The returned
     * type is {@link Serializable}.
     */
    public static Type canonicalize(Type type) {
        if (type instanceof ParameterizedTypeImpl) {
            return type;

        } else if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return new ParameterizedTypeImpl(p.getOwnerType(), p.getRawType(), p.getActualTypeArguments());

        } else {
            // type is either serializable as-is or unsupported
            return type;
        }
    }

    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            // type is a normal class.
            return (Class<?>) type;

        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class.
            // Neal isn't either but suspects some pathological case related
            // to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class<?>)) {
                throw unexpectedType(rawType, Class.class);
            }
            return (Class<?>) rawType;

        } else if (type instanceof GenericArrayType) {
            // TODO: Is this sufficient?
            return Object[].class;
        } else if (type instanceof TypeVariable || type instanceof WildcardType) {
            // we could use the variable's bounds, but that'll won't work if there are multiple.
            // having a raw type that's more general than necessary is okay
            return Object.class;
        } else {
            // type is a parameterized type.
            throw unexpectedType(type, ParameterizedType.class);
        }
    }

    private static AssertionError unexpectedType(Type type, Class<?> expected) {
        return new AssertionError(
                "Unexpected type. Expected: " + expected.getName()
                        + ", got: " + type.getClass().getName()
                        + ", for type literal: " + type.toString() + ".");
    }

    /**
     * Returns true if {@code a} and {@code b} are equal.
     */
    public static boolean equals(Type a, Type b) {
        if (a == b) {
            // also handles (a == null && b == null)
            return true;

        } else if (a instanceof Class) {
            // Class already specifies equals().
            return a.equals(b);

        } else if (a instanceof ParameterizedType) {
            if (!(b instanceof ParameterizedType)) {
                return false;
            }

            ParameterizedType pa = (ParameterizedType) a;
            ParameterizedType pb = (ParameterizedType) b;
            return equal(pa.getOwnerType(), pb.getOwnerType())
                    && pa.getRawType().equals(pb.getRawType())
                    && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());

        } else if (a instanceof GenericArrayType) {
            if (!(b instanceof GenericArrayType)) {
                return false;
            }

            GenericArrayType ga = (GenericArrayType) a;
            GenericArrayType gb = (GenericArrayType) b;
            return equals(ga.getGenericComponentType(), gb.getGenericComponentType());

        } else {
            // This isn't a type we support. Could be a generic array type, wildcard
            // type, etc.
            return false;
        }
    }

    public static boolean equal(Object o1, Object o2) {
      return (o1 == null) ? (o2 == null) : o1.equals(o2);
    }

    /**
     * Returns the hashCode of {@code type}.
     */
    public static int hashCode(Type type) {
        if (type instanceof Class) {
            // Class specifies hashCode().
            return type.hashCode();

        } else if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return Arrays.hashCode(p.getActualTypeArguments())
                    ^ p.getRawType().hashCode()
                    ^ hashCodeOrZero(p.getOwnerType());

        } else if (type instanceof GenericArrayType) {
            return hashCode(((GenericArrayType) type).getGenericComponentType());

        } else {
            // This isn't a type we support. Could be a generic array type, wildcard type, etc.
            return hashCodeOrZero(type);
        }
    }

    private static int hashCodeOrZero(Object o) {
        return o != null ? o.hashCode() : 0;
    }

    public static String toString(Type type) {
        if (type instanceof Class<?>) {
            return ((Class) type).getName();

        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] arguments = parameterizedType.getActualTypeArguments();
            Type ownerType = parameterizedType.getOwnerType();
            StringBuilder stringBuilder = new StringBuilder();
            if (ownerType != null) {
                stringBuilder.append(toString(ownerType)).append(".");
            }
            stringBuilder.append(toString(parameterizedType.getRawType()))
                    .append("<")
                    .append(toString(arguments[0]));
            for (int i = 1; i < arguments.length; i++) {
                stringBuilder.append(", ").append(toString(arguments[i]));
            }
            return stringBuilder.append(">").toString();


        } else if (type instanceof GenericArrayType) {
            return toString(((GenericArrayType) type).getGenericComponentType()) + "[]";

        } else {
            return type.toString();
        }
    }

    /**
     * Returns the generic supertype for {@code supertype}. For example, given a class {@code
     * IntegerSet}, the result for when supertype is {@code Set.class} is {@code Set<Integer>} and the
     * result when the supertype is {@code Collection.class} is {@code Collection<Integer>}.
     */
    public static Type getGenericSupertype(Type type, Class<?> rawType, Class<?> toResolve) {
        if (toResolve == rawType) {
            return type;
        }

        // we skip searching through interfaces if unknown is an interface
        if (toResolve.isInterface()) {
            Class[] interfaces = rawType.getInterfaces();
            for (int i = 0, length = interfaces.length; i < length; i++) {
                if (interfaces[i] == toResolve) {
                    return rawType.getGenericInterfaces()[i];
                } else if (toResolve.isAssignableFrom(interfaces[i])) {
                    return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
                }
            }
        }

        // check our supertypes
        if (!rawType.isInterface()) {
            while (rawType != Object.class) {
                Class<?> rawSupertype = rawType.getSuperclass();
                if (rawSupertype == toResolve) {
                    return rawType.getGenericSuperclass();
                } else if (toResolve.isAssignableFrom(rawSupertype)) {
                    return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
                }
                rawType = rawSupertype;
            }
        }

        // we can't resolve this further
        return toResolve;
    }

    public static Type resolveTypeVariable(Type type, Class<?> rawType, TypeVariable unknown) {
        Class<?> declaredByRaw = declaringClassOf(unknown);

        // we can't reduce this further
        if (declaredByRaw == null) {
            return unknown;
        }

        Type declaredBy = getGenericSupertype(type, rawType, declaredByRaw);
        if (declaredBy instanceof ParameterizedType) {
            int index = Arrays.asList(declaredByRaw.getTypeParameters()).indexOf(unknown);
            return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
        }

        return unknown;
    }

    /**
     * Returns the declaring class of {@code typeVariable}, or {@code null} if it was not declared by
     * a class.
     */
    private static Class<?> declaringClassOf(TypeVariable typeVariable) {
      GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
      return genericDeclaration instanceof Class
          ? (Class<?>) genericDeclaration
          : null;
    }

    public static class ParameterizedTypeImpl implements ParameterizedType, Serializable {
        private final Type ownerType;
        private final Type rawType;
        private final Type[] typeArguments;

        public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
            this.ownerType = ownerType == null ? null : canonicalize(ownerType);
            this.rawType = canonicalize(rawType);
            this.typeArguments = typeArguments.clone();
            for (int t = 0; t < this.typeArguments.length; t++) {
                checkArgument(!(this.typeArguments[t] instanceof Class<?>)
                        || !((Class) this.typeArguments[t]).isPrimitive(),
                        "Parameterized types may not have primitive arguments: %s", this.typeArguments[t]);
                this.typeArguments[t] = canonicalize(this.typeArguments[t]);
            }
        }

        @Override
        public Type[] getActualTypeArguments() {
            return typeArguments.clone();
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof ParameterizedType
                    && MoreTypes.equals(this, (ParameterizedType) other);
        }

        @Override
        public int hashCode() {
            return MoreTypes.hashCode(this);
        }

        @Override
        public String toString() {
            return MoreTypes.toString(this);
        }

        private static final long serialVersionUID = 0;

        private static void checkArgument(boolean expression, String errorMessageFormat, Object... errorMessageArgs) {
          if (!expression)
            throw new IllegalArgumentException(String.format(errorMessageFormat, errorMessageArgs));
        }
    }
}