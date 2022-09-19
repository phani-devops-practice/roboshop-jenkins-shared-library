def call() {
  node() {
    common.pipelineInit()
    stage('Compile packages') {
      sh 'mvn clean package'
    }
    if(env.BRANCH_NAME == env.TAG_NAME)
    {
      common.publishArtifact()
    }
  }
}