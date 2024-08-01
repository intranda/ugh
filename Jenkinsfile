pipeline {

  agent {
    docker {
      registryUrl 'https://nexus.intranda.com:4443'
      registryCredentialsId 'jenkins-docker'
      args '-v $HOME/.m2:/var/maven/.m2:z -v $HOME/.config:/var/maven/.config -v $HOME/.sonar:/var/maven/.sonar -u 1000 -ti -e _JAVA_OPTIONS=-Duser.home=/var/maven -e MAVEN_CONFIG=/var/maven/.m2'

    }
  }

  options {
    buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '15', daysToKeepStr: '90', numToKeepStr: '')
  }

  stages {
    stage('prepare') {
      steps {
        sh 'git clean -fdx'
      }
    }

  stage('build') {
      steps {
        sh 'mvn -f ugh/pom.xml install'
        recordIssues enabledForFailure: true, aggregatingResults: true, tools: [java(), javaDoc()]
      }
    }

    stage('deployment to maven repository') {
      when {
        anyOf {
          branch 'master'
            branch 'v*.*.*'
        }
      }
      steps {
        sh 'mvn -f ugh/pom.xml deploy'
      }
    }
  }

  post {
    always {
      junit "**/target/surefire-reports/*.xml"
    }
    success {
      archiveArtifacts artifacts: '**/target/*.jar, */plugin_*.xml, plugin_*.xml', fingerprint: true, onlyIfSuccessful: true
    }
    changed {
      emailext(
        subject: '${DEFAULT_SUBJECT}',
        body: '${DEFAULT_CONTENT}',
        recipientProviders: [requestor(),culprits()],
        attachLog: true
      )
    }
  }
}

/* vim: set ts=2 sw=2 tw=120 et :*/
