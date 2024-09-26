# Gallery Magick Image Processing

Gallery Magick Image Processing provides an image file processing library and CMS plugins to manipulate image files, 
using any of these:
- [imgscalr - Java Image-Scaling Library](https://github.com/rkalla/imgscalr)
- [GraphicsMagick](http://www.graphicsmagick.org/)
- [ImageMagick](http://www.imagemagick.org/)


# Documentation (Local)

The documentation can generated locally by this command:

```bash
$ mvn clean install
$ mvn clean site
```

The output is in the ```target/site/``` directory by default. You can open ```target/site/index.html``` in a browser.

# Documentation (GitHub Pages)

Documentation is available at [https://bloomreach-forge.github.io/gallery-magick/index.html](https://bloomreach-forge.github.io/gallery-magick/index.html).

You can generate the GitHub pages only from ```master``` branch by this command:

```bash
$ mvn clean install
$ find docs -name "*.html" -exec rm {} \;
$ mvn -Pgithub.pages clean site
```

The output is in the ```docs/``` directory by default. You can open ```docs/index.html``` in a browser.

You can push it and GitHub Pages will be served for the site automatically.
