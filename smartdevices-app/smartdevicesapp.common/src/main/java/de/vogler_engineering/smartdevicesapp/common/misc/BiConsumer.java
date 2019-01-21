/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.misc;

import io.reactivex.annotations.NonNull;

public interface BiConsumer<T1, T2> {
    /**
     * Supply some value to a Consumer.
     * @param t1 the first input value
     * @param t2 the first input value
     */
    void apply(@NonNull T1 t1, @NonNull T2 t2);
}