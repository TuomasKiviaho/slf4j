/**
 * Copyright (c) 2004-2011 QOS.ch
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.slf4j.impl;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class LoggerFactoryImpl implements ILoggerFactory {

  ServiceTracker logServiceTracker;
  BundleContext bundleContext;

  Map loggers;
  Integer logLevel;

  public LoggerFactoryImpl() {
    loggers = new HashMap();
  }

  public synchronized Logger getLogger(String name) {
    LoggerImpl logger = (LoggerImpl) loggers.get(name);
    if (logger == null) {
      logger = new LoggerImpl(name);
      logger.logServiceTracker = logServiceTracker;
      logger.logLevel = logLevel;
      if (bundleContext != null) {
        try {
          logger.serviceTracker = new ServiceTracker(bundleContext, name,
            null);
        } catch (IllegalStateException e) {
          bundleContext = null;
        }
      }
      loggers.put(name, logger);
    }
    return logger;
  }

}