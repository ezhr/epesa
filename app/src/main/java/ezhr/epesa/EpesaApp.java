package ezhr.epesa;

import android.app.Application;

import com.orhanobut.hawk.Hawk;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by ezhr on 25/9/17.
 */

public class EpesaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /*if (BuildConfig.DEBUG) {
            AndroidDevMetrics.initWith(this);
        }*/
        com.blankj.utilcode.util.Utils.init(this);
        Realm.init(this);
        /*RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(0) // Must be bumped when the schema changes
                .migration(new DatabaseMigration()) // Migration to run instead of throwing an exception
                .build();*/
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
        /*Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());*/
        Hawk.init(this).build();
        FormatStrategy strategy = PrettyFormatStrategy.newBuilder().tag(Utils.getTag()).build();
        Logger.addLogAdapter(new AndroidLogAdapter(strategy));
    }
}
