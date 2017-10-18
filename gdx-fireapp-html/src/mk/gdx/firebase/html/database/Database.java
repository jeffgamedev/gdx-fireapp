/*
 * Copyright 2017 mk
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

package mk.gdx.firebase.html.database;

import com.badlogic.gdx.Gdx;

import java.util.Map;

import mk.gdx.firebase.callbacks.CompleteCallback;
import mk.gdx.firebase.callbacks.DataCallback;
import mk.gdx.firebase.callbacks.TransactionCallback;
import mk.gdx.firebase.distributions.DatabaseDistribution;
import mk.gdx.firebase.exceptions.DatabaseReferenceNotSetException;
import mk.gdx.firebase.html.firebase.ScriptRunner;
import mk.gdx.firebase.listeners.ConnectedListener;
import mk.gdx.firebase.listeners.DataChangeListener;

/**
 * GWT Firebase API implementation.
 * <p>
 * It is throws when required annotation is missing.
 * <p>
 * Because of missing {@link java.lang.reflect.ParameterizedType} on GWT platform there is a need of
 * dealing with generic types in some other way. GdxFireapp using annotation for this.
 * <p>
 * There are two rules when you dealing with nested generic types (for ex. Callback<List<User>>) on gwt:
 * 1. Every callback must be separated class and added to reflection inside yours .gwt.xml configuration file.
 * 2. The callback method must have {@link mk.gdx.firebase.annotations.NestedGenericType} annotation with description of nested class.
 * <p>
 * Here is some example of valid declaration:
 * <p>
 * {@code
 * public class MyCallback implements Callback<List<User>>{
 *
 * @NestedGenericType(User.class)
 * @Override public void process(List<User> data)
 * {
 * // do some stuff
 * }
 * }
 * @see DatabaseDistribution
 */
public class Database implements DatabaseDistribution
{

    private String refPath;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnect(final ConnectedListener connectedListener)
    {
        ScriptRunner.firebaseScript(new Runnable()
        {
            @Override
            public void run()
            {
                DatabaseJS.onConnect(connectedListener);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabaseDistribution inReference(String databasePath)
    {
        refPath = databasePath;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final Object value)
    {
        ScriptRunner.firebaseScript(new ScriptRunner.ScriptDBAction(databaseReference())
        {
            @Override
            public void run()
            {
                DatabaseJS.set(scriptRefPath, StringGenerator.dataToString(value));
            }
        });
        terminateOperation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final Object value, final CompleteCallback completeCallback)
    {
        ScriptRunner.firebaseScript(new ScriptRunner.ScriptDBAction(databaseReference())
        {
            @Override
            public void run()
            {
                DatabaseJS.setWithCallback(scriptRefPath, StringGenerator.dataToString(value), completeCallback);
            }
        });
        terminateOperation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, R extends T> void readValue(final Class<T> dataType, final DataCallback<R> callback)
    {
        ScriptRunner.firebaseScript(new ScriptRunner.ScriptDBAction(databaseReference())
        {
            @Override
            public void run()
            {
                DatabaseJS.setNextDataCallback(new JsonDataCallback<R>(dataType, callback));
                DatabaseJS.once(scriptRefPath);
            }
        });
        terminateOperation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, R extends T> void onDataChange(final Class<T> dataType, final DataChangeListener<R> listener)
    {
        // TODO test, String.class value
        ScriptRunner.firebaseScript(new ScriptRunner.ScriptDBAction(databaseReference())
        {
            @Override
            public void run()
            {
                // TODO DataChangeListener::onCancelled
                if (listener != null && !DatabaseJS.hasListener(scriptRefPath)) {
                    DatabaseJS.addDataListener(scriptRefPath, new JsonDataListener<R>(dataType, listener));
                    DatabaseJS.onValue(scriptRefPath);
                } else if (listener == null) {
                    DatabaseJS.offValue(scriptRefPath);
                }
            }
        });
        terminateOperation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabaseDistribution push()
    {
        ScriptRunner.firebaseScript(new ScriptRunner.ScriptDBAction(databaseReference())
        {
            @Override
            public void run()
            {
                // TODO - some special callback for other actions? On the other hand, no others action can be done before this if it is waiting for firebase.js
                refPath = DatabaseJS.push(scriptRefPath);
            }
        });
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeValue()
    {
        ScriptRunner.firebaseScript(new ScriptRunner.ScriptDBAction(databaseReference())
        {
            @Override
            public void run()
            {
                DatabaseJS.remove(scriptRefPath);
            }
        });
        terminateOperation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeValue(final CompleteCallback completeCallback)
    {
        ScriptRunner.firebaseScript(new ScriptRunner.ScriptDBAction(databaseReference())
        {
            @Override
            public void run()
            {
                DatabaseJS.removeWithCallback(scriptRefPath, completeCallback);
            }
        });
        terminateOperation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateChildren(final Map<String, Object> data)
    {
        ScriptRunner.firebaseScript(new ScriptRunner.ScriptDBAction(databaseReference())
        {
            @Override
            public void run()
            {
                DatabaseJS.update(scriptRefPath, MapTransformer.mapToJSON(data));
            }
        });
        terminateOperation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateChildren(final Map<String, Object> data, final CompleteCallback completeCallback)
    {
        ScriptRunner.firebaseScript(new Runnable()
        {
            @Override
            public void run()
            {
                DatabaseJS.updateWithCallback(databaseReference(), MapTransformer.mapToJSON(data), completeCallback);
            }
        });
        terminateOperation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, R extends T> void transaction(final Class<T> dataType, final TransactionCallback<R> transactionCallback, final CompleteCallback completeCallback)
    {
        // TODO test
        ScriptRunner.firebaseScript(new ScriptRunner.ScriptDBAction(databaseReference())
        {
            @Override
            public void run()
            {
                DatabaseJS.transaction(scriptRefPath, new JsonDataModifier<R>(dataType, transactionCallback), completeCallback);
            }
        });
        terminateOperation();
    }

    /**
     * Firebase web api does not support this feature.
     * <p>
     *
     * @param enabled If true persistence will be enabled.
     */
    @Override
    public void setPersistenceEnabled(boolean enabled)
    {
        Gdx.app.log("GdxFireapp", "No such feature on firebase web platform.");
    }

    /**
     * Firebase web api does not support this feature.
     *
     * @param synced If true sync for specified database path will be enabled
     */
    @Override
    public void keepSynced(boolean synced)
    {
        Gdx.app.log("GdxFireapp", "No such feature on firebase web platform.");
    }

    /**
     * Gets firebase database path from local var or throw exception when it is null.
     *
     * @return Database reference path. Every action will be deal with it.
     * @throws DatabaseReferenceNotSetException It is thrown when user forgot to call {@link #inReference(String)}
     */
    private String databaseReference()
    {
        if (refPath == null)
            throw new DatabaseReferenceNotSetException("Please call GdxFIRDatabase#inReference() first.");
        return refPath;
    }

    /**
     * Reset {@link #refPath} to initial state.
     * <p>
     * After each flow-terminate operation {@link #refPath} should be reset the initial value,
     * it forces the users to call {@link #inReference(String)} before each flow-terminate operation.
     * <p>
     * Flow-terminate operations are: <uL>
     * <li>{@link #setValue(Object)}</li>
     * <li>{@link #setValue(Object, CompleteCallback)}</li>
     * <li>{@link #readValue(Class, DataCallback)}</li>
     * <li>{@link #onDataChange(Class, DataChangeListener)}</li>
     * <li>{@link #updateChildren(Map)}</li>
     * <li>{@link #updateChildren(Map, CompleteCallback)}</li>
     * <li>{@link #transaction(Class, TransactionCallback, CompleteCallback)}</li>
     * </uL>
     */
    private void terminateOperation()
    {
        refPath = null;
    }

}
