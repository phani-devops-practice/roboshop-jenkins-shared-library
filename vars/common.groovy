def pipelineInit() {
    stage('Initiate repo') {
      sh 'rm -rf *'
      git branch: 'main', url: "https://github.com/phani-devops-practice/${COMPONENT}.git"
    }
}

def publishArtifact() {
    stage('Prepare Artifacts') {
      sh """
        zip -r ${COMPONENT}.zip node_modules server.js
      """
    }
}