package co.createlou.cmta;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Method;

/**
 * Created by Bryan on 4/9/2017.
 */

public class MyApp extends Application {
        @Override
        public void onCreate() {
            super.onCreate();
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            if(Build.VERSION.SDK_INT>=24){
                try{
                    Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                    m.invoke(null);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
}
