# portal-social
[![Build Status](https://server.stijnhooft.be/jenkins/buildStatus/icon?job=portal-social/master)](https://server.stijnhooft.be/jenkins/job/portal-social/job/master/)

Keeping in touch with friends and family is something that should come naturally.

However, in busy or stressful times (times where we need social contact the most), this can get pushed to the background.

This module is an experimental tool, to test if I maintain better relations when I get **reminded** regularly.

It also lets me keep **notes of interesting talking points**. Someone just had an interesting event in his or her life? Let's talk about that! 

## Dependencies
This service is dependent on a deployment of **portal-recurring-tasks**.

## Release
### How to release
To release a module, this project makes use of the JGitflow plugin and the Dockerfile-maven-plugin.

1. Make sure all changes have been committed and pushed to Github.
1. Switch to the dev branch.
1. Make sure that the dev branch has at least all commits that were made to the master branch
1. Make sure that your Maven has been set up correctly (see below)
1. Run `mvn jgitflow:release-start -Pproduction`.
1. Run `mvn jgitflow:release-finish -Pproduction`.
1. In Github, mark the release as latest release.
1. Congratulations, you have released both a Maven and a Docker build!

More information about the JGitflow plugin can be found [here](https://gist.github.com/lemiorhan/97b4f827c08aed58a9d8).

#### Maven configuration
At the moment, releases are made on a local machine. No Jenkins job has been made (yet).
Therefore, make sure you have the following config in your Maven `settings.xml`;

````$xml
<servers>
		<server>
			<id>docker.io</id>
			<username>your_username</username>
			<password>*************</password>
		</server>
		<server>
			<id>portal-nexus-releases</id>
			<username>your_username</username>
            <password>*************</password>
		</server>
	</servers>
````
* docker.io points to the Docker Hub.
* portal-nexus-releases points to my personal Nexus (see `<distributionManagement>` in the project's `pom.xml`)
