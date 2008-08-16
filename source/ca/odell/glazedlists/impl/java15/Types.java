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
package ca.odell.glazedlists.impl.java15;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Static methods for working with types.
 *
 * <p>This class contains modifications by James Lemieux to include it in the
 * Glazed Lists project. The original, unmodified version of this class can be
 * found <a href="http://code.google.com/p/google-guice/source/browse/trunk/src/com/google/inject/util/Types.java">here</a>.
 *
 * @author crazybob@google.com (Bob Lee)
 * @author James Lemieux
 */
class Types {
    private Types() {
    }

    /**
     * Returns a new parameterized type, applying {@code typeArguments} to
     * {@code rawType}. The returned type does not have an owner type.
     *
     * @return a {@link java.io.Serializable serializable} parameterized type.
     */
    public static ParameterizedType newParameterizedType(Type rawType, Type... typeArguments) {
        return newParameterizedTypeWithOwner(null, rawType, typeArguments);
    }

    /**
     * Returns a new parameterized type, applying {@code typeArguments} to
     * {@code rawType} and enclosed by {@code ownerType}.
     *
     * @return a {@link java.io.Serializable serializable} parameterized type.
     */
    public static ParameterizedType newParameterizedTypeWithOwner(Type ownerType, Type rawType, Type... typeArguments) {
        return new ca.odell.glazedlists.impl.java15.MoreTypes.ParameterizedTypeImpl(ownerType, rawType, typeArguments);
    }

    /**
     * Returns an array whose elements are all instances of
     * {@code componentType}.
     *
     * @return a {@link java.io.Serializable serializable} generic array type.
     */
    public static GenericArrayType arrayOf(Type componentType) {
        return new ca.odell.glazedlists.impl.java15.MoreTypes.GenericArrayTypeImpl(componentType);
    }

    /**
     * Returns a type modelling a {@link List} whose elements are of type
     * {@code elementType}.
     *
     * @return a {@link java.io.Serializable serializable} parameterized type.
     */
    public static ParameterizedType listOf(Type elementType) {
        return newParameterizedType(List.class, elementType);
    }

    /**
     * Returns a type modelling a {@link Set} whose elements are of type
     * {@code elementType}.
     *
     * @return a {@link java.io.Serializable serializable} parameterized type.
     */
    public static ParameterizedType setOf(Type elementType) {
        return newParameterizedType(Set.class, elementType);
    }

    /**
     * Returns a type modelling a {@link Map} whose keys are of type
     * {@code keyType} and whose values are of type {@code valueType}.
     *
     * @return a {@link java.io.Serializable serializable} parameterized type.
     */
    public static ParameterizedType mapOf(Type keyType, Type valueType) {
        return newParameterizedType(Map.class, keyType, valueType);
    }
}