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

Note: The best way to download these examples is by using the Repo tool. If you
are reading the code associated with the Wiley distribution, we strongly
recommend that you install git and begin your work from the code repositories
and keep the code up to date periodically using a "git pull" operation.

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

## The README files contained in every project directory provide the best
source of instructions for running their associated project.

## Android Setup

About half of these projects are Android projects.  In order to work with them,
you will need the Android SDK.  Projects were tested with Android SDK v 22.0.1.
They have not been tested with Android Studio.  Instructions for setting up an
Android  development environment are here:

http://developer.android.com/sdk/index.html

You will, of course, need a working Java.  All projects were tested with Java 6.
Most were tested with Java 7 as well.

## Using Eclipse
Most projects will build in Eclipse.  To create an Eclipse project for one of the code directories here,
run the following commands, from the command line, while in the subdirectory containing the code:
```shell
cp tools/ide/eclipse/classpath .classpath
cp tools/ide/eclipse/project .project
```

This will copy the necessary eclipse configuration into the project directory.  Once you have done that
you will be able to Import > General > Import Existing Project into Workspace.

The following projects contain a Windows script in their tools directory for
completing the above task for Eclipse:

KeyValClient
KeyValCP
KeyValDB
MigrateClinic
MigrateContacts
restfulCachingProviderContacts
syncAdapterContacts

The rest of the projects rely on an ant target for setting up eclipse:

ant eclipse

The project README files document this step.

===

Some of the projects require special eclipse extentions (WTP - Web Tools Platform, Ivy, etc.).  See the
README file in the project for specifics.

