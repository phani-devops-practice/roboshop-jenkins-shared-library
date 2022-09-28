def pipelineInit() {
    stage('Initiate repo') {
      sh 'rm -rf *'
      git branch: 'main', url: "https://github.com/phani-devops-practice/${COMPONENT}.git"
    }
}

def publishArtifact() {
  env.ENV = "dev"
  stage('Prepare Artifacts') {
    if (env.APP_TYPE == "nodejs") {
      sh """
        zip -r ${ENV}-${COMPONENT}-${TAG_NAME}.zip node_modules server.js
      """
    }
    if (env.APP_TYPE == "maven") {
      sh """
        mv target/${COMPONENT}-1.0.jar ${COMPONENT}.jar
        zip -r ${ENV}-${COMPONENT}-${TAG_NAME}.zip ${COMPONENT}.jar  
      """
    }
    if (env.APP_TYPE == "python") {
      sh """
        zip -r ${ENV}-${COMPONENT}-${TAG_NAME}.zip *.py ${COMPONENT}.ini requirements.txt  
      """
    }
    if (env.APP_TYPE == "nginx") {
      sh """
        cd static
        zip -r ../${ENV}-${COMPONENT}-${TAG_NAME}.zip *   
      """
    }
  }

  stage('Push Artifacts to Nexus') {
    withCredentials([usernamePassword(credentialsId: 'nexus', passwordVariable: 'pass', usernameVariable: 'user')]) {
      sh """
        curl -v -u ${user}:${pass} --upload-file ${ENV}-${COMPONENT}-${TAG_NAME}.zip http://nexus-p.roboshop.internal:8081/repository/${COMPONENT}/${ENV}-${COMPONENT}-${TAG_NAME}.zip
      """
    }
  }
  stage('Deploy to Dev Env') {
    build job: 'deploy-to-any-env', parameters: [string(name: 'COMPONENT', value: "${COMPONENT}"), string(name: 'ENV', value: "${ENV}"), string(name: 'APP_VERSION', value: "${TAG_NAME}")]
  }
  stage('Run smoke Tests on DEV') {
    sh 'echo Smoke tests'
  }
  PromoteRelease("dev" , "qa")

  stage('Deploy to QA Env') {
//    build job: 'deploy-to-any-env', parameters: [string(name: 'COMPONENT', value: "${COMPONENT}"), string(name: 'ENV', value: "qa"), string(name: 'APP_VERSION', value: "${TAG_NAME}")]
    echo 'QA deploy'
  }
  testRuns()
  
  stage('Run smoke Tests on QA') {
    sh 'echo Smoke tests'
  }
  PromoteRelease("qa" , "prod")

}

def testRuns() {
  stage('Quality checks') {
    parallel([
      intigrationTests: {
        echo 'intigrationTests'
      },
      penetrationTests: {
        echo 'penetrationTests'
      },
      e2eTests: {
        echo 'e2eTests'
      }
    ])
  }
}

def PromoteRelease(SOURCE_ENV,ENV) {
  stage("Promoting artifact from ${SOURCE_ENV} to ${ENV}") {
    withCredentials([usernamePassword(credentialsId: 'nexus', passwordVariable: 'pass', usernameVariable: 'user')]) {
      sh """
        cp ${SOURCE_ENV}-${COMPONENT}-${TAG_NAME}.zip ${ENV}-${COMPONENT}-${TAG_NAME}.zip
      curl -v -u ${user}:${pass} --upload-file ${ENV}-${COMPONENT}-${TAG_NAME}.zip http://nexus-p.roboshop.internal:8081/repository/${COMPONENT}/${ENV}-${COMPONENT}-${TAG_NAME}.zip
      """
    }
  }
}


def codeChecks() {
  stage('Quality checks and Unit tests') {
    parallel([
            qualityChecks: {
//              withCredentials([usernamePassword(credentialsId: 'sonar', passwordVariable: 'pass', usernameVariable: 'user')]) {
//                sh "sonar-scanner -Dsonar.projectKey=${COMPONENT} -Dsonar.host.url=http://172.31.1.147:9000 -Dsonar.login=${user} -Dsonar.password=${pass} ${EXTRA_OPTS}"
//                sh "sonar-quality-gate.sh ${user} ${pass} 172.31.1.147 ${COMPONENT}"
//              }
              echo "Code Checks"
            },
            unitTests: {
              unitTests()
            }
    ])
  }
}

def unitTests() {
  stage("Prepare Artifacts") {
    if (env.APP_TYPE == "nodejs") {
      sh """
        # npm run test
        echo Run test cases
      """
    }
    if (env.APP_TYPE == "maven") {
      sh """
        # mvn test
        echo Run test cases
      """
    }

    if (env.APP_TYPE == "python") {
      sh """
        # python -m unittest
        echo Run test cases
      """
    }

    if (env.APP_TYPE == "nginx") {
      sh """ 
        # npm run test
        echo Run test cases
      """
    }

    if (env.APP_TYPE == "golang") {
      sh """
        # go test
        echo Run test cases 
      """
    }
  }
}
