# Gallery Magick Image Processing

Gallery Magick Image Processing provides an image file processing library and CMS plugins to manipulate image files,
using any of these:
- [imgscalr - Java Image-Scaling Library](https://github.com/rkalla/imgscalr)
- [GraphicsMagick](http://www.graphicsmagick.org/)
- [ImageMagick](http://www.imagemagick.org/)

# Prerequisites

A native image processing binary must be installed on the server for the `MagickCommandGalleryProcessor` to resize
images and extract metadata. Without one, uploads will still succeed but images will be stored unresized and without
dimension metadata.

Install **one** of the following:

**GraphicsMagick** (recommended):
```bash
# macOS
brew install graphicsmagick

# Debian / Ubuntu
apt-get install graphicsmagick

# RHEL / CentOS
yum install GraphicsMagick
```

**ImageMagick**:
```bash
# macOS
brew install imagemagick

# Debian / Ubuntu
apt-get install imagemagick

# RHEL / CentOS
yum install ImageMagick
```

The plugin detects which tool to use via the `magick.image.processor` configuration property in the CMS service
registration (`magickCommandGalleryProcessorService.yaml`). The default is `GraphicsMagick`.

You can override the binary path with system properties if the executable is not on the server's `PATH`:
```
-Dorg.onehippo.forge.gallerymagick.core.command.gm=/usr/local/bin/gm
-Dorg.onehippo.forge.gallerymagick.core.command.im.convert=/usr/local/bin/convert
-Dorg.onehippo.forge.gallerymagick.core.command.im.identify=/usr/local/bin/identify
```

# Documentation (Local)

The documentation can generated locally by this command:

```bash
$ mvn clean install
$ mvn clean site
```

The output is in the ```target/site/``` directory by default. You can open ```target/site/index.html``` in a browser.

# Documentation (GitHub Pages)

Documentation is available at [https://bloomreach-forge.github.io/gallery-magick/](https://bloomreach-forge.github.io/gallery-magick/).

You can generate the GitHub pages only from ```master``` branch by this command:

```bash
$ mvn clean install
$ find docs -name "*.html" -exec rm {} \;
$ mvn -Pgithub.pages clean site
```

The output is in the ```docs/``` directory by default. You can open ```docs/index.html``` in a browser.

You can push it and GitHub Pages will be served for the site automatically.
