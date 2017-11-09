package ezhr.epesa;

import com.orhanobut.logger.Logger;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by ezhr on 27/9/17.
 */

public class DatabaseMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        /*RealmSchema schema = realm.getSchema();

        Logger.d("OLD " + oldVersion);
        Logger.d("NEW " + newVersion);
        if (oldVersion == 0) {
            schema.create("Transaction")
                    .addField("_id", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("amount", Float.class, FieldAttribute.REQUIRED)
                    .addField("contactPhone", Long.class, FieldAttribute.REQUIRED)
                    .addField("contactName", String.class)
                    .addField("date", Long.class, FieldAttribute.REQUIRED)
                    .addField("message", String.class)
                    .addField("sent", Boolean.class);
            oldVersion++;
        }*/
    }
}
