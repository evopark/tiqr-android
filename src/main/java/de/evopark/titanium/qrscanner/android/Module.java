package de.evopark.titanium.qrscanner.android;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

/**
 * Entry point for the Titanium module
 *
 * Licensed under the terms of the Apache Public License 2.0
 * Please see the LICENSE included with this distribution for details.
 */

@Kroll.module(name="titaniumqr", id=ModuleInfo.MODULE_ID)
public class Module extends KrollModule {

  public Module() {
    super();
  }
}
