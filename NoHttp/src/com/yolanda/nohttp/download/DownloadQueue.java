/*
 * Copyright © YOLANDA. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yolanda.nohttp.download;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Download queue</br>
 * Created in Oct 21, 2015 2:44:19 PM
 * 
 * @author YOLANDA
 */
public class DownloadQueue {
	/**
	 * Save download task
	 */
	private final LinkedBlockingQueue<NetworkDownloadRequest> mDownloadQueue = new LinkedBlockingQueue<>();
	/**
	 * Download Network task execution interface
	 */
	private final Downloader mDownloader;
	/**
	 * Download queue polling thread array
	 */
	private DownloadDispatch[] mDispatchers;

	/**
	 * Create download queue manager
	 * 
	 * @param downloader Download the network task execution interface, where you need to implement the download tasks that have been implemented.
	 * @param threadPoolSize Number of thread pool
	 */
	public DownloadQueue(Downloader downloader, int threadPoolSize) {
		mDownloader = downloader;
		mDispatchers = new DownloadDispatch[threadPoolSize];
	}

	/**
	 * Start polling the download queue, a one of the implementation of the download task, if you have started to poll the download queue, then it will stop all the threads, to re create thread
	 * execution
	 */
	public void start() {
		stop();
		for (int i = 0; i < mDispatchers.length; i++) {
			DownloadDispatch networkDispatcher = new DownloadDispatch(mDownloadQueue, mDownloader);
			mDispatchers[i] = networkDispatcher;
			networkDispatcher.start();
		}
	}

	/**
	 * Add a download task to download queue, waiting for execution, if there is no task in the queue or the number of tasks is less than the number of thread pool, will be executed immediately
	 * 
	 * @param what Used to distinguish Download
	 * @param downloadRequest Download request object
	 * @param downloadListener Download results monitor
	 */
	public void add(int what, DownloadRequest downloadRequest, DownloadListener downloadListener) {
		mDownloadQueue.add(new NetworkDownloadRequest(what, downloadRequest, downloadListener));
	}

	/**
	 * Polling the queue will not be executed, and this will not be canceled.
	 */
	public void stop() {
		for (int i = 0; i < mDispatchers.length; i++) {
			if (mDispatchers[i] != null)
				mDispatchers[i].quit();
		}
	}

	/**
	 * All requests for the sign specified in the queue, if you are executing, will interrupt the download task
	 * 
	 * @param sign This sign will be the same as sign's DownloadRequest, and if it is the same, then cancel the task.
	 */
	public void cancelAll(Object sign) {
		synchronized (mDownloadQueue) {
			for (NetworkDownloadRequest networkDownloadRequest : mDownloadQueue)
				networkDownloadRequest.downloadRequest.cancelBySign(sign);
		}
	}

}
