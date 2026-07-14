# Building and running a Jenkins master with Docker

This Jenkins master image is configured to be used by integration tests, see the [main README](../../../../../README.md)


Build the image
```bash
docker build -t jenkins-rest/jenkins .
```

You can decide which Jenkins version to used by passing the `jenkins_tag` docker build argument like in the following
```bash
docker build --build-arg jenkins_tag=2.568.1-lts-jdk21 -t jenkins-rest/jenkins .
```

Note that the plugins pinned in `plugins.yml` require a Jenkins core of at least 2.541.3, so older
tags will fail to load them.

Run the jenkins master container
```bash
docker run -d --rm -p 8080:8080 --name jenkins-rest jenkins-rest/jenkins
```