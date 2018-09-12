package com.ancestry.oops.coinshot.service.apple;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.relayrides.pushy.apns.ApnsEnvironment;
import com.relayrides.pushy.apns.ExpiredToken;
import com.relayrides.pushy.apns.ExpiredTokenListener;
import com.relayrides.pushy.apns.FailedConnectionListener;
import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.PushManagerConfiguration;
import com.relayrides.pushy.apns.RejectedNotificationListener;
import com.relayrides.pushy.apns.RejectedNotificationReason;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.SSLContextUtil;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;
import com.relayrides.pushy.apns.util.TokenUtil;

/**
 * @author lcheeran
 */
public class TestApplePush
{

    private static String file
            = "/Users/lcheeran/IdeaProjects/oops-coinshot/coinshot-app/src/main/resources/TreeToGoProdAPSCertificates.p12";
    private static String pwd = "MiniM@c11";

    public static void main(String[] args)
    {

        try
        {
            final PushManager<SimpleApnsPushNotification> pushManager =
                    new PushManager<SimpleApnsPushNotification>(
                            ApnsEnvironment.getProductionEnvironment(),
                            SSLContextUtil.createDefaultSSLContext(file, pwd),
                            null, // Optional: custom event loop group
                            null, // Optional: custom ExecutorService for calling listeners
                            null, // Optional: custom BlockingQueue implementation
                            new PushManagerConfiguration(),
                            "ExamplePushManager");
            pushManager.start();

            pushManager.registerRejectedNotificationListener(new MyRejectedNotificationListener());
            pushManager.registerFailedConnectionListener(new MyFailedConnectionListener());
            pushManager.registerExpiredTokenListener(new MyExpiredTokenListener());
            //pushManager.requestExpiredTokens();

            Thread t = new Thread(() -> {
                try
                {
                    TimeUnit.SECONDS.sleep(10);
                    System.out.println(" calling requestExpireTokens");
                    pushManager.requestExpiredTokens();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            });
            t.start();

            final List<String> strings = Arrays.asList("cb438a4da64cecf3c94aec2f6d950e3fdffd6f85983faac1b291ef45ea5fd5ee",
                    "eab3fef79135c961a8bc98b5c0f5792d284ee07d6a42a8944065f2416dc238d5");

            strings.forEach((s) -> {
                final byte[] token;
                try
                {
                    token = TokenUtil.tokenStringToByteArray(s);
                    final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();

                    payloadBuilder.setAlertBody("Ring ring, Neo.");
                    payloadBuilder.setSoundFileName("default");

                    final String payload = payloadBuilder.buildWithDefaultMaximumLength();

                    pushManager.getQueue().put(new SimpleApnsPushNotification(token, payload));

                    System.out.println(" done " + s);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }


            });


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}

class MyRejectedNotificationListener implements RejectedNotificationListener<SimpleApnsPushNotification>
{

    @Override
    public void handleRejectedNotification(
            final PushManager<? extends SimpleApnsPushNotification> pushManager,
            final SimpleApnsPushNotification notification,
            final RejectedNotificationReason reason)
    {

        System.out.format("%s was rejected with rejection reason %s\n", notification, reason);
    }
}

class MyFailedConnectionListener implements FailedConnectionListener<SimpleApnsPushNotification>
{

    @Override
    public void handleFailedConnection(
            final PushManager<? extends SimpleApnsPushNotification> pushManager,
            final Throwable cause)
    {

        System.out.println(" called FailedConn");
    }
}

class MyExpiredTokenListener implements ExpiredTokenListener<SimpleApnsPushNotification>
{

    @Override
    public void handleExpiredTokens(
            final PushManager<? extends SimpleApnsPushNotification> pushManager,
            final Collection<ExpiredToken> expiredTokens)
    {
        System.out.println(" Expired token " + expiredTokens.size());
        expiredTokens.forEach(System.out::println);
    }
}
i
