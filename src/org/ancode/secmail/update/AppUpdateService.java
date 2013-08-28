package org.ancode.secmail.update;

import java.io.File;

import org.ancode.secmail.R;
import org.ancode.secmail.update.internal.FoundVersionDialog;
import org.ancode.secmail.update.internal.NetworkUtil;
import org.ancode.secmail.update.internal.ResponseCallback;
import org.ancode.secmail.update.internal.VerifyTask;
import org.ancode.secmail.update.internal.VersionDialogListener;
import org.ancode.secmail.update.internal.VersionPersistent;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

public class AppUpdateService {

	private Context context;
	private DownloadManager downloader;
	private DownloadReceiver downloaderReceiver;
	private NetworkStateReceiver networkReceiver;

	private boolean updateDirectly = false;
	private boolean _3thPartyDownloader = false;
	private boolean isRegistered = false;

	private long downloadTaskId = -12306;
	private static AutoUpgradeDelegate updateDelegate;

	class AutoUpgradeDelegate implements AppUpdate, ResponseCallback, VersionDialogListener {

		private DisplayDelegate customShowingDelegate;
		private Version latestVersion;

		private String checkUrl;
		private ResponseParser parser;

		@Override
		public void checkLatestVersion(String url, ResponseParser parser) {
			setCheckUrl(url);
			setResponseParser(parser);
			checkVersion(false);
		}

		@Override
		public void setCheckUrl(String url) {
			checkUrl = url;
		}

		@Override
		public void setResponseParser(ResponseParser parser) {
			this.parser = parser;
		}

		@Override
		public void checkAndUpdateDirectly(String url, ResponseParser parser) {
			setCheckUrl(url);
			setResponseParser(parser);
			checkVersion(true);
		}

		@Override
		public void checkAndShow() {
			checkVersion(false);
		}

		void checkVersion(boolean isUpdateDirectly) {
			updateDirectly = isUpdateDirectly;
			if (isNetworkActive()) {
				VerifyTask task = new VerifyTask(context, parser, this);
				task.execute(checkUrl);
			} else if (isUpdateDirectly) {
				// 手动更新时，才提示用户网络没有开启
				Toast.makeText(context, R.string.network_not_activated, Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void downloadAndInstallCurrent() {
			downloadAndInstall(latestVersion);
		}

		@Override
		public void enable3thPartyDownloader(boolean enable) {
			_3thPartyDownloader = enable;
		}

		@Override
		public void downloadAndInstall(Version version) {
			if (version == null || !isNetworkActive()) {
				return;
			}
			// 判断同名文件是否存在
			String apkName = extractName(version.targetUrl);
			String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
					.getAbsolutePath();
			dirPath = dirPath.endsWith(File.separator) ? dirPath : dirPath + File.separator;
			String targetApkPath = dirPath + apkName;
			File targetApkFile = new File(targetApkPath);
			if (targetApkFile.exists()) {
				installAPKFile(targetApkFile);
				return;
			}

			// 第三方下载
			if (_3thPartyDownloader) {
				Uri uri = Uri.parse(version.targetUrl);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				context.startActivity(intent);
				return;
			}

			downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			Query query = new Query();
			query.setFilterById(downloadTaskId);
			Cursor cur = downloader.query(query);
			// 下载任务已经存在的话
			if (cur.moveToNext()) {
				return;
			}
			cur.close();
			DownloadManager.Request task = new DownloadManager.Request(Uri.parse(version.targetUrl));
			String title = version.name;
			task.setTitle(title);
			task.setDescription(version.feature);
			task.setVisibleInDownloadsUi(true);
			task.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
			task.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);
			downloadTaskId = downloader.enqueue(task);
		}

		boolean isNetworkActive() {
			return NetworkUtil.getNetworkType(context) != NetworkUtil.NO_CONNECTION;
		}

		String extractName(String path) {
			String tempFileName = "_temp@" + path.hashCode();
			boolean fileNameExist = path.substring(path.length() - 5, path.length()).contains(".");
			if (fileNameExist) {
				tempFileName = path.substring(path.lastIndexOf(File.separator) + 1);
			}
			return tempFileName;
		}

		@Override
		public void onFoundLatestVersion(Version version) {
			this.latestVersion = version;

			if (updateDirectly) {
				String versionTipFormat = context.getResources().getString(R.string.update_latest_version_title);
				Toast.makeText(context, String.format(versionTipFormat, latestVersion.name), Toast.LENGTH_LONG).show();
				downloadAndInstall(latestVersion);
				return; // 直接下载，不需要弹出提示窗口。
			}

			if (customShowingDelegate != null) {
				customShowingDelegate.showFoundLatestVersion(latestVersion);
			} else {
				FoundVersionDialog dialog = new FoundVersionDialog(context, latestVersion, this);
				dialog.show();
			}
		}

		@Override
		public void onCurrentIsLatest() {
			if (customShowingDelegate != null) {
				customShowingDelegate.showIsLatestVersion();
			} else {
				Toast.makeText(context, R.string.is_latest_version_label, Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void callOnResume() {
			if (isRegistered)
				return;
			isRegistered = true;
			context.registerReceiver(downloaderReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
			context.registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		}

		@Override
		public void callOnPause() {
			if (!isRegistered)
				return;
			isRegistered = false;
			context.unregisterReceiver(downloaderReceiver);
			context.unregisterReceiver(networkReceiver);
		}

		@Override
		public void setDisplayDelegate(DisplayDelegate displayDelegate) {
			customShowingDelegate = displayDelegate;
		}

		@Override
		public Version getLatestVersion() {
			return latestVersion;
		}

		@Override
		public void doUpdate(boolean laterOnWifi) {
			if (!laterOnWifi) {
				downloadAndInstall(latestVersion);
			} else {
				new VersionPersistent(context).save(latestVersion);
			}
		}

		@Override
		public void doIgnore() {
		}

	}

	private void installAPKFile(File apkFile) {
		Intent installIntent = new Intent();
		installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		installIntent.setAction(android.content.Intent.ACTION_VIEW);
		installIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
		context.startActivity(installIntent);
		apkFile.deleteOnExit();
	}

	/**
	 * 下载完成的广播
	 */
	class DownloadReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context c, Intent intent) {
			if (downloader == null)
				return;
			long completeId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
			if (completeId == downloadTaskId) {
				Query query = new Query();
				query.setFilterById(downloadTaskId);
				Cursor cur = downloader.query(query);
				if (cur.moveToFirst()) {
					int columnIndex = cur.getColumnIndex(DownloadManager.COLUMN_STATUS);
					if (DownloadManager.STATUS_SUCCESSFUL == cur.getInt(columnIndex)) {
						// 下载任务已经完成，清除
						new VersionPersistent(context).clear();
						String uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
						File apkFile = new File(Uri.parse(uriString).getPath());
						if (apkFile.exists()) {
							installAPKFile(apkFile);
						} else {
							Toast.makeText(context, R.string.apk_file_not_exists, Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(context, R.string.download_failure, Toast.LENGTH_SHORT).show();
					}
				}
				cur.close();
			}

		}
	}

	/**
	 * 网络发生变化
	 */
	class NetworkStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
				if (NetworkUtil.getNetworkType(context) == NetworkUtil.WIFI) {
					Version versionTask = new VersionPersistent(context).load();
					if (versionTask != null) {
						Toast.makeText(context, R.string.later_update_tip, Toast.LENGTH_SHORT).show();
						updateDelegate.downloadAndInstall(versionTask);
					}
				}
			}
		}
	}

	private AppUpdate getAppUpdate() {
		if (updateDelegate == null) {
			updateDelegate = new AutoUpgradeDelegate();
		}
		return updateDelegate;
	}

	public static AppUpdateService updateServiceInstance = null;

	public static AppUpdate getAppUpdate(Context context) {
		if (null == updateServiceInstance) {
			updateServiceInstance = new AppUpdateService(context);
		}
		return updateServiceInstance.getAppUpdate();
	}

	private AppUpdateService(Context context) {
		this.context = context;
		downloaderReceiver = new DownloadReceiver();
		networkReceiver = new NetworkStateReceiver();
	}

}
