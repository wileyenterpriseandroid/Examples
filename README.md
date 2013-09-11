# Example code for Wiley's "Enterprise Android"

This repository contains a series projects for "Enterprise Android" from Wrox Press.  The 
projects correspond to chapters according to the table below:

1: FragmentFramework

3: KeyValDB

4: KeyValCP, KeyValClient

5: restfulCachingProviderContacts, syncAdapterContacts

6: springServiceContacts, restfulCachingProviderContacts, syncAdapterContacts

7: awsServiceContacts, googleAppEngineContacts

10: MigrateContacts, MigrateClinic

12: AndroidSecurity, springServiceContacts

## Getting the code

The best way to download these examples is by using the Repo tool.
Get it like this:
```
curl https://dl-ssl.google.com/dl/googlesource/git-repo/repo > ~/bin/repo
```

Once repo is installed, download this source with the following commands:

```
repo init -u https://github.com/wileyenterpriseandroid/manifests.git
repo sync
```

You can download the source without using repo, with the following two commands:

```
git clone https://github.com/wileyenterpriseandroid/migrate-sdk.git
git clone https://github.com/wileyenterpriseandroid/Examples.git ea-examples
```

## Android Setup

About half of these projects are Android projects.  In order to work with them,
you will need the Android SDK.  Projects were tested with Android SDK v 22.0.1.
They have not been tested with Android Studio.  Instructions for setting up an
Android  development environment are here:

http://developer.android.com/sdk/index.html

You will, of course, need a working Java.  All projects were tested with Java 6.
Most were tested with Java 7 as well.

## Using Eclipse
Most projects will build in eclipse.  To create an eclipse project for one of the code directories here,
run the following commands, from the command line, while in the subdirectory containing the code:
```shell
cp tools/ide/eclipse/classpath .classpath
cp tools/ide/eclipse/project .project
```

This will copy the necessary eclipse configuration into the project directory.  Once you have done that
you will be able to Import > General > Import Existing Project into Workspace.

Some of the projects require special eclipse extentions (WTP - Web Tools Platform, Ivy, etc.).  See the
README file in the project for specifics.

