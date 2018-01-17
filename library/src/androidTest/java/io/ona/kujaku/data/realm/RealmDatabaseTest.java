package io.ona.kujaku.data.realm;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.data.MapBoxDeleteTask;
import io.ona.kujaku.data.MapBoxDownloadTask;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 29/12/2017.
 */
public class RealmDatabaseTest extends RealmRelatedInstrumentedTest {

    private String sampleMapBoxStyleURL = "mapbox://styles/user/i89lkjscd";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        insertValueInPrivateStaticField(RealmDatabase.class, "realmDatabase", null);
    }

    @Test
    public void initShouldCreateNewInstance() throws NoSuchFieldException, IllegalAccessException {
        Object object = getValueInPrivateField(RealmDatabase.class, null, "realmDatabase");
        assertEquals(null, object);

        RealmDatabase realmDatabase = RealmDatabase.init(context);

        Object newValue = getValueInPrivateField(RealmDatabase.class, null, "realmDatabase");
        assertTrue(newValue != null);
        assertTrue(realmDatabase != null);
    }

    @Test
    public void initShouldNotCreateNewInstanceWhenSingletonAlreadyExists() throws NoSuchFieldException, IllegalAccessException {
        RealmDatabase expectedRealmDatabase = RealmDatabase.init(context);

        Object object = getValueInPrivateField(RealmDatabase.class, null, "realmDatabase");
        assertTrue(expectedRealmDatabase != null);
        assertEquals(object, expectedRealmDatabase);

        RealmDatabase realmDatabase = RealmDatabase.init(context);
        assertEquals(expectedRealmDatabase, realmDatabase);
    }

    @Test
    public void constructorShouldCreateValidConfigurationWhenGivenContext() {
        RealmDatabase.init(context);

        Realm realm = Realm.getDefaultInstance();
        RealmConfiguration realmConfiguration = realm.getConfiguration();

        assertEquals(RealmDatabase.NAME, realmConfiguration.getRealmFileName());
        assertEquals(RealmDatabase.VERSION, realmConfiguration.getSchemaVersion());
    }

    @Test
    public void deleteTaskShouldReturnFalseWhenGivenNonExistentTask() {
        RealmDatabase realmDatabase = RealmDatabase.init(context);

        boolean isDeleted = realmDatabase.deleteTask("klskdf", false);
        assertFalse(isDeleted);

        isDeleted = realmDatabase.deleteTask("klkjsdf", true);
        assertFalse(isDeleted);
    }

    @Test
    public void deleteTaskShouldReturnFalseWhenGivenWrongTaskTypeWithExistentTask() {
        RealmDatabase realmDatabase = RealmDatabase.init(context);

        String downloadMapName = UUID.randomUUID().toString();
        String deleteMapName = UUID.randomUUID().toString();
        MapBoxDownloadTask mapBoxDownloadTask = createSampleDownloadTask("kl", downloadMapName, sampleMapBoxStyleURL);
        addedRecords.add(MapBoxDownloadTask.constructMapBoxOfflineQueueTask(mapBoxDownloadTask));

        MapBoxDeleteTask mapBoxDeleteTask = new MapBoxDeleteTask(deleteMapName, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
        addedRecords.add(MapBoxDeleteTask.constructMapBoxOfflineQueueTask(mapBoxDeleteTask));

        assertFalse(realmDatabase.deleteTask(downloadMapName, false));
        assertFalse(realmDatabase.deleteTask(deleteMapName, true));
    }

    @Test
    public void deleteTaskShouldShouldReturnTrueWhenGivenExistentTasks() {
        RealmDatabase realmDatabase = RealmDatabase.init(context);

        String downloadMapName = UUID.randomUUID().toString();
        String deleteMapName = UUID.randomUUID().toString();
        MapBoxDownloadTask mapBoxDownloadTask = createSampleDownloadTask("kl", downloadMapName, sampleMapBoxStyleURL);
        MapBoxDownloadTask.constructMapBoxOfflineQueueTask(mapBoxDownloadTask);

        MapBoxDeleteTask mapBoxDeleteTask = new MapBoxDeleteTask(deleteMapName, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
        MapBoxDeleteTask.constructMapBoxOfflineQueueTask(mapBoxDeleteTask);

        assertTrue(realmDatabase.deleteTask(downloadMapName, true));
        assertFalse(realmDatabase.deleteTask(downloadMapName, true));

        assertTrue(realmDatabase.deleteTask(deleteMapName, false));
        assertFalse(realmDatabase.deleteTask(deleteMapName, false));
    }

    /*
    ---------------------------

    HELPER METHODS

    ---------------------------
     */

    private MapBoxDownloadTask createSampleDownloadTask(String packageName, String mapName, String mapBoxStyleURL) {

        return new MapBoxDownloadTask(
                packageName,
                mapName,
                mapBoxStyleURL,
                10d,
                12d,
                new LatLng(
                        -17.854564,
                        25.854782
                ),
                new LatLng(
                        -17.875469,
                        25.876589
                ),
                BuildConfig.MAPBOX_SDK_ACCESS_TOKEN
        );
    }
}