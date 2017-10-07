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

package mk.gdx.firebase.html.auth;

import mk.gdx.firebase.GdxFIRAuth;
import mk.gdx.firebase.auth.GdxFirebaseUser;
import mk.gdx.firebase.callbacks.AuthCallback;

/**
 * Provides calls to firebase javascript api.
 */
class AuthJS
{
    public static native FirebaseUserJSON firebaseUser() /*-{
        if( typeof $wnd.firebase == 'undefined') return { isNULL : true };
        var user = $wnd.firebase.auth().currentUser;
        return (typeof user != 'undefined' && user != null) ? user : { isNULL : true };
    }-*/;

    public static native void singInAnonymously(final AuthCallback callback) /*-{
        var removeAuthListener = $wnd.firebase.auth().onAuthStateChanged(function(user){
            if( user ){
                callback.@mk.gdx.firebase.callbacks.AuthCallback::onSuccess(Lmk/gdx/firebase/auth/GdxFirebaseUser;)(
                    @mk.gdx.firebase.html.auth.AuthJS::getUserBridge()()
                );
            }
            removeAuthListener();
        });
        $wnd.firebase.auth().signInAnonymously()['catch'](function(error) {
          callback.@mk.gdx.firebase.callbacks.AuthCallback::onFail(Ljava/lang/Exception;)(
            @java.lang.Exception::new(Ljava/lang/String;)(error.message)
          );
        });
    }-*/;

    public static native void signInWithEmailAndPassword(final String email, final String password, final AuthCallback callback) /*-{
        var removeAuthListener = $wnd.firebase.auth().onAuthStateChanged(function(user){
            if( user ){
                callback.@mk.gdx.firebase.callbacks.AuthCallback::onSuccess(Lmk/gdx/firebase/auth/GdxFirebaseUser;)(
                    @mk.gdx.firebase.html.auth.AuthJS::getUserBridge()()
                );
            }
            removeAuthListener();
        });
        $wnd.firebase.auth().signInWithEmailAndPassword(email, password)['catch'](function(error) {
            callback.@mk.gdx.firebase.callbacks.AuthCallback::onFail(Ljava/lang/Exception;)(
                @java.lang.Exception::new(Ljava/lang/String;)(error.message)
            );
        });
    }-*/;

    public static native void createUserWithEmailAndPassword(final String email, final String password, final AuthCallback callback) /*-{
        var removeAuthListener = $wnd.firebase.auth().onAuthStateChanged(function(user){
            if( user ){
                callback.@mk.gdx.firebase.callbacks.AuthCallback::onSuccess(Lmk/gdx/firebase/auth/GdxFirebaseUser;)(
                    @mk.gdx.firebase.html.auth.AuthJS::getUserBridge()()
                );
            }
            removeAuthListener();
        });
        $wnd.firebase.auth().createUserWithEmailAndPassword(email, password)['catch'](function(error) {
            callback.@mk.gdx.firebase.callbacks.AuthCallback::onFail(Ljava/lang/Exception;)(
                @java.lang.Exception::new(Ljava/lang/String;)(error.message)
            );
        });
    }-*/;

    public static native void signInWithToken(final String token, final AuthCallback callback) /*-{
        var removeAuthListener = $wnd.firebase.auth().onAuthStateChanged(function(user){
            if( user ){
                callback.@mk.gdx.firebase.callbacks.AuthCallback::onSuccess(Lmk/gdx/firebase/auth/GdxFirebaseUser;)(
                    @mk.gdx.firebase.html.auth.AuthJS::getUserBridge()()
                );
            }
            removeAuthListener();
        });
        $wnd.firebase.auth().signInWithCustomToken(token)['catch'](function(error) {
            callback.@mk.gdx.firebase.callbacks.AuthCallback::onFail(Ljava/lang/Exception;)(
                @java.lang.Exception::new(Ljava/lang/String;)(error.message)
            );
        });
    }-*/;

    /**
     * @return Simplest way for javascript to get GdxFirebaseUser.
     */
    public static GdxFirebaseUser getUserBridge()
    {
        return GdxFIRAuth.instance().getCurrentUser();
    }
}
