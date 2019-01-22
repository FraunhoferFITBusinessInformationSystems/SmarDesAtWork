/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.common.util;

import androidx.databinding.ObservableField;

import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

import static androidx.databinding.Observable.OnPropertyChangedCallback;

public class RxUtils {

    private RxUtils() {
    }

    public static <T> Observable<T> toObservable(final ObservableField<T> observableField) {
        return Observable.create(emitter -> {
            final OnPropertyChangedCallback callback = new OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(androidx.databinding.Observable dataBindingObservable, int propertyId) {
                    if (dataBindingObservable == observableField) {
                        emitter.onNext(observableField.get());
                    }
                }
            };
            observableField.addOnPropertyChangedCallback(callback);
            emitter.setCancellable(() -> observableField.removeOnPropertyChangedCallback(callback));
        });
    }

    public static class RetryWithDelay implements Function<Flowable<Throwable>, Publisher<?>> {
        private final int maxRetries;
        private final int retryDelayMillis;
        private int retryCount;

        public RetryWithDelay(final int maxRetries, final int retryDelayMillis) {
            this.maxRetries = maxRetries;
            this.retryDelayMillis = retryDelayMillis;
            this.retryCount = 0;
        }

//        @Override
//        public Observable<?> apply(final Observable<? extends Throwable> attempts) {
//            return attempts
//                    .flatMap((final Throwable throwable) -> {
//                            if (maxRetries < 0 || ++retryCount < maxRetries) {
//                                // When this Observable calls onNext, the original
//                                // Observable will be retried (i.e. re-subscribed).
//                                return Observable.timer(retryDelayMillis,
//                                        TimeUnit.MILLISECONDS);
//                            }
//
//                            // Max retries hit. Just pass the error along.
//                            return Observable.error(throwable);
//                        }
//                    );
//        }

        @Override
        public Publisher<?> apply(Flowable<Throwable> attempts) throws Exception {
            return attempts
                    .flatMap((final Throwable throwable) -> {
                                if (maxRetries < 0 || ++retryCount < maxRetries) {
                                    // When this Observable calls onNext, the original
                                    // Observable will be retried (i.e. re-subscribed).
                                    return Flowable.timer(retryDelayMillis,
                                            TimeUnit.MILLISECONDS);
                                }

                                // Max retries hit. Just pass the error along.
                                return Flowable.error(throwable);
                            }
                    );
        }
    }
}
