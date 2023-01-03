/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.patterndetection;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;

/**
 * Provides access to the ThreadPool executor for all Scans to share.
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
public class ScanningThreadPool {
	
	private static ExecutorService executorService;

    private ScanningThreadPool(){}
    
    public static synchronized ExecutorService getThreadPoolExecutor() {
    	 if (executorService == null) {
             var boundedQueue = new ArrayBlockingQueue<Runnable>(1000);
     		executorService = new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS, boundedQueue, new AbortPolicy());
         }
         return executorService;
    }

}
