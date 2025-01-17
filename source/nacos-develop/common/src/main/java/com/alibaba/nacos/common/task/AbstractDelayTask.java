/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.task;

/**
 * Abstract task which can delay and merge.
 *
 * @author huali
 * @author xiweng.yy
 */
public abstract class AbstractDelayTask implements NacosTask {
    
    /**
     * Task time interval between twice processing, unit is millisecond.
     * 任务处理间隔 多久处理一次 单位毫秒
     */
    private long taskInterval;
    
    /**
     * The time which was processed at last time, unit is millisecond.
     * 任务最近处理 单位毫秒 对象创建时 lastProcessTime 为当前时间
     */
    private long lastProcessTime;
    
    /**
     * The default time interval, in milliseconds, between tasks.
     * 默认间隔时间1秒钟
     */
    protected static final long INTERVAL = 1000L;
    
    /**
     * merge task.
     *
     * @param task task
     */
    public abstract void merge(AbstractDelayTask task);
    
    public void setTaskInterval(long interval) {
        this.taskInterval = interval;
    }
    
    public long getTaskInterval() {
        return this.taskInterval;
    }
    
    public void setLastProcessTime(long lastProcessTime) {
        this.lastProcessTime = lastProcessTime;
    }
    
    public long getLastProcessTime() {
        return this.lastProcessTime;
    }
    
    @Override
    public boolean shouldProcess() {
        return (System.currentTimeMillis() - this.lastProcessTime >= this.taskInterval);
    }
    
}
