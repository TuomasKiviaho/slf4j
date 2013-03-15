/**
 * Copyright (c) 2004-2011 QOS.ch
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
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

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.spi.LocationAwareLogger;

public class LoggerImpl extends MessageFormattingLogger {

  private static final long serialVersionUID = 1838085355547330016L;

  ServiceTracker logServiceTracker;
  ServiceTracker serviceTracker;
  Integer logLevel;

  LoggerImpl(String name) {
    this.name = name;
  }

  protected boolean isLevelEnabled(int logLevel) {
    return this.logLevel != null && this.logLevel.intValue() <= logLevel;
  }

  protected void log(int level, String message, Throwable t) {
    if (isLevelEnabled(level)) {
      ServiceTracker logServiceTracker = this.logServiceTracker;
      LogService logService = logServiceTracker == null ? null
          : (LogService) logServiceTracker.getService();
      if (logService != null) {
        int logLevel = (int) (-(level - LocationAwareLogger.ERROR_INT) / 10) + 1;
        ServiceTracker serviceTracker = this.serviceTracker;
        ServiceReference[] serviceReferences = serviceTracker == null ? null
            : serviceTracker.getServiceReferences();
        if (serviceReferences == null) {
          logService.log(null, logLevel, message, t); 
        } else {
          for (int i = 0; i < serviceReferences.length; i++) {
            logService.log(serviceReferences[i], logLevel, message, t); 
          }
        }
      }
    }
  }

}