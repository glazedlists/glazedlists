## Glazed Lists Bug Fixes

This fork is primarily fixes for strict+CONTAINS:
[AutoCompleteSupport: TextMatcherEditor.CONTAINS not working in strict mode](https://github.com/glazedlists/glazedlists/issues/676),
see [ComboBox UI issues with AutoCompleteSupport](https://github.com/glazedlists/glazedlists/issues/696) for the visual differences seen in the JComboBox. Specifically, as described in the issue, the problems arise when:
- setting the AutoCompleteSupport's "strict" property to "true"
- and setting the AutoCompleteSupport's "filterMode" property to TextMatcherEditor.CONTAINS

There are also fixes for a few bugs found along the way while putting the primary fix into production,
in particular [glazedlists combox doesn't handle "\n" compatibly](https://github.com/glazedlists/glazedlists/issues/695)
and [ComboBox caret positioning hides user input in narrow ComboBox](https://github.com/glazedlists/glazedlists/issues/699).

The following has fixes for these three bugs.
```
<dependency>
    <groupId>com.raelity.3rdparty.com.glazedlists</groupId>
    <artifactId>glazedlists</artifactId>
    <version>1.11.1203</version>
</dependency>
```

The unusual version number indiates that it's post glazedlists-1.11, base on the glazedlists-1.12 development release. Note that 1.12-dev is used in production by at least one of its authors.

For the issue `ComboBox caret positioning hides user input in narrow ComboBox` there's a new property PositionCaretTowardZero, when set to true
```
autoCompleteSupport.setPositionCaretTowardZero(true)
```
the caret is positioned where the next user input character goes rather than always at the end of the text.

-------------------------------

## Glazed Lists - List transformations in Java

### CI build status

[![Build Status](https://travis-ci.org/glazedlists/glazedlists.svg?branch=master)](https://travis-ci.org/glazedlists/glazedlists)

[![Build Status](https://github.com/glazedlists/glazedlists/workflows/Java%20CI/badge.svg)](https://github.com/glazedlists/glazedlists/actions)

[![Build Status](https://dev.azure.com/glazedlists/glazedlists/_apis/build/status/glazedlists.glazedlists?branchName=master)](https://dev.azure.com/glazedlists/glazedlists/_build/latest?definitionId=1&branchName=master)

[![Join the chat at https://gitter.im/glazedlists/glazedlists](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/glazedlists/glazedlists)

### Project sites

Home page: 		http://www.glazedlists.com/

### Documentation

[Tutorial](https://glazedlists.github.io/glazedlists-tutorial/)

See further documentation at [www.glazedlists.com](http://www.glazedlists.com/documentation)

### Issue tracker

[GitHub Issues](https://github.com/glazedlists/glazedlists/issues)

### Releases

Browse [Release Notes](http://www.glazedlists.com/releases)

Download [Latest Release](http://repo1.maven.org/maven2/com/glazedlists/glazedlists/1.11.0/)

Download [Latest Snapshot version](https://oss.sonatype.org/content/repositories/snapshots/com/glazedlists/glazedlists/1.12.0-SNAPSHOT/)

Configure [Maven](http://www.glazedlists.com/Home/maven)

### Mailing list archives

[Overview of mailing lists](http://glazedlists.1045722.n5.nabble.com/GlazedLists-f3416377.subapps.html)

### Stack Overflow

[Ask questions](https://stackoverflow.com/questions/tagged/glazedlists)

### License

Glazed Lists is free software and business friendly. It allows you to

  * distribute Glazed Lists free of charge
  * use Glazed Lists in a commercial or closed source application

It does not allow you to

  * create a fork of Glazed Lists that is closed-source

It is made available under two licenses:

  * [LGPL](http://creativecommons.org/licenses/LGPL/2.1/)
  * [MPL](http://www.mozilla.org/MPL/)
