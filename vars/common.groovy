def pipelineInit() {
    stage('Initiate repo') {
      sh 'rm -rf *'
      git branch: 'main', url: "https://github.com/phani-devops-practice/${COMPONENT}.git"
    }
}

def publishArtifact() {
  stage('Prepare Artifacts') {
    if (env.APP_TYPE == "nodejs")
      sh """
        zip -r ${COMPONENT}-${TAG_NAME}.zip node_modules server.js
      """
    }
    if (env.APP_TYPE == "maven")
      sh """
        mv target/${COMPONENT}-1.0.jar ${COMPONENT}.jar
        zip -r ${COMPONENT}-${TAG_NAME}.zip ${COMPONENT}.jar  
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