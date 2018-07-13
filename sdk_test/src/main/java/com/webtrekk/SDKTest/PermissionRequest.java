package com.webtrekk.SDKTest;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.subjects.AsyncSubject;

/**
 * Created by vartbaronov on 03.10.17.
 */

//This class request permission. It works as injection to Activity class.
public class PermissionRequest {
    private final static int PERMISSION_REQUEST = 1;
    private Map<String, AsyncSubject<Void>> observables = new HashMap<>();

    /**main function request for permission. If Completable completes permission is granted.
     * Otherwise error is emitted.
    */
    public List<Completable> requestPermission(@NotNull Activity activity, @NotNull String permissions[]) {

        List<Completable> completables = new ArrayList<>();
        List<String> permissionsResult = new ArrayList<>();

        for (String permission: permissions) {
            AsyncSubject<Void> observable = AsyncSubject.create();
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsResult.add(permission);
                observables.put(permission, observable);
                completables.add(Completable.fromObservable(observable));
            } else {
                completables.add(Completable.complete());
            }
        }

        ActivityCompat.requestPermissions(activity,
                permissionsResult.toArray(new String[permissionsResult.size()]), PERMISSION_REQUEST);

        return completables;
    }

    // process response for onRequestPermissionsResult message
    public void processResponse(int requestCode,
                                 @NotNull String permissions[], @NotNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                for (int i = 0; i< grantResults.length; i++){

                    final AsyncSubject<Void> observable = observables.get(permissions[i]);

                    if (observable == null){
                        continue;
                    }

                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                        observable.onComplete();
                    } else {
                        observable.onError(new SecurityException());
                    }
                }
            }
        }
    }
}
