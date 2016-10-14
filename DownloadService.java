package cn.tedu.media_player_v4.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.app.IntentService;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Environment;
import cn.tedu.media_player_v4.R;
import cn.tedu.media_player_v4.util.HttpUtils;

public class DownloadService extends IntentService{
	private static final int NOTIFICATION_ID = 100;
	private NotificationManager manager;
	private Builder builder;
	
	public DownloadService() {
		super("xiaoming");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		this.builder = new Builder(this);
	}
	
	/**
	 * 在工作线程中执行
	 * 当调用startService方法时，将会把
	 * onHandleIntent中的业务逻辑添加到
	 * 任务队列等待执行。当轮到该任务时
	 * 才执行该方法中的代码。
	 */
	protected void onHandleIntent(Intent intent) {
		//获取请求参数
		String url=intent.getStringExtra("url");
		String filename=intent.getStringExtra("filename");
		String bitrate = intent.getStringExtra("bitrate");
		int totalsize = intent.getIntExtra("total", 0);
		//声明需要写入的目标File对象
		//targetFile:   /mnt/sdcard/Music/64/xxx.mp3
		File targetFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "_"+bitrate+"/"+filename);
		//判断文件是否已经下载过  文件是否已存在
		if(targetFile.exists()){
			return;
		}
		if(!targetFile.getParentFile().exists()){
			targetFile.getParentFile().mkdirs();
		}
		//如果文件不存在，则发送http请求
		try {
			FileOutputStream fos = new FileOutputStream(targetFile);
			InputStream is = HttpUtils.getInputStream(url);
			//获取is，执行“边读边写”的操作。
			byte[] buffer = new byte[1024*200];
			int length=0;
			//发通知提示音乐开始下载
			sendNotification("音乐开始下载..", "音乐开始下载");
			int progress = 0;
			while((length=is.read(buffer)) != -1){
				fos.write(buffer, 0, length);
				fos.flush();
				progress += length;
				//发送通知提示下载进度
				String text = "当前下载进度为:"+(Math.floor(1000.0*progress/totalsize)/10)+"%";
				sendNotification(text, "音乐开始下载");
			}
			fos.close();
			//保存完毕后使用notification通知用户。
			clearNotification();
			sendNotification("音乐下载完成", "下载完毕");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** 发送通知 */
	public void sendNotification(String text,String ticker){
		builder.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("音乐下载")
			.setContentText(text)
			.setTicker(ticker);
		Notification n = builder.build();
		manager.notify(NOTIFICATION_ID, n);
	}
	/** 清除通知 */
	public void clearNotification(){
		manager.cancel(NOTIFICATION_ID);
	}
	
}






