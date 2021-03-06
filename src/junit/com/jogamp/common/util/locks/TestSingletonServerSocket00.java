/**
 * Copyright 2012 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */
package com.jogamp.common.util.locks;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestSingletonServerSocket00 {
    // public static final String SINGLE_INSTANCE_LOCK_FILE = "UITestCase.lock";
    public static final int SINGLE_INSTANCE_LOCK_PORT = 59999;
    public static final long SINGLE_INSTANCE_LOCK_TO   = 3*60*1000; // wait up to 3 min
    public static final long SINGLE_INSTANCE_LOCK_POLL =       100; // poll every 100ms
    private static volatile SingletonInstance singletonInstance;

    @BeforeClass
    public static void oneTimeSetUp() {
        // one-time initialization code
        singletonInstance = SingletonInstance.createServerSocket(SINGLE_INSTANCE_LOCK_POLL, SINGLE_INSTANCE_LOCK_PORT);
    }

    @Test
    public void testLockUnlock() {
        Assert.assertTrue("Could not lock single instance: "+singletonInstance.getName(), singletonInstance.tryLock(SINGLE_INSTANCE_LOCK_TO));
        System.gc(); // force cleanup
        singletonInstance.unlock();
    }

    @Test
    public void test2ndInstanceLockTimeout() {
        Assert.assertTrue("Could not lock single instance: "+singletonInstance.getName(), singletonInstance.tryLock(SINGLE_INSTANCE_LOCK_TO));
        SingletonInstance instanceTwo = SingletonInstance.createServerSocket(SINGLE_INSTANCE_LOCK_POLL, SINGLE_INSTANCE_LOCK_PORT);
        Assert.assertFalse("Could lock 2nd instance: "+instanceTwo.getName(), instanceTwo.tryLock(1000)); // 10x
        System.gc(); // force cleanup
        singletonInstance.unlock();
    }

    private Thread startLockUnlockOffThread(int i) {
        final Thread t = new Thread(new Runnable() {
            public void run() {
                SingletonInstance myLock = SingletonInstance.createServerSocket(10, SINGLE_INSTANCE_LOCK_PORT);
                System.err.println(Thread.currentThread().getName()+" LOCK try ..");
                Assert.assertTrue(Thread.currentThread().getName()+" - Could not lock instance: "+myLock.getName(), myLock.tryLock(1000));
                System.err.println(Thread.currentThread().getName()+" LOCK ON");
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) { }
                myLock.unlock();
                System.err.println(Thread.currentThread().getName()+" LOCK OFF");
            }
        }, "LockUnlock #"+i);
        t.start();
        return t;
    }

    @Test
    public void testOffthreadLockUnlock() throws InterruptedException {
        Assert.assertTrue("Could not lock single instance: "+singletonInstance.getName(), singletonInstance.tryLock(SINGLE_INSTANCE_LOCK_TO));
        Thread t1 = startLockUnlockOffThread(1);
        Thread t2 = startLockUnlockOffThread(2);
        Thread.sleep(300);
        System.gc(); // force cleanup
        singletonInstance.unlock();
        while(t1.isAlive() || t2.isAlive()) {
            Thread.sleep(100);
        }
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        String tstname = TestSingletonServerSocket00.class.getName();
        org.junit.runner.JUnitCore.main(tstname);
    }
}
