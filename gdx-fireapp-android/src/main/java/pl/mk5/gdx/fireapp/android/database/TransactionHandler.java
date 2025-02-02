/*
 * Copyright 2018 mk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.mk5.gdx.fireapp.android.database;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import pl.mk5.gdx.fireapp.GdxFIRLogger;
import pl.mk5.gdx.fireapp.annotations.MapConversion;
import pl.mk5.gdx.fireapp.functional.Function;
import pl.mk5.gdx.fireapp.promises.FuturePromise;
import pl.mk5.gdx.fireapp.reflection.AnnotationFinder;
import pl.mk5.gdx.fireapp.reflection.DefaultTypeRecognizer;

/**
 * Provides transactionFunction invocation
 */
class TransactionHandler<R> implements Transaction.Handler {

    private static final String TRANSACTION_NOT_ABLE_TO_COMMIT = "The database value at given path was not be able to commit";
    private static final String TRANSACTION_ERROR = "Transaction error - aborting";


    private final Function<R, R> transactionFunction;
    private final FuturePromise<Void> promise;
    private final Class<?> dataType;


    /**
     * @param dataType            DataType, not null
     * @param transactionFunction Transaction function, not null
     * @param promise             Promise, not null
     */
    TransactionHandler(Class<?> dataType, Function<R, R> transactionFunction, FuturePromise<Void> promise) {
        this.dataType = dataType;
        this.transactionFunction = transactionFunction;
        this.promise = promise;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Transaction.Result doTransaction(MutableData mutableData) {
        try {
            if (mutableData.getValue() == null) {
                mutableData.setValue(transactionFunction.apply((R) DefaultTypeRecognizer.getDefaultValue(dataType)));
                return Transaction.success(mutableData);
            }
            MapConversion mapConversionAnnotation = null;
            R transactionData;
            if (promise.getThenConsumer() != null) {
                mapConversionAnnotation = AnnotationFinder.getMethodAnnotation(MapConversion.class, promise.getThenConsumer());
            }
            if (mapConversionAnnotation != null) {
                transactionData = (R) mutableData.getValue(mapConversionAnnotation.value());
            } else {
                transactionData = (R) mutableData.getValue();
            }
            mutableData.setValue(transactionFunction.apply(transactionData));
            return Transaction.success(mutableData);
        } catch (Exception e) {
            GdxFIRLogger.error(TRANSACTION_ERROR, e);
            return Transaction.abort();
        }
    }

    @Override
    public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
        if (databaseError != null) {
            promise.doFail(databaseError.toException());
        } else {
            if (committed) {
                promise.doComplete(null);
            } else {
                promise.doFail(TRANSACTION_NOT_ABLE_TO_COMMIT, null);
            }
        }
    }
}
