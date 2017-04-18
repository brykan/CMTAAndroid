package co.createlou.cmta;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Bryan on 4/9/2017.
 */

public class MyApp extends Application {
        @Override
        public void onCreate() {
            super.onCreate();
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        }
}
