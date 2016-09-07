/**
 * Copyright (C) 2006 Google Inc.
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

import static ca.odell.glazedlists.impl.Preconditions.checkArgument;
import static ca.odell.glazedlists.impl.Preconditions.checkNotNull;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a generic type {@code T}. Java doesn't yet provide a way to
 * represent generic types, so this class does. Forces clients to create a
 * subclass of this class which enables retrieval the type information even at
 * runtime.
 *
 * <p>For example, to create a type literal for {@code List<String>}, you can
 * create an empty anonymous inner class:
 *
 * <p>
 * {@code TypeLiteral<List<String>> list = new TypeLiteral<List<String>>() {};}
 *
 * <p>Assumes that type {@code T} implements {@link Object#equals} and
 * {@link Object#hashCode()} as value (as opposed to identity) comparison.
 *
 * <p>This class contains modifications by James Lemieux to include it in the
 * Glazed Lists project. The original, unmodified version of this class can be
 * found <a href="http://code.google.com/p/google-guice/source/browse/trunk/src/com/google/inject/TypeLiteral.java">here</a>.
 *
 * @author crazybob@google.com (Bob Lee)
 * @author jessewilson@google.com (Jesse Wilson)
 * @author James Lemieux
 */
public class TypeLiteral<T> {

  final Class<? super T> rawType;
  final Type type;
  final int hashCode;

  /**
   * Unsafe. Constructs a type literal manually.
   */
  @SuppressWarnings("unchecked")
  TypeLiteral(Type type) {
    this.type = MoreTypes.canonicalize(checkNotNull(type, "type"));
    this.rawType = (Class<? super T>) MoreTypes.getRawType(this.type);
    this.hashCode = MoreTypes.hashCode(this.type);
  }

  /**
   * Returns the raw (non-generic) type for this type.
   *
   * @since 2.0
   */
  public final Class<? super T> getRawType() {
    return rawType;
  }

  /**
   * Gets underlying {@code Type} instance.
   */
  public final Type getType() {
    return type;
  }

  @Override public final int hashCode() {
    return this.hashCode;
  }

  @Override public final boolean equals(Object o) {
    return o instanceof TypeLiteral<?>
        && MoreTypes.equals(type, ((TypeLiteral) o).type);
  }

  @Override public final String toString() {
    return MoreTypes.toString(type);
  }

  /**
   * Gets type literal for the given {@code Type} instance.
   */
  public static TypeLiteral<?> get(Type type) {
    return new TypeLiteral<Object>(type);
  }

  /**
   * Gets type literal for the given {@code Class} instance.
   */
  public static <T> TypeLiteral<T> get(Class<T> type) {
    return new TypeLiteral<T>(type);
  }


  /** Returns an immutable list of the resolved types. */
  private List<TypeLiteral<?>> resolveAll(Type[] types) {
    TypeLiteral<?>[] result = new TypeLiteral<?>[types.length];
    for (int t = 0; t < types.length; t++) {
      result[t] = resolve(types[t]);
    }
    return Arrays.asList(result);
  }

  /**
   * Resolves known type parameters in {@code toResolve} and returns the result.
   */
  TypeLiteral<?> resolve(Type toResolve) {
    return TypeLiteral.get(resolveType(toResolve));
  }

  Type resolveType(Type toResolve) {
    // this implementation is made a little more complicated in an attempt to avoid object-creation
    while (true) {
      if (toResolve instanceof TypeVariable) {
        TypeVariable original = (TypeVariable) toResolve;
        toResolve = MoreTypes.resolveTypeVariable(type, rawType, original);
        if (toResolve == original) {
          return toResolve;
        }

      } else if (toResolve instanceof ParameterizedType) {
        ParameterizedType original = (ParameterizedType) toResolve;
        Type ownerType = original.getOwnerType();
        Type newOwnerType = resolveType(ownerType);
        boolean changed = newOwnerType != ownerType;

        Type[] args = original.getActualTypeArguments();
        for (int t = 0, length = args.length; t < length; t++) {
          Type resolvedTypeArgument = resolveType(args[t]);
          if (resolvedTypeArgument != args[t]) {
            if (!changed) {
              args = args.clone();
              changed = true;
            }
            args[t] = resolvedTypeArgument;
          }
        }

          return changed
            ? new MoreTypes.ParameterizedTypeImpl(newOwnerType, original.getRawType(), args)
            : original;

      } else {
        return toResolve;
      }
    }
  }

  /**
   * Returns the resolved generic parameter types of {@code methodOrConstructor}.
   *
   * @param methodOrConstructor a method or constructor defined by this or any supertype.
   * @since 2.0
   */
  public List<TypeLiteral<?>> getParameterTypes(Member methodOrConstructor) {
    Type[] genericParameterTypes;

    if (methodOrConstructor instanceof Method) {
      Method method = (Method) methodOrConstructor;
      checkArgument(method.getDeclaringClass().isAssignableFrom(rawType),
          "%s is not defined by a supertype of %s", method, type);
      genericParameterTypes = method.getGenericParameterTypes();

    } else if (methodOrConstructor instanceof Constructor) {
      Constructor constructor = (Constructor) methodOrConstructor;
      checkArgument(constructor.getDeclaringClass().isAssignableFrom(rawType),
          "%s does not construct a supertype of %s", constructor, type);
      genericParameterTypes = constructor.getGenericParameterTypes();

    } else {
      throw new IllegalArgumentException("Not a method or a constructor: " + methodOrConstructor);
    }

    return resolveAll(genericParameterTypes);
  }

  /**
   * Returns the resolved generic return type of {@code method}.
   *
   * @param method a method defined by this or any supertype.
   * @since 2.0
   */
  public TypeLiteral<?> getReturnType(Method method) {
    checkArgument(method.getDeclaringClass().isAssignableFrom(rawType),
        "%s is not defined by a supertype of %s", method, type);
    return resolve(method.getGenericReturnType());
  }
}
