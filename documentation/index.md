# QR Scanner for Titanium Android

Provides an easy way to scan for QR codes using the phone's camera.

## Example usage

```
if OS_ANDROID
  # import the module
  TiQr = require("de.evopark.tiqr")
  # register a handler to be invoked when codes are recognized
  TiQr.onCodeReceived = (code) ->
    # do something with the code
    return
  # optional: also create a view so the user sees what the camera is looking at
  scannerView = TiQr.createCameraView()
  $.cameraViewContainer.add(scannerView)
  # finally: start scanning!
  TiQr.scanning = true

```

------------

Developed with love by [evopark](https://www.evopark.de).
See `LICENSE` and `NOTICE` files for more information.