#!groovy
@Library('iot-invent-shared') _

// One line, built from main. A release is a run of main with RELEASE ticked: the next free version
// <revision>.N, counted from the tags v<revision>.N.
pipeline {

    agent any
    tools {
        jdk 'jdk21'
        maven 'M3'
    }
    options {
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
        skipStagesAfterUnstable()
    }
    parameters {
        booleanParam(name: "RELEASE",
                description: "Release instead of deploying a SNAPSHOT: builds with the next free version (v<revision>.N, counted from the tags), deploys it and pushes the tag. Only on main.",
                defaultValue: false)
        booleanParam(name: "CLEANUP",
                description: "Cleanup Current Workspace.",
                defaultValue: false)
        string(name: "MVN_PARAMS", defaultValue: "", description: "Additional Maven Parameters for: mvn clean deploy")
    }

    stages {
        stage("Cleanup") {
            when { expression { params.CLEANUP } }
            steps { cleanWs(); checkout scm }
        }
        stage('Build & Deploy') {
            when { expression { !params.RELEASE } }
            steps {
                withMaven(maven: 'M3', mavenSettingsConfig: 'iot_maven') {
                    withEnv(["MVN_PARAMS=${mvnParams()}"]) {
                        sh "mvn \$MVN_PARAMS -e -B clean deploy"
                    }
                }
            }
        }
        stage("Release") {
            when { expression { params.RELEASE } }
            steps {
                script {
                    if (env.BRANCH_NAME != 'main') {
                        error "RELEASE is only allowed on main, not on ${env.BRANCH_NAME}"
                    }
                    cleanWs()
                    checkout scm
                    // After the checkout: getNextRelease reads pom.xml and the tags of this workspace
                    env.RELEASE_VERSION = getNextRelease()
                    echo "Releasing ${env.RELEASE_VERSION}"
                }
                withMaven(maven: 'M3', mavenSettingsConfig: 'iot_maven') {
                    sh "mvn -B clean -Prelease -Drevision=${env.RELEASE_VERSION} -Dchangelist="
                    sh "mvn -B enforcer:enforce -Prelease -Drevision=${env.RELEASE_VERSION} -Dchangelist="
                    sh "mvn -B deploy -Prelease -Drevision=${env.RELEASE_VERSION} -Dchangelist="
                }
                withCredentials([gitUsernamePassword(credentialsId: 'iot-invent-bot', gitToolName: 'Default')]) {
                    sh """
                    git config --global user.email support@iot-invent.com
                    git config --global user.name iot-invent-bot
                    """
                    sh "git tag v${env.RELEASE_VERSION}"
                    sh "git push origin v${env.RELEASE_VERSION}"
                }
            }
        }
    }

    post { always { sendNotifications currentBuild.result } }
}

/**
 * MVN_PARAMS, checked: Maven options only, nothing the shell would read as a command of its own (RM-182).
 * It reaches the shell through the environment, not by Groovy interpolation.
 */
String mvnParams() {
    String value = (params.MVN_PARAMS ?: '').trim()
    if (!(value ==~ /[A-Za-z0-9=._:,\/@+\- ]*/)) {
        error "MVN_PARAMS may only hold Maven options (letters, digits, = . _ : , / @ + - and spaces), not '${value}'"
    }
    return value
}
