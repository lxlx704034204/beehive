package com.gustz.beehive.jpush.api;

import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosAlert;
import cn.jpush.api.push.model.notification.Notification;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.gustz.beehive.jpush.BaseRemotePushTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * XXX
 *
 * @author zhangzhenfeng
 * @since 2016-09-08
 */
public class NotificationTest extends BaseRemotePushTest {

    @Test
    public void sendNotification_alert_json() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("key1", "value1");
        json.addProperty("key2", true);

        String alert = json.toString();
        System.out.println(alert);

        PushPayload payload = PushPayload.newBuilder()
                .setAudience(Audience.all())
                .setPlatform(Platform.all())
                .setNotification(Notification.newBuilder()
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .setAlert(alert)
                                .setTitle("title").build()).build())
                .build();
        PushResult result = _client.sendPush(payload);
        Assert.assertTrue(result.isResultOK());
    }

    // --------------- Android

    @Test
    public void sendNotification_android_title() throws Exception {
        PushPayload payload = PushPayload.newBuilder()
                .setAudience(Audience.all())
                .setPlatform(Platform.all())
                .setNotification(Notification.newBuilder()
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .setAlert(ALERT)
                                .setTitle("title").build()).build())
                .build();
        PushResult result = _client.sendPush(payload);
        Assert.assertTrue(result.isResultOK());
    }

    @Test
    public void sendNotification_android_buildId() throws Exception {
        PushPayload payload = PushPayload.newBuilder()
                .setAudience(Audience.all())
                .setPlatform(Platform.all())
                .setNotification(Notification.newBuilder()
                        .setAlert(ALERT)
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .setBuilderId(100)
                                .build()).build())
                .build();
        PushResult result = _client.sendPush(payload);
        Assert.assertTrue(result.isResultOK());
    }

    @Test
    public void sendNotification_android_extras() throws Exception {
        PushPayload payload = PushPayload.newBuilder()
                .setAudience(Audience.all())
                .setPlatform(Platform.all())
                .setNotification(Notification.newBuilder()
                        .setAlert(ALERT)
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .addExtra("key1", "value1")
                                .addExtra("key2", 222)
                                .build()).build())
                .build();
        PushResult result = _client.sendPush(payload);
        Assert.assertTrue(result.isResultOK());
    }
    // ------------------ ios

    @Test
    public void sendNotification_ios_badge() throws Exception {
        PushPayload payload = PushPayload.newBuilder()
                .setAudience(Audience.all())
                .setPlatform(Platform.ios())
                .setNotification(Notification.ios_auto_badge())
                .build();
        PushResult result = _client.sendPush(payload);
        System.out.println("**************");
        System.out.println(result);
        System.out.println(result.getResponseCode());
        System.out.println("**************");
        Assert.assertTrue(result.isResultOK());
    }

    @Test
    public void sendNotification_ios_alert_jsonStr() throws Exception {
        JsonObject alert = new JsonObject();
        alert.add("title", new JsonPrimitive("Game Request"));
        alert.add("body", new JsonPrimitive("Bob wants to play poker"));
        alert.add("action-loc-key", new JsonPrimitive("PLAY"));
        //
        Audience audience = Audience.alias("1");
        PushPayload payload = PushPayload.newBuilder()
                .setAudience(audience)
                .setPlatform(Platform.ios())
                .setNotification(Notification.alert(alert.toString()))
                .build();
        // {"msg_id":1769052566,"sendno":1939247965}
        PushResult result = _client.sendPush(payload);
        System.out.println("**************");
        System.out.println(result);
        System.out.println(result.getResponseCode());
        System.out.println("**************");
        Assert.assertTrue(result.isResultOK());
    }

    @Test
    public void sendNotification_ios_alert_jsonObj() throws Exception {
        IosAlert alert = IosAlert.newBuilder()
                .setTitleAndBody("ios title", "test ios title")
                .build();

        PushPayload payload = PushPayload.newBuilder()
                .setAudience(Audience.all())
                .setPlatform(Platform.ios())
                .setNotification(Notification.alert(alert))
                .build();
        PushResult result = _client.sendPush(payload);
        Assert.assertTrue(result.isResultOK());
    }
}
