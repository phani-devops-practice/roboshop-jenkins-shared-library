def pipelineInit() {
    stage('Initiate repo') {
      sh 'rm -rf *'
      git branch: 'main', url: "https://github.com/phani-devops-practice/${COMPONENT}.git"
    }
}

def publishArtifact() {
  stage('Prepare Artifacts') {
    if (env.APP_TYPE == "nodejs") {
      sh """
        zip -r ${COMPONENT}-${TAG_NAME}.zip node_modules server.js
      """
    }
    if (env.APP_TYPE == "maven") {
      sh """
        mv target/${COMPONENT}-1.0.jar ${COMPONENT}.jar
        zip -r ${COMPONENT}-${TAG_NAME}.zip ${COMPONENT}.jar  
      """
    }
    if (env.APP_TYPE == "python") {
      sh """
        zip -r ${COMPONENT}-${TAG_NAME}.zip *.py ${COMPONENT}.ini requirements.txt  
      """
    }
    if (env.APP_TYPE == "nginx") {
      sh """
        cd static
        zip -r ../${COMPONENT}-${TAG_NAME}.zip *   
      """
    }
    if (env.APP_TYPE == "golang") {
      sh """
        zip -r ../${COMPONENT}-${TAG_NAME}.zip main.go   
      """
    }
  }

  stage('Push Artifacts to Nexus') {
    withCredentials([usernamePassword(credentialsId: 'nexus', passwordVariable: 'pass', usernameVariable: 'user')]) {
      sh """
        curl -v -u ${user}:${pass} --upload-file ${COMPONENT}-${TAG_NAME}.zip http://nexus-p.roboshop.internal:8081/repository/${COMPONENT}/${COMPONENT}-${TAG_NAME}.zip
      """
    }
  }
}

def codeChecks() {
  stage('unit test') {
    parallel([
            qualityChecks: {
              withCredentials([usernamePassword(credentialsId: 'sonar', passwordVariable: 'pass', usernameVariable: 'user')]) {
                sh """
                  sonar-scanner -Dsonar.projectKey=${COMPONENT} -Dsonar.host.url=http://172.31.2.218:9000 -Dsonar.login=${user} -Dsonar.password=${pass}
                  sonar-quality-gate.sh ${user} ${pass} 172.31.2.218 ${COMPONENT} ${EXTRA_OPTS}
                """
              }
            },
            unitTests: {
              unitTests()
            }
    ])
  }
}

def unitTests() {
  stage('Prepare unitTests') {
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