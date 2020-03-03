package xsg.lychee.richalpha.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import xsg.lychee.richalpha.AppActivity;
import xsg.lychee.richalpha.R;
import xsg.lychee.richalpha.utils.AppUtils;
import xsg.lychee.richalpha.utils.Constant;
import xsg.lychee.richalpha.utils.PermissionUtil;


public class MyFirebaseMessagingService extends FirebaseMessagingService
{
	private final static String TAG = "MyFBMessagingService";
	private static int mNotifyId = 0;

	public MyFirebaseMessagingService() {
	}

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage)
	{
		++mNotifyId;
		// Not getting messages here? See why this may be: https://goo.gl/39bRNJ
		Log.d(TAG, "mNotifyId: " + mNotifyId);

		// Check if message contains a data payload.
		if( remoteMessage.getData().size() > 0 )
		{
			String messageData = remoteMessage.getData().get("data");
			String title = remoteMessage.getData().get("title");
			String content = remoteMessage.getData().get("message");
//			AppUtils.setAppLaunchInfo(messageData);

			Context context = AppActivity.getContext();
			Intent intent = new Intent(context, AppActivity.class);
			intent.setAction(Intent.ACTION_MAIN);
			intent.putExtra("data", messageData);
			PendingIntent pendingIntent = PendingIntent.getActivity(
					context, mNotifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			Notification notification = null;

			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(NOTIFICATION_SERVICE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				// 1. 创建一个通知(必须设置channelId)
				String channelId = Constant.INFORM_CHANNEL_ID;
				notification = new NotificationCompat.Builder(context, channelId)
						.setSmallIcon(R.mipmap.ic_launcher)
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
						.setContentTitle(title)
						.setContentText(content)
//						.setTicker(content)
						.setAutoCancel(true)
						.setContentIntent(pendingIntent)
						.setFullScreenIntent(pendingIntent, true)
						.setPriority(NotificationManager.IMPORTANCE_HIGH)
						.setDefaults(NotificationCompat.DEFAULT_ALL)
						.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
						.build();

				// 2. 获取系统的通知管理器(必须设置channelId)

				NotificationChannel channel = new NotificationChannel(
						channelId,
						getString(R.string.activity_inform),
						NotificationManager.IMPORTANCE_HIGH);
				channel.enableLights(true);
				channel.enableVibration(true);
				channel.setShowBadge(true);
				channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

				notificationManager.createNotificationChannel(channel);

//				new Thread(()->{
//					try {
//						int notifyId = mNotifyId;
//						Thread.sleep(5000);
//
//						notificationManager.cancel(notifyId);
//						Log.d(TAG, "onMessageReceived: notifyId " + notifyId);
//					} catch(InterruptedException e) {
//						e.printStackTrace();
//					}
//
//				}).start();
			}
			else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				notification = new NotificationCompat.Builder(context, null)
						.setSmallIcon(R.mipmap.ic_launcher)
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
						.setContentTitle(title)
						.setContentText(content)
						.setTicker(content)
						.setAutoCancel(true)
						.setContentIntent(pendingIntent)
						.setPriority(NotificationManager.IMPORTANCE_MAX)
						.setDefaults(NotificationCompat.DEFAULT_ALL)
						.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
						.build();
			}
			else {
				notification = new NotificationCompat.Builder(context, null)
						.setSmallIcon(R.mipmap.ic_launcher)
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
						.setContentTitle(title)
						.setContentText(content)
						.setTicker(content)
						.setAutoCancel(true)
						.setContentIntent(pendingIntent)
						.setPriority(5)
						.setDefaults(NotificationCompat.DEFAULT_ALL)
						.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
						.build();
			}

			if (notification != null) {
				notificationManager.notify(mNotifyId, notification);
			}


			Log.d(TAG, "Message data payload: " + remoteMessage.getData());
		}

		// Check if message contains a notification payload.
		if( remoteMessage.getNotification() != null )
		{
			String bodyMsg = remoteMessage.getNotification().getBody();
			Log.d(TAG, "Message Notification Body: " + bodyMsg);
		}

		// Also if you intend on generating your own notifications as a result of a received FCM
		// message, here is where that should be initiated. See sendNotification method below.
	}

	@Override
	public void onNewToken(String token)
	{
		AppUtils.receiveToken(token);
	}
}
