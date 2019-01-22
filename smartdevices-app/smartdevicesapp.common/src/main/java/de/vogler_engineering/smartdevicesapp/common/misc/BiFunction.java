/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.misc;

public interface BiFunction<T, U, R> {
    R apply(T var1, U var2);

    default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
        throw new RuntimeException("Stub!");
    }
}
